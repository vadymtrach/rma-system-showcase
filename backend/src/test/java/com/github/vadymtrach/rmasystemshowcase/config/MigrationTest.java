package com.github.vadymtrach.rmasystemshowcase.config;

import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the migrations into scratch schemas to check data-changing migrations against
 * both a fresh database and one upgraded from an older version.
 */
class MigrationTest extends IntegrationTest {

    private static final String SEEDED_ADMIN = "admin123@gmail.com";

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void dropSchemas() {
        jdbc.execute("DROP SCHEMA IF EXISTS migration_fresh CASCADE");
        jdbc.execute("DROP SCHEMA IF EXISTS migration_upgrade CASCADE");
    }

    @Test
    void freshDatabaseHasNoSeededAdmin() {
        migrate("migration_fresh", "latest");

        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM migration_fresh.users WHERE email = ?", Integer.class, SEEDED_ADMIN))
                .isZero();
    }

    @Test
    void upgradeDisablesReferencedSeededAdminAndLowercasesEmails() {
        migrate("migration_upgrade", "2");
        jdbc.update("INSERT INTO migration_upgrade.users(email, password, full_name, role) "
                + "VALUES ('Mixed@Case.com', 'x', 'Mixed', 'EMPLOYEE')");
        jdbc.update("INSERT INTO migration_upgrade.complaints(rma_number, assigned_to_id) "
                + "SELECT 'RMA-1', id FROM migration_upgrade.users WHERE email = ?", SEEDED_ADMIN);

        migrate("migration_upgrade", "latest");

        // Kept because a complaint references it, but no longer usable.
        Map<String, Object> admin = jdbc.queryForMap(
                "SELECT password, active FROM migration_upgrade.users WHERE email = ?", SEEDED_ADMIN);
        assertThat(admin).containsEntry("password", "{disabled}").containsEntry("active", false);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM migration_upgrade.users WHERE email = 'mixed@case.com'", Integer.class))
                .isOne();
    }

    private void migrate(String schema, String target) {
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration")
                .target(target)
                .load()
                .migrate();
    }
}

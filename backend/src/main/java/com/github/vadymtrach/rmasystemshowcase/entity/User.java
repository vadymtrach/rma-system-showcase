package com.github.vadymtrach.rmasystemshowcase.entity;

import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Setter @Getter
public class User {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    private boolean active = true;

    /**
     * Emails are stored lowercase so lookups and the unique constraint are case-insensitive.
     */
    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

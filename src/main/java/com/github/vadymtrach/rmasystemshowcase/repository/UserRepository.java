package com.github.vadymtrach.rmasystemshowcase.repository;

import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findUserByEmail(String email);

    public boolean existsByRole(Role role);
}

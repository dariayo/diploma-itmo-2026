package com.diploma.repository;

import io.micrometer.common.lang.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import com.diploma.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @NotNull Optional<User> findById(@NotNull Long id);

    @NonNull
    List<User> findAll();
}

package com.dan.backend.repository;

import com.dan.backend.entity.User;
import com.dan.backend.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findAllByOrderByCreatedAtDesc();

    long countByRole(Role role);
}

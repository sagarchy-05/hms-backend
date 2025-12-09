package com.genc.hms.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.genc.hms.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user by their unique email address (used for login/authentication)
    Optional<User> findByEmail(String email);

    // Returns the total number of users with role ADMIN
    // Useful for enforcing constraints like at least one admin must exist
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    Long getAdminCount();
}

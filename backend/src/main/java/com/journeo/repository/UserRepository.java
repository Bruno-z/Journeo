package com.journeo.repository;

import com.journeo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Cette ligne permet de chercher un utilisateur par email
    Optional<User> findByEmail(String email);
}
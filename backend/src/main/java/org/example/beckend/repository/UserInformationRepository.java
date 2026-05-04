package org.example.beckend.repository;

import java.util.Optional;

import org.example.beckend.entity.UserInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInformationRepository extends JpaRepository<UserInformation, Long> {

    boolean existsByUserEmailIgnoreCase(String userEmail);

    Optional<UserInformation> findByUserEmailIgnoreCase(String userEmail);
}

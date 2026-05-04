package org.example.beckend.repository;

import org.example.beckend.entity.LoginAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAccountRepository extends JpaRepository<LoginAccount, Long> {
}

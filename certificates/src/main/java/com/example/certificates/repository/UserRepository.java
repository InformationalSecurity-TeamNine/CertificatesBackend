package com.example.certificates.repository;

import com.example.certificates.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByTelephoneNumber(String telephoneNumber);

    @Query("select u from User u where u.email=:email and u.isEmailConfirmed=true")
    Optional<User> isEmailConfirmed(String email);

    @Query("select u from User u inner join CertificateRequest cr on u.id=cr.issuer.id where cr.id=:id")
    User findByRequestId(Long id);

    @Query("select u from User u inner join Certificate c on u.id=c.user.id where c.id=:id")
    User getByCertificateId(Long id);

    @Query(value = "SELECT u FROM User u WHERE u.verification.verificationCode = ?1")
    Optional<User> findUserByVerification(String code);

    @Query(value = "select u.lastTimePasswordChanged from User u where u.email=:email")
    Optional<LocalDateTime> findLastTimePasswordChanged(String email);
}

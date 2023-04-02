package com.example.certificates.repository;

import com.example.certificates.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByTelephoneNumber(String telephoneNumber);

    @Query("select u from User u where u.email=:email and u.isEmailConfirmed=true")
    Optional<User> isEmailConfirmed(String email);

    @Query("select u from User u inner join CertificateRequest cr on u.id=cr.issuer.id where u.id=:id")
    User findByRequestId(Long id);
}

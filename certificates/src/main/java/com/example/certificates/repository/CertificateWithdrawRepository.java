package com.example.certificates.repository;

import com.example.certificates.model.CertificateWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateWithdrawRepository extends JpaRepository<CertificateWithdraw, Long> {
}


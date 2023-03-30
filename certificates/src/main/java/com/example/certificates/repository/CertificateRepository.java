package com.example.certificates.repository;

import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Query("select c from Certificate c where c.issuingCertificate.serialNumber=:issuerSN")
    Certificate findByIssuerSN(String issuerSN);

    @Query("select new com.example.certificates.dto.CertificateDTO(c.id, c.validFrom, u, c.type, c.serialNumber)" +
            " from Certificate c inner join User u where c.user.id=u.id")
    List<CertificateDTO> getAllCertificates();
}

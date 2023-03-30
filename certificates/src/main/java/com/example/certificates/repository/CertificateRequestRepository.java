package com.example.certificates.repository;

import com.example.certificates.dto.CertificateRequestResponse;
import com.example.certificates.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<Certificate, Long> {

    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.parentCertificate.serialNumber, c.issuer.email, c.keyUsageFlags, c.status)" +
            " from CertificateRequest c ")
    List<CertificateRequestResponse> getAllRequests();

    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.parentCertificate.serialNumber, c.issuer.email, c.keyUsageFlags, c.status)" +
            " from CertificateRequest c where c.issuer.id=:userId")
    List<CertificateRequestResponse> getRequestFromUser(Integer userId);
}

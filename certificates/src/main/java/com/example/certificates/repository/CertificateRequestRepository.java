package com.example.certificates.repository;

import com.example.certificates.dto.CertificateRequestResponse;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.parentCertificate.serialNumber, c.issuer.email,  c.status, c.certificateType)" +
            " from CertificateRequest c ")
    List<CertificateRequestResponse> getAllRequests();

    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.parentCertificate.serialNumber, c.issuer.email,  c.status, c.certificateType)" +
            " from CertificateRequest c where c.issuer.id=:userId")
    List<CertificateRequestResponse> getRequestFromUser(Integer userId);


    @Query("select u.id from CertificateRequest cr inner join Certificate c on cr.parentCertificate=c " +
            "inner join User u on c.user.id=u.id where cr.id=:requestId")
    Long getIssuerCertificateUserIdByRequestId(Long requestId);
}

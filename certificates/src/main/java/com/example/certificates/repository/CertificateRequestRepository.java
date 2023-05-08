package com.example.certificates.repository;

import com.example.certificates.dto.CertificateRequestResponse;
import com.example.certificates.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {


    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.issuer.email,  c.status, c.certificateType, c.reason)" +
            " from CertificateRequest c ")
    List<CertificateRequestResponse> getAllRequests();

    @Query("select new com.example.certificates.dto.CertificateRequestResponse(c.id, c.issuer.email,  c.status, c.certificateType, c.reason)" +
            " from CertificateRequest c inner join User u on c.issuer = u where u.id=:userId")
    List<CertificateRequestResponse> getRequestFromUser(Long userId);


    @Query("select u.id from CertificateRequest cr inner join Certificate c on cr.parentCertificate=c " +
            "inner join User u on c.user.id=u.id where cr.id=:requestId")
    Long getIssuerCertificateUserIdByRequestId(Long requestId);

    @Query("select cr.parentCertificate.user.email from CertificateRequest cr where cr.id=:id")
    String getIssuerByRequestId(Long id);
}

package com.example.certificates.repository;

import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Transactional
    @Query("select c from Certificate c where c.serialNumber=:serialNumber")
    Certificate findByIssuerSN(String serialNumber);


    @Transactional
    @Query("select new com.example.certificates.dto.CertificateDTO(c.id, c.validFrom, u, c.type, c.serialNumber)" +
            " from Certificate c inner join User u on c.user.id=u.id")
    List<CertificateDTO> getAllCertificates();


    @Query("select c.user from Certificate c where c.id=:id")
    User getUserByCertificateId(Long id);
}

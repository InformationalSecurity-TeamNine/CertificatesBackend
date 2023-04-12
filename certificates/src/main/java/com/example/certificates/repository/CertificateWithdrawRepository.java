package com.example.certificates.repository;

import com.example.certificates.dto.WithdrawnCertificateDTO;
import com.example.certificates.model.CertificateWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface CertificateWithdrawRepository extends JpaRepository<CertificateWithdraw, Long> {

    @Transactional
    @Query("select new com.example.certificates.dto.WithdrawnCertificateDTO(c.serialNumber, cw.reason, cw.withdrawnAt, u.email, cw.isChild) from CertificateWithdraw cw inner join Certificate c on cw.certificate=c inner join User u on cw.user=u")
    List<WithdrawnCertificateDTO> findAllWithdrawnCertificates();
}


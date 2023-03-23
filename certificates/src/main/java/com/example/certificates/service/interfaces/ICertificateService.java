package com.example.certificates.service.interfaces;

import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.Paginated;

public interface ICertificateService {

    Paginated<CertificateDTO> getAll();
}

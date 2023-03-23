package com.example.certificates.service.interfaces;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.Paginated;

public interface ICertificateService {

    Paginated<Certificate> getAll();

    DeclineRequestDTO declineRequest(Long id, String declineReason);

    AcceptRequestDTO acceptRequest(Long id);
}

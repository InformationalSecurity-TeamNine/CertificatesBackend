package com.example.certificates.repository;

import com.example.certificates.dto.PastPasswordsDTO;
import com.example.certificates.model.PastPasswords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PastPasswordRepository extends JpaRepository<PastPasswords, Long> {

    @Query(value = "select new com.example.certificates.dto.PastPasswordsDTO(pp.user.email, pp.password) " +
            "from PastPasswords pp where pp.user.id=:userId order by pp.timeChanged desc")
    List<PastPasswordsDTO> findPastPasswordsByUserId(Long userId);
}

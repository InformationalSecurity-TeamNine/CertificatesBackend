package com.example.certificates.model;

import lombok.*;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;


@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class IssuerData {

    private X500Name x500name;
    private PrivateKey privateKey;

}

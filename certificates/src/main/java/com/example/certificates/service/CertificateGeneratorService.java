package com.example.certificates.service;

import com.example.certificates.enums.CertificateStatus;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.exceptions.InvalidCertificateEndDateException;
import com.example.certificates.model.*;
import com.example.certificates.model.Certificate;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.ICertificateGeneratorService;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class CertificateGeneratorService implements ICertificateGeneratorService {
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    @Autowired
    public CertificateGeneratorService(CertificateRepository certificateRepository, UserRepository userRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public Certificate createCertificate(CertificateRequest certificateRequest, KeyPair keyPair) {
        Certificate certificate = new Certificate();
        certificate.setValidFrom(LocalDateTime.now());
        Certificate parentCertificate = null;
        if (certificateRequest.getCertificateType() == CertificateType.ROOT){
            certificate.setValidTo(certificateRequest.getTime());
            if (certificateRequest.getTime().isBefore(LocalDateTime.now())){
                throw new InvalidCertificateEndDateException("Certificate expiration date can not be in the past");
            }
        }else {
            parentCertificate = this.certificateRepository.getParentCertificateByRequestId(certificateRequest.getId());
            User parentIssuer = this.userRepository.getByCertificateId(parentCertificate.getId());
            parentCertificate.setUser(parentIssuer);
            certificate.setIssuingCertificate(parentCertificate);
            if (certificateRequest.getTime().isAfter(parentCertificate.getValidTo())){
                throw new InvalidCertificateEndDateException("Certificate expiration date can not be after parent certificate");
            }
            if (certificateRequest.getTime().isBefore(LocalDateTime.now())){
                throw new InvalidCertificateEndDateException("Certificate expiration date can not be in the past");
            }
            certificate.setValidTo(certificateRequest.getTime());
        }
        certificate.setSerialNumber(UUID.randomUUID().toString());
        certificate.setPublicKey(Base64.toBase64String(keyPair.getPublic().getEncoded()));
        if (certificate.getType()!= CertificateType.ROOT) {
            certificate.setIssuingCertificate(parentCertificate);
        }
        else certificate.setIssuingCertificate(null);
        User user = this.userRepository.findByRequestId(certificateRequest.getId());
        certificate.setUser(user);
        certificate.setStatus(CertificateStatus.VALID);
        certificate.setType(certificateRequest.getCertificateType());
        certificate.setSignatureAlgorithm("SHA256WithRSAEncryption");
        SubjectData subjectData = generateSubjectData(certificate);
        IssuerData issuerData  = generateIssuerData(certificate, keyPair, user);
        X509Certificate newCertificate = generateCertificate(subjectData, issuerData);
        certificate = this.certificateRepository.save(certificate);
        saveCertificate(newCertificate);
        savePrivateKey(newCertificate, keyPair);
        return certificate;
    }

    public X509Certificate generateCertificate(SubjectData subjectData, IssuerData issuerData) {
        try {
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
            builder = builder.setProvider("BC");
            ContentSigner contentSigner = builder.build(issuerData.getPrivateKey());

            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                    issuerData.getX500name(),
                    new BigInteger(subjectData.getSerialNumber().replace("-", ""), 16),
                    Date.from(subjectData.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(subjectData.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    subjectData.getX500name(),
                    subjectData.getPublicKey());
            X509CertificateHolder certHolder = certGen.build(contentSigner);
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");
            return certConverter.getCertificate(certHolder);
        } catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SubjectData generateSubjectData(Certificate certificate) {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, certificate.getUser().getEmail());
        builder.addRDN(BCStyle.SURNAME, certificate.getUser().getSurname());
        builder.addRDN(BCStyle.GIVENNAME, certificate.getUser().getName());
        builder.addRDN(BCStyle.UID, String.valueOf(certificate.getUser().getId()));

        return new SubjectData(convertByteToPublicKey(certificate.getPublicKey()), builder.build(), certificate.getSerialNumber(), certificate.getValidFrom().toLocalDate(), certificate.getValidTo().toLocalDate());
    }
    private IssuerData generateIssuerData(Certificate certificate, KeyPair keyPair, User user){
        PrivateKey issuerKey;
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        if (certificate.getType()!=CertificateType.ROOT) {
            builder.addRDN(BCStyle.CN, certificate.getIssuingCertificate().getUser().getEmail());
            builder.addRDN(BCStyle.SURNAME, certificate.getIssuingCertificate().getUser().getSurname());
            builder.addRDN(BCStyle.GIVENNAME, certificate.getIssuingCertificate().getUser().getName());
            builder.addRDN(BCStyle.UID, String.valueOf(certificate.getIssuingCertificate().getUser().getId()));
        }
        if (certificate.getType() == CertificateType.ROOT)
        {
            builder.addRDN(BCStyle.CN, user.getEmail());
            builder.addRDN(BCStyle.SURNAME, user.getSurname());
            builder.addRDN(BCStyle.GIVENNAME, user.getName());
            builder.addRDN(BCStyle.UID, String.valueOf(user.getId()));
            issuerKey = keyPair.getPrivate();
        }
        else if (Objects.equals(certificate.getUser().getId(), certificate.getIssuingCertificate().getUser().getId())) {
            issuerKey = keyPair.getPrivate();
        } else {
            issuerKey = getPrivateKey(certificate.getIssuingCertificate().getSerialNumber());
        }
        return new IssuerData(builder.build(), issuerKey);
    }

    private void savePrivateKey(X509Certificate certificate,KeyPair keyPair){
        try {
            JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter("keys/" + certificate.getSerialNumber() + ".key"));
            pemWriter.writeObject(keyPair.getPrivate());
            pemWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void saveCertificate(X509Certificate certificate){
        try {
            X509CertificateHolder certHolder = new JcaX509CertificateHolder(certificate);
            FileOutputStream fos = new FileOutputStream("certs/" + certificate.getSerialNumber() + ".crt");
            fos.write(certHolder.getEncoded());
            fos.close();
        } catch (CertificateEncodingException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public PrivateKey getPrivateKey(String certificateSN) {
        try {

            File keyFile = new File("keys/" + new BigInteger(certificateSN.replace("-", ""), 16) + ".key");
            PEMParser pemParser = new PEMParser(new FileReader(keyFile));
            Object obj = pemParser.readObject();
            pemParser.close();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey privateKey = null;
            if (obj instanceof PEMKeyPair) {
                privateKey = converter.getPrivateKey(((PEMKeyPair) obj).getPrivateKeyInfo());
            } else if (obj instanceof PrivateKeyInfo) {
                privateKey = converter.getPrivateKey((PrivateKeyInfo) obj);
            }
            return privateKey;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private PublicKey convertByteToPublicKey(String key){
        byte[] byteKey = Base64.decode(key.getBytes());
        try {
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



}

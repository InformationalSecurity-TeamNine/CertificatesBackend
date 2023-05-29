package com.example.certificates.service;

import com.example.certificates.controller.CertificateController;
import com.example.certificates.dto.*;

import com.example.certificates.enums.CertificateStatus;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.enums.RequestStatus;
import com.example.certificates.enums.UserRole;
import com.example.certificates.exceptions.*;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.CertificateWithdraw;
import com.example.certificates.model.User;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.repository.CertificateRequestRepository;
import com.example.certificates.repository.CertificateWithdrawRepository;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.security.UserRequestValidation;
import com.example.certificates.service.interfaces.ICertificateGeneratorService;
import com.example.certificates.service.interfaces.ICertificateService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CertificateService implements ICertificateService {
    private final CertificateRepository certificateRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final UserRepository userRepository;
    private final UserRequestValidation userRequestValidation;
    private final ICertificateGeneratorService certificateGeneratorService;

    private final CertificateWithdrawRepository certificateWithdrawRepository;
    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);


    @Autowired
    public CertificateService(CertificateRepository certificateRepository, CertificateRequestRepository certificateRequestRepository, UserRepository userRepository, UserRequestValidation userRequestValidation, ICertificateGeneratorService certificateGeneratorService, CertificateWithdrawRepository certificateWithdrawRepository){
        this.certificateRepository = certificateRepository;
        this.certificateRequestRepository = certificateRequestRepository;
        this.userRepository = userRepository;
        this.userRequestValidation = userRequestValidation;
        this.certificateGeneratorService = certificateGeneratorService;
        this.certificateWithdrawRepository = certificateWithdrawRepository;
    }

    @Override
    public List<CertificateUserDTO> getAll() {
        List<CertificateDTO> certificates = this.certificateRepository.getAllCertificates();
        List<CertificateUserDTO> newCertificates = new ArrayList<>();
        for(CertificateDTO certificate: certificates){
            newCertificates.add(new CertificateUserDTO(certificate));
        }
        return newCertificates;
    }

    @Override
    public List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        Integer usId = this.userRequestValidation.getUserId(authHeader);

        logger.info("Korisnik sa ID: " + usId + " je pokrenuo funkcionalnost za dobavljanje zahteva za izdavanje sertifikata.");

        List<CertificateRequestResponse> requests = new ArrayList<>();
        if(role.equalsIgnoreCase("admin")){
             requests =  this.certificateRequestRepository.getAllRequests();

        }
        else {
            Integer userId = this.userRequestValidation.getUserId(authHeader);
            requests = this.certificateRequestRepository.getRequestFromUser(userId.longValue());
        }
        for(CertificateRequestResponse request: requests){
            if(request.getType()!=CertificateType.ROOT)
                request.setIssuerUsername(this.certificateRequestRepository.getIssuerByRequestId(request.getId()));
        }
        return requests;
    }

    @Override
    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest, Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        Integer userId = this.userRequestValidation.getUserId(authHeader);
        logger.info("Korisnik sa id: " + userId + " je pokrenuo funkcionalnost za kreiranje sertifikata.");

        Certificate issuer = null;
        if (!certificateRequest.getIssuerSN().isEmpty()){
            issuer = this.certificateRepository.findByIssuerSN(certificateRequest.getIssuerSN());

        }
        if (issuer != null){
            issuer.setUser(this.certificateRepository.getUserByCertificateId(issuer.getId()));
            if(!this.isValid(issuer.getId()))
                throw new InvalidIssuerException("Issuing certificate is invalid.");

            validateIssuerEndCertificate(certificateRequest, issuer);
            //validateCertificateEndDate(certificateRequest, issuer);
        }
        CertificateRequest request = new CertificateRequest();
        Optional<User> requestIssuer = this.userRepository.findById(Long.valueOf(userId));
        requestIssuer.ifPresent(request::setIssuer);
        request.setStatus(RequestStatus.PENDING);
        request.setParentCertificate(issuer);
        request.setTime(certificateRequest.getTime());


        if(role.equalsIgnoreCase("admin")){

            if (issuer!=null){
                validateIssuer(certificateRequest);
            }
            request.setCertificateType(CertificateType.valueOf(certificateRequest.getType()));
            CertificateRequest newRequest = this.certificateRequestRepository.save(request);

//            if(request.getParentCertificate() != null && userId.longValue() == request.getParentCertificate().getUser().getId()) {
                this.acceptRequest(newRequest.getId(), authHeader);
                newRequest.setStatus(RequestStatus.ACCEPTED);
           // }

//            else if(request.getParentCertificate() == null){
//                this.acceptRequest(newRequest.getId(), authHeader);
//                newRequest.setStatus(RequestStatus.ACCEPTED);
//            }
            return newRequest;
        }
        if(certificateRequest.getType().toString().equalsIgnoreCase(CertificateType.ROOT.toString())){

            logger.warn("Korisnik sa id: " + userId + " nije uspesno kreirao sertifikat.");
            throw new InvalidCertificateTypeException("Cannot create root certificate as a default user");
        }
        validateIssuer(certificateRequest);
        request.setCertificateType(CertificateType.valueOf(certificateRequest.getType()));
        if(issuer == null){
            logger.warn("Korisnik sa id: " + userId + " nije uspesno kreirao sertifikat.");

            throw new NonExistingParentCertificateException("Parent with that serial number does not exist");
        }
        CertificateRequest newRequest = this.certificateRequestRepository.save(request);

        if(issuer.getUser().getId() == userId.longValue()){
            this.acceptRequest(newRequest.getId(), authHeader);
        }
        logger.info("Korisnik sa id: " + userId + " je uspesno kreirao sertifikat.");

        return newRequest;
    }

    @Override
    public boolean isValid(Long id) {
        logger.info("Ulogovan korisnik je pokrenuo funkcionalnost za proveru validnosti sertifikata preko IDa: " + id);

        Optional<Certificate> certificate = certificateRepository.findById(id);
        if(certificate.isEmpty()) {

            logger.warn("Sertifikat sa idom: " + id + " ne postoji.");
            throw new NonExistingCertificateException("Certificate with the given ID does not exist.");
        }
        if(isExpired(certificate.get())) return false;
        if(isWithdrawn(certificate.get())) return false;
        return !isStoredCertificateInvalid(id);
    }

    @Override
    public String findCertificateFileName(Long id) {
        Optional<Certificate> certificate = certificateRepository.findById(id);
        if(certificate.isEmpty())
            throw new NonExistingCertificateException("Certificate with the given ID does not exist.");

        String serialNumber = certificate.get().getSerialNumber();
        String fileName = new BigInteger(serialNumber.replace("-", ""), 16).toString();

        return fileName;
    }



    @Override
    public CertificateWithdrawDTO withdraw(Long id, WithdrawReasonDTO withdrawReason, Map<String, String> headers) {
        Integer userId = this.userRequestValidation.getUserId(headers);
        logger.info("Korisnik sa ID: " + userId + " je pokrenuo funkcionalnost za povlacenje sertifikata.");

        String role = this.userRequestValidation.getRoleFromToken(headers);

        Optional<User> userOpt = this.userRepository.findById(userId.longValue());
        if(userOpt.isEmpty()) throw new NonExistingUserException("User with given id not found");


        Optional<Certificate> certificateOpt = this.certificateRepository.findById(id);
        if(certificateOpt.isEmpty()) throw new NonExistingCertificateException("Certificate with the given ID does not exist.");
        Certificate certificate = certificateOpt.get();
        if(certificate.getStatus().toString().equals(CertificateStatus.NOT_VALID.toString()))
            throw new CertificateWithdrawnException("The certificate with the given id is already withdrawn.");
        if(role.toString().equals(UserRole.BASIC_USER.toString())){
            User user = this.userRepository.getByCertificateId(certificate.getId());
            if(!Objects.equals(user.getId(), userOpt.get().getId()))
                throw new NonExistingCertificateException("The certificate with the given id does not exist.");

        }

        certificate.setStatus(CertificateStatus.NOT_VALID);
        certificate = this.certificateRepository.save(certificate);

        LocalDateTime now = LocalDateTime.now();
        this.certificateWithdrawRepository.save(
                new CertificateWithdraw(
                        userOpt.get(),
                        certificate,
                        now,
                        withdrawReason.getReason(),
                        false
                )
        );
        withdrawCertificateChain(id, now, userOpt.get(), withdrawReason.getReason());

        logger.info("Korisnik sa ID: " + userId + " je uspesno povukao zeljeni sertifikat i svu njegovudecu.");
        return new CertificateWithdrawDTO(certificate.getId(), withdrawReason.getReason());
    }

    @Override
    public List<WithdrawnCertificateDTO> getWithdrawnCertificates() {
        return this.certificateWithdrawRepository.findAllWithdrawnCertificates();
    }

    private void withdrawCertificateChain(Long parentCertificateId,
                                          LocalDateTime withdrawTime,
                                          User user,
                                          String reason){
        List<Certificate> certificates = this.certificateRepository.findByParentId(parentCertificateId);
        if(certificates.isEmpty())
            return;

        for(Certificate certificate:certificates){
            if(certificate.getStatus().equals(CertificateStatus.NOT_VALID)) continue;
            if(certificate.getType() == CertificateType.END)
                certificate.setStatus(CertificateStatus.NOT_VALID);
            else
            {
                certificate.setStatus(CertificateStatus.NOT_VALID);
                this.withdrawCertificateChain(certificate.getId(), withdrawTime, user, reason);
            }
            this.certificateRepository.save(certificate);
            this.certificateWithdrawRepository.save(
                    new CertificateWithdraw(
                           user,
                            certificate,
                            withdrawTime,
                            reason,
                            true
                    )
            );
        }

    }

    private byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            // Kreiranje objekta koji nudi funkcionalnost digitalnog potpisivanja
            // Prilikom getInstance poziva prosledjujemo algoritam koji cemo koristiti
            // U ovom slucaju cemo generisati SHA-1 hes kod koji cemo potpisati upotrebom RSA asimetricne sifre
            Signature sig = Signature.getInstance("SHA1withRSA");

            // Navodimo kljuc kojim potpisujemo
            sig.initSign(privateKey);

            // Postavljamo podatke koje potpisujemo
            sig.update(data);

            // Vrsimo potpisivanje
            return sig.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            // Kreiranje objekta koji nudi funkcionalnost digitalnog potpisivanja
            // Prilikom getInstance poziva prosledjujemo algoritam koji cemo koristiti
            // U ovom slucaju cemo generisati SHA-1 hes kod koji cemo potpisati upotrebom RSA asimetricne sifre
            Signature sig = Signature.getInstance("SHA1withRSA");

            // Navodimo kljuc sa kojim proveravamo potpis
            sig.initVerify(publicKey);

            // Postavljamo podatke koje potpisujemo
            sig.update(data);

            // Vrsimo proveru digitalnog potpisa
            return sig.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private java.security.cert.Certificate readFromBinEncFile(String sn) {
        try {
            String name = "certs/" + new BigInteger(sn.replace("-", ""), 16) + ".crt";

            FileInputStream fis = new FileInputStream(name);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(fis);
            Iterator i = c.iterator();
            while (i.hasNext()) {
                return (java.security.cert.Certificate) i.next();
            }
        } catch (FileNotFoundException | CertificateException e) {
            return null;
        }
        return null;
    }
    private PublicKey convertStringToPublicKey(String key) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            return keyFactory.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public X509Certificate getX509CertificateFromFile(MultipartFile file) {

        if(file.getSize() > 1073741824) {
            //return false;
            logger.warn("Korisnik je pokusao da uploaduje fajl koji je veci od dozvoljene granice,");

            throw new InvalidFileException("Upladed file is bigger than 1gb");
        }

        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if(!fileExtension.equals("crt")) {
           // return false;
            logger.warn("Korisnik je pokusao da uploaduje fajl koji nije crt formata.");

            throw new InvalidFileException("Uploaded file isn't .crt file");
        }

        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            //return null;
            throw new InvalidFileException("File could not be read ");
        }

        CertificateFactory factory;
        X509Certificate cert;
        try {
            factory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) factory.generateCertificate(inputStream);
        } catch (CertificateException e) {
            //return null;
            logger.warn("Korisnik je pokusao da uploaduje fajl koji je bio modifikovan.");

            throw new InvalidFileException("Certificate has been modified");
        }

        return cert;

    }


    @Override
    public Certificate getCertificateFromX509Certificate(X509Certificate certX509){
//        System.out.println("Ovo je uneti SN: " + cert.getSerialNumber().toString());
//        System.out.println("Ovo je vraceni unatrag: " + cert.getSerialNumber().toString(16));

        StringBuilder sb = new StringBuilder(certX509.getSerialNumber().toString(16));

        sb.insert(8, "-");
        sb.insert(13, "-");
        sb.insert(18, "-");
        sb.insert(23, "-");

        String modifiedString = sb.toString();
//        System.out.println("Ovo je krajnji:" + modifiedString);

        Certificate cert = this.certificateRepository.findByIssuerSN(modifiedString);
        if(cert == null) throw new NonExistingCertificateException("The certificate with this SN does not exist");
        return cert;
    }

    @Override
    public boolean isUploadedInvalid(X509Certificate certX509, Certificate cert) {

        String sn = cert.getSerialNumber();


        PublicKey probaKey = certX509.getPublicKey();

        byte[] probaSignature = certX509.getSignature();

        Signature sig;
        try {
            sig = Signature.getInstance(certX509.getSigAlgName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            sig.initVerify(probaKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        try {
            sig.update(certX509.getTBSCertificate());
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        boolean signatureValid;
        try {
            signatureValid = sig.verify(probaSignature);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

        return signatureValid;
    }

    @Override
    public byte[] getZipContents(String publicPartPath, String privatePartPath, Map<String, String> authHeader, Long certificateId) {
        Integer userId = this.userRequestValidation.getUserId(authHeader);
        String role = this.userRequestValidation.getRoleFromToken(authHeader);

        Optional<User> userOpt = this.userRepository.findById(userId.longValue());
        logger.info("Korisnik sa id: " + userId + " je pokrenuo funkcionalnost za preuzimanje fajlova");

        if(userOpt.isEmpty()) throw new NonExistingUserException("User with given id not found");


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        try {

            File publicPartFile = new File(publicPartPath);
            FileInputStream publicPartInputStream = new FileInputStream(publicPartFile);
            ZipEntry publicPartEntry = new ZipEntry(publicPartFile.getName());
            zipOutputStream.putNextEntry(publicPartEntry);
            byte[] publicPartBytes = new byte[1024];
            int length;
            while ((length = publicPartInputStream.read(publicPartBytes)) >= 0) {
                zipOutputStream.write(publicPartBytes, 0, length);
            }
            publicPartInputStream.close();
            zipOutputStream.closeEntry();

            Optional<Certificate> certificate = certificateRepository.findById(certificateId);
            if(certificate.isEmpty()) {
                throw new NonExistingCertificateException("Certificate with the given ID does not exist.");

            }

            if(role.equals(UserRole.ADMIN.toString()) || (long)userId == certificate.get().getUser().getId()){

                File privatePartFile = new File(privatePartPath);
                FileInputStream privatePartInputStream = new FileInputStream(privatePartFile);
                ZipEntry privatePartEntry = new ZipEntry(privatePartFile.getName());
                zipOutputStream.putNextEntry(privatePartEntry);
                byte[] privatePartBytes = new byte[1024];
                while ((length = privatePartInputStream.read(privatePartBytes)) >= 0) {
                    zipOutputStream.write(privatePartBytes, 0, length);
                }
                privatePartInputStream.close();
                zipOutputStream.closeEntry();

            }

            zipOutputStream.close();

        } catch (IOException e) {
            throw new InvalidFileException("Error while creating zip archive");
        }

        logger.warn("Korisnik sa id: " + userId + " je uspesno preuzeo fajl.");
        return byteArrayOutputStream.toByteArray();
    }

    private boolean isStoredCertificateInvalid(Long id){
        Optional<Certificate> certificate = this.certificateRepository.findById(id);
        if(certificate.isEmpty()){

            throw new NonExistingCertificateException("Certificate with that id does not exist");
        }
        String publicKey = certificate.get().getPublicKey();

        String sn = certificate.get().getSerialNumber();
        PrivateKey privateKey = this.certificateGeneratorService.getPrivateKey(sn);
        java.security.cert.Certificate certificate1 = readFromBinEncFile(sn);
        if (certificate1 == null){
            return true;
        }
        byte[] dataToSign = new byte[0];
        try {
            dataToSign = certificate1.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        byte[] signature = sign(dataToSign, privateKey);


        return !verify(dataToSign,signature,convertStringToPublicKey(publicKey));
    }

    private boolean isWithdrawn(Certificate certificate){
        return certificate.getStatus().toString().equals(CertificateStatus.NOT_VALID.toString());
    }

    private boolean isExpired(Certificate certificate){
        return  certificate.getValidTo().isBefore(LocalDateTime.now());
    }


    private void validateIssuerEndCertificate(CertificateRequestDTO certificateRequest, Certificate issuer) {
        if(certificateRequest.getIssuerSN().length() > 0) {
            if(issuer.getType().toString().equals(CertificateType.END.toString()))
                throw new EndIssuerException("Type of issuer certificate cannot be end.");
        }
    }

    private void validateIssuer(CertificateRequestDTO certificateRequest) {
        if(certificateRequest.getIssuerSN().isEmpty()){
            throw new InvalidIssuerException("Issuer cannot be null for intermediate or end certificates.");
        }
    }

    public DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader) {

        Integer userId = this.userRequestValidation.getUserId(authHeader);

        logger.info("Korisnik sa id: " + userId + " je pokrenuo funkcionalnost za odbijanje sertifikata");
        Optional<CertificateRequest> request = this.certificateRequestRepository.findById(id);
        if(request.isEmpty()) throw new NonExistingRequestException("The request with the given id doesn't exist");

        if (request.get().getStatus()!=RequestStatus.PENDING){
            throw new RequestAlreadyProcessedException("The request has already been processed");
        }
        if(userId.longValue() != this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId())){
            throw new NonExistingRequestException("The request with the given id doesn't exist");
        }



        request.get().setStatus(RequestStatus.DECLINED);
        request.get().setReason(declineReason);
        DeclineRequestDTO declineRequestDTO = new DeclineRequestDTO(request.get().getId(), declineReason);
        this.certificateRequestRepository.save(request.get());

        logger.info("Korisnik sa id: " + userId + " je uspesno odbio sertifikat");

        return declineRequestDTO;
    }

    @Override
    public String acceptRequest(Long id, Map<String, String> authHeader) {

        Integer userId = this.userRequestValidation.getUserId(authHeader);
        logger.info("Korisnik sa id: " + userId + " je pokrenuo funkcionalnost za prihvatanje zahteva");

        Optional<CertificateRequest> request = this.certificateRequestRepository.findById(id);
        if(request.isEmpty()) throw new NonExistingRequestException("The request with the given id doesn't exist");

        if (request.get().getCertificateType()!=CertificateType.ROOT){
            if (this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId()) == null){
                logger.warn("Korisnik sa id: " + userId + " nije uspesno prihvatio zahtev.");

                throw new NonExistingParentCertificateException("Invalid parent Id");
            }

            if (!this.userRequestValidation.getRoleFromToken(authHeader).equalsIgnoreCase("admin") && userId.longValue() != this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId())) {
                logger.warn("Korisnik sa id: " + userId + " nije uspesno prihvatio zahtev.");

                throw new NonExistingRequestException("The request with the given id doesn't exist");
            }
        }

        if (request.get().getStatus()!=RequestStatus.PENDING){
            logger.warn("Korisnik sa id: " + userId + " nije uspesno prihvatio zahtev.");

            throw new RequestAlreadyProcessedException("The request has already been processed");
        }
        KeyPair keyPair = certificateGeneratorService.generateKeyPair();
        this.certificateGeneratorService.createCertificate(request.get(), keyPair);
        request.get().setStatus(RequestStatus.ACCEPTED);
        this.certificateRequestRepository.save(request.get());
        logger.info("Korisnik sa id: " + userId + " je uspesno prihvatio zahtev");

        return "Request accepted";
    }


}

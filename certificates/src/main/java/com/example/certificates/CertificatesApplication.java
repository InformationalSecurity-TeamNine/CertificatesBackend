package com.example.certificates;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class CertificatesApplication {

	//"email": "bogdan@gmail.com",
//	"password": "Bogdan1234!"
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(CertificatesApplication.class, args);
	}

}

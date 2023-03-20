package com.example.certificates.controllerTests;

import com.example.certificates.dto.LoginDTO;
import com.example.certificates.dto.TokenDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {
    private final String BASE_PATH = "http://localhost:8082/api/user";

    @Autowired
    private TestRestTemplate restTemplate;

    private TestRestTemplate adminRestTemplate, userRestTemplate;

    private String adminToken, userToken;

    HttpHeaders headers = new HttpHeaders();

    @BeforeAll
    private void initalize(){
        headers.setContentType(MediaType.APPLICATION_JSON);
        private String adminEmail = "";
        private String adminPassword = "";
        private String userEmail = "";
        private String userPassword = "";
        loginAsAdmin(adminEmail, adminPassword);
        loginAsUser(userEmail, userPassword);
        createRestTemplatesForUsers();
    }

    private void createRestTemplatesForUsers() {
        RestTemplateBuilder builder = new RestTemplateBuilder(rt -> rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("X-Auth-Token", this.adminToken);
            return execution.execute(request, body);
        }));
        this.adminRestTemplate = new TestRestTemplate(builder);

        RestTemplateBuilder passBuilder = new RestTemplateBuilder(rt -> rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("X-Auth-Token", this.userToken);
            return execution.execute(request, body);
        }));

        this.userRestTemplate = new TestRestTemplate(passBuilder);
    }

    private void loginAsUser(String userEmail, String userPassword) {
        HttpEntity<LoginDTO> userLogin = new HttpEntity<>(new LoginDTO(userEmail, userPassword), headers);

        ResponseEntity<TokenDTO> userResponse = restTemplate
                .exchange("/api/user/login",
                        HttpMethod.POST,
                        userLogin,
                        new ParameterizedTypeReference<TokenDTO>() {
                        });

        this.userToken = userResponse.getBody().getToken();
    }

    private void loginAsAdmin(String adminEmail, String adminPassword) {
        HttpEntity<LoginDTO> adminLogin = new HttpEntity<>(new LoginDTO(adminEmail, adminPassword), headers);

        ResponseEntity<TokenDTO> adminResponse = restTemplate
                .exchange("/api/user/login",
                        HttpMethod.POST,
                        adminLogin,
                        new ParameterizedTypeReference<TokenDTO>() {
                        });

        this.adminToken = adminResponse.getBody().getToken();
    }

}

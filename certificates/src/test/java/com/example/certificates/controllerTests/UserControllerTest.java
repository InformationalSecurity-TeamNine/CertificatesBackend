package com.example.certificates.controllerTests;

import com.example.certificates.dto.LoginDTO;
import com.example.certificates.dto.TokenDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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

//    @BeforeAll
//    private void initalize(){
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        String adminEmail = "marko@gmail.com";
//        String adminPassword = "$2a$12$XiI35lbz2Lr1UgWJZhjCQuOTpj9L52tOAJfEEhrXhd6wNwigLztPK";
//        String userEmail = "nebojsa@gmail.com";
//        String userPassword = "$2a$12$HXFqjPtx.FE7OP530tCzQOMibvJx8RfFzzUJKoCB4wo7ugTTSReS6";
//        loginAsAdmin(adminEmail, adminPassword);
//        loginAsUser(userEmail, userPassword);
//        createRestTemplatesForUsers();
//    }
//
//    private void createRestTemplatesForUsers() {
//        RestTemplateBuilder builder = new RestTemplateBuilder(rt -> rt.getInterceptors().add((request, body, execution) -> {
//            request.getHeaders().add("X-Auth-Token", this.adminToken);
//            return execution.execute(request, body);
//        }));
//        this.adminRestTemplate = new TestRestTemplate(builder);
//
//        RestTemplateBuilder passBuilder = new RestTemplateBuilder(rt -> rt.getInterceptors().add((request, body, execution) -> {
//            request.getHeaders().add("X-Auth-Token", this.userToken);
//            return execution.execute(request, body);
//        }));
//
//        this.userRestTemplate = new TestRestTemplate(passBuilder);
//    }
//
//    private void loginAsUser(String userEmail, String userPassword) {
//        HttpEntity<LoginDTO> userLogin = new HttpEntity<>(new LoginDTO(userEmail, userPassword), headers);
//
//        ResponseEntity<TokenDTO> userResponse = restTemplate
//                .exchange("/api/user/login",
//                        HttpMethod.POST,
//                        userLogin,
//                        new ParameterizedTypeReference<TokenDTO>() {
//                        });
//
//        this.userToken = userResponse.getBody().getToken();
//    }
//
//    private void loginAsAdmin(String adminEmail, String adminPassword) {
//        HttpEntity<LoginDTO> adminLogin = new HttpEntity<>(new LoginDTO(adminEmail, adminPassword), headers);
//
//        ResponseEntity<TokenDTO> adminResponse = restTemplate
//                .exchange("/api/user/login",
//                        HttpMethod.POST,
//                        adminLogin,
//                        new ParameterizedTypeReference<TokenDTO>() {
//                        });
//
//        this.adminToken = adminResponse.getBody().getToken();
//    }
//
//    @Test
//    @DisplayName("Logs in as a Regular User")
//    public void login_as_regularUser(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("bogdan@gmail.com", "Bogdan1234!"), headers);
//
//        ResponseEntity<TokenDTO> response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<TokenDTO>() {
//                }
//        );
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody().getToken());
//    }
//    @Test
//    @DisplayName("Logs in as an Admin")
//    public void login_as_admin(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("marko@gmail.com", "Marko1234!"), headers);
//
//        ResponseEntity<TokenDTO> response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<TokenDTO>() {
//                }
//        );
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody().getToken());
//    }
//    @Test
//    @DisplayName("Doesnt log in with invalid Input")
//    public void login_with_invalidInput(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("marko123@gmail.com", "Marko1234!"), headers);
//
//        ResponseEntity<TokenDTO> response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<TokenDTO>() {
//                }
//        );
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    }
//
//    @Test
//    @DisplayName("Doesnt log in with non confirmed account")
//    public void login_with_nonConfirmedAccount(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("mirko@gmail.com", "Mirko1234!"), headers);
//
//        ResponseEntity<TokenDTO> response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<TokenDTO>() {
//                }
//        );
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//
//    }
//
//    @Test
//    @DisplayName("Doesnt log in with wrong email input format")
//    public void login_with_wrong_email_inputFormat(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("mirkomail.com", "Mirko1234!"), headers);
//
//        ResponseEntity response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<>() {
//                }
//        );
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//
//    }
//
//    @Test
//    @DisplayName("Doesnt log in with wrong password input format")
//    public void login_with_wrong_password_inputFormat(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("mirko@gmail.com", "safas!"), headers);
//
//        ResponseEntity response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<>() {
//                }
//        );
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//
//    }
//    @Test
//    @DisplayName("Doesnt log in with empty password and email")
//    public void login_with_empty_input(){
//
//        HttpEntity<LoginDTO> loginDTO =
//                new HttpEntity<>(new LoginDTO("", ""), headers);
//
//        ResponseEntity response = this.restTemplate.exchange(
//                "/api/user/login",
//                HttpMethod.POST,
//                loginDTO,
//                new ParameterizedTypeReference<>() {
//                }
//        );
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//
//    }

}

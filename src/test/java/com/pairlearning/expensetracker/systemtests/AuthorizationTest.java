package com.pairlearning.expensetracker.systemtests;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthorizationTest {
    public static String baseUrl = "http://localhost:8080";

    @Test
    public void testUserRegistration() {
        String requestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \"email@example.com\", \"password\": \"password123\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl+"/api/users/register");

        assertEquals(201, response.getStatusCode());
        assertEquals("User registered successfully", response.jsonPath().getString("message"));
    }

}

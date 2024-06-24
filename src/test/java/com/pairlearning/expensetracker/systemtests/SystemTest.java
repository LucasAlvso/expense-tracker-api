package com.pairlearning.expensetracker.systemtests;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SystemTest
{
    public static String baseUrl = "http://localhost:8080";

    @Test
    public void userShouldntBeAbleToRegisterExistingEmail(){
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";

        String firstRequestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \""+randomEmail+"\", \"password\": \"123\" }";
        String secondRequestBody = "{ \"firstName\": \"Test2\", \"lastName\": \"Use2r\", \"email\": \""+randomEmail+"\", \"password\": \"456\" }";

        Response firstRegistration = given()
                .header("Content-Type", "application/json")
                .body(firstRequestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(200, firstRegistration.getStatusCode());

        Response secondRegistration = given()
                .header("Content-Type", "application/json")
                .body(secondRequestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(401, secondRegistration.getStatusCode());
    }

    @Test
    public void userShouldntBeAbleToRegisterExistingEmailButWithDifferentCase(){
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";
        String randomEmailUpper = "EMAIL" + System.currentTimeMillis() + "@TEST.COM";

        String firstRequestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \""+randomEmail+"\", \"password\": \"123\" }";
        String secondRequestBody = "{ \"firstName\": \"Test2\", \"lastName\": \"Use2r\", \"email\": \""+randomEmailUpper+"\", \"password\": \"456\" }";

        Response firstRegistration = given()
                .header("Content-Type", "application/json")
                .body(firstRequestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(200, firstRegistration.getStatusCode());

        Response secondRegistration = given()
                .header("Content-Type", "application/json")
                .body(secondRequestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(401, secondRegistration.getStatusCode());
    }

    @Test
    public void emailMustHaveACapOf30CharactersInSize(){
        String randomEmail = "emailaaaaaaa" + System.currentTimeMillis() + "@test.com";

        String requestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \""+randomEmail+"\", \"password\": \"123\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(401, response.getStatusCode());
    }


    @Test
    public void UnregisteredUsersShouldntBeAbleToAccessCategories() {
        Response response = given()
                .get(baseUrl+"/api/categories");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void InvalidTokensShouldntBeAbleToAccessCategories()
    {
        Response response = given()
                .header("Authorization", "Bearer invalidtoken")
                .get(baseUrl+"/api/categories");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void testUserRegistrationReturnsTokenAndAuthenticatesUser() {
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";

        String requestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \""+randomEmail+"\", \"password\": \"123\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl+"/api/users/register");
        assertEquals(200, response.getStatusCode());


        Map responseMap = response.getBody().as(Map.class);
        String jwtToken = (String) responseMap.get("token");


        Response response2 = given()
                .header("Authorization", "Bearer "+jwtToken)
                .get(baseUrl+"/api/categories");
        assertEquals(200, response2.getStatusCode());
        assertEquals("[]", response2.getBody().asString());
    }

    @Test
    public void userShouldBeAbleToRegisterLogInAndThenAccessCategories()
    {
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";

        String registrationRequestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \"" + randomEmail + "\", \"password\": \"123\" }";

        // Register user
        Response registrationResponse = given()
                .header("Content-Type", "application/json")
                .body(registrationRequestBody)
                .post(baseUrl + "/api/users/register");
        assertEquals(200, registrationResponse.getStatusCode());

        String loginRequestBody = "{\"email\": \"" + randomEmail + "\", \"password\": \"123\"}";

        // Log in user
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(loginRequestBody)
                .post(baseUrl + "/api/users/login");
        assertEquals(200, loginResponse.getStatusCode());

        // Get token and try to access categories
        String token = loginResponse.getBody().jsonPath().getString("token");
        Response categoriesResponse = given()
                .header("Authorization", "Bearer " + token)
                .get(baseUrl + "/api/categories");
        assertEquals(200, categoriesResponse.getStatusCode());
        assertEquals("[]", categoriesResponse.getBody().asString());
    }

    @Test
    public void invalidEmailFormatShouldReturnUnauthorized() {
        String requestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \"invalid-email\", \"password\": \"123456\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl + "/api/users/register");
        assertEquals(401, response.getStatusCode());
    }

    // Validation failed, test won't pass. Defect detected.
    @Test
    public void emptyPasswordShouldReturnBadRequest() {
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";
        String requestBody = "{ \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \""+randomEmail+"\", \"password\": \"\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl + "/api/users/register");
        assertEquals(400, response.getStatusCode());
    }


    // Validation failed, test won't pass. Defect detected.
    @Test
    public void emptyRequiredFieldsShouldReturnBadRequest() {
        String randomEmail = "email" + System.currentTimeMillis() + "@test.com";
        String requestBody = "{ \"firstName\": \"\", \"lastName\": \"\", \"email\": \""+randomEmail+"\", \"password\": \"123\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(baseUrl + "/api/users/register");
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void accessCategoriesWithInvalidBearerPrefixShouldReturnForbidden() {
        String invalidToken = "invalidtoken";

        Response response = given()
                .header("Authorization", "InvalidBearer " + invalidToken)
                .get(baseUrl + "/api/categories");
        assertEquals(403, response.getStatusCode());
    }




}

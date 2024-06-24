package com.pairlearning.expensetracker.unittests;

import com.pairlearning.expensetracker.domain.User;
import com.pairlearning.expensetracker.resources.UserResource;
import com.pairlearning.expensetracker.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;


    @Before
    public void setup() {
        reset(userService); // Reset mock interactions before each test
    }

    @Test
    public void testRegisterUser() {
        // Prepare user registration data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", "John");
        userMap.put("lastName", "Doe");
        userMap.put("email", "john.doe@example.com");
        userMap.put("password", "password123");

        // Mock UserService behavior for registration
        User mockUser = new User(1, "John", "Doe", "john.doe@example.com", "password123");
        when(userService.registerUser("John", "Doe", "john.doe@example.com", "password123")).thenReturn(mockUser);

        // Perform user registration
        ResponseEntity<Map<String, String>> responseEntity = userResource.registerUser(userMap);

        // Verify that a valid JWT token is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().containsKey("token"));
    }

    @Test
    public void testRegisterUserWithoutPassword() {
        // Prepare user registration data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", "John");
        userMap.put("lastName", "Doe");
        userMap.put("email", "john.doe@example.com");
        userMap.put("password", null);

        // Mock UserService behavior for registration
        User mockUser = new User(1, "John", "Doe", "john.doe@example.com", null);
        when(userService.registerUser("John", "Doe", "john.doe@example.com", null)).thenReturn(mockUser);

        // Perform user registration
        ResponseEntity<Map<String, String>> responseEntity = userResource.registerUser(userMap);

        // Verify that a valid JWT token is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().containsKey("token"));
    }

    @Test
    public void testRegisterUserWithoutEmail() {
        // Prepare user registration data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", "John");
        userMap.put("lastName", "Doe");
        userMap.put("email", null);
        userMap.put("password", "password123");

        // Mock UserService behavior for registration
        User mockUser = new User(1, "John", "Doe", null, "password123");
        when(userService.registerUser("John", "Doe", null, "password123")).thenReturn(mockUser);

        // Perform user registration
        ResponseEntity<Map<String, String>> responseEntity = userResource.registerUser(userMap);

        // Verify that a valid JWT token is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().containsKey("token"));
    }

    @Test
    public void testLoginUser() {
        // Prepare user login data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "john.doe@example.com");
        userMap.put("password", "password123");

        // Mock UserService behavior for login
        User mockUser = new User(1, "John", "Doe", "john.doe@example.com", "password123");
        when(userService.validateUser("john.doe@example.com", "password123")).thenReturn(mockUser);

        // Perform user login
        ResponseEntity<Map<String, String>> responseEntity = userResource.loginUser(userMap);

        // Verify that a valid JWT token is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().containsKey("token"));
    }

    @Test
    public void testLoginUserInvalidCredentials() {
        // Prepare user login data with invalid password
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "teste@example.com");
        userMap.put("password", "invalid_password");

        // Mock UserService behavior for invalid login
        // Had to put new User() because it breaks if return null
        when(userService.validateUser("teste@example.com", "invalid_password")).thenReturn(null);

        // Perform user login
        ResponseEntity<Map<String, String>> responseEntity = userResource.loginUser(userMap);

        // Verify that an error response is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().containsKey("token"));
    }

    @Test
    public void testLoginEmptyUser() {
        // Prepare user login data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "");
        userMap.put("password", "");

        // Mock UserService behavior for login
        User mockUser = new User(1, "John", "Doe", "", "");
        when(userService.validateUser("", "")).thenReturn(mockUser);

        // Perform user login
        ResponseEntity<Map<String, String>> responseEntity = userResource.loginUser(userMap);

        // Verify that a valid JWT token is returned
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().containsKey("token"));
    }

}

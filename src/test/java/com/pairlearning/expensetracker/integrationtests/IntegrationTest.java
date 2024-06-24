package com.pairlearning.expensetracker.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
public class IntegrationTest
{

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private String token;

    @Container
    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("expensetrackerdb")
            .withUsername("postgres")
            .withPassword("postgres");
    @Before
    public void startContainer() {
        postgresqlContainer.start();
    }
    @After
    public void stopContainer() {
        postgresqlContainer.stop();
    }

    @Test
    public void testDatabaseConnection() {
        // Check if the database container is running and accessible. Pre-requisite for the following tests
        String jdbcUrl = postgresqlContainer.getJdbcUrl();
        assertNotNull(jdbcUrl);
    }

   @Before
   public void setup() throws Exception {
       objectMapper = new ObjectMapper();
       String randomEmail = "email" + System.currentTimeMillis() + "@test.com";
       // Register a new user
       Map<String, Object> userMap = new HashMap<>();
       userMap.put("firstName", "John");
       userMap.put("lastName", "Doe");
       userMap.put("email", randomEmail);
       userMap.put("password", "password");

       ResultActions resultActions = mockMvc.perform(post("/api/users/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(userMap)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").exists());

       token = resultActions.andReturn().getResponse().getContentAsString();
       token = objectMapper.readTree(token).get("token").asText();
   }

    @Test
    @Order(1)
    public void testUserRegistrationAndLogin() throws Exception {
        // Login with the registered user
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "johndoe@example.com");
        userMap.put("password", "password");

        ResultActions resultActions = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Set class attribute for further requests
        token = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("token").asText();

        // Add a new category
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("title", "Food");
        categoryMap.put("description", "Expenses for food");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryMap)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Food")))
                .andExpect(jsonPath("$.description", is("Expenses for food")));

        // Fetch all categories and assert there is at least one
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$[0].title", is("Food")))
                .andExpect(jsonPath("$[0].description", is("Expenses for food")));
    }

    @Test
    public void userShouldntBeAbleToAccessCategoriesWithoutToken() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userShouldntBeAbleToAccessCategoriesWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    public void testDatabaseShouldEnforceCharacterLimit() throws Exception {
        // Login with the registered user
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", "johndoe@example.com");
        userMap.put("password", "password");

        ResultActions resultActions = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Set class attribute for further requests
        token = objectMapper.readTree(resultActions.andReturn().getResponse().getContentAsString()).get("token").asText();

        // Add a new category
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("title", "FoodExceedsLimitOfCharactersOrAtLeastItShould");
        categoryMap.put("description", "Expenses for food");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryMap)))
                .andExpect(status().isBadRequest());
    }
}

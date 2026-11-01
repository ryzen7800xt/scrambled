package com.scrambled.recipeapp;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrambled.recipeapp.model.User;
import com.scrambled.recipeapp.repository.UserRepository;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AccountRecipeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("null")
@Test
    void recipeCreationRequiresAnAccount() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test recipe","realRecipe":"Mix and bake."}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @SuppressWarnings("null")
@Test
    void registeredUserCanCreateRecipeAndReadPrivateInbox() throws Exception {
        String username = "tester-" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.com";
        String password = "correct horse battery";
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email, username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        JsonNode registeredUser = objectMapper.readTree(registration.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertFalse(registeredUser.has("passwordHash"));
        User storedUser = userRepository.findByUsername(username).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(email, storedUser.getEmail());
        org.junit.jupiter.api.Assertions.assertNotEquals(password, storedUser.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(storedUser.getPasswordHash().startsWith("$2a$"));

        String cookieValue = registration.getResponse().getHeader("Set-Cookie")
                .split(";", 2)[0].split("=", 2)[1];
        Cookie cookie = new Cookie("SCRAMBLED_SESSION", cookieValue);
        mockMvc.perform(post("/api/recipes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"My test recipe","realRecipe":"Mix and bake."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.username").value(username));

        mockMvc.perform(get("/api/inbox").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].message").value(containsString("My test recipe")));

        mockMvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/recipes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"After logout","realRecipe":"Mix and bake."}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email.toUpperCase(), null, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @SuppressWarnings("null")
    @Test
    void registrationRejectsMalformedAndDuplicateEmails() throws Exception {
        String username = "test-" + UUID.randomUUID().toString().substring(0, 8);
        String password = "correct horse battery";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials("not-an-email", username, password))))
                .andExpect(status().isBadRequest());

        String email = username + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email, username, password))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email.toUpperCase(), username + "x", password))))
                .andExpect(status().isConflict());
    }

    private record Credentials(String email, String username, String password) { }
}

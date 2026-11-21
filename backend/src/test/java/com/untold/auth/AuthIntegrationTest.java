package com.untold.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired private MockMvc mvc;

    @Test
    void registrationStoresPasswordHashAndSessionLoginProtectsMe() throws Exception {
        String email = "test-" + java.util.UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"a-long-test-password\"}"))
            .andExpect(status().isCreated());

        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"a-long-test-password\"}"))
            .andExpect(status().isOk())
            .andReturn();

        mvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void accountDetailsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsShortPasswords() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"short@example.com\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest());
    }
}

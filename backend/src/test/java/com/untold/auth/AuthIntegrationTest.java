package com.untold.auth;

import com.untold.writing.PrivateWriting;
import com.untold.writing.PrivateWritingRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.nio.charset.StandardCharsets;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private PrivateWritingRepository writings;

    private MockHttpSession createAccountAndLogin(String email) throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"a-long-test-password\"}"))
            .andExpect(status().isCreated());
        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"a-long-test-password\"}"))
            .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    @Test
    void registrationAndSessionLoginWork() throws Exception {
        String email = "test-" + java.util.UUID.randomUUID() + "@example.com";
        MockHttpSession session = createAccountAndLogin(email);
        mvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
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

    @Test
    void privateWritingIsEncryptedAndOwnedByCreatingAccount() throws Exception {
        String ownerEmail = "owner-" + java.util.UUID.randomUUID() + "@example.com";
        String otherEmail = "other-" + java.util.UUID.randomUUID() + "@example.com";
        MockHttpSession owner = createAccountAndLogin(ownerEmail);
        MockHttpSession other = createAccountAndLogin(otherEmail);

        MvcResult created = mvc.perform(post("/api/private-writing/void").session(owner).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"A private note\",\"content\":\"This should only belong to its owner.\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("This should only belong to its owner."))
            .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        PrivateWriting stored = writings.findAll().stream().filter(item -> item.getId().toString().equals(id)).findFirst().orElseThrow();
        assertFalse(new String(stored.getEncryptedContent(), StandardCharsets.UTF_8).contains("This should only belong to its owner."));
        assertFalse(new String(stored.getEncryptedTitle(), StandardCharsets.UTF_8).contains("A private note"));
        mvc.perform(get("/api/private-writing/void/" + id).session(other))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/private-writing/void").session(other))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(put("/api/private-writing/void/" + id).session(other).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hijack\",\"content\":\"not yours\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/api/private-writing/void/" + id).session(other).with(csrf()))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/private-writing/void/" + id).session(owner))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("A private note"));
        mvc.perform(delete("/api/private-writing/void/" + id).session(owner).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/private-writing/void/" + id).session(owner))
            .andExpect(status().isNotFound());
    }
}

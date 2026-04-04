package ru.musintimur.eventmanager.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import ru.musintimur.eventmanager.AbstractIntegrationTest

@AutoConfigureMockMvc
class AuthControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET login returns login view`() {
        mockMvc
            .perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("auth/login"))
    }

    @Test
    fun `GET register returns register view`() {
        mockMvc
            .perform(get("/register"))
            .andExpect(status().isOk)
            .andExpect(view().name("auth/register"))
    }

    @Test
    fun `POST register with valid data redirects to login`() {
        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "newuser_${System.nanoTime()}")
                    .param("password", "password123")
                    .param("passwordConfirm", "password123")
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login?registered"))
    }

    @Test
    fun `POST register with mismatched passwords returns form with errors`() {
        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "anyuser")
                    .param("password", "password123")
                    .param("passwordConfirm", "different")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(view().name("auth/register"))
    }

    @Test
    fun `POST register with short password returns form with errors`() {
        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "anyuser")
                    .param("password", "123")
                    .param("passwordConfirm", "123")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(view().name("auth/register"))
    }

    @Test
    fun `GET profile without auth redirects to login`() {
        mockMvc
            .perform(get("/profile"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `GET admin without auth redirects to login`() {
        mockMvc
            .perform(get("/admin/users"))
            .andExpect(status().is3xxRedirection)
    }
}

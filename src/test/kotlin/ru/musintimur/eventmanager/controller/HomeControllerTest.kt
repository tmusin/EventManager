package ru.musintimur.eventmanager.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import ru.musintimur.eventmanager.AbstractIntegrationTest

@AutoConfigureMockMvc
class HomeControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET slash returns index view with events page`() {
        mockMvc
            .perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("eventsPage"))
            .andExpect(model().attributeExists("currentPage"))
    }

    @Test
    fun `GET slash with page param returns correct page`() {
        mockMvc
            .perform(get("/").param("page", "0"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `GET 403 returns access denied view`() {
        mockMvc
            .perform(get("/403"))
            .andExpect(status().isOk)
            .andExpect(view().name("error/403"))
    }
}

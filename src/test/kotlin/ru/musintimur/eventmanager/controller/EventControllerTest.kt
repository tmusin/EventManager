package ru.musintimur.eventmanager.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.EventRepository
import ru.musintimur.eventmanager.repository.UserRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureMockMvc
@Transactional
class EventControllerTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var organizer: User
    private lateinit var otherUser: User

    @BeforeEach
    fun setUp() {
        organizer =
            userRepository.save(
                User(username = "organizer_${System.nanoTime()}", password = "hashed"),
            )
        otherUser =
            userRepository.save(
                User(username = "other_${System.nanoTime()}", password = "hashed"),
            )
    }

    private fun saveEvent(
        status: EventStatus = EventStatus.PENDING,
        owner: User = organizer,
    ): Event =
        eventRepository.save(
            Event(
                title = "Test Event",
                description = "Some description",
                eventDate = LocalDateTime.now().plusDays(5),
                price = BigDecimal("200.00"),
                maxParticipants = 10,
                organizer = owner,
                status = status,
            ),
        )

    // ── GET /events/{id} ─────────────────────────────────────────────────────

    @Test
    fun `GET event page is accessible anonymously`() {
        val event = saveEvent(EventStatus.APPROVED)

        mockMvc
            .perform(get("/events/${event.id}"))
            .andExpect(status().isOk)
            .andExpect(view().name("event/view"))
            .andExpect(model().attributeExists("event"))
    }

    @Test
    fun `GET event page populates model for authenticated user`() {
        val event = saveEvent(EventStatus.APPROVED)

        mockMvc
            .perform(
                get("/events/${event.id}")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(organizer.username),
                    ),
            ).andExpect(status().isOk)
            .andExpect(model().attributeExists("currentUser", "isRegistered", "isOrganizer"))
    }

    // ── GET /events/new ───────────────────────────────────────────────────────

    @Test
    fun `GET new event form requires authentication`() {
        mockMvc
            .perform(get("/events/new"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithMockUser
    fun `GET new event form returns form view for authenticated user`() {
        mockMvc
            .perform(get("/events/new"))
            .andExpect(status().isOk)
            .andExpect(view().name("event/form"))
            .andExpect(model().attributeExists("form"))
    }

    // ── POST /events/new ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "organizer_test")
    fun `POST new event with valid data creates event and redirects`() {
        // Создаём пользователя, который будет организатором
        userRepository.save(User(username = "organizer_test", password = "hashed"))

        mockMvc
            .perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "New Event")
                    .param("description", "Event description")
                    .param("eventDate", LocalDateTime.now().plusDays(10).toString())
                    .param("price", "0.00")
                    .param("maxParticipants", "0")
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/events/*"))
    }

    @Test
    @WithMockUser
    fun `POST new event with blank title returns form with errors`() {
        mockMvc
            .perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "")
                    .param("description", "Some description")
                    .param("eventDate", LocalDateTime.now().plusDays(3).toString())
                    .param("price", "0.00")
                    .param("maxParticipants", "0")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(view().name("event/form"))
    }

    @Test
    @WithMockUser
    fun `POST new event with past date returns form with errors`() {
        mockMvc
            .perform(
                post("/events/new")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "Past Event")
                    .param("description", "Some description")
                    .param("eventDate", LocalDateTime.now().minusDays(1).toString())
                    .param("price", "0.00")
                    .param("maxParticipants", "0")
                    .with(csrf()),
            ).andExpect(status().isOk)
            .andExpect(view().name("event/form"))
    }

    // ── GET /events/{id}/edit ─────────────────────────────────────────────────

    @Test
    fun `GET edit event requires authentication`() {
        val event = saveEvent()

        mockMvc
            .perform(get("/events/${event.id}/edit"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `GET edit event by organizer returns form view`() {
        val event = saveEvent(status = EventStatus.PENDING, owner = organizer)

        mockMvc
            .perform(
                get("/events/${event.id}/edit")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(organizer.username),
                    ),
            ).andExpect(status().isOk)
            .andExpect(view().name("event/form"))
            .andExpect(model().attributeExists("form", "event"))
    }

    @Test
    fun `GET edit approved event redirects back to event page`() {
        val event = saveEvent(status = EventStatus.APPROVED, owner = organizer)

        mockMvc
            .perform(
                get("/events/${event.id}/edit")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(organizer.username),
                    ),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/events/*"))
    }

    @Test
    fun `GET edit event by non-organizer redirects to 403`() {
        val event = saveEvent(status = EventStatus.PENDING, owner = organizer)

        mockMvc
            .perform(
                get("/events/${event.id}/edit")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(otherUser.username),
                    ),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/403*"))
    }

    // ── POST /events/{id}/complete ────────────────────────────────────────────

    @Test
    fun `POST complete event by organizer changes status to COMPLETED`() {
        val event = saveEvent(status = EventStatus.APPROVED, owner = organizer)

        mockMvc
            .perform(
                post("/events/${event.id}/complete")
                    .with(csrf())
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(organizer.username),
                    ),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/events/*"))

        val updated = eventRepository.findById(event.id).get()
        assert(updated.status == EventStatus.COMPLETED)
    }

    @Test
    fun `POST complete event by non-organizer redirects to 403`() {
        val event = saveEvent(status = EventStatus.APPROVED, owner = organizer)

        mockMvc
            .perform(
                post("/events/${event.id}/complete")
                    .with(csrf())
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors
                            .user(otherUser.username),
                    ),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/403*"))
    }
}

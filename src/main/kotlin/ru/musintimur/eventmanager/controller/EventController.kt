package ru.musintimur.eventmanager.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.service.AclService
import ru.musintimur.eventmanager.service.CommentService
import ru.musintimur.eventmanager.service.EventService
import ru.musintimur.eventmanager.service.GalleryService
import ru.musintimur.eventmanager.service.RegistrationService
import ru.musintimur.eventmanager.service.UserService
import java.math.BigDecimal
import java.time.LocalDateTime

data class EventFormDto(
    @field:NotBlank(message = "Название не может быть пустым")
    @field:Size(max = 200, message = "Не более 200 символов")
    val title: String = "",
    @field:NotBlank(message = "Описание не может быть пустым")
    val description: String = "",
    @field:NotNull(message = "Укажите дату")
    @field:Future(message = "Дата должна быть в будущем")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val eventDate: LocalDateTime? = null,
    @field:DecimalMin(value = "0.0", message = "Стоимость не может быть отрицательной")
    val price: BigDecimal = BigDecimal.ZERO,
    @field:Min(value = 0, message = "Количество мест не может быть отрицательным")
    val maxParticipants: Int = 0,
)

@Controller
@RequestMapping("/events")
class EventController(
    private val eventService: EventService,
    private val userService: UserService,
    private val commentService: CommentService,
    private val registrationService: RegistrationService,
    private val galleryService: GalleryService,
    private val aclService: AclService,
) {
    /** Страница мероприятия (публичная) */
    @GetMapping("/{id}")
    fun eventPage(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserDetails?,
        model: Model,
    ): String {
        val event = eventService.getApprovedById(id)
        val comments = commentService.getByEvent(id)
        val photos = galleryService.getByEvent(id)
        val participants = registrationService.getByEvent(id)

        model.addAttribute("event", event)
        model.addAttribute("comments", comments)
        model.addAttribute("photos", photos)
        model.addAttribute("participants", participants)

        if (principal != null) {
            val user = userService.findByUsername(principal.username)
            val isRegistered = registrationService.isRegistered(id, user.id)
            val isOrganizer = event.organizer.id == user.id
            model.addAttribute("currentUser", user)
            model.addAttribute("isRegistered", isRegistered)
            model.addAttribute("isOrganizer", isOrganizer)
            model.addAttribute("hasAvailableSlots", eventService.hasAvailableSlots(event))
        }

        return "event/view"
    }

    /** Форма создания мероприятия */
    @GetMapping("/new")
    fun newEventPage(model: Model): String {
        model.addAttribute("form", EventFormDto())
        return "event/form"
    }

    /** Сохранение нового мероприятия */
    @PostMapping("/new")
    fun createEvent(
        @Valid @ModelAttribute("form") form: EventFormDto,
        bindingResult: BindingResult,
        @RequestParam("coverImage") coverImage: MultipartFile?,
        @AuthenticationPrincipal principal: UserDetails,
    ): String {
        if (bindingResult.hasErrors()) return "event/form"

        val organizer = userService.findByUsername(principal.username)
        val event =
            eventService.create(
                title = form.title,
                description = form.description,
                eventDate = form.eventDate!!,
                price = form.price,
                maxParticipants = form.maxParticipants,
                organizer = organizer,
            )

        if (coverImage != null && !coverImage.isEmpty) {
            val path = galleryService.saveCoverImage(event, coverImage)
            eventService.updateCoverImage(event, path)
        }

        // Выдаём ACL-права организатору и менеджерам
        aclService.grantOwnerPermissions(event, principal.username)
        aclService.grantManagerWritePermission(event)

        return "redirect:/events/${event.id}"
    }

    /** Форма редактирования мероприятия */
    @GetMapping("/{id}/edit")
    fun editEventPage(
        @PathVariable id: Long,
        model: Model,
    ): String {
        val event = eventService.findById(id)
        val form =
            EventFormDto(
                title = event.title,
                description = event.description,
                eventDate = event.eventDate,
                price = event.price,
                maxParticipants = event.maxParticipants,
            )
        model.addAttribute("form", form)
        model.addAttribute("event", event)
        return "event/form"
    }

    /** Сохранение изменений мероприятия */
    @PostMapping("/{id}/edit")
    fun updateEvent(
        @PathVariable id: Long,
        @Valid @ModelAttribute("form") form: EventFormDto,
        bindingResult: BindingResult,
        @RequestParam("coverImage") coverImage: MultipartFile?,
        model: Model,
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("event", eventService.findById(id))
            return "event/form"
        }
        val event = eventService.findById(id)
        eventService.update(
            event = event,
            title = form.title,
            description = form.description,
            eventDate = form.eventDate!!,
            price = form.price,
            maxParticipants = form.maxParticipants,
        )
        if (coverImage != null && !coverImage.isEmpty) {
            val path = galleryService.saveCoverImage(event, coverImage)
            eventService.updateCoverImage(event, path)
        }
        return "redirect:/events/$id"
    }

    /** Организатор отмечает мероприятие как состоявшееся */
    @PostMapping("/{id}/complete")
    fun completeEvent(
        @PathVariable id: Long,
    ): String {
        val event = eventService.findById(id)
        eventService.markAsCompleted(event)
        return "redirect:/events/$id"
    }
}

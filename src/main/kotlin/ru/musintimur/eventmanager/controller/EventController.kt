package ru.musintimur.eventmanager.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import ru.musintimur.eventmanager.domain.EventStatus
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
    private val log: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    /** Страница мероприятия (публичная) */
    @GetMapping("/{id}")
    fun eventPage(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserDetails?,
        model: Model,
    ): String {
        val event = eventService.findById(id)
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

        // Шаг 1: сохраняем обложку на диск ДО создания записи в БД.
        // Если БД недоступна — файл не должен оставаться на диске.
        // Для этого нам нужен временный путь, который мы передадим в create().
        // Но id мероприятия ещё неизвестен, поэтому сохраняем во временную папку.
        val tempCoverPath: String? =
            if (coverImage != null && !coverImage.isEmpty) {
                galleryService.saveTempCoverImage(coverImage)
            } else {
                null
            }

        return try {
            // Шаг 2: создаём запись мероприятия в БД
            val event =
                eventService.create(
                    title = form.title,
                    description = form.description,
                    eventDate = form.eventDate!!,
                    price = form.price,
                    maxParticipants = form.maxParticipants,
                    organizer = organizer,
                    tempCoverPath = tempCoverPath,
                )

            // Шаг 3: выдаём ACL-права
            try {
                aclService.grantOwnerPermissions(event, principal.username)
                aclService.grantManagerWritePermission(event)
            } catch (aclEx: Exception) {
                // Мероприятие создано, но ACL не выдан.
                // Логируем — администратор сможет восстановить права вручную.
                // Файл и запись НЕ откатываем: мероприятие существует.
                log.error(
                    "Не удалось выдать ACL для мероприятия #${event.id}. Требуется ручное восстановление прав.",
                    aclEx,
                )
            }

            "redirect:/events/${event.id}"
        } catch (ex: Exception) {
            // Запись в БД не прошла — удаляем временный файл обложки
            if (tempCoverPath != null) {
                log.error(
                    "Ошибка создания мероприятия в БД, удаляем временный файл обложки: $tempCoverPath",
                    ex,
                )
                galleryService.deleteFileQuietly(tempCoverPath)
            }
            throw ex
        }
    }

    /** Форма редактирования мероприятия */
    @GetMapping("/{id}/edit")
    fun editEventPage(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserDetails,
        model: Model,
    ): String {
        val event = eventService.findById(id)

        val currentUser = userService.findByUsername(principal.username)
        val isOrganizer = event.organizer.id == currentUser.id
        val isAdmin = principal.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isOrganizer && !isAdmin) return "redirect:/403"

        // Блокируем редактирование опубликованных и завершённых мероприятий
        if (event.status == EventStatus.APPROVED || event.status == EventStatus.COMPLETED) {
            return "redirect:/events/$id"
        }

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
        @AuthenticationPrincipal principal: UserDetails,
        model: Model,
    ): String {
        val event = eventService.findById(id)

        val currentUser = userService.findByUsername(principal.username)
        val isOrganizer = event.organizer.id == currentUser.id
        val isAdmin = principal.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isOrganizer && !isAdmin) return "redirect:/403"

        if (event.status == EventStatus.APPROVED || event.status == EventStatus.COMPLETED) {
            return "redirect:/events/$id"
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("event", event)
            return "event/form"
        }

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
        @AuthenticationPrincipal principal: UserDetails,
    ): String {
        val event = eventService.findById(id)
        val currentUser = userService.findByUsername(principal.username)
        val isOrganizer = event.organizer.id == currentUser.id
        val isAdmin = principal.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isOrganizer && !isAdmin) {
            return "redirect:/403"
        }
        eventService.markAsCompleted(event)
        return "redirect:/events/$id"
    }
}

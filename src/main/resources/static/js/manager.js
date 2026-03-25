/**
 * Логика страницы менеджера:
 * одобрение / отклонение мероприятий без перезагрузки страницы.
 */

async function approveEvent(btn) {
    const eventId = btn.dataset.eventId;
    if (!confirm('Одобрить мероприятие?')) return;
    try {
        await apiCall(`/api/manager/events/${eventId}/approve`, 'POST');
        removeEventRow(eventId, 'Мероприятие одобрено и опубликовано', 'success');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function rejectEvent(btn) {
    const eventId = btn.dataset.eventId;
    if (!confirm('Отклонить мероприятие?')) return;
    try {
        await apiCall(`/api/manager/events/${eventId}/reject`, 'POST');
        removeEventRow(eventId, 'Мероприятие отклонено', 'warning');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

/**
 * Плавно убирает карточку мероприятия из списка после действия.
 * @param {string|number} eventId
 * @param {string} message
 * @param {'success'|'warning'|'danger'|'info'} toastType
 */
function removeEventRow(eventId, message, toastType) {
    showToast(message, toastType);
    const row = document.getElementById(`event-row-${eventId}`);
    if (!row) return;

    row.style.transition = 'opacity 0.4s ease';
    row.style.opacity = '0';
    setTimeout(() => {
        row.remove();
        checkEmptyList();
    }, 400);
}

/**
 * Если список стал пустым — показывает информационный блок.
 */
function checkEmptyList() {
    const rows = document.querySelectorAll('[id^="event-row-"]');
    if (rows.length === 0) {
        const container = document.querySelector('.row.g-4');
        if (container) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle me-2"></i>
                        Нет мероприятий, ожидающих проверки.
                    </div>
                </div>`;
        }
    }
}
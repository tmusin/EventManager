/**
 * Логика страницы мероприятия:
 *  - запись / отмена записи
 *  - добавление / удаление комментариев
 *  - загрузка / удаление фотографий
 *  - управление участниками (организатор)
 */

// ── Запись на мероприятие ────────────────────────────────────────────────────

async function registerForEvent(btn) {
    const eventId = btn.dataset.eventId;
    try {
        const data = await apiCall(`/api/registrations/events/${eventId}`, 'POST');
        showToast(data.message, 'success');
        btn.disabled = true;
        btn.innerHTML = '<i class="bi bi-calendar-check me-1"></i>Вы записаны';
        btn.classList.replace('btn-primary', 'btn-success');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function cancelRegistration(btn) {
    if (!confirm('Отменить запись на мероприятие?')) return;
    const eventId = btn.dataset.eventId;
    try {
        const data = await apiCall(`/api/registrations/events/${eventId}`, 'DELETE');
        showToast(data.message, 'info');
        btn.disabled = true;
        btn.innerHTML = '<i class="bi bi-calendar-x me-1"></i>Запись отменена';
        btn.classList.replace('btn-outline-danger', 'btn-secondary');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ── Управление участниками (организатор) ─────────────────────────────────────

async function markPaid(btn) {
    const regId = btn.dataset.regId;
    try {
        await apiCall(`/api/registrations/${regId}/paid`, 'POST');
        showToast('Участник отмечен как оплативший', 'success');
        updateRegistrationBadge(regId, 'PAID', 'bg-success');
        btn.closest('.d-flex.gap-1')?.remove();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function rejectParticipant(btn) {
    if (!confirm('Отклонить участника?')) return;
    const regId = btn.dataset.regId;
    try {
        await apiCall(`/api/registrations/${regId}/reject`, 'POST');
        showToast('Участник отклонён', 'warning');
        updateRegistrationBadge(regId, 'REJECTED', 'bg-danger');
        btn.closest('.d-flex.gap-1')?.remove();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

function updateRegistrationBadge(regId, text, bgClass) {
    const row = document.getElementById(`reg-${regId}`);
    if (!row) return;
    const badge = row.querySelector('.badge');
    if (!badge) return;
    badge.className = `badge ms-1 ${bgClass}`;
    badge.textContent = text;
}

// ── Комментарии ──────────────────────────────────────────────────────────────

async function submitComment(btn) {
    const eventId = btn.dataset.eventId;
    const textarea = document.getElementById('commentContent');
    const content = textarea.value.trim();

    if (!content) {
        showToast('Комментарий не может быть пустым', 'warning');
        return;
    }

    try {
        const comment = await apiCall(`/api/comments/events/${eventId}`, 'POST', { content });
        textarea.value = '';

        const noComments = document.getElementById('noComments');
        if (noComments) noComments.remove();

        document.getElementById('commentsList').insertAdjacentHTML(
            'beforeend',
            buildCommentHtml(comment),
        );
        showToast('Комментарий добавлен', 'success');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function deleteComment(btn) {
    if (!confirm('Удалить комментарий?')) return;
    const commentId = btn.dataset.commentId;
    try {
        await apiCall(`/api/comments/${commentId}`, 'DELETE');
        document.getElementById(`comment-${commentId}`)?.remove();
        showToast('Комментарий удалён', 'info');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

/**
 * Строит HTML-блок нового комментария из ответа API.
 * @param {object} c — объект комментария
 * @returns {string}
 */
function buildCommentHtml(c) {
    const organizerBadge = c.isOrganizer
        ? '<span class="badge bg-primary ms-1">Организатор</span>'
        : '';
    const participantBadge = c.isParticipant && !c.isOrganizer
        ? '<span class="badge bg-success ms-1">Участник</span>'
        : '';

    return `
        <div id="comment-${c.id}" class="d-flex gap-3 mb-3 pb-3 border-bottom">
            <div class="flex-grow-1">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${escapeHtml(c.author)}</strong>
                        ${organizerBadge}${participantBadge}
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <small class="text-muted">${escapeHtml(c.createdAt)}</small>
                        <button class="btn btn-outline-danger btn-sm py-0"
                                data-comment-id="${c.id}"
                                onclick="deleteComment(this)">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
                <p class="mb-0 mt-1">${escapeHtml(c.content)}</p>
            </div>
        </div>`;
}

// ── Галерея ──────────────────────────────────────────────────────────────────

async function uploadPhoto(btn) {
    const eventId = btn.dataset.eventId;
    const fileInput = document.getElementById('photoFile');
    const captionInput = document.getElementById('photoCaption');

    if (!fileInput.files.length) {
        showToast('Выберите файл для загрузки', 'warning');
        return;
    }

    const csrf = getCsrf();
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    if (captionInput.value.trim()) {
        formData.append('caption', captionInput.value.trim());
    }

    try {
        const response = await fetch(`/api/gallery/events/${eventId}`, {
            method: 'POST',
            headers: { [csrf.header]: csrf.token },
            body: formData,
        });
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const photo = await response.json();

        fileInput.value = '';
        captionInput.value = '';

        document.getElementById('photoGallery').insertAdjacentHTML(
            'beforeend',
            buildPhotoHtml(photo),
        );
        showToast('Фото загружено', 'success');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function deletePhoto(btn) {
    if (!confirm('Удалить фотографию?')) return;
    const photoId = btn.dataset.photoId;
    try {
        await apiCall(`/api/gallery/${photoId}`, 'DELETE');
        document.getElementById(`photo-${photoId}`)?.remove();
        showToast('Фото удалено', 'info');
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

/**
 * Строит HTML-карточку нового фото из ответа API.
 * @param {object} p — объект фото
 * @returns {string}
 */
function buildPhotoHtml(p) {
    const caption = p.caption
        ? `<p class="small text-muted mt-1">${escapeHtml(p.caption)}</p>`
        : '';
    return `
        <div id="photo-${p.id}" class="col">
            <div class="position-relative">
                <img src="/uploads/${escapeHtml(p.filePath)}"
                     class="img-fluid rounded" alt="Фото"/>
                ${caption}
                <button class="btn btn-danger btn-sm position-absolute top-0 end-0 m-1"
                        data-photo-id="${p.id}"
                        onclick="deletePhoto(this)">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </div>`;
}

// ── Утилиты ──────────────────────────────────────────────────────────────────

/**
 * Экранирует HTML-спецсимволы для безопасной вставки в DOM.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
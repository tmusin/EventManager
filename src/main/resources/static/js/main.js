/**
 * Общие утилиты для всего приложения.
 */

/**
 * Возвращает объект с CSRF-токеном и именем заголовка.
 * Токен хранится в скрытом div#csrf, добавляемом в шаблоны.
 * @returns {{ token: string, header: string }}
 */
function getCsrf() {
    const el = document.getElementById('csrf');
    return {
        token: el.dataset.token,
        header: el.dataset.header,
    };
}

/**
 * Универсальная обёртка над fetch для JSON API-вызовов.
 * Автоматически добавляет CSRF-заголовок.
 *
 * @param {string} url
 * @param {string} method  — HTTP-метод
 * @param {object|null} body — тело запроса (будет сериализовано в JSON)
 * @returns {Promise<object>}
 */
async function apiCall(url, method = 'POST', body = null) {
    const csrf = getCsrf();
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            [csrf.header]: csrf.token,
        },
    };
    if (body !== null) {
        options.body = JSON.stringify(body);
    }
    const response = await fetch(url, options);
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Ошибка сервера: ${response.status}`);
    }
    return response.json();
}

/**
 * Показывает всплывающее уведомление Bootstrap toast.
 * Требует наличия элемента #toast-container в DOM.
 *
 * @param {string} message
 * @param {'success'|'danger'|'warning'|'info'} type
 */
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '1100';
        document.body.appendChild(container);
    }

    const id = `toast-${Date.now()}`;
    const iconMap = {
        success: 'bi-check-circle-fill',
        danger: 'bi-exclamation-triangle-fill',
        warning: 'bi-exclamation-circle-fill',
        info: 'bi-info-circle-fill',
    };
    const icon = iconMap[type] ?? 'bi-bell-fill';

    container.insertAdjacentHTML('beforeend', `
        <div id="${id}" class="toast align-items-center text-bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi ${icon} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto"
                        data-bs-dismiss="toast"></button>
            </div>
        </div>
    `);

    const toastEl = document.getElementById(id);
    const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}
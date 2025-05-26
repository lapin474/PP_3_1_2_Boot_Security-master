document.addEventListener("DOMContentLoaded", () => {
    const hasAdminRole = window.hasAdminRole === true;

    if (!hasAdminRole) {
        // Скрыть Admin-вкладку полностью
        const adminNavItem = document.getElementById("adminBtn")?.parentElement;
        if (adminNavItem) {
            adminNavItem.style.display = "none";
        }
    }

    if (hasAdminRole) {
        setActiveTab('admin');
        loadAdminContent();
    } else {
        setActiveTab('user');
        loadPage('/user-page');
    }
});

async function loadPage(url, updateHistory = true) {
    try {
        const response = await fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (!response.ok) {
            throw new Error(`Ошибка загрузки: ${response.status}`);
        }

        const html = await response.text();
        const contentContainer = document.getElementById("dynamic-content");

        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;

        const adminContent = tempDiv.querySelector('#adminContent');
        if (adminContent) {
            contentContainer.innerHTML = '';
            contentContainer.appendChild(adminContent);
        } else {
            contentContainer.innerHTML = html;
        }

        if (updateHistory) {
            history.pushState({ url }, "", url);
        }

        attachEventListeners(); // важен до fetchUsers / fetchUserInfo
        console.log("Загруженный HTML с", url, ":\n", html);

        if (url === '/admin-content') {
            try {
                await waitForElement('#usersTableBody');
                await fetchUsers();

                await waitForElement('#roleIds');
                await loadRoles();

                // 👇 ВАЖНО: навесить обработчики после генерации DOM
                attachEventListeners();

            } catch (e) {
                console.warn('Ошибка загрузки админ-данных:', e);
            }
    } else if (url === '/user-page') {
            try {
                await waitForElement('#user-info-container');
                await fetchUserInfo();
            } catch (e) {
                console.warn('Ошибка загрузки пользовательских данных:', e);
            }
        }

    } catch (error) {
        console.error("Ошибка при загрузке страницы:", error);
    }
}

async function loadAdminContent(updateHistory = true) {
    await loadPage('/admin-content', updateHistory);
}
async function fetchUsers() {
    try {
        await waitForElement('#usersTableBody');
        const tbody = document.getElementById("usersTableBody");
        const modalsContainer = document.getElementById("modalsContainer");

        if (!tbody || !modalsContainer) {
            console.warn("Элементы таблицы пользователей ещё не загружены.");
            return;
        }

        console.log("Загружаем пользователей...");
        const [usersRes, rolesRes] = await Promise.all([
            fetch('/api/users'),
            fetch('/api/roles')
        ]);

        if (!usersRes.ok) throw new Error('Ошибка загрузки пользователей');
        if (!rolesRes.ok) throw new Error('Ошибка загрузки ролей');

        const users = await usersRes.json();
        const allRoles = await rolesRes.json();

        tbody.innerHTML = '';
        modalsContainer.innerHTML = '';

        users.forEach(user => {
            const rolesText = (user.roleNames || []).map(name => name.replace('ROLE_', '')).join(', ');

            const row = `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.email}</td>
                    <td>${user.firstName}</td>
                    <td>${user.lastName}</td>
                    <td>${rolesText}</td>
                    <td>
                        <button class="btn btn-sm btn-info me-1" data-bs-toggle="modal" data-bs-target="#editModal-${user.id}">Edit</button>
                        <button class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal-${user.id}">Delete</button>
                    </td>
                </tr>
            `;
            tbody.insertAdjacentHTML('beforeend', row);
            modalsContainer.insertAdjacentHTML('beforeend', generateEditModal(user, allRoles));
            modalsContainer.insertAdjacentHTML('beforeend', generateDeleteModal(user));
        });

    } catch (error) {
        console.error('Ошибка при загрузке пользователей:', error);
    }
}

async function fetchUserInfo() {
    try {
        const userIdEl = document.getElementById('user-id');
        const firstNameEl = document.getElementById('user-firstname');
        const lastNameEl = document.getElementById('user-lastname');
        const emailEl = document.getElementById('user-email');
        const rolesEl = document.getElementById('user-roles');

        if (!userIdEl || !firstNameEl || !lastNameEl || !emailEl || !rolesEl) {
            console.warn('Один из элементов для отображения пользователя не найден');
            return;
        }

        const response = await fetch('/api/users/user');
        if (!response.ok) {
            throw new Error('Ошибка загрузки информации о пользователе');
        }

        const user = await response.json();

        // Формируем строку ролей без префикса ROLE_
        const roles = user.roleNames ? user.roleNames.map(r => r.replace('ROLE_', '')).join(', ') : '–';

        userIdEl.textContent = user.id ?? '–';
        firstNameEl.textContent = user.firstName ?? '–';
        lastNameEl.textContent = user.lastName ?? '–';
        emailEl.textContent = user.email ?? '–';
        rolesEl.textContent = roles;

    } catch (error) {
        console.error('Ошибка при загрузке данных пользователя:', error);
    }
}

// Запускаем после загрузки страницы
document.addEventListener('DOMContentLoaded', fetchUserInfo);

async function waitForElement(selector, timeout = 10000) {
    const start = Date.now();
    while ((Date.now() - start) < timeout) {
        const el = document.querySelector(selector);
        if (el) return el;
        await new Promise(res => setTimeout(res, 100));
    }
    console.error(`⚠️ Не удалось найти ${selector}. HTML:`, document.body.innerHTML);
    throw new Error(`Элемент ${selector} не найден за ${timeout} мс`);
}

function setActiveTab(role) {
    const adminBtn = document.getElementById('adminBtn');
    const userBtn = document.getElementById('userBtn');

    if (role === 'admin') {
        adminBtn.classList.add('bg-primary', 'text-white');
        userBtn.classList.remove('bg-primary', 'text-white');
    } else if (role === 'user') {
        userBtn.classList.add('bg-primary', 'text-white');
        adminBtn.classList.remove('bg-primary', 'text-white');
    }
}
async function attachEventListeners() {
    const newUserForm = document.querySelector('#newUser form');
    if (newUserForm) {
        newUserForm.addEventListener('submit', async e => {
            e.preventDefault();
            const formData = new FormData(newUserForm);
            const user = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                email: formData.get('email'),
                password: formData.get('password'),
                roleIds: Array.from(formData.getAll('roleIds')).map(Number)
            };
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('[name="_csrf"]').value
                },
                body: JSON.stringify(user)
            });
            if (response.ok) {
                alert('Пользователь создан');
                newUserForm.reset();
                loadAdminContent();
            } else {
                alert('Ошибка при создании пользователя');
            }
        });
    }

    // Навесить обработчики submit на формы редактирования пользователей
    document.querySelectorAll('form[id^="editUserForm-"]').forEach(form => {
        const userId = form.id.split('-')[1]; // получить id пользователя из id формы
        form.addEventListener('submit', event => submitEdit(event, userId));
    });

    document.addEventListener('click', event => {
        if (event.target.matches('button[data-action="delete"]')) {
            const userId = event.target.dataset.userid;
            deleteUser(userId);
        }
    });

    const createUserForm = document.getElementById("createUserForm");
    if (createUserForm) {
        createUserForm.addEventListener("submit", async e => {
            e.preventDefault();
            const form = e.target;
            const formData = new FormData(form);

            const user = {
                firstName: formData.get("firstName"),
                lastName: formData.get("lastName"),
                email: formData.get("email"),
                password: formData.get("password"),
                roleIds: Array.from(form.querySelector('[name="roleIds"]').selectedOptions).map(opt => +opt.value)
            };

            try {
                const res = await fetch("/api/users", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": document.querySelector('[name="_csrf"]').value
                    },
                    body: JSON.stringify(user)
                });
                if (!res.ok) throw new Error("Ошибка создания пользователя");

                await loadAdminContent();
                form.reset();
            } catch (err) {
                console.error("Ошибка при создании:", err);
            }
        });
    }
}
async function deleteUser(id) {
    const response = await fetch(`/api/users/${id}`, {
        method: 'DELETE',
        headers: { 'X-CSRF-TOKEN': document.querySelector('[name="_csrf"]').value },
    });
    if (response.ok) {
        alert('Пользователь удалён');
        loadAdminContent();
    } else {
        alert('Ошибка при удалении пользователя');
    }
}

function submitEdit(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const user = {
        id: id,
        firstName: formData.get("firstName"),
        lastName: formData.get("lastName"),
        email: formData.get("email"),
        password: formData.get("password") || null,
        roleIds: Array.from(form.querySelector('[name="roleIds"]').selectedOptions).map(opt => +opt.value)
    };

    fetch(`/api/users/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('[name="_csrf"]').value
        },
        body: JSON.stringify(user)
    })
        .then(res => {
            if (res.ok) {
                bootstrap.Modal.getInstance(document.getElementById(`editModal-${id}`)).hide();
                return fetchUsers().then(() => attachEventListeners());
            } else {
                alert("Ошибка при обновлении пользователя");
            }
        })
        .catch(err => console.error("Ошибка обновления:", err));
}


function submitDelete(event, id) {
    event.preventDefault();
    fetch(`/api/users/${id}`, {
        method: 'DELETE',
        headers: { 'X-CSRF-TOKEN': document.querySelector('[name="_csrf"]').value }
    })
        .then(res => {
            if (res.ok) {
                bootstrap.Modal.getInstance(document.getElementById(`deleteModal-${id}`)).hide();
                fetchUsers();
            } else {
                alert("Ошибка при удалении пользователя");
            }
        })
        .catch(err => console.error("Ошибка удаления:", err));
}
function generateEditModal(user, allRoles = []) {
    const userRoleIds = (user.roles || []).map(r => r.id);

    const rolesOptions = allRoles.map(role => {
        const selected = userRoleIds.includes(role.id) ? 'selected' : '';
        return `<option value="${role.id}" ${selected}>${role.name.replace('ROLE_', '')}</option>`;
    }).join('');

    return `
        <div class="modal fade" id="editModal-${user.id}" tabindex="-1" aria-labelledby="editModalLabel-${user.id}" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form id="editUserForm-${user.id}">
                        <div class="modal-header">
                            <h5 class="modal-title" id="editModalLabel-${user.id}">Edit User #${user.id}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label for="email-${user.id}" class="form-label">Email</label>
                                <input type="email" class="form-control" name="email" value="${user.email}" required>
                            </div>
                            <div class="mb-3">
                                <label for="firstName-${user.id}" class="form-label">First Name</label>
                                <input type="text" class="form-control" name="firstName" value="${user.firstName}" required>
                            </div>
                            <div class="mb-3">
                                <label for="lastName-${user.id}" class="form-label">Last Name</label>
                                <input type="text" class="form-control" name="lastName" value="${user.lastName}" required>
                            </div>
                            <div class="mb-3">
                              <label for="roles-${user.id}" class="form-label">Roles</label>
                              <select multiple class="form-select" id="roles-${user.id}" name="roleIds">
                                ${rolesOptions}
                              </select>
                              <small class="form-text text-muted">Select one or more roles</small>
                            </div>

                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save changes</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>`;
}


function generateDeleteModal(user) {
    return `
        <div class="modal fade" id="deleteModal-${user.id}" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form onsubmit="submitDelete(event, ${user.id})">
                        <div class="modal-header">
                            <h5 class="modal-title">Delete user</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p>Are you sure you want to delete user <strong>${user.email}</strong>?</p>
                        </div>
                        <div class="modal-footer">
                            <button type="submit" class="btn btn-danger">Delete</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>`;
}
async function loadRoles() {
    try {
        const response = await fetch('/api/roles');
        if (!response.ok) throw new Error("Не удалось загрузить роли");

        const roles = await response.json();
        const select = document.getElementById('roleIds');
        if (!select) return;

        select.innerHTML = '';
        roles.forEach(role => {
            const option = document.createElement('option');
            option.value = role.id;
            option.textContent = role.name.replace('ROLE_', '');
            select.appendChild(option);
        });
    } catch (err) {
        console.error("Ошибка при загрузке ролей:", err);
    }
}

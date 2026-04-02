const state = {
    auth: loadAuth(),
    expenses: [],
    page: 0,
    size: 10,
    totalPages: 0,
    filters: {
        category: "",
        startDate: "",
        endDate: "",
        sortBy: "date",
        sortDir: "desc"
    }
};

const elements = {
    authSection: document.getElementById("auth-section"),
    appSection: document.getElementById("app-section"),
    loginTab: document.getElementById("login-tab"),
    registerTab: document.getElementById("register-tab"),
    loginForm: document.getElementById("login-form"),
    registerForm: document.getElementById("register-form"),
    expenseForm: document.getElementById("expense-form"),
    filtersForm: document.getElementById("filters-form"),
    summaryForm: document.getElementById("summary-form"),
    expenseTableBody: document.getElementById("expense-table-body"),
    usersTableBody: document.getElementById("users-table-body"),
    adminPanel: document.getElementById("admin-panel"),
    welcomeTitle: document.getElementById("welcome-title"),
    welcomeSubtitle: document.getElementById("welcome-subtitle"),
    metricTotal: document.getElementById("metric-total"),
    metricCount: document.getElementById("metric-count"),
    metricRole: document.getElementById("metric-role"),
    summaryMonthLabel: document.getElementById("summary-month-label"),
    summaryTotal: document.getElementById("summary-total"),
    summaryBreakdown: document.getElementById("summary-breakdown"),
    logoutBtn: document.getElementById("logout-btn"),
    docsBtn: document.getElementById("docs-btn"),
    prevPage: document.getElementById("prev-page"),
    nextPage: document.getElementById("next-page"),
    pageIndicator: document.getElementById("page-indicator"),
    toast: document.getElementById("toast"),
    resetExpenseForm: document.getElementById("reset-expense-form"),
    refreshUsersBtn: document.getElementById("refresh-users-btn"),
    formTitle: document.getElementById("form-title")
};

bootstrap();

function bootstrap() {
    bindEvents();
    hydrateDefaults();
    updateAuthView();
}

function bindEvents() {
    elements.loginTab.addEventListener("click", () => switchAuthTab("login"));
    elements.registerTab.addEventListener("click", () => switchAuthTab("register"));
    elements.loginForm.addEventListener("submit", onLogin);
    elements.registerForm.addEventListener("submit", onRegister);
    elements.expenseForm.addEventListener("submit", onSaveExpense);
    elements.filtersForm.addEventListener("submit", onApplyFilters);
    elements.summaryForm.addEventListener("submit", onLoadSummary);
    elements.logoutBtn.addEventListener("click", onLogout);
    elements.docsBtn.addEventListener("click", () => window.open("/swagger-ui.html", "_blank"));
    elements.prevPage.addEventListener("click", () => changePage(-1));
    elements.nextPage.addEventListener("click", () => changePage(1));
    elements.resetExpenseForm.addEventListener("click", resetExpenseForm);
    elements.refreshUsersBtn.addEventListener("click", loadUsers);
}

function hydrateDefaults() {
    const today = new Date();
    document.getElementById("expense-date").value = toDateInput(today);
    document.getElementById("summary-year").value = today.getFullYear();
    document.getElementById("summary-month").value = today.getMonth() + 1;
}

function switchAuthTab(tab) {
    const loginMode = tab === "login";
    elements.loginTab.classList.toggle("tab--active", loginMode);
    elements.registerTab.classList.toggle("tab--active", !loginMode);
    elements.loginForm.classList.toggle("hidden", !loginMode);
    elements.registerForm.classList.toggle("hidden", loginMode);
}

async function onLogin(event) {
    event.preventDefault();
    try {
        const payload = {
            email: document.getElementById("login-email").value.trim(),
            password: document.getElementById("login-password").value
        };
        const data = await apiRequest("/auth/login", {
            method: "POST",
            body: JSON.stringify(payload)
        }, false);
        saveAuth(data);
        showToast("Login successful");
        updateAuthView();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function onRegister(event) {
    event.preventDefault();
    try {
        const payload = {
            name: document.getElementById("register-name").value.trim(),
            email: document.getElementById("register-email").value.trim(),
            password: document.getElementById("register-password").value,
            confirmPassword: document.getElementById("register-confirm-password").value
        };
        const data = await apiRequest("/auth/register", {
            method: "POST",
            body: JSON.stringify(payload)
        }, false);
        saveAuth(data);
        showToast("Registration successful");
        updateAuthView();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function onSaveExpense(event) {
    event.preventDefault();
    try {
        const expenseId = document.getElementById("expense-id").value;
        const payload = {
            title: document.getElementById("expense-title").value.trim(),
            description: document.getElementById("expense-description").value.trim(),
            amount: Number(document.getElementById("expense-amount").value),
            date: document.getElementById("expense-date").value,
            category: document.getElementById("expense-category").value
        };
        const method = expenseId ? "PUT" : "POST";
        const url = expenseId ? `/expenses/${expenseId}` : "/expenses";

        await apiRequest(url, { method, body: JSON.stringify(payload) });
        showToast(expenseId ? "Expense updated" : "Expense created");
        resetExpenseForm();
        await Promise.all([loadExpenses(), loadSummary(), maybeLoadUsers()]);
    } catch (error) {
        showToast(error.message, true);
    }
}

async function onApplyFilters(event) {
    event.preventDefault();
    state.page = 0;
    state.filters = {
        category: document.getElementById("filter-category").value,
        startDate: document.getElementById("filter-start-date").value,
        endDate: document.getElementById("filter-end-date").value,
        sortBy: document.getElementById("filter-sort-by").value,
        sortDir: document.getElementById("filter-sort-dir").value
    };
    await loadExpenses();
}

async function onLoadSummary(event) {
    event.preventDefault();
    await loadSummary();
}

async function onLogout() {
    try {
        if (state.auth?.refreshToken) {
            await apiRequest("/auth/logout", {
                method: "POST",
                body: JSON.stringify({ refreshToken: state.auth.refreshToken })
            });
        }
    } catch (error) {
        showToast(error.message, true);
    } finally {
        clearAuth();
        updateAuthView();
        showToast("Logged out");
    }
}

async function loadInitialAppData() {
    await Promise.all([loadExpenses(), loadSummary(), maybeLoadUsers()]);
}

async function loadExpenses() {
    const query = new URLSearchParams({
        page: String(state.page),
        size: String(state.size),
        sortBy: state.filters.sortBy,
        sortDir: state.filters.sortDir
    });

    if (state.filters.category) query.set("category", state.filters.category);
    if (state.filters.startDate) query.set("startDate", state.filters.startDate);
    if (state.filters.endDate) query.set("endDate", state.filters.endDate);

    const data = await apiRequest(`/expenses?${query.toString()}`);
    state.expenses = data.content || [];
    state.totalPages = data.totalPages || 0;
    renderExpenses();
    elements.metricCount.textContent = String(data.totalElements ?? 0);
    elements.pageIndicator.textContent = `Page ${data.page + 1} of ${Math.max(data.totalPages, 1)}`;
    elements.prevPage.disabled = data.first;
    elements.nextPage.disabled = data.last || data.totalPages === 0;
}

async function loadSummary() {
    const year = document.getElementById("summary-year").value;
    const month = document.getElementById("summary-month").value;
    const data = await apiRequest(`/expenses/summary/monthly?year=${year}&month=${month}`);

    elements.summaryMonthLabel.textContent = data.month;
    elements.summaryTotal.textContent = formatCurrency(data.totalAmount || 0);
    elements.metricTotal.textContent = formatCurrency(data.totalAmount || 0);
    elements.summaryBreakdown.innerHTML = "";

    const entries = Object.entries(data.categoryBreakdown || {});
    if (!entries.length) {
        elements.summaryBreakdown.innerHTML = "<li><span>No expenses</span><strong>INR 0</strong></li>";
        return;
    }

    for (const [category, amount] of entries) {
        const item = document.createElement("li");
        item.innerHTML = `<span>${titleCase(category)}</span><strong>${formatCurrency(amount)}</strong>`;
        elements.summaryBreakdown.appendChild(item);
    }
}

async function loadUsers() {
    if (state.auth?.role !== "ADMIN") return;

    const users = await apiRequest("/admin/users");
    elements.usersTableBody.innerHTML = "";

    for (const user of users) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${escapeHtml(user.name || "-")}</td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="tag">${escapeHtml(user.role)}</span></td>
        `;
        elements.usersTableBody.appendChild(row);
    }
}

async function maybeLoadUsers() {
    if (state.auth?.role === "ADMIN") {
        await loadUsers();
    }
}

function renderExpenses() {
    elements.expenseTableBody.innerHTML = "";

    if (!state.expenses.length) {
        const row = document.createElement("tr");
        row.innerHTML = `<td colspan="6" class="muted">No expenses found for the current filters.</td>`;
        elements.expenseTableBody.appendChild(row);
        return;
    }

    for (const expense of state.expenses) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${escapeHtml(expense.title)}</td>
            <td><span class="tag">${titleCase(expense.category)}</span></td>
            <td>${formatCurrency(expense.amount)}</td>
            <td>${formatDate(expense.date)}</td>
            <td>${escapeHtml(expense.description || "-")}</td>
            <td>
                <div class="action-row">
                    <button class="btn btn--secondary btn--small" type="button" data-action="edit">Edit</button>
                    <button class="btn btn--ghost btn--small" type="button" data-action="delete">Delete</button>
                </div>
            </td>
        `;
        row.querySelector('[data-action="edit"]').addEventListener("click", () => populateExpenseForm(expense));
        row.querySelector('[data-action="delete"]').addEventListener("click", () => deleteExpense(expense.id));
        elements.expenseTableBody.appendChild(row);
    }
}

async function deleteExpense(id) {
    const confirmed = window.confirm("Delete this expense?");
    if (!confirmed) return;

    try {
        await apiRequest(`/expenses/${id}`, { method: "DELETE" });
        showToast("Expense deleted");
        await Promise.all([loadExpenses(), loadSummary()]);
    } catch (error) {
        showToast(error.message, true);
    }
}

function populateExpenseForm(expense) {
    document.getElementById("expense-id").value = expense.id;
    document.getElementById("expense-title").value = expense.title;
    document.getElementById("expense-description").value = expense.description || "";
    document.getElementById("expense-amount").value = expense.amount;
    document.getElementById("expense-date").value = expense.date.slice(0, 10);
    document.getElementById("expense-category").value = expense.category;
    elements.formTitle.textContent = "Edit Expense";
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetExpenseForm() {
    elements.expenseForm.reset();
    document.getElementById("expense-id").value = "";
    document.getElementById("expense-date").value = toDateInput(new Date());
    document.getElementById("expense-category").value = "FOOD";
    elements.formTitle.textContent = "Add Expense";
}

async function changePage(delta) {
    const nextPage = state.page + delta;
    if (nextPage < 0 || (state.totalPages && nextPage >= state.totalPages)) return;
    state.page = nextPage;
    await loadExpenses();
}

function updateAuthView() {
    const isAuthenticated = Boolean(state.auth?.token);
    elements.authSection.classList.toggle("hidden", isAuthenticated);
    elements.appSection.classList.toggle("hidden", !isAuthenticated);

    if (!isAuthenticated) {
        state.expenses = [];
        renderExpenses();
        return;
    }

    elements.welcomeTitle.textContent = `Hello, ${state.auth.name || "there"}`;
    elements.welcomeSubtitle.textContent = `${state.auth.email} • Stay on top of your spending.`;
    elements.metricRole.textContent = state.auth.role || "USER";
    elements.adminPanel.classList.toggle("hidden", state.auth.role !== "ADMIN");
    loadInitialAppData().catch(error => showToast(error.message, true));
}

async function apiRequest(url, options = {}, authRequired = true) {
    const config = {
        method: options.method || "GET",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    };

    if (authRequired && state.auth?.token) {
        config.headers.Authorization = `Bearer ${state.auth.token}`;
    }
    if (options.body) {
        config.body = options.body;
    }

    let response = await fetch(url, config);

    if (response.status === 401 && authRequired && state.auth?.refreshToken) {
        const refreshed = await refreshAuthToken();
        if (refreshed) {
            config.headers.Authorization = `Bearer ${state.auth.token}`;
            response = await fetch(url, config);
        }
    }

    if (!response.ok) {
        let message = "Something went wrong";
        try {
            const errorData = await response.json();
            message = errorData.message || JSON.stringify(errorData.errors || errorData);
        } catch (error) {
            message = response.statusText || message;
        }
        throw new Error(message);
    }

    if (response.status === 204) return null;
    return response.json();
}

async function refreshAuthToken() {
    try {
        const data = await apiRequest("/auth/refresh", {
            method: "POST",
            body: JSON.stringify({ refreshToken: state.auth.refreshToken })
        }, false);
        saveAuth(data);
        return true;
    } catch (error) {
        clearAuth();
        updateAuthView();
        showToast("Session expired. Please log in again.", true);
        return false;
    }
}

function saveAuth(auth) {
    state.auth = auth;
    localStorage.setItem("expense-tracker-auth", JSON.stringify(auth));
}

function loadAuth() {
    const raw = localStorage.getItem("expense-tracker-auth");
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch (error) {
        return null;
    }
}

function clearAuth() {
    state.auth = null;
    localStorage.removeItem("expense-tracker-auth");
}

function showToast(message, isError = false) {
    elements.toast.textContent = message;
    elements.toast.classList.remove("hidden", "toast--error");
    if (isError) {
        elements.toast.classList.add("toast--error");
    }
    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(() => {
        elements.toast.classList.add("hidden");
    }, 3200);
}

function titleCase(value) {
    return String(value || "")
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\b\w/g, char => char.toUpperCase());
}

function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

function formatCurrency(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 2
    }).format(amount);
}

function toDateInput(date) {
    return date.toISOString().slice(0, 10);
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

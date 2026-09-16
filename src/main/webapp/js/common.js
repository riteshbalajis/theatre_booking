
let currentUser = null;

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

function qs(selector, root = document) { return root.querySelector(selector); }
function formatMoney(value) { return Number(value || 0).toFixed(2); }
function parseDateTime(value) {
  if (!value) return null;
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    return new Date(year, month - 1, day, hour, minute, second);
  }
  const date = new Date(value);
  return isNaN(date.getTime()) ? null : date;
}
function formatDate(value) {
  if (!value) return 'Date unavailable';
  if (Array.isArray(value)) {
    const d = parseDateTime(value);
    return d ? new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(d) : 'Date unavailable';
  }
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
}
function formatTime(value) { return value ? value.slice(0, 5) : 'Time unavailable'; }
function todayIso() { return new Date().toISOString().slice(0, 10); }
function setMessage(element, text, type = '') { if (element) { element.textContent = text || ''; element.className = `message ${type}`.trim(); } }
function getSavedUser() { try { return JSON.parse(sessionStorage.getItem('screenlyUser') || 'null'); } catch (error) { return null; } }
function saveUser(user) { sessionStorage.setItem('screenlyUser', JSON.stringify(user)); }
function clearUser() { sessionStorage.removeItem('screenlyUser'); }
function requireLogin() { if (!getSavedUser()) { window.location.href = 'login.html'; return false; } return true; }

function getTotpStatus(userId) {
  return localStorage.getItem(`screenly_totp_${userId}`);
}
function setTotpStatus(userId, status) {
  if (status) {
    localStorage.setItem(`screenly_totp_${userId}`, status);
  } else {
    localStorage.removeItem(`screenly_totp_${userId}`);
  }
}

function renderHeader() {
  const target = qs('#site-header');
  if (!target) return;
  const user = currentUser;
  target.className = 'site-header';
  target.innerHTML = `<header class="navbar"><a class="brand" href="index.html">SCREENLY</a>
  <nav class="nav-links"><a href="movies.html">Movies</a>
  <a href="theatres.html">Theatres</a>
  ${user ? '<a href="bookings.html">My bookings</a><a href="settings.html">Settings</a>' : ''}
  ${user && user.role === 'ADMIN' ? '<a href="admin.html">Admin</a>' : ''}
  ${user ? `<span class="nav-user">${escapeHtml(user.name)}</span>
  <a href="#" data-logout>Log out</a>` : '<a href="login.html">Log in</a><a class="button button-small register-button" href="register.html">Register</a>'}</nav></header>`;
  const logout = qs('[data-logout]', target);
  if (logout) logout.addEventListener('click', async event => { 
    event.preventDefault(); 
    try { 
      await API.post('/api/auth/logout'); 
    } 
    catch (error) 
    { /* Session may already be gone. */ } 
    currentUser = null;
    window.location.href = 'index.html'; });
}

async function loadCurrentUser() {
    try {
        currentUser = await API.get('/api/auth/session');
        return currentUser;
    } catch (error) {
        currentUser = null;
        return null;
    }
}

function requireLogin() {
    if (!currentUser) {
        window.location.href = 'login.html';
        return false;
    }

    return true;
}

function requireAdmin() {
    if (!currentUser) {
        window.location.href = 'login.html';
        return false;
    }

    if (currentUser.role !== 'ADMIN') {
        window.location.href = 'index.html';
        return false;
    }

    return true;
}

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
  const str = String(value).trim();
  const d = str.includes('T') ? new Date(str) : new Date(`${str}T00:00:00`);
  return isNaN(d.getTime()) ? 'Date unavailable' : new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(d);
}

function formatTime(value) {
  if (!value) return 'Time unavailable';
  if (Array.isArray(value)) {
    const hour = String(value[0] ?? 0).padStart(2, '0');
    const minute = String(value[1] ?? 0).padStart(2, '0');
    return `${hour}:${minute}`;
  }
  const str = String(value).trim();
  if (str.includes(',')) {
    const parts = str.split(',').map(p => p.trim());
    return `${parts[0].padStart(2, '0')}:${(parts[1] || '00').padStart(2, '0')}`;
  }
  return str.length >= 5 ? str.slice(0, 5) : str;
}
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
  const page = document.body ? (document.body.dataset.page || '') : '';
  const pathname = window.location.pathname || '';

  const isMovies = page === 'movies' || page === 'details' || pathname.endsWith('movies.html') || pathname.endsWith('movie-details.html');
  const isTheatres = page === 'theatres' || page === 'theatre_details' || pathname.endsWith('theatres.html') || pathname.endsWith('theatre-details.html');
  const isBookings = page === 'bookings' || page === 'ticket_view' || pathname.endsWith('bookings.html') || pathname.endsWith('booking.html');
  const isAdmin = page === 'admin' || pathname.endsWith('admin.html');
  const isCheckin = page === 'checkin' || page === 'ticket_checkin' || pathname.endsWith('checkin.html');
  const isSettings = page === 'settings' || pathname.endsWith('settings.html');

  const avatarInitial = user ? ((user.name || user.email || 'U').trim().charAt(0).toUpperCase()) : 'U';

  target.className = 'site-header';
  target.innerHTML = `<header class="navbar"><a class="brand" href="index.html">SCREENLY</a>
  <nav class="nav-links">
    <a href="movies.html" class="${isMovies ? 'nav-link-active' : ''}">Movies</a>
    <a href="theatres.html" class="${isTheatres ? 'nav-link-active' : ''}">Theatres</a>
    ${user && user.role !== 'ADMIN' ? `<a href="bookings.html" class="${isBookings ? 'nav-link-active' : ''}">My bookings</a>` : ''}
    ${user && user.role === 'ADMIN' ? `
      <a href="admin.html" class="${isAdmin ? 'nav-link-active' : ''}">Manage Catalogue</a>
      <a href="checkin.html" class="${isCheckin ? 'nav-link-active' : ''}">Verify Tickets</a>
    ` : ''}
    ${user ? `
    <div class="nav-profile-menu-wrap">
      <button id="nav-profile-btn" class="nav-profile-btn ${isSettings ? 'nav-profile-btn-active' : ''}" type="button" aria-haspopup="true" aria-expanded="false" title="Account & Profile">
        <span class="nav-profile-avatar">${escapeHtml(avatarInitial)}</span>
      </button>
      <div id="nav-profile-dropdown" class="nav-profile-dropdown" role="menu">
        <div class="dropdown-user-header">
          <span class="dropdown-avatar-large">${escapeHtml(avatarInitial)}</span>
          <div class="dropdown-user-meta">
            <strong class="dropdown-user-name">${escapeHtml(user.name || 'User')}</strong>
            <span class="dropdown-user-email">${escapeHtml(user.email || '')}</span>
            <span class="dropdown-user-role badge ${user.role === 'ADMIN' ? 'badge-primary' : 'badge-teal'}">${escapeHtml(user.role || 'CUSTOMER')}</span>
          </div>
        </div>
        <div class="dropdown-divider"></div>
        ${user.role === 'ADMIN' ? `
        <a href="checkin.html" class="dropdown-item" role="menuitem">
          <span class="dropdown-item-icon">🎟️</span>
          <span class="dropdown-item-text">Verify Tickets</span>
        </a>
        <a href="admin.html" class="dropdown-item" role="menuitem">
          <span class="dropdown-item-icon">🎬</span>
          <span class="dropdown-item-text">Manage Catalogue</span>
        </a>
        <div class="dropdown-divider"></div>
        ` : ''}
        <a href="settings.html#profile" class="dropdown-item" role="menuitem">
          <span class="dropdown-item-icon">👤</span>
          <span class="dropdown-item-text">Profile Details</span>
        </a>
        <a href="settings.html#security" class="dropdown-item" role="menuitem">
          <span class="dropdown-item-icon">⚙️</span>
          <span class="dropdown-item-text">Settings & Security</span>
        </a>
        <div class="dropdown-divider"></div>
        <a href="#" data-logout class="dropdown-item dropdown-item-danger" role="menuitem">
          <span class="dropdown-item-icon">🚪</span>
          <span class="dropdown-item-text">Log out</span>
        </a>
      </div>
    </div>` : '<a href="login.html">Log in</a><a class="button button-small register-button" href="register.html">Register</a>'}
  </nav></header>`;

  const profileBtn = qs('#nav-profile-btn', target);
  const profileDropdown = qs('#nav-profile-dropdown', target);

  if (profileBtn && profileDropdown) {
    profileBtn.addEventListener('click', event => {
      event.stopPropagation();
      const isExpanded = profileDropdown.classList.toggle('show');
      profileBtn.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
    });

    document.addEventListener('click', event => {
      if (!profileDropdown.contains(event.target) && event.target !== profileBtn) {
        profileDropdown.classList.remove('show');
        profileBtn.setAttribute('aria-expanded', 'false');
      }
    });

    document.addEventListener('keydown', event => {
      if (event.key === 'Escape' && profileDropdown.classList.contains('show')) {
        profileDropdown.classList.remove('show');
        profileBtn.setAttribute('aria-expanded', 'false');
      }
    });
  }

  const logout = qs('[data-logout]', target);
  if (logout) logout.addEventListener('click', async event => {
    event.preventDefault();
    try {
      await API.post('/api/auth/logout');
    }
    catch (error) { /* Session may already be gone. */ }
    currentUser = null;
    window.location.href = 'index.html';
  });
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
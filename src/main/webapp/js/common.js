function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

function qs(selector, root = document) { return root.querySelector(selector); }
function formatMoney(value) { return Number(value || 0).toFixed(2); }
function formatDate(value) {
  if (!value) return 'Date unavailable';
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
}
function formatTime(value) { return value ? value.slice(0, 5) : 'Time unavailable'; }
function todayIso() { return new Date().toISOString().slice(0, 10); }
function setMessage(element, text, type = '') { if (element) { element.textContent = text || ''; element.className = `message ${type}`.trim(); } }
function getSavedUser() { try { return JSON.parse(sessionStorage.getItem('screenlyUser') || 'null'); } catch (error) { return null; } }
function saveUser(user) { sessionStorage.setItem('screenlyUser', JSON.stringify(user)); }
function clearUser() { sessionStorage.removeItem('screenlyUser'); }
function requireLogin() { if (!getSavedUser()) { window.location.href = 'login.html'; return false; } return true; }

function renderHeader() {
  const target = qs('#site-header');
  if (!target) return;
  const user = getSavedUser();
  target.className = 'site-header';
  target.innerHTML = `<header class="navbar"><a class="brand" href="index.html">SCREENLY</a>
  <nav class="nav-links"><a href="movies.html">Movies</a>
  ${user ? '<a href="bookings.html">My bookings</a>' : ''}
  ${user && user.role === 'ADMIN' ? '<a href="admin.html">Admin</a>' : ''}
  ${user ? `<span class="nav-user">${escapeHtml(user.name)}</span>
  <a href="#" data-logout>Log out</a>` : '<a href="login.html">Log in</a><a class="button button-small" href="register.html">Register</a>'}</nav></header>`;
  const logout = qs('[data-logout]', target);
  if (logout) logout.addEventListener('click', async event => { event.preventDefault(); try { await API.post('/api/auth/logout'); } catch (error) { /* Session may already be gone. */ } clearUser(); window.location.href = 'index.html'; });
}

function requireAdmin() 
{ const user = getSavedUser(); 
    if (!user || user.role !== 'ADMIN') { 
        window.location.href = 'login.html'; 
        return false; 
    } return true; 
}

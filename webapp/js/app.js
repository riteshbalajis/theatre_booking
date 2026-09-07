document.addEventListener('DOMContentLoaded', () => {
  renderHeader();
  const page = document.body.dataset.page;
  const handlers = { 
    home: loadHome, 
    movies: loadMoviesPage, 
    login: setupLogin, 
    register: setupRegister, 
    details: loadDetails, 
    seats: loadSeats, 
    bookings: loadBookings, 
    admin: loadAdmin };
  if (handlers[page]) handlers[page]();
});

function movieCard(movie) {
  return `<article class="movie-card">
  <div class="poster-placeholder">${escapeHtml(movie.title)}</div>
  <div class="movie-card-body"><h3>${escapeHtml(movie.title)}</h3>
  
  <p class="movie-meta">
  ${escapeHtml(movie.genre || 'Feature')} &middot; 
  ${escapeHtml(movie.language || 'Original')} &middot; 
  ${movie.durationMinutes || 0} min</p>
  
  ${movie.description ? `<p class="movie-description">${escapeHtml(movie.description)}</p>` : ''}
  
  <a class="button button-small" href="movie-details.html?id=${movie.movieId}">View shows</a>
  </div>
  </article>`;
}

async function getActiveMovies(keyword = '') {
  const path = keyword ? `/api/movies?keyword=${encodeURIComponent(keyword)}` : '/api/movies';
  const movies = await API.get(path);
  return (movies || []).filter(movie => movie.status === 'ACTIVE');
}

async function loadHome() {
  const list = qs('#movie-list'); 
  const message = qs('#page-message');
  try { 
    const movies = await getActiveMovies();
     list.innerHTML = movies.length ? movies.map(movieCard).join('') : '<p class="muted">No active movies are available right now.</p>';
     }
  catch (error) { setMessage(message, error.message, 'error'); }
}

async function loadMoviesPage() {

    const list = qs('#movie-list');
    const message = qs('#page-message'); 
    const form = qs('#movie-search');
    const render = async keyword => { 
        setMessage(message, 'Loading movies...'); 
        try { 
            const movies = await getActiveMovies(keyword); 
            list.innerHTML = movies.length ? movies.map(movieCard).join('') : '<p class="muted">No active movies match your search.</p>'; 
            setMessage(message, ''); 
        } 
        catch (error) { 
            setMessage(message, error.message, 'error'); 
        } 
    };
    form.addEventListener('submit', event => { 
        event.preventDefault(); 
        render(new FormData(form).get('keyword').trim());
        }
    );
    render('');
}

function setupLogin() {
  const form = qs('#login-form'); 
  const message = qs('#form-message');
  form.addEventListener('submit', async event => { 
    event.preventDefault(); 
    setMessage(message, 'Signing in...'); 
    const data = Object.fromEntries(new FormData(form)); 
    try { const result = await API.post('/api/auth/login', data); 
        saveUser(result.user); 
        window.location.href = result.user.role === 'ADMIN' ? 'admin.html' : 'index.html'; 
    } 
    catch (error) { 
        setMessage(message, error.message, 'error'); 
    } });
}

function setupRegister() {
  const form = qs('#register-form'); 
  const message = qs('#form-message');
  form.addEventListener('submit', async event => { 
    event.preventDefault(); 
    setMessage(message, 'Creating account...'); 
    const data = Object.fromEntries(new FormData(form)); 
    try { 
      await API.post('/api/auth/register', data); 
      window.location.href = 'login.html?registered=1'; 
    } catch (error) { 
      setMessage(message, error.message, 'error'); 
    } });
}

async function loadDetails() {
  const id = new URLSearchParams(window.location.search).get('id'); 
  const detail = qs('#movie-detail'); 
  const list = qs('#show-list'); 
  const dateInput = qs('#show-date'); 
  const message = qs('#page-message');
  if (!id) { 
    setMessage(message, 'A movie is required.', 'error'); 
    return;
 }
  dateInput.value = todayIso();
  try { 
    const movie = await API.get(`/api/movies/${id}`); 
    detail.innerHTML = `<div class="detail-poster">${escapeHtml(movie.title)}</div>
    <div class="detail-copy"><p class="eyebrow">Movie details</p>
    <h1>${escapeHtml(movie.title)}</h1>
    <p>${escapeHtml(movie.description || 'No description available.')}</p>
    <p class="movie-meta">${escapeHtml(movie.genre || 'Feature')} &middot; 
    ${escapeHtml(movie.language || 'Original')} &middot; 
    ${movie.durationMinutes} minutes</p></div>`; 
  } 
  catch (error) { 
    setMessage(message, error.message, 'error'); 
    return;
 }
  const loadShows = async () => { 
    setMessage(message, 'Loading showtimes...'); 
    list.innerHTML = ''; 
    try { 
        const shows = await API.get(`/api/shows/movie/${id}/date/${dateInput.value}`); 
        list.innerHTML = shows.length ? shows.map(show => `<article class="show-card">
        <span class="eyebrow">${escapeHtml(show.status)}</span>
        <strong class="show-time">${formatTime(show.startTime)} - ${formatTime(show.endTime)}</strong>
        <small>Screen ${show.screenId}</small>
        <a class="button button-small" href="seats.html?showId=${show.showId}">Choose seats</a>
        </article>`).join('') : '<p class="muted">No shows are scheduled for this date.</p>';
        setMessage(message, '');
     } 
     catch (error) { 
        setMessage(message, error.message, 'error');
     } 
    };
  dateInput.addEventListener('change', loadShows); loadShows();
}

async function loadSeats() {
  if (!requireLogin()) return;

  const showId = new URLSearchParams(window.location.search).get('showId'); 
  const map = qs('#seat-map'); 
  const summary = qs('#show-summary'); 
  const selectedLabel = qs('#selected-seats'); 
  const total = qs('#total-amount'); 
  const button = qs('#confirm-booking'); 
  const message = qs('#page-message');
  if (!showId) { setMessage(message, 'A show is required.', 'error'); return; }
  const selected = new Map();
  try { 
    const show = await API.get(`/api/shows/${showId}`); 
    summary.textContent = `${formatDate(show.showDate)} | ${formatTime(show.startTime)} - ${formatTime(show.endTime)} | Screen ${show.screenId}`; 
    const seats = await API.get(`/api/show-seats/show/${showId}`); 
    renderSeatMap(seats); 
  } 
  catch (error) { 
    setMessage(message, error.message, 'error'); 
    return; 
   }
  function renderSeatMap(seats) { 
    const rows = {}; 
    seats.forEach(seat => { (rows[seat.rowLabel || '?'] ||= []).push(seat); }); 
    map.innerHTML = Object.entries(rows).sort(([a], [b]) => a.localeCompare(b)).map(([row, rowSeats]) => `<div class="seat-row"><span class="row-label">${escapeHtml(row)}</span>${rowSeats.sort((a, b) => a.seatNumber - b.seatNumber).map(seat => `<button class="seat ${seat.status === 'BOOKED' ? 'booked' : ''}" data-seat-id="${seat.showSeatId}" data-price="${seat.price}" ${seat.status === 'BOOKED' ? 'disabled' : ''} aria-label="Row ${escapeHtml(row)} seat ${seat.seatNumber}">${seat.seatNumber}</button>`).join('')}</div>`).join(''); map.querySelectorAll('.seat:not(.booked)').forEach(seat => seat.addEventListener('click', () => { 
        const seatId = Number(seat.dataset.seatId); 
        if (selected.has(seatId)) { 
            selected.delete(seatId); 
            seat.classList.remove('selected'); 
        } 
        else { 
            selected.set(seatId, Number(seat.dataset.price)); 
            seat.classList.add('selected'); 
        } updateSummary(); })); }



  function updateSummary() { 
    const entries = [...selected.entries()]; 
    selectedLabel.textContent = entries.length ? entries.map(([id]) => `Seat ${id}`).join(', ') : 'No seats selected'; 
    total.textContent = formatMoney(entries.reduce((sum, [, price]) => sum + price, 0)); 
    button.disabled = !entries.length; }
    button.addEventListener('click', async () => { 
        button.disabled = true; 
        setMessage(message, 'Confirming booking...'); 
        try { const result = await API.post('/api/bookings', { showId: Number(showId), showSeatIds: [...selected.keys()] }); 
        window.location.href = `bookings.html?booked=${result.bookingId}`; 
        } 
        catch (error) { 
            button.disabled = false; setMessage(message, error.message, 'error'); 
        } 
    });
}

async function loadBookings() {
  if (!requireLogin()) return;
  const list = qs('#booking-list'); 
  const message = qs('#page-message');
  try { 
    const bookings = await API.get('/api/bookings'); 
    if (!bookings.length) { 
        list.innerHTML = '<p class="muted">You have no bookings yet.</p>';
         return;
    } 
    list.innerHTML = bookings.map(booking => `<article class="booking-card"><div>
        <h3>Booking #${booking.bookingId}</h3>
        <p>Show #${booking.showId} &middot; ${booking.seats.map(seat => `${escapeHtml(seat.rowLabel)}${seat.seatNumber}`).join(', ')}</p>
        <p>Booked ${booking.bookedAt ? new Date(booking.bookedAt).toLocaleString() : 'recently'}</p></div>
        <div><p class="status ${booking.status === 'CANCELLED' ? 'cancelled' : ''}">${escapeHtml(booking.status)}</p>
        <p><strong>${formatMoney(booking.totalAmount)}</strong></p>
        ${booking.status !== 'CANCELLED' && booking.status !== 'COMPLETED' ? `<button class="button button-small danger" data-cancel="${booking.bookingId}">Cancel</button>` : ''}</div></article>`).join(''); 
        list.querySelectorAll('[data-cancel]').forEach(button => button.addEventListener('click', () => cancelBooking(button.dataset.cancel))); 
        const booked = new URLSearchParams(window.location.search).get('booked'); 
        if (booked) setMessage(message, `Booking #${booked} confirmed.`, 'success'); } catch (error) { setMessage(message, error.message, 'error'); }
}

async function cancelBooking(id) { if (!window.confirm('Cancel this booking?')) return; try { await API.remove(`/api/bookings/${id}`); window.location.reload(); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } }

const ADMIN_CONFIG = {
  movies: {
    title: 'Movies', endpoint: '/api/movies', id: 'movieId',
    fields: [['title', 'Title', 'text', true], ['description', 'Description', 'textarea', false], ['durationMinutes', 'Duration (minutes)', 'number', true], ['language', 'Language', 'text', true], ['genre', 'Genre', 'text', false], ['releaseDate', 'Release date', 'date', false]],
    columns: [['title', 'Title'], ['genre', 'Genre'], ['language', 'Language'], ['durationMinutes', 'Minutes'], ['status', 'Status']]
  },
  theatres: {
    title: 'Theatres', endpoint: '/api/theatres', id: 'theatreId',
    fields: [['name', 'Name', 'text', true], ['location', 'Location', 'text', true]],
    columns: [['name', 'Name'], ['location', 'Location'], ['status', 'Status']]
  },
  screens: {
    title: 'Screens', endpoint: '/api/screens', id: 'screenId',
    fields: [['theatreId', 'Theatre', 'select-theatre', true], ['name', 'Name', 'text', true], ['capacity', 'Capacity', 'number', true]],
    columns: [['name', 'Name'], ['theatreId', 'Theatre'], ['capacity', 'Capacity'], ['status', 'Status']]
  },
  seats: {
    title: 'Seats', endpoint: '/api/seats', id: 'seatId',
    fields: [['screenId', 'Screen', 'select-screen', true], ['rowLabel', 'Row', 'text', true], ['seatNumber', 'Number', 'number', true], ['seatType', 'Type', 'select-seat-type', true]],
    columns: [['screenId', 'Screen'], ['rowLabel', 'Row'], ['seatNumber', 'Number'], ['seatType', 'Type'], ['status', 'Status']]
  },
  shows: {
    title: 'Shows', endpoint: '/api/shows', id: 'showId',
    fields: [['movieId', 'Movie', 'select-movie', true], ['screenId', 'Screen', 'select-screen', true], ['showDate', 'Date', 'date', true], ['startTime', 'Starts', 'time', true], ['endTime', 'Ends', 'time', true], ['regularPrice', 'Regular price', 'number', true], ['premiumPrice', 'Premium price', 'number', true], ['reclinerPrice', 'Recliner price', 'number', true]],
    columns: [['movieId', 'Movie'], ['screenId', 'Screen'], ['showDate', 'Date'], ['startTime', 'Starts'], ['status', 'Status']]
  }
};

async function loadAdmin() {
  if (!requireAdmin()) return;
  const tabs = qs('#admin-tabs');
  tabs.addEventListener('click', event => { const tab = event.target.closest('[data-resource]'); if (!tab) return; tabs.querySelectorAll('.tab').forEach(item => item.classList.toggle('active', item === tab)); renderAdminResource(tab.dataset.resource); });
  renderAdminResource('movies');
}

async function adminOptions(type) {
  if (type === 'select-movie') return API.get('/api/movies');
  if (type === 'select-theatre') return API.get('/api/theatres');
  if (type === 'select-screen') return API.get('/api/screens');
  return [];
}

function optionMarkup(type, values, current = '') {
  if (type === 'select-seat-type') values = ['REGULAR', 'PREMIUM', 'RECLINER'].map(value => ({ value, label: value }));
  const fieldName = { 'select-seat-type': 'seatType', 'select-movie': 'movieId', 'select-theatre': 'theatreId', 'select-screen': 'screenId' }[type];
  const immutableOnUpdate = current && ['select-theatre', 'select-screen'].includes(type);
  return `<select name="${fieldName}" ${immutableOnUpdate ? 'disabled' : ''} required><option value="">Select</option>${values.map(item => { const value = item.value ?? item.movieId ?? item.theatreId ?? item.screenId; const label = item.label ?? item.title ?? item.name; return `<option value="${value}" ${String(value) === String(current) ? 'selected' : ''}>${escapeHtml(label)}</option>`; }).join('')}</select>`;
}

async function formMarkup(config, item = {}) {
  const fields = await Promise.all(config.fields.map(async ([name, label, type, required]) => {
    const value = item[name] ?? '';
    if (type.startsWith('select-')) return `<label>${label}${optionMarkup(type, await adminOptions(type), value)}</label>`;
    if (type === 'textarea') return `<label class="wide">${label}<textarea name="${name}" ${required ? 'required' : ''}>${escapeHtml(value)}</textarea></label>`;
    return `<label>${label}<input name="${name}" type="${type}" value="${escapeHtml(type === 'time' ? String(value).slice(0, 5) : value)}" ${required ? 'required' : ''} ${type === 'number' ? 'min="0" step="0.01"' : ''}></label>`;
  }));
  return fields.join('');
}

async function renderAdminResource(resource) {
  const config = ADMIN_CONFIG[resource]; const workspace = qs('#admin-workspace'); const message = qs('#page-message');
  workspace.innerHTML = '<p class="muted">Loading resource...</p>';
  try { const items = resource === 'seats' ? await loadAllSeats() : await API.get(config.endpoint); const rows = items || []; workspace.innerHTML = `<div class="admin-toolbar"><h2>${config.title}</h2><button class="button button-small" data-new-resource>New ${config.title.slice(0, -1)}</button></div><div data-admin-form></div><table class="admin-table"><thead><tr>${config.columns.map(([, label]) => `<th>${label}</th>`).join('')}<th>Actions</th></tr></thead><tbody>${rows.map(item => `<tr>${config.columns.map(([key]) => `<td>${escapeHtml(item[key])}</td>`).join('')}<td><div class="admin-actions"><button class="button button-small" data-edit="${item[config.id]}">Edit</button><button class="button button-small danger" data-delete="${item[config.id]}">Deactivate</button></div></td></tr>`).join('')}</tbody></table>`; workspace.querySelector('[data-new-resource]').addEventListener('click', () => openAdminForm(resource)); workspace.querySelectorAll('[data-edit]').forEach(button => button.addEventListener('click', () => openAdminForm(resource, rows.find(item => String(item[config.id]) === button.dataset.edit)))); workspace.querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click', () => deactivateResource(resource, button.dataset.delete))); setMessage(message, rows.length ? '' : `No ${config.title.toLowerCase()} found.`); } catch (error) { setMessage(message, error.message, 'error'); workspace.innerHTML = ''; }
}

async function loadAllSeats() {
  const screens = await API.get('/api/screens'); const result = [];
  for (const screen of screens || []) { const seats = await API.get(`/api/seats/screen/${screen.screenId}`); result.push(...(seats || [])); }
  return result;
}

async function openAdminForm(resource, item = null) {
  const config = ADMIN_CONFIG[resource]; const target = qs('[data-admin-form]');
  const formConfig = item && resource === 'shows' ? { ...config, fields: config.fields.filter(([name]) => !['regularPrice', 'premiumPrice', 'reclinerPrice'].includes(name)) } : config;
  target.innerHTML = `<form class="admin-form"><input type="hidden" name="recordId" value="${item ? item[config.id] : ''}">${await formMarkup(formConfig, item || {})}<div class="form-actions"><button class="button" type="submit">${item ? 'Save changes' : 'Create'}</button><button class="button button-small danger" type="button" data-close-form>Close</button></div></form>`;
  target.querySelector('[data-close-form]').addEventListener('click', () => { target.innerHTML = ''; });
  target.querySelector('form').addEventListener('submit', async event => { event.preventDefault(); const form = event.target; const values = Object.fromEntries(new FormData(form)); delete values.recordId; ['movieId', 'theatreId', 'screenId', 'durationMinutes', 'capacity', 'seatNumber'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); }); ['regularPrice', 'premiumPrice', 'reclinerPrice'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); }); try { const id = form.elements.recordId.value; if (id) { await API.put(`${config.endpoint}/${id}`, values); } else { await API.post(config.endpoint, values); } await renderAdminResource(resource); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } });
}

async function deactivateResource(resource, id) { if (!window.confirm('Deactivate this resource?')) return; const config = ADMIN_CONFIG[resource]; try { await API.remove(`${config.endpoint}/${id}`); await renderAdminResource(resource); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } }

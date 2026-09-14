document.addEventListener('DOMContentLoaded', () => {
  renderHeader();
  const page = document.body.dataset.page;
  const handlers = {
    home: loadHome,
    movies: loadMoviesPage,
    login: setupLogin,
    register: setupRegister,
    forgot_password: setupForgetPassword,
    forget_password: setupForgetPassword,
    details: loadDetails,
    seats: loadSeats,
    bookings: loadBookings,
    payment: setupPayment,
    admin: loadAdmin,
    ticket_view: loadTicketView
  };
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

  const params = new URLSearchParams(window.location.search);
  if (params.get('reset') === 'success') {
    setMessage(message, 'Password reset successfully! Please log in with your new password.', 'success');
  } else if (params.get('registered') === '1') {
    setMessage(message, 'Registration successful! Please log in.', 'success');
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    setMessage(message, 'Signing in...');
    const data = Object.fromEntries(new FormData(form));
    try {
      const result = await API.post('/api/auth/login', data);
      saveUser(result.user);
      window.location.href = result.user.role === 'ADMIN' ? 'admin.html' : 'index.html';
    }
    catch (error) {
      setMessage(message, error.message, 'error');
    }
  });
}

function setupForgetPassword() {
  let userEmail = '';
  let resetToken = '';

  const step1Form = qs('#forgot-password-form');
  const step2Form = qs('#verify-otp-form');
  const step3Form = qs('#reset-password-form');

  const emailInput = qs('#forgot-email');
  const otpInput = qs('#reset-otp');
  const newPasswordInput = qs('#new-password');
  const confirmPasswordInput = qs('#confirm-password');

  const forgotMessage = qs('#forgot-message');
  const otpMessage = qs('#otp-message');
  const resetMessage = qs('#reset-message');

  const forgotButton = qs('#forgot-button');
  const verifyOtpButton = qs('#verify-otp-button');
  const resetPasswordButton = qs('#reset-password-button');

  const step1Ind = qs('#step-ind-1');
  const step2Ind = qs('#step-ind-2');
  const step3Ind = qs('#step-ind-3');

  const targetEmailSpan = qs('#otp-target-email');
  const changeEmailBtn = qs('#change-email-btn');
  const resendOtpLink = qs('#resend-otp-link');

  function setStep(step) {
    if (step1Form) step1Form.style.display = step === 1 ? 'grid' : 'none';
    if (step2Form) step2Form.style.display = step === 2 ? 'grid' : 'none';
    if (step3Form) step3Form.style.display = step === 3 ? 'grid' : 'none';

    if (step1Ind) {
      step1Ind.className = `step-item ${step === 1 ? 'active' : (step > 1 ? 'completed' : '')}`;
    }
    if (step2Ind) {
      step2Ind.className = `step-item ${step === 2 ? 'active' : (step > 2 ? 'completed' : '')}`;
    }
    if (step3Ind) {
      step3Ind.className = `step-item ${step === 3 ? 'active' : ''}`;
    }

    if (step === 1 && emailInput) {
      emailInput.focus();
    } else if (step === 2 && otpInput) {
      otpInput.value = '';
      otpInput.focus();
    } else if (step === 3 && newPasswordInput) {
      newPasswordInput.value = '';
      if (confirmPasswordInput) confirmPasswordInput.value = '';
      newPasswordInput.focus();
    }
  }

  // Step 1: Request OTP
  if (step1Form) {
    step1Form.addEventListener('submit', async event => {
      event.preventDefault();
      const email = emailInput ? emailInput.value.trim() : '';
      if (!email) {
        setMessage(forgotMessage, 'Please enter a valid email address.', 'error');
        return;
      }
      userEmail = email;
      if (forgotButton) forgotButton.disabled = true;
      setMessage(forgotMessage, 'Sending OTP to your email...');

      try {
        const response = await API.post('/api/auth/forgot_password', { email: userEmail });
        const successMsg = (response && response.message) || 'OTP sent successfully. Please check your email.';
        if (targetEmailSpan) targetEmailSpan.textContent = userEmail;
        setStep(2);
        setMessage(otpMessage, successMsg, 'success');
      } catch (error) {
        setMessage(forgotMessage, error.message, 'error');
      } finally {
        if (forgotButton) forgotButton.disabled = false;
      }
    });
  }

  // Resend OTP
  if (resendOtpLink) {
    resendOtpLink.addEventListener('click', async event => {
      event.preventDefault();
      if (!userEmail) return;
      setMessage(otpMessage, 'Resending OTP...');
      try {
        const response = await API.post('/api/auth/forgot_password', { email: userEmail });
        setMessage(otpMessage, (response && response.message) || 'New OTP sent to your email.', 'success');
      } catch (error) {
        setMessage(otpMessage, error.message, 'error');
      }
    });
  }

  // Change Email
  if (changeEmailBtn) {
    changeEmailBtn.addEventListener('click', () => {
      setMessage(forgotMessage, '');
      setStep(1);
    });
  }

  // Step 2: Verify OTP
  if (step2Form) {
    step2Form.addEventListener('submit', async event => {
      event.preventDefault();
      const otp = otpInput ? otpInput.value.trim() : '';
      if (!otp) {
        setMessage(otpMessage, 'Please enter the OTP code.', 'error');
        return;
      }
      if (verifyOtpButton) verifyOtpButton.disabled = true;
      setMessage(otpMessage, 'Verifying OTP...');

      try {
        const response = await API.post('/api/auth/verify_reset_otp', {
          email: userEmail,
          otp: otp
        });
        resetToken = response.resetToken;
        setStep(3);
        setMessage(resetMessage, 'OTP verified! Please set your new password.', 'success');
      } catch (error) {
        setMessage(otpMessage, error.message, 'error');
      } finally {
        if (verifyOtpButton) verifyOtpButton.disabled = false;
      }
    });
  }

  // Step 3: Reset Password
  if (step3Form) {
    step3Form.addEventListener('submit', async event => {
      event.preventDefault();
      const newPassword = newPasswordInput ? newPasswordInput.value : '';
      const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';

      if (!newPassword || newPassword.length < 6) {
        setMessage(resetMessage, 'Password must be at least 6 characters long.', 'error');
        return;
      }
      if (newPassword !== confirmPassword) {
        setMessage(resetMessage, 'Passwords do not match. Please verify.', 'error');
        return;
      }

      if (resetPasswordButton) resetPasswordButton.disabled = true;
      setMessage(resetMessage, 'Updating password...');

      try {
        await API.post('/api/auth/reset_password', {
          email: userEmail,
          resetToken: resetToken,
          newPassword: newPassword
        });

        setMessage(resetMessage, 'Password reset successfully! Redirecting to login...', 'success');
        if (newPasswordInput) newPasswordInput.disabled = true;
        if (confirmPasswordInput) confirmPasswordInput.disabled = true;

        setTimeout(() => {
          window.location.href = 'login.html?reset=success';
        }, 1500);
      } catch (error) {
        setMessage(resetMessage, error.message, 'error');
        if (resetPasswordButton) resetPasswordButton.disabled = false;
      }
    });
  }
}

async function setupPayment() {
  if (!requireLogin()) return;

  const params = new URLSearchParams(window.location.search);
  const bookingId = params.get('bookingId') || params.get('id') || params.get('booking_id');

  const bookingIdElement = qs('#booking-id');
  const amountElement = qs('#booking-amount');
  const countdownElement = qs('#countdown');
  const form = qs('#payment-form');
  const pinInput = qs('#payment-pin');
  const button = qs('#payment-button');
  const message = qs('#payment-message');

  if (!bookingId) {
    setMessage(message, 'Booking ID is required.', 'error');
    if (button) button.disabled = true;
    return;
  }

  let timerExpired = false;

  // Always bind the submit handler so user submission is never blocked
  if (form) {
    form.addEventListener('submit', async event => {
      event.preventDefault();
      const pin = pinInput ? pinInput.value.trim() : '';

      if (!pin) {
        setMessage(message, 'Please enter your payment PIN.', 'error');
        return;
      }

      if (timerExpired) {
        setMessage(message, 'Payment time has expired. Please select your seats again.', 'error');
        return;
      }

      if (button) button.disabled = true;
      setMessage(message, 'Processing payment...');

      try {
        await API.post(`/api/bookings/${bookingId}/payment`, { pin: pin });
        sessionStorage.removeItem(`booking_hold_${bookingId}`);

        setMessage(message, 'Payment successful! Booking confirmed. Opening your ticket...', 'success');
        if (pinInput) pinInput.disabled = true;

        setTimeout(() => {
          window.location.href = `bookings.html?booked=${bookingId}`;
        }, 1000);
      } catch (error) {
        if (!timerExpired && button) button.disabled = false;
        setMessage(message, error.message || 'Payment failed.', 'error');
        if (pinInput) pinInput.focus();
      }
    });
  }

  try {
    const booking = await API.get(`/api/bookings/${bookingId}`);

    if (bookingIdElement) bookingIdElement.textContent = `#${booking.bookingId}`;
    if (amountElement) amountElement.textContent = formatMoney(booking.totalAmount);

    if (booking.status === 'CONFIRMED' || booking.status === 'COMPLETED') {
      setMessage(message, 'This booking has already been confirmed.', 'success');
      if (button) button.disabled = true;
      if (pinInput) pinInput.disabled = true;
      setTimeout(() => {
        window.location.href = `bookings.html?booked=${bookingId}`;
      }, 1500);
      return;
    }

    if (booking.status === 'CANCELLED') {
      setMessage(message, 'This booking has expired or was cancelled. Please select your seats again.', 'error');
      if (button) button.disabled = true;
      if (pinInput) pinInput.disabled = true;
      return;
    }

    // Determine 2-minute deadline
    const savedDeadline = sessionStorage.getItem(`booking_hold_${bookingId}`);
    let holdDeadline;
    if (savedDeadline && !isNaN(Number(savedDeadline))) {
      holdDeadline = Number(savedDeadline);
    } else if (booking.holdUntil) {
      holdDeadline = new Date(booking.holdUntil).getTime();
    } else {
      // 2 minutes from current time
      holdDeadline = Date.now() + 2 * 60 * 1000;
    }
    sessionStorage.setItem(`booking_hold_${bookingId}`, String(holdDeadline));

    startPaymentCountdown(
      holdDeadline,
      countdownElement,
      button,
      pinInput,
      message,
      () => { timerExpired = true; }
    );

  } catch (error) {
    setMessage(message, error.message, 'error');
    if (button) button.disabled = true;
  }
}

function startPaymentCountdown(deadline, countdownElement, button, pinInput, message, onExpire) {
  const updateCountdown = () => {
    const remaining = deadline - Date.now();

    if (remaining <= 0) {
      if (countdownElement) {
        countdownElement.textContent = '00:00';
        countdownElement.classList.add('warning');
      }
      if (button) button.disabled = true;
      if (pinInput) pinInput.disabled = true;
      setMessage(message, 'Payment time has expired (2 minutes limit). Please return to select seats again.', 'error');
      if (onExpire) onExpire();
      return false;
    }

    const totalSeconds = Math.floor(remaining / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    if (countdownElement) {
      countdownElement.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
      if (remaining <= 30000) {
        countdownElement.classList.add('warning');
      } else {
        countdownElement.classList.remove('warning');
      }
    }

    return true;
  };

  if (!updateCountdown()) return;

  const timer = setInterval(() => {
    if (!updateCountdown()) {
      clearInterval(timer);
    }
  }, 1000);
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
    }
  });
}

async function handleGoogleLogin(response) {
  const message = qs('#form-message');

  setMessage(message, 'Signing in with Google...');

  try {
    const result = await API.post('/api/auth/google', {
      credential: response.credential
    });

    saveUser(result.user);

    window.location.href =
      result.user.role === 'ADMIN' ? 'admin.html' : 'index.html';

  } catch (error) {
    setMessage(message, error.message, 'error');
  }
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
      } updateSummary();
    }));
  }



  function updateSummary() {
    const entries = [...selected.entries()];
    selectedLabel.textContent = entries.length ? entries.map(([id]) => `Seat ${id}`).join(', ') : 'No seats selected';
    total.textContent = formatMoney(entries.reduce((sum, [, price]) => sum + price, 0));
    button.disabled = !entries.length;
  }
  button.addEventListener('click', async () => {
    button.disabled = true;
    setMessage(message, 'Confirming booking...');
    try {
      const result = await API.post('/api/bookings', { showId: Number(showId), showSeatIds: [...selected.keys()] });
      const holdTime = result.holdUntil ? new Date(result.holdUntil).getTime() : (Date.now() + 2 * 60 * 1000);
      sessionStorage.setItem(`booking_hold_${result.bookingId}`, String(holdTime));
      window.location.href = `payment.html?bookingId=${result.bookingId}`;
    }
    catch (error) {
      button.disabled = false; setMessage(message, error.message, 'error');
    }
  });
}

function renderTicketCard(booking, show, movie, screen) {
  const seatsFormatted = (booking.seats || []).map(s => `${escapeHtml(s.rowLabel || '')}${s.seatNumber}`).join(', ') || 'N/A';
  const showDateStr = show ? formatDate(show.showDate) : 'Date unavailable';
  const showTimeStr = show ? `${formatTime(show.startTime)} - ${formatTime(show.endTime)}` : 'Time unavailable';
  const screenName = screen ? escapeHtml(screen.name) : (show ? `Screen ${show.screenId}` : 'Screen');
  const movieTitle = movie ? escapeHtml(movie.title) : `Show #${booking.showId}`;
  const movieMeta = movie ? `${escapeHtml(movie.genre || 'Feature')} &middot; ${escapeHtml(movie.language || '')} &middot; ${movie.durationMinutes || 0} min` : '';

  return `<article class="ticket-card" id="confirmed-ticket-${booking.bookingId}">
    <div class="ticket-top">
      <div>
        <div class="ticket-brand">SCREENLY CINEMAS</div>
        <h2 class="ticket-movie-title">${movieTitle}</h2>
        ${movieMeta ? `<p class="ticket-movie-meta">${movieMeta}</p>` : ''}
      </div>
      <span class="ticket-badge">${escapeHtml(booking.status || 'CONFIRMED')}</span>
    </div>
    <div class="ticket-perforated"></div>
    <div class="ticket-body">
      <div class="ticket-grid">
        <div class="ticket-field">
          <span class="ticket-field-label">Booking Reference</span>
          <span class="ticket-field-value">#${booking.bookingId}</span>
        </div>
        <div class="ticket-field">
          <span class="ticket-field-label">Date & Showtime</span>
          <span class="ticket-field-value">${showDateStr}<br><small class="muted">${showTimeStr}</small></span>
        </div>
        <div class="ticket-field">
          <span class="ticket-field-label">Screen</span>
          <span class="ticket-field-value">${screenName}</span>
        </div>
        <div class="ticket-field">
          <span class="ticket-field-label">Confirmed Seats</span>
          <span class="ticket-field-value ticket-seats-highlight">${seatsFormatted}</span>
        </div>
        <div class="ticket-field">
          <span class="ticket-field-label">Amount Paid</span>
          <span class="ticket-field-value price-highlight">₹${formatMoney(booking.totalAmount)}</span>
        </div>
        <div class="ticket-field">
          <span class="ticket-field-label">Booked At</span>
          <span class="ticket-field-value">${booking.bookedAt ? new Date(booking.bookedAt).toLocaleDateString() : 'Today'}</span>
        </div>
      </div>
      <div class="ticket-footer">
        <span class="muted" style="font-size: .85rem;">Present this confirmed ticket at the cinema entrance.</span>
        <div class="ticket-actions">
          <button class="button button-small button-light" type="button" onclick="window.print()">Print Ticket</button>
          <a class="button button-small" href="movies.html">Browse Movies</a>
        </div>
      </div>
    </div>
  </article>`;
}

async function fetchFullBookingDetails(bookingId) {
  const booking = await API.get(`/api/bookings/${bookingId}`);
  let show = null;
  let movie = null;
  let screen = null;
  try {
    if (booking.showId) {
      show = await API.get(`/api/shows/${booking.showId}`);
      if (show && show.movieId) {
        movie = await API.get(`/api/movies/${show.movieId}`);
      }
      if (show && show.screenId) {
        try {
          screen = await API.get(`/api/screens/${show.screenId}`);
        } catch (ignored) { }
      }
    }
  } catch (ignored) { }
  return { booking, show, movie, screen };
}

async function loadBookings() {
  if (!requireLogin()) return;
  const list = qs('#booking-list');
  const message = qs('#page-message');
  const params = new URLSearchParams(window.location.search);
  const bookedId = params.get('booked') || params.get('bookingId') || params.get('id');

  try {
    const bookings = await API.get('/api/bookings');

    let ticketHtml = '';
    if (bookedId) {
      setMessage(message, `🎉 Booking #${bookedId} confirmed successfully! Here is your ticket.`, 'success');
      try {
        const full = await fetchFullBookingDetails(bookedId);
        ticketHtml = `<div class="ticket-container">${renderTicketCard(full.booking, full.show, full.movie, full.screen)}</div><hr style="margin: 30px 0; border: 0; border-top: 1px solid var(--line);">`;
      } catch (err) {
        // Continue if detail load fails
      }
    }

    if (!bookings.length && !ticketHtml) {
      list.innerHTML = '<p class="muted">You have no bookings yet.</p>';
      return;
    }

    const cardsHtml = bookings.map(booking => `<article class="booking-card"><div>
        <h3>Booking #${booking.bookingId}</h3>
        <p>Show #${booking.showId} &middot; ${(booking.seats || []).map(seat => `${escapeHtml(seat.rowLabel || '')}${seat.seatNumber}`).join(', ')}</p>
        <p>Booked ${booking.bookedAt ? new Date(booking.bookedAt).toLocaleString() : 'recently'}</p></div>
        <div><p class="status ${booking.status === 'CANCELLED' ? 'cancelled' : ''}">${escapeHtml(booking.status)}</p>
        <p><strong>₹${formatMoney(booking.totalAmount)}</strong></p>
        ${booking.status !== 'CANCELLED' && booking.status !== 'COMPLETED' ? `<button class="button button-small danger" data-cancel="${booking.bookingId}">Cancel</button>` : ''}</div></article>`).join('');

    list.innerHTML = ticketHtml + cardsHtml;
    list.querySelectorAll('[data-cancel]').forEach(button => button.addEventListener('click', () => cancelBooking(button.dataset.cancel)));
  } catch (error) { setMessage(message, error.message, 'error'); }
}

async function loadTicketView() {
  if (!requireLogin()) return;
  const container = qs('#ticket-container');
  const message = qs('#page-message');
  const params = new URLSearchParams(window.location.search);
  const bookingId = params.get('id') || params.get('bookingId') || params.get('booked');

  if (!bookingId) {
    setMessage(message, 'A Booking ID is required to view a ticket.', 'error');
    if (container) container.innerHTML = '<p class="muted"><a href="bookings.html">Go to My Bookings</a></p>';
    return;
  }

  try {
    const full = await fetchFullBookingDetails(bookingId);
    container.innerHTML = renderTicketCard(full.booking, full.show, full.movie, full.screen);
  } catch (error) {
    setMessage(message, error.message, 'error');
    if (container) container.innerHTML = '<p class="muted"><a href="bookings.html">Go to My Bookings</a></p>';
  }
}

async function cancelBooking(id) { if (!window.confirm('Cancel this booking?')) return; try { await API.remove(`/api/bookings/${id}`); window.location.reload(); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } }

const ADMIN_CONFIG = {
  movies: {
    title: 'Movies', endpoint: '/api/movies', id: 'movieId',

    /*Property: title
    Label: Title
    Input type: text
    Required: true-> requried false-> optional

    fields-> tells about the forms that is asked to add movie,threte..
    columns-> tells about the columns that is displayed in the table 
    */


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
  target.querySelector('form').addEventListener('submit', async event => { event.preventDefault(); const form = event.target; const values = Object.fromEntries(new FormData(form)); delete values.recordId;['movieId', 'theatreId', 'screenId', 'durationMinutes', 'capacity', 'seatNumber'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); });['regularPrice', 'premiumPrice', 'reclinerPrice'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); }); try { const id = form.elements.recordId.value; if (id) { await API.put(`${config.endpoint}/${id}`, values); } else { await API.post(config.endpoint, values); } await renderAdminResource(resource); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } });
}

async function deactivateResource(resource, id) { if (!window.confirm('Deactivate this resource?')) return; const config = ADMIN_CONFIG[resource]; try { await API.remove(`${config.endpoint}/${id}`); await renderAdminResource(resource); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } }

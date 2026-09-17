document.addEventListener('DOMContentLoaded', async () => {

    await loadCurrentUser();

    renderHeader();

    const page = document.body.dataset.page;

    const handlers = {
        home: loadHome,
        movies: loadMoviesPage,
        theatres: loadTheatresPage,
        theatre_details: loadTheatreDetails,
        login: setupLogin,
        settings: setupSettings,
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

    if (handlers[page]) {
        handlers[page]();
    }
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

  const params = new URLSearchParams(window.location.search);
  if (params.get('failed') === '1' || params.get('failure') === '1') {
    setMessage(message, 'Payment failed. Your booking was not completed. Please select your seats again.', 'error');
  } else if (params.get('cancelled') === '1' || params.get('cancel') === '1') {
    setMessage(message, 'Payment was cancelled. Your booking was not completed.', 'error');
  }

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

function theatreCard(theatre) {
  const createdDate = parseDateTime(theatre.createdAt);
  const estText = createdDate ? `Est. ${createdDate.getFullYear()}` : '';
  return `<article class="movie-card theatre-card">
  <div class="poster-placeholder theatre-poster-placeholder">
    <span class="theatre-badge-icon">🏛️</span>
    <span class="theatre-poster-title">${escapeHtml(theatre.name)}</span>
  </div>
  <div class="movie-card-body">
    <h3>${escapeHtml(theatre.name)}</h3>
    <p class="movie-meta theatre-meta">
      <span class="theatre-location-pin">📍</span> ${escapeHtml(theatre.location || 'Location unavailable')}
    </p>
    <div class="theatre-card-status">
      <span class="status-badge ${theatre.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">${escapeHtml(theatre.status || 'ACTIVE')}</span>
      ${estText ? `<span class="theatre-date">${escapeHtml(estText)}</span>` : ''}
    </div>
    <a class="button button-small" href="theatre-details.html?id=${theatre.theatreId}">View details</a>
  </div>
  </article>`;
}

async function getActiveTheatres(keyword = '') {
  const path = keyword ? `/api/theatres?keyword=${encodeURIComponent(keyword)}` : '/api/theatres';
  const theatres = await API.get(path);
  return (theatres || []).filter(theatre => !theatre.status || theatre.status === 'ACTIVE');
}

async function loadTheatresPage() {
  const list = qs('#theatre-list');
  const message = qs('#page-message');
  const form = qs('#theatre-search');

  const render = async keyword => {
    setMessage(message, 'Loading theatres...');
    try {
      const theatres = await getActiveTheatres(keyword);
      list.innerHTML = theatres.length ? theatres.map(theatreCard).join('') : '<p class="muted">No active theatres match your search.</p>';
      setMessage(message, '');
    } catch (error) {
      setMessage(message, error.message, 'error');
    }
  };

  if (form) {
    form.addEventListener('submit', event => {
      event.preventDefault();
      render(new FormData(form).get('keyword').trim());
    });
  }
  render('');
}

async function loadTheatreDetails() {
  const id = new URLSearchParams(window.location.search).get('id');
  const detail = qs('#theatre-detail');
  const screensList = qs('#theatre-screens-list');
  const message = qs('#page-message');

  if (!id) {
    setMessage(message, 'A theatre ID is required.', 'error');
    return;
  }

  setMessage(message, 'Loading theatre details...');
  try {
    const theatre = await API.get(`/api/theatres/${id}`);
    const createdDate = parseDateTime(theatre.createdAt);
    const createdDateStr = createdDate ? createdDate.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' }) : 'N/A';

    detail.innerHTML = `
      <div class="detail-poster theatre-detail-poster">
        <span class="theatre-hero-icon">🏛️</span>
        <span class="theatre-hero-title">${escapeHtml(theatre.name)}</span>
      </div>
      <div class="detail-copy">
        <p class="eyebrow">Theatre Details</p>
        <h1>${escapeHtml(theatre.name)}</h1>
        <p class="theatre-detail-address">
          <strong>Location:</strong> ${escapeHtml(theatre.location || 'Location not specified')}
        </p>
        <p class="movie-meta theatre-meta-details">
          <span><strong>Status:</strong> <span class="status-badge ${theatre.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">${escapeHtml(theatre.status || 'ACTIVE')}</span></span> &middot;
          <span><strong>Theatre ID:</strong> #${theatre.theatreId}</span> &middot;
          <span><strong>Registered:</strong> ${escapeHtml(createdDateStr)}</span>
        </p>
        <div class="theatre-quick-actions" style="margin-top: 20px;">
          <a class="button button-small" href="movies.html">Browse Movies</a>
        </div>
      </div>
    `;

    try {
      const allScreens = await API.get('/api/screens');
      const theatreScreens = (allScreens || []).filter(screen => String(screen.theatreId) === String(id));

      if (theatreScreens.length) {
        screensList.innerHTML = theatreScreens.map(screen => `
          <article class="show-card screen-card">
            <span class="eyebrow">${escapeHtml(screen.status || 'ACTIVE')}</span>
            <strong class="show-time screen-name-display">${escapeHtml(screen.name)}</strong>
            <small>Capacity: ${screen.capacity} seats</small>
            <span class="screen-badge">Screen #${screen.screenId}</span>
          </article>
        `).join('');
      } else {
        screensList.innerHTML = '<p class="muted">No screens are currently registered for this theatre.</p>';
      }
    } catch (screenError) {
      screensList.innerHTML = '<p class="muted">Screens information unavailable at this time.</p>';
    }

    setMessage(message, '');
  } catch (error) {
    setMessage(message, error.message, 'error');
  }
}

function setupLogin() {
    const loginSection = qs('#login-section');
    const form = qs('#login-form');
    const message = qs('#form-message');

    const totpSection = qs('#totp-section');
    const totpForm = qs('#totp-form');
    const totpCode = qs('#totp-code');
    const totpMessage = qs('#totp-message');
    const totpButton = qs('#totp-verify-button');
    const totpBackButton = qs('#totp-back-button');
    const recoveryActions = qs('#totp-recovery-actions');
    const recoveryEmailButton = qs('#totp-recovery-email-button');
    const recoveryPanel = qs('#totp-email-recovery');
    const recoverySendButton = qs('#totp-send-recovery-button');
    const recoveryForm = qs('#totp-recovery-form');
    const recoveryCode = qs('#totp-recovery-code');
    const recoveryVerifyButton = qs('#totp-recovery-verify-button');
    const recoverySetup = qs('#totp-recovery-setup');
    const recoverySecret = qs('#totp-recovery-secret');
    const recoverySetupForm = qs('#totp-recovery-setup-form');
    const recoverySetupCode = qs('#totp-recovery-setup-code');
    const recoverySetupButton = qs('#totp-recovery-setup-button');

    const params = new URLSearchParams(window.location.search);

    if (params.get('reset') === 'success') {
        setMessage(
            message,
            'Password reset successfully! Please log in with your new password.',
            'success'
        );
    } else if (params.get('registered') === '1') {
        setMessage(
            message,
            'Registration successful! Please log in.',
            'success'
        );
    }

    /*
     * STEP 1: Email + Password login
     */
    if (form) {
        form.addEventListener('submit', async event => {
            event.preventDefault();
            setMessage(message, 'Signing in...');

            const data = Object.fromEntries(new FormData(form));

            try {
                const result = await API.post('/api/auth/login', data);

                /*
                 * Case A: TOTP is enabled on this account.
                 * Password is valid, but TOTP verification is required.
                 */
                if (result.totpRequired) {
                    setMessage(message, '');
                    if (loginSection) loginSection.style.display = 'none';
                    if (totpSection) totpSection.style.display = 'block';

                    setMessage(
                        totpMessage,
                        'Password verified. Enter your 6-digit authenticator code.',
                        'success'
                    );

                    if (totpCode) {
                        totpCode.value = '';
                        totpCode.focus();
                    }
                    return;
                }

                /*
                 * Case B: TOTP is NOT enabled.
                 * Login is completely successful.
                 */
                currentUser = result.user;
                saveUser(result.user);
                if (result.user && result.user.userId) {
                    setTotpStatus(result.user.userId, 'DISABLED');
                }

                window.location.href = result.user && result.user.role === 'ADMIN'
                    ? 'admin.html'
                    : 'index.html';

            } catch (error) {
                setMessage(message, error.message, 'error');
            }
        });
    }

    /*
     * STEP 2: TOTP Verification
     */
    if (totpForm) {
        totpForm.addEventListener('submit', async event => {
            event.preventDefault();
            const code = totpCode ? totpCode.value.trim() : '';

            if (!/^\d{6}$/.test(code)) {
                setMessage(totpMessage, 'Enter a valid 6-digit numeric code.', 'error');
                return;
            }

            if (totpButton) totpButton.disabled = true;
            setMessage(totpMessage, 'Verifying authenticator code...');

            try {
                await API.post('/api/totp/login/verify', { code: code });

                // Promoted to authenticated session
                const user = await loadCurrentUser();
                if (user && user.userId) {
                    setTotpStatus(user.userId, 'ENABLED');
                    saveUser(user);
                }

                window.location.href = user && user.role === 'ADMIN'
                    ? 'admin.html'
                    : 'index.html';

            } catch (error) {
                setMessage(totpMessage, error.message || 'Invalid TOTP code.', 'error');
                if (totpButton) totpButton.disabled = false;
                if (totpCode) totpCode.focus();
            }
        });
    }

    /*
     * Back to Login button
     */
    if (totpBackButton) {
        totpBackButton.addEventListener('click', () => {
            setMessage(totpMessage, '');
            setMessage(message, '');
            if (totpSection) totpSection.style.display = 'none';
            if (loginSection) loginSection.style.display = 'block';
        });
    }

        if (recoveryEmailButton) {
          recoveryEmailButton.addEventListener('click', () => {
            if (recoveryPanel) recoveryPanel.style.display = 'block';
            if (recoveryActions) recoveryActions.style.display = 'none';
            setMessage(totpMessage, 'Choose email recovery to continue.');
          });
        }

        if (recoverySendButton) {
          recoverySendButton.addEventListener('click', async () => {
            recoverySendButton.disabled = true;
            setMessage(totpMessage, 'Sending recovery code...');
            try {
              await API.post('/api/totp/recovery/email/request');
              recoverySendButton.style.display = 'none';
              if (recoveryForm) recoveryForm.style.display = 'block';
              setMessage(totpMessage, 'Recovery code sent. Check your email.', 'success');
              if (recoveryCode) recoveryCode.focus();
            } catch (error) {
              setMessage(totpMessage, error.message, 'error');
              recoverySendButton.disabled = false;
            }
          });
        }

        if (recoveryForm) {
          recoveryForm.addEventListener('submit', async event => {
            event.preventDefault();
            const code = recoveryCode ? recoveryCode.value.trim() : '';
            if (!/^\d{6}$/.test(code)) {
              setMessage(totpMessage, 'Enter a valid 6-digit recovery code.', 'error');
              return;
            }

            if (recoveryVerifyButton) recoveryVerifyButton.disabled = true;
            setMessage(totpMessage, 'Verifying recovery code...');
            try {
              await API.post('/api/totp/recovery/email/verify', { otp: code });
              const response = await API.post('/api/totp/regenerate');
              if (recoveryForm) recoveryForm.style.display = 'none';
              if (recoverySetup) recoverySetup.style.display = 'block';
              if (recoverySecret) recoverySecret.textContent = response.secret;
              if (window.ScreenlyQR && response.otpauthUri) {
                ScreenlyQR.render('#totp-recovery-qr', response.otpauthUri, 180);
              }
              setMessage(totpMessage, 'Scan the new QR code and verify it below.', 'success');
              if (recoverySetupCode) recoverySetupCode.focus();
            } catch (error) {
              setMessage(totpMessage, error.message, 'error');
              if (recoveryVerifyButton) recoveryVerifyButton.disabled = false;
            }
          });
        }

        if (recoverySetupForm) {
          recoverySetupForm.addEventListener('submit', async event => {
            event.preventDefault();
            const code = recoverySetupCode ? recoverySetupCode.value.trim() : '';
            if (!/^\d{6}$/.test(code)) {
              setMessage(totpMessage, 'Enter a valid 6-digit authenticator code.', 'error');
              return;
            }

            if (recoverySetupButton) recoverySetupButton.disabled = true;
            setMessage(totpMessage, 'Activating your new authenticator...');
            try {
              await API.post('/api/totp/setup/verify', { code: code });
              const user = await loadCurrentUser();
              if (user && user.userId) {
                saveUser(user);
                setTotpStatus(user.userId, 'ENABLED');
              }
              window.location.href = user && user.role === 'ADMIN'
                ? 'admin.html'
                : 'index.html';
            } catch (error) {
              setMessage(totpMessage, error.message, 'error');
              if (recoverySetupButton) recoverySetupButton.disabled = false;
            }
          });
        }
}

async function setupSettings() {
    if (!requireLogin()) return;

    // 1. Load User Profile
    const user = currentUser || await loadCurrentUser();
    if (!user) {
        window.location.href = 'login.html';
        return;
    }

    const nameEl = qs('#user-display-name');
    const emailEl = qs('#user-display-email');
    const phoneEl = qs('#user-display-phone');
    const roleEl = qs('#user-display-role');
    const statusEl = qs('#user-display-status');
    const joinedEl = qs('#user-display-joined');
    const avatarEl = qs('#user-avatar-initial');

    if (nameEl) nameEl.textContent = user.name || 'User';
    if (emailEl) emailEl.textContent = user.email || '';
    if (phoneEl) phoneEl.textContent = user.phone || 'Not provided';
    if (roleEl) roleEl.textContent = user.role || 'CUSTOMER';
    if (statusEl) statusEl.textContent = user.status || 'ACTIVE';
    if (joinedEl) joinedEl.textContent = user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Recently';
    if (avatarEl) avatarEl.textContent = (user.name || 'U').charAt(0).toUpperCase();

    // 2. TOTP Interface Elements
    const badge = qs('#totp-status-badge');
    const alertMessage = qs('#totp-alert-message');

    const panelDisabled = qs('#panel-totp-disabled');
    const panelSetup = qs('#panel-totp-setup');
    const panelEnabled = qs('#panel-totp-enabled');
    const panelRegen = qs('#panel-totp-regenerated');
    const panelDisableConfirm = qs('#panel-totp-disable-confirm');

    const btnStartSetup = qs('#btn-start-setup');
    const btnCancelSetup = qs('#btn-cancel-setup');
    const formVerifySetup = qs('#form-verify-setup');
    const setupVerifyCode = qs('#setup-verify-code');
    const setupSecretCode = qs('#totp-setup-secret');
    const btnCopySecret = qs('#btn-copy-secret');

    const btnStartRegen = qs('#btn-start-regenerate');
    const regenSecretCode = qs('#totp-regen-secret');
    const btnCopyRegenSecret = qs('#btn-copy-regen-secret');
    const btnDoneRegen = qs('#btn-done-regenerate');

    const btnOpenDisable = qs('#btn-open-disable');
    const formDisableTotp = qs('#form-disable-totp');
    const disableTotpCode = qs('#disable-totp-code');
    const btnCancelDisable = qs('#btn-cancel-disable');

    function hideAllPanels() {
        [panelDisabled, panelSetup, panelEnabled, panelRegen, panelDisableConfirm].forEach(p => {
            if (p) p.style.display = 'none';
        });
    }

    function setBadgeState(state) {
        if (!badge) return;
        if (state === 'ENABLED') {
            badge.className = 'badge badge-success';
            badge.textContent = 'Active';
        } else {
            badge.className = 'badge badge-muted';
            badge.textContent = 'Disabled';
        }
    }

    function showEnabledView() {
        hideAllPanels();
        setBadgeState('ENABLED');
        setTotpStatus(user.userId, 'ENABLED');
        if (panelEnabled) panelEnabled.style.display = 'block';
    }

    function showDisabledView() {
        hideAllPanels();
        setBadgeState('DISABLED');
        setTotpStatus(user.userId, 'DISABLED');
        if (panelDisabled) panelDisabled.style.display = 'block';
    }

    // Determine initial state from cached status
    const cached = getTotpStatus(user.userId);
    if (cached === 'ENABLED') {
        showEnabledView();
    } else {
        showDisabledView();
    }

    // Copy-to-clipboard helper
    function setupCopyButton(btn, textGetter) {
        if (!btn) return;
        btn.addEventListener('click', async () => {
            const text = textGetter();
            if (!text || text === 'LOADING...') return;
            try {
                await navigator.clipboard.writeText(text);
                const originalText = btn.textContent;
                btn.textContent = 'Copied!';
                setTimeout(() => { btn.textContent = originalText; }, 1800);
            } catch (e) {
                prompt('Copy key manually:', text);
            }
        });
    }

    setupCopyButton(btnCopySecret, () => setupSecretCode ? setupSecretCode.textContent : '');
    setupCopyButton(btnCopyRegenSecret, () => regenSecretCode ? regenSecretCode.textContent : '');

    /*
     * FLOW 1: SETUP TOTP
     */
    if (btnStartSetup) {
        btnStartSetup.addEventListener('click', async () => {
            btnStartSetup.disabled = true;
            setMessage(alertMessage, 'Generating security keys...');

            try {
                const response = await API.post('/api/totp/setup');
                setMessage(alertMessage, '');
                hideAllPanels();
                if (panelSetup) panelSetup.style.display = 'block';

                if (setupSecretCode) setupSecretCode.textContent = response.secret;
                if (window.ScreenlyQR && response.otpauthUri) {
                    ScreenlyQR.render('#totp-setup-qr', response.otpauthUri, 180);
                }
                if (setupVerifyCode) {
                    setupVerifyCode.value = '';
                    setupVerifyCode.focus();
                }
            } catch (error) {
                // Self-healing: if backend tells us TOTP is already enabled, update the UI
                if (error.message && error.message.toLowerCase().includes('already enabled')) {
                    showEnabledView();
                    setMessage(alertMessage, 'Two-factor authentication is already active on this account.', 'success');
                } else {
                    setMessage(alertMessage, error.message, 'error');
                }
            } finally {
                btnStartSetup.disabled = false;
            }
        });
    }

    if (btnCancelSetup) {
        btnCancelSetup.addEventListener('click', () => {
            setMessage(alertMessage, '');
            showDisabledView();
        });
    }

    if (formVerifySetup) {
        formVerifySetup.addEventListener('submit', async event => {
            event.preventDefault();
            const code = setupVerifyCode ? setupVerifyCode.value.trim() : '';

            if (!/^\d{6}$/.test(code)) {
                setMessage(alertMessage, 'Please enter a valid 6-digit code.', 'error');
                return;
            }

            const submitBtn = qs('#btn-submit-setup-verify');
            if (submitBtn) submitBtn.disabled = true;
            setMessage(alertMessage, 'Verifying code and activating 2FA...');

            try {
                const response = await API.post('/api/totp/setup/verify', { code: code });
                setMessage(alertMessage, (response && response.message) || 'Two-factor authentication enabled successfully!', 'success');
                showEnabledView();
            } catch (error) {
                setMessage(alertMessage, error.message, 'error');
            } finally {
                if (submitBtn) submitBtn.disabled = false;
            }
        });
    }

    /*
     * FLOW 2: REGENERATE TOTP
     */
    if (btnStartRegen) {
        btnStartRegen.addEventListener('click', async () => {
            if (!confirm('Regenerating your 2FA secret will immediately invalidate your previous authenticator configuration. Do you wish to continue?')) {
                return;
            }

            btnStartRegen.disabled = true;
            setMessage(alertMessage, 'Regenerating TOTP secret...');

            try {
                const response = await API.post('/api/totp/regenerate');
                setMessage(alertMessage, '');
                hideAllPanels();
                if (panelRegen) panelRegen.style.display = 'block';

                if (regenSecretCode) regenSecretCode.textContent = response.secret;
                if (window.ScreenlyQR && response.otpauthUri) {
                    ScreenlyQR.render('#totp-regen-qr', response.otpauthUri, 180);
                }
            } catch (error) {
                // Self-healing: if backend says not enabled, reflect that in UI
                if (error.message && error.message.toLowerCase().includes('not currently enabled')) {
                    showDisabledView();
                    setMessage(alertMessage, 'Two-factor authentication is not currently enabled.', 'error');
                } else {
                    setMessage(alertMessage, error.message, 'error');
                }
            } finally {
                btnStartRegen.disabled = false;
            }
        });
    }

    if (btnDoneRegen) {
        btnDoneRegen.addEventListener('click', () => {
            setMessage(alertMessage, 'New 2FA credentials confirmed.', 'success');
            showEnabledView();
        });
    }

    /*
     * FLOW 3: DISABLE TOTP
     */
    if (btnOpenDisable) {
        btnOpenDisable.addEventListener('click', () => {
            setMessage(alertMessage, '');
            hideAllPanels();
            if (panelDisableConfirm) panelDisableConfirm.style.display = 'block';
            if (disableTotpCode) {
                disableTotpCode.value = '';
                disableTotpCode.focus();
            }
        });
    }

    if (btnCancelDisable) {
        btnCancelDisable.addEventListener('click', () => {
            setMessage(alertMessage, '');
            showEnabledView();
        });
    }

    if (formDisableTotp) {
        formDisableTotp.addEventListener('submit', async event => {
            event.preventDefault();
            const code = disableTotpCode ? disableTotpCode.value.trim() : '';

            if (!/^\d{6}$/.test(code)) {
                setMessage(alertMessage, 'Please enter a valid 6-digit code.', 'error');
                return;
            }

            const confirmBtn = qs('#btn-confirm-disable');
            if (confirmBtn) confirmBtn.disabled = true;
            setMessage(alertMessage, 'Disabling two-factor authentication...');

            try {
                const response = await API.post('/api/totp/disable', { code: code });
                setMessage(alertMessage, (response && response.message) || 'Two-factor authentication disabled successfully.', 'success');
                showDisabledView();
            } catch (error) {
                setMessage(alertMessage, error.message, 'error');
            } finally {
                if (confirmBtn) confirmBtn.disabled = false;
            }
        });
    }
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

async function createRazorpayOrder(bookingId) {
  return await API.post(
    `/api/bookings/${bookingId}/razorpay_order`
  );
}

async function setupPayment() {
  if (!requireLogin()) return;

  const params = new URLSearchParams(window.location.search);
  let bookingId = params.get('bookingId') || params.get('id') || params.get('booking_id');
  if (bookingId === 'undefined' || bookingId === 'null') {
    bookingId = null;
  }

  const bookingIdElement = qs('#booking-id');
  const amountElement = qs('#booking-amount');
  const countdownElement = qs('#countdown');
  const payButton = qs('#pay-razorpay-btn') || qs('#payment-button');
  const cancelButton = qs('#cancel-payment-btn');
  const message = qs('#payment-message');
  const activeSection = qs('#payment-active-section');
  const timeoutSection = qs('#payment-timeout-section');
  const timeoutReason = qs('#timeout-reason-text');
  const reselectSeatsLink = qs('#reselect-seats-link');

  if (!bookingId) {
    setMessage(message, 'Booking reference is required. Please select your seats again.', 'error');
    if (payButton) payButton.disabled = true;
    if (cancelButton) cancelButton.disabled = true;
    return;
  }

  let timerExpired = false;
  let isModalOpen = false;
  let isPaymentProcessing = false;
  let paymentCompleted = false;
  let activeRazorpayInstance = null;
  let hardTimeoutId = null;

  function showTimeoutView(reasonText) {
    if (activeRazorpayInstance && typeof activeRazorpayInstance.close === 'function') {
      try { activeRazorpayInstance.close(); } catch (ignored) {}
    }
    if (activeSection) activeSection.style.display = 'none';
    if (timeoutSection) timeoutSection.style.display = 'block';
    if (timeoutReason && reasonText) {
      timeoutReason.textContent = reasonText;
    }
  }

  async function triggerTimeoutCancellation(reason) {
    if (paymentCompleted || isPaymentProcessing) return;
    showTimeoutView(reason || 'Your 2-minute payment window has expired and your held seats have been released.');
    try {
      await API.remove(`/api/bookings/${bookingId}`);
    } catch (ignored) {}
    sessionStorage.removeItem(`booking_timer_${bookingId}`);
    sessionStorage.removeItem(`booking_hold_${bookingId}`);
  }

  async function handleCancelPayment() {
    if (paymentCompleted) return;
    if (hardTimeoutId) clearTimeout(hardTimeoutId);
    if (payButton) payButton.disabled = true;
    if (cancelButton) cancelButton.disabled = true;
    setMessage(message, 'Cancelling booking and redirecting to home page...', 'error');
    try {
      await API.remove(`/api/bookings/${bookingId}`);
    } catch (e) {
      console.warn('Booking cancellation error:', e);
    }
    sessionStorage.removeItem(`booking_timer_${bookingId}`);
    sessionStorage.removeItem(`booking_hold_${bookingId}`);
    setTimeout(() => {
      window.location.href = 'index.html?cancelled=1';
    }, 1200);
  }

  async function handlePaymentFailure(failureReason) {
    if (paymentCompleted) return;
    if (hardTimeoutId) clearTimeout(hardTimeoutId);
    if (payButton) payButton.disabled = true;
    if (cancelButton) cancelButton.disabled = true;
    const desc = failureReason ? `: ${failureReason}` : '';
    setMessage(message, `Payment failed${desc}. Cancelling booking and returning to home...`, 'error');
    try {
      await API.remove(`/api/bookings/${bookingId}`);
    } catch (e) {
      console.warn('Booking cancellation error:', e);
    }
    sessionStorage.removeItem(`booking_timer_${bookingId}`);
    sessionStorage.removeItem(`booking_hold_${bookingId}`);
    setTimeout(() => {
      window.location.href = 'index.html?failed=1';
    }, 1500);
  }

  if (cancelButton) {
    cancelButton.addEventListener('click', handleCancelPayment);
  }

  try {
    const { booking, show, movie, screen } = await fetchFullBookingDetails(bookingId);

    if (!booking) {
      throw new Error('Booking could not be loaded.');
    }

    if (bookingIdElement) bookingIdElement.textContent = `#${booking.bookingId}`;
    if (amountElement) amountElement.textContent = formatMoney(booking.totalAmount);

    const movieElement = qs('#booking-movie');
    if (movieElement) {
      movieElement.textContent = movie ? movie.title : (show ? `Show #${show.showId}` : 'Movie');
    }

    const showDetailsElement = qs('#booking-show-details');
    if (showDetailsElement) {
      if (show) {
        const screenName = screen ? screen.name : `Screen ${show.screenId}`;
        const dateStr = formatDate(show.showDate);
        const timeStr = `${formatTime(show.startTime)} - ${formatTime(show.endTime)}`;
        showDetailsElement.textContent = `${screenName} · ${dateStr} · ${timeStr}`;
      } else {
        showDetailsElement.textContent = 'Show details unavailable';
      }
    }

    const seatsElement = qs('#booking-seats');
    if (seatsElement) {
      let seatText = '';
      if (Array.isArray(booking.seats) && booking.seats.length > 0) {
        seatText = booking.seats.map(s => {
          const row = s.rowLabel || '';
          const num = s.seatNumber || '';
          return (row || num) ? `${row}${num}` : `Seat #${s.showSeatId || ''}`;
        }).join(', ');
      }
      seatsElement.textContent = seatText || 'No seats recorded';
    }

    if (reselectSeatsLink && booking && booking.showId) {
      reselectSeatsLink.href = `seats.html?showId=${booking.showId}`;
      reselectSeatsLink.style.display = 'block';
    }

    if (booking.status === 'CONFIRMED' || booking.status === 'COMPLETED') {
      setMessage(message, 'This booking has already been confirmed. Opening ticket...', 'success');
      if (payButton) payButton.disabled = true;
      if (cancelButton) cancelButton.disabled = true;
      setTimeout(() => {
        window.location.href = `booking.html?bookingId=${bookingId}`;
      }, 1000);
      return;
    }

    if (booking.status === 'CANCELLED') {
      showTimeoutView('This booking has expired or was cancelled. Your held seats have been released.');
      return;
    }

    // 2-minute active visible payment window (120 seconds)
    const savedTimer = sessionStorage.getItem(`booking_timer_${bookingId}`);
    let activeDeadline;
    if (savedTimer && !isNaN(Number(savedTimer)) && Number(savedTimer) > Date.now() - 3600000) {
      activeDeadline = Number(savedTimer);
    } else {
      activeDeadline = Date.now() + 2 * 60 * 1000;
    }
    sessionStorage.setItem(`booking_timer_${bookingId}`, String(activeDeadline));

    // Hard cutoff is 30 seconds after the 2-minute timer (at 2 minutes 30 seconds)
    const hardCutoffTime = activeDeadline + 30 * 1000;

    startPaymentCountdown(
      activeDeadline,
      countdownElement,
      payButton,
      message,
      () => {
        timerExpired = true;
        if (!isModalOpen) {
          // User was idle on the page and never opened Razorpay (or closed it before 2:00)
          triggerTimeoutCancellation('Payment time has expired (2 minutes limit). Cancelling booking and returning to home...');
        } else {
          // User is currently inside the Razorpay modal!
          // Give 30-second grace period for OTP authorization (until 2:30 hard cutoff).
          setMessage(message, 'Payment window closing soon. Please complete authorization in Razorpay.', 'warning');
        }
      }
    );

    // Schedule the hard cutoff at 2:30
    const msUntilHardCutoff = hardCutoffTime - Date.now();
    if (msUntilHardCutoff <= 0) {
      triggerTimeoutCancellation('Payment session timed out. Cancelling booking and returning to home...');
    } else {
      hardTimeoutId = setTimeout(() => {
        if (!paymentCompleted && !isPaymentProcessing) {
          triggerTimeoutCancellation('Payment session timed out (grace period ended). Cancelling booking and returning to home...');
        }
      }, msUntilHardCutoff);
    }

    async function launchRazorpay() {
      if (timerExpired) {
        setMessage(message, 'Payment time has expired. Please select your seats again.', 'error');
        return;
      }
      if (typeof Razorpay === 'undefined') {
        setMessage(message, 'Payment gateway is loading. Please try again in a moment.', 'error');
        return;
      }
      setMessage(message, 'Preparing secure Razorpay checkout...');
      if (payButton) payButton.disabled = true;

      try {
        const razorpayOrder = await createRazorpayOrder(bookingId);
        console.log('Razorpay order created:', razorpayOrder);

        const options = {
          key: razorpayOrder.keyId,
          amount: Math.round(Number(razorpayOrder.amount) * 100),
          currency: razorpayOrder.currency,
          name: 'Screenly Cinemas',
          description: `Movie booking #${bookingId}`,
          order_id: razorpayOrder.orderId,

          handler: async function (response) {
            isPaymentProcessing = true;
            console.log('Razorpay payment successful:', response);
            setMessage(message, 'Payment authorized! Verifying with server...', 'success');
            try {
              const verification = await API.post(
                `/api/bookings/${bookingId}/razorpay_verify`,
                {
                  razorpayPaymentId: response.razorpay_payment_id,
                  razorpayOrderId: response.razorpay_order_id,
                  razorpaySignature: response.razorpay_signature
                }
              );
              const verifiedBookingId = verification.bookingId;

              paymentCompleted = true;
              isPaymentProcessing = false;
              if (hardTimeoutId) clearTimeout(hardTimeoutId);
              sessionStorage.removeItem(`booking_timer_${bookingId}`);
              sessionStorage.removeItem(`booking_hold_${bookingId}`);
              setMessage(message, 'Payment verified successfully! Opening your ticket...', 'success');

              setTimeout(() => {
                window.location.href = `booking.html?bookingId=${verifiedBookingId}`;
              }, 1000);
            } catch (error) {
              isPaymentProcessing = false;
              console.error('Razorpay verification failed:', error);
              handlePaymentFailure(error.message || 'Payment verification failed.');
            }
          },

          modal: {
            ondismiss: function () {
              isModalOpen = false;
              if (paymentCompleted) return;
              console.log('Razorpay Checkout closed by user.');

              if (timerExpired) {
                triggerTimeoutCancellation('Payment time expired. Booking cancelled.');
              } else {
                setMessage(message, 'Payment window closed. Click "Pay with Razorpay" when ready to proceed before the timer runs out.', 'error');
                if (payButton && !timerExpired) {
                  payButton.disabled = false;
                }
              }
            }
          }
        };

        activeRazorpayInstance = new Razorpay(options);

        activeRazorpayInstance.on('payment.failed', function (resp) {
          isModalOpen = false;
          isPaymentProcessing = false;
          console.error('Razorpay payment failed:', resp.error);
          const errorDesc = resp.error && resp.error.description ? resp.error.description : 'Transaction failed';
          handlePaymentFailure(errorDesc);
        });

        isModalOpen = true;
        activeRazorpayInstance.open();
        setMessage(message, 'Complete payment in the Razorpay window.');
      } catch (error) {
        isModalOpen = false;
        isPaymentProcessing = false;
        console.error('Error starting Razorpay:', error);
        setMessage(message, error.message || 'Unable to open Razorpay payment gateway.', 'error');
        if (!timerExpired && !paymentCompleted && payButton) payButton.disabled = false;
      }
    }

    if (payButton) {
      payButton.addEventListener('click', launchRazorpay);
    }

  } catch (error) {
    setMessage(message, error.message, 'error');
    if (payButton) payButton.disabled = true;
    if (cancelButton) cancelButton.disabled = true;
  }
}

function startPaymentCountdown(deadline, countdownElement, button, message, onExpire) {
  const updateCountdown = () => {
    const remaining = deadline - Date.now();

    if (remaining <= 0) {
      if (countdownElement) {
        countdownElement.textContent = '00:00';
        countdownElement.classList.add('warning');
      }
      if (button) button.disabled = true;
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

    map.innerHTML = Object.entries(rows)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([row, rowSeats]) => `
        <div class="seat-row">
          <span class="row-label">${escapeHtml(row)}</span>
          ${rowSeats.sort((a, b) => a.seatNumber - b.seatNumber).map(seat => {
            const status = (seat.status || '').toUpperCase();
            const isAvailable = status === 'AVAILABLE';
            const isBooked = status === 'BOOKED';
            const isHeld = status === 'HELD' || status === 'HOLD';

            let statusClass = 'available';
            let labelSuffix = '';
            if (isBooked) {
              statusClass = 'booked';
              labelSuffix = ' (Booked)';
            } else if (isHeld) {
              statusClass = 'held';
              labelSuffix = ' (On Hold)';
            } else if (!isAvailable) {
              statusClass = 'booked';
              labelSuffix = ' (Unavailable)';
            }

            const disabledAttr = !isAvailable ? 'disabled' : '';
            return `<button class="seat ${statusClass}" data-seat-id="${seat.showSeatId}" data-price="${seat.price}" ${disabledAttr} aria-label="Row ${escapeHtml(row)} seat ${seat.seatNumber}${labelSuffix}">${seat.seatNumber}</button>`;
          }).join('')}
        </div>`
      ).join('');

    map.querySelectorAll('.seat.available:not([disabled])').forEach(seat => seat.addEventListener('click', () => {
      const seatId = Number(seat.dataset.seatId);
      if (selected.has(seatId)) {
        selected.delete(seatId);
        seat.classList.remove('selected');
      }
      else {
        selected.set(seatId, Number(seat.dataset.price));
        seat.classList.add('selected');
      }
      updateSummary();
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
      const bookingId = result && (result.bookingId ?? result.bookingId ?? result.booking_id ?? result.id);
      const parsedHold = result && result.holdUntil ? parseDateTime(result.holdUntil) : null;
      const holdTime = parsedHold ? parsedHold.getTime() : (Date.now() + 2 * 60 * 1000);
      if (bookingId) {
        sessionStorage.setItem(`booking_hold_${bookingId}`, String(holdTime));
        window.location.href = `payment.html?bookingId=${bookingId}`;
      } else {
        throw new Error('Booking created but reference ID is missing.');
      }
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
          <span class="ticket-field-value">${booking.bookedAt ? (parseDateTime(booking.bookedAt) ? parseDateTime(booking.bookedAt).toLocaleDateString() : new Date(booking.bookedAt).toLocaleDateString()) : 'Today'}</span>
        </div>
      </div>
      ${booking.ticketCode && booking.ticketStatus === 'VALID' ? `
      <div class="ticket-qr-section">
        <div id="ticket-qr-${booking.bookingId}" class="ticket-qr"></div>
        <span class="muted ticket-qr-caption">Scan this code at the cinema entrance.</span>
      </div>` : ''}
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

function renderTicketQr(booking) {
  if (!booking || !booking.ticketCode || booking.ticketStatus !== 'VALID'
      || !window.ScreenlyQR) {
    return;
  }

  ScreenlyQR.render(
    `#ticket-qr-${booking.bookingId}`,
    booking.ticketCode,
    180
  );
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
        list.innerHTML = ticketHtml;
        renderTicketQr(full.booking);
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
        <p>Booked ${booking.bookedAt ? (parseDateTime(booking.bookedAt) ? parseDateTime(booking.bookedAt).toLocaleString() : new Date(booking.bookedAt).toLocaleString()) : 'recently'}</p></div>
        <div><p class="status ${booking.status === 'CANCELLED' ? 'cancelled' : ''}">${escapeHtml(booking.status)}</p>
        <p><strong>₹${formatMoney(booking.totalAmount)}</strong></p>
        <div style="display: flex; gap: 8px; justify-content: flex-end; align-items: center; flex-wrap: wrap;">
          <a class="button button-small button-light" href="booking.html?bookingId=${booking.bookingId}">View Ticket</a>
          ${booking.status !== 'CANCELLED' && booking.status !== 'COMPLETED' ? `<button class="button button-small danger" data-cancel="${booking.bookingId}">Cancel</button>` : ''}
        </div></div></article>`).join('');

    list.innerHTML = ticketHtml + cardsHtml;
    if (bookedId) {
      const ticket = document.getElementById(`confirmed-ticket-${bookedId}`);
      if (ticket) {
        const booking = bookings.find(item => String(item.bookingId) === String(bookedId));
        if (booking) renderTicketQr(booking);
      }
    }
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
    renderTicketQr(full.booking);
    setMessage(message, `🎉 Booking #${bookingId} confirmed successfully! Here is your ticket.`, 'success');
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
  setupAdminTicketCheckin();
  const tabs = qs('#admin-tabs');
  tabs.addEventListener('click', event => { const tab = event.target.closest('[data-resource]'); if (!tab) return; tabs.querySelectorAll('.tab').forEach(item => item.classList.toggle('active', item === tab)); renderAdminResource(tab.dataset.resource); });
  renderAdminResource('movies');
}

function setupAdminTicketCheckin() {
  const form = qs('#ticket-checkin-form');
  const input = qs('#ticket-checkin-code');
  const button = qs('#ticket-checkin-button');
  const message = qs('#ticket-checkin-message');
  if (!form || !input || !button || !message) return;

  form.addEventListener('submit', async event => {
    event.preventDefault();
    const ticketCode = input.value.trim();
    if (!ticketCode) {
      setMessage(message, 'Enter a ticket code.', 'error');
      return;
    }

    button.disabled = true;
    setMessage(message, 'Checking ticket...');
    try {
      const result = await API.post('/api/admin/tickets/checkin', { ticketCode });
      setMessage(message, `${result.message} ${result.bookingId ? `Booking #${result.bookingId}.` : ''}`, 'success');
      input.value = '';
      input.focus();
    } catch (error) {
      setMessage(message, error.message || 'Ticket could not be verified.', 'error');
      input.select();
    } finally {
      button.disabled = false;
    }
  });
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
    const timeVal = type === 'time' ? (formatTime(value) === 'Time unavailable' ? '' : formatTime(value)) : value;
    return `<label>${label}<input name="${name}" type="${type}" value="${escapeHtml(timeVal)}" ${required ? 'required' : ''} ${type === 'number' ? 'min="0" step="0.01"' : ''}></label>`;
  }));
  return fields.join('');
}

async function renderAdminResource(resource) {
  const config = ADMIN_CONFIG[resource]; const workspace = qs('#admin-workspace'); const message = qs('#page-message');
  workspace.innerHTML = '<p class="muted">Loading resource...</p>';
  try { const items = resource === 'seats' ? await loadAllSeats() : await API.get(config.endpoint); const rows = items || []; workspace.innerHTML = `<div class="admin-toolbar"><h2>${config.title}</h2><button class="button button-small" data-new-resource>New ${config.title.slice(0, -1)}</button></div><div data-admin-form></div><table class="admin-table"><thead><tr>${config.columns.map(([, label]) => `<th>${label}</th>`).join('')}<th>Actions</th></tr></thead><tbody>${rows.map(item => `<tr>${config.columns.map(([key]) => { let cellVal = item[key]; if (key === 'startTime' || key === 'endTime') cellVal = formatTime(cellVal); else if (key === 'showDate') cellVal = formatDate(cellVal); return `<td>${escapeHtml(cellVal)}</td>`; }).join('')}<td><div class="admin-actions"><button class="button button-small" data-edit="${item[config.id]}">Edit</button><button class="button button-small danger" data-delete="${item[config.id]}">Deactivate</button></div></td></tr>`).join('')}</tbody></table>`; workspace.querySelector('[data-new-resource]').addEventListener('click', () => openAdminForm(resource)); workspace.querySelectorAll('[data-edit]').forEach(button => button.addEventListener('click', () => openAdminForm(resource, rows.find(item => String(item[config.id]) === button.dataset.edit)))); workspace.querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click', () => deactivateResource(resource, button.dataset.delete))); setMessage(message, rows.length ? '' : `No ${config.title.toLowerCase()} found.`); } catch (error) { setMessage(message, error.message, 'error'); workspace.innerHTML = ''; }
}

async function loadAllSeats() {
  const screens = await API.get('/api/screens'); const result = [];
  for (const screen of screens || []) { const seats = await API.get(`/api/seats/screen/${screen.screenId}`); result.push(...(seats || [])); }
  return result;
}

async function openAdminForm(resource, item = null) {
  const config = ADMIN_CONFIG[resource]; const target = qs('[data-admin-form]');
  const formConfig = item && resource === 'shows' ? { ...config, fields: config.fields.filter(([name]) => !['regularPrice', 'premiumPrice', 'reclinerPrice'].includes(name)) } : config;
  target.innerHTML = `<div data-form-message class="message" role="alert"></div><form class="admin-form"><input type="hidden" name="recordId" value="${item ? item[config.id] : ''}">${await formMarkup(formConfig, item || {})}<div class="form-actions"><button class="button" type="submit">${item ? 'Save changes' : 'Create'}</button><button class="button button-small danger" type="button" data-close-form>Close</button></div></form>`;
  target.querySelector('[data-close-form]').addEventListener('click', () => { target.innerHTML = ''; });
  target.querySelector('form').addEventListener('submit', async event => { event.preventDefault(); const form = event.target; const formMessage = target.querySelector('[data-form-message]'); const values = Object.fromEntries(new FormData(form)); delete values.recordId;['movieId', 'theatreId', 'screenId', 'durationMinutes', 'capacity', 'seatNumber'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); });['regularPrice', 'premiumPrice', 'reclinerPrice'].forEach(key => { if (values[key] !== undefined) values[key] = Number(values[key]); }); setMessage(formMessage, ''); try { const id = form.elements.recordId.value; if (id) { await API.put(`${config.endpoint}/${id}`, values); } else { await API.post(config.endpoint, values); } await renderAdminResource(resource); } catch (error) { setMessage(formMessage, error.message, 'error'); } });
}

async function deactivateResource(resource, id) { if (!window.confirm('Deactivate this resource?')) return; const config = ADMIN_CONFIG[resource]; try { await API.remove(`${config.endpoint}/${id}`); await renderAdminResource(resource); } catch (error) { setMessage(qs('#page-message'), error.message, 'error'); } }

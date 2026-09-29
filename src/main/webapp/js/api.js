const API_CONTEXT_PATH = window.location.pathname.startsWith('/movie_booking/')
  ? '/movie_booking'
  : '';

const API = {
  async request(path, options = {}) {
    const requestPath = path.startsWith('/api/')
      ? `${API_CONTEXT_PATH}${path}`
      : path;
    const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
    const defaultHeaders = isFormData ? {} : { 'Content-Type': 'application/json' };
    const headers = { ...defaultHeaders, ...(options.headers || {}) };
    if (isFormData && headers['Content-Type']) {
      delete headers['Content-Type'];
    }
    const response = await fetch(requestPath, {
      credentials: 'same-origin',
      headers,
      ...options
    });
    const text = await response.text();
    let body = null;
    try { body = text ? JSON.parse(text) : null; } catch (error) { body = null; }
    if (!response.ok) {
      const message = body && body.message ? body.message : `Request failed (${response.status}).`;
      const error = new Error(message);
      error.status = response.status;
      error.data = body;
      throw error;
    }
    return body;
  },
  get(path) { return this.request(path); },
  send(path, method, data) {
    const isFormData = typeof FormData !== 'undefined' && data instanceof FormData;
    return this.request(path, {
      method,
      body: data === undefined ? undefined : (isFormData ? data : JSON.stringify(data))
    });
  },
  post(path, data) { return this.send(path, 'POST', data); },
  put(path, data) { return this.send(path, 'PUT', data); },
  remove(path) { return this.send(path, 'DELETE'); }
};

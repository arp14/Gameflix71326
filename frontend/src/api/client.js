const TOKEN_KEY = 'gameflix.token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`/api${path}`, { ...options, headers });
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    const message = (body && body.message) || `Request failed (${response.status})`;
    throw new Error(message);
  }

  return body;
}

export function get(path) {
  return request(path, { method: 'GET' });
}

export function post(path, data) {
  return request(path, { method: 'POST', body: JSON.stringify(data) });
}

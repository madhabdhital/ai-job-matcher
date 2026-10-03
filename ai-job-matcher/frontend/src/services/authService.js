const API_URL = '/api/v1/auth'

// The backend returns either { message: "text" } or, for validation
// failures, { message: { fieldName: "text", ... } }. Turn both into one string.
function extractMessage(data, fallback) {
  if (!data || !data.message) return fallback
  if (typeof data.message === 'string') return data.message
  return Object.values(data.message).join(' ') || fallback
}

async function post(path, body, fallback) {
  let response

  try {
    response = await fetch(`${API_URL}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  } catch {
    throw new Error('Cannot reach the server. Is the backend running on port 8080?')
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(extractMessage(data, fallback))
  }

  return data
}

export function loginUser(email, password) {
  return post('/login', { email, password }, 'Login failed')
}

export function registerUser(fullName, email, password) {
  return post('/register', { fullName, email, password }, 'Registration failed')
}

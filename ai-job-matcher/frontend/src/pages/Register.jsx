import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registerUser } from '../services/authService'
import './Login.css'

const PASSWORD_RULE =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,100}$/

function Register() {
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const navigate = useNavigate()

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    if (fullName.trim().length < 2) {
      setError('Full name must be at least 2 characters.')
      return
    }

    if (!PASSWORD_RULE.test(password)) {
      setError(
        'Password needs 8+ characters with an uppercase letter, a lowercase letter, a number and one of @ $ ! % * ? &'
      )
      return
    }

    setLoading(true)

    try {
      const data = await registerUser(fullName.trim(), email.trim(), password)

      // The backend returns a token on registration, so log the user straight in.
      localStorage.setItem('token', data.token)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="auth-logo">AJ</span>
          <h1>Create your account</h1>
          <p>Match your resume to jobs with AI in minutes.</p>
        </div>

        {error && <p className="error-message" role="alert">{error}</p>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              type="text"
              value={fullName}
              onChange={(event) => setFullName(event.target.value)}
              placeholder="Madhab Sharma"
              autoComplete="name"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Create a strong password"
              autoComplete="new-password"
              required
            />
            <small className="field-hint">
              8+ characters, with upper &amp; lower case, a number and a symbol (@ $ ! % * ? &amp;).
            </small>
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  )
}

export default Register

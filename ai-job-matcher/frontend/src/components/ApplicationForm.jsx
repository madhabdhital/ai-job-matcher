import { useState } from 'react'
import { createApplication } from '../services/applicationService'

function ApplicationForm({ onApplicationCreated }) {
  const [formData, setFormData] = useState({
    jobTitle: '',
    company: '',
    applyUrl: '',
    status: 'APPLIED',
    notes: '',
  })

  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target

    setFormData((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()

    setError('')
    setLoading(true)

    try {
      const application = await createApplication(formData)

      onApplicationCreated(application)

      setFormData({
        jobTitle: '',
        company: '',
        applyUrl: '',
        status: 'APPLIED',
        notes: '',
      })
    } catch (error) {
      setError(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="application-form">

      <h2>Add Application</h2>

      {error && (
        <p className="error-message">
          {error}
        </p>
      )}

      <form onSubmit={handleSubmit}>

        <input
          type="text"
          name="jobTitle"
          placeholder="Job title"
          value={formData.jobTitle}
          onChange={handleChange}
          required
        />

        <input
          type="text"
          name="company"
          placeholder="Company"
          value={formData.company}
          onChange={handleChange}
        />

        <input
          type="url"
          name="applyUrl"
          placeholder="Job URL"
          value={formData.applyUrl}
          onChange={handleChange}
        />

        <select
          name="status"
          value={formData.status}
          onChange={handleChange}
        >
          <option value="APPLIED">Applied</option>
          <option value="INTERVIEWING">Interviewing</option>
          <option value="OFFER">Offer</option>
          <option value="REJECTED">Rejected</option>
        </select>

        <textarea
          name="notes"
          placeholder="Notes"
          value={formData.notes}
          onChange={handleChange}
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Adding...' : 'Add Application'}
        </button>

      </form>

    </div>
  )
}

export default ApplicationForm
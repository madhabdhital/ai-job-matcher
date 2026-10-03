import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "./Applications.css";

function Applications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadApplications();
  }, []);

  async function loadApplications() {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        "/api/v1/applications",
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to load applications");
      }

      const data = await response.json();

      setApplications(data);
    } catch (error) {
      console.error(error);
      setError("Unable to load applications.");
    } finally {
      setLoading(false);
    }
  }

  async function updateStatus(applicationId, newStatus) {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/v1/applications/${applicationId}/status?status=${newStatus}`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || "Failed to update status"
        );
      }

      await loadApplications();

    } catch (error) {
      console.error(error);

      alert(
        error.message ||
          "Failed to update status"
      );
    }
  }

  async function deleteApplication(applicationId) {
    const confirmed = window.confirm(
      "Are you sure you want to remove this application?"
    );

    if (!confirmed) {
      return;
    }

    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/v1/applications/${applicationId}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message ||
            "Failed to delete application"
        );
      }

      setApplications((previous) =>
        previous.filter(
          (application) =>
            application.id !== applicationId
        )
      );

    } catch (error) {
      console.error(error);

      alert(
        error.message ||
          "Failed to remove application"
      );
    }
  }

  function getStatusClass(status) {
    switch (status) {
      case "APPLIED":
        return "status-applied";

      case "INTERVIEWING":
        return "status-interviewing";

      case "SELECTED":
        return "status-selected";

      case "REJECTED":
        return "status-rejected";

      default:
        return "";
    }
  }

  function formatStatus(status) {
    if (!status) {
      return "Unknown";
    }

    return status.charAt(0) +
      status.slice(1).toLowerCase();
  }

  if (loading) {
    return (
      <>
        <Navbar />

        <main className="applications-page">
          <div className="applications-container">
            <p className="page-message">
              Loading applications...
            </p>
          </div>
        </main>
      </>
    );
  }

  if (error) {
    return (
      <>
        <Navbar />

        <main className="applications-page">
          <div className="applications-container">
            <p className="page-message error">
              {error}
            </p>
          </div>
        </main>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main className="applications-page">

        <div className="applications-container">

          <div className="applications-header">

            <div>
              <h1>My Applications</h1>

              <p>
                Track and manage the jobs you have
                applied for.
              </p>
            </div>

            <div className="application-count">
              {applications.length}{" "}
              {applications.length === 1
                ? "Application"
                : "Applications"}
            </div>

          </div>

          {applications.length === 0 ? (
            <div className="empty-state">

              <h2>No applications yet</h2>

              <p>
                Jobs you track from the dashboard
                will appear here.
              </p>

              <Link
                to="/dashboard"
                className="browse-jobs-button"
              >
                Browse Jobs
              </Link>

            </div>
          ) : (
            <div className="applications-list">

              {applications.map(
                (application) => (

                  <div
                    className="application-card"
                    key={application.id}
                  >

                    <div className="application-main">

                      <div className="application-title-section">

                        <h2>
                          {application.jobTitle ||
                            "Job"}
                        </h2>

                        <p className="company-name">
                          {application.company ||
                            "Company not specified"}
                        </p>

                      </div>

                      <span
                        className={`status-badge ${getStatusClass(
                          application.status
                        )}`}
                      >
                        {formatStatus(
                          application.status
                        )}
                      </span>

                    </div>

                    <div className="application-details">

                      <div className="detail-item">

                        <span className="detail-label">
                          Location
                        </span>

                        <span className="detail-value">
                          {application.location ||
                            "N/A"}
                        </span>

                      </div>

                      <div className="detail-item">

                        <span className="detail-label">
                          Applied
                        </span>

                        <span className="detail-value">
                          {application.appliedDate
                            ? new Date(
                                application.appliedDate
                              ).toLocaleDateString()
                            : "N/A"}
                        </span>

                      </div>

                    </div>

                    <div className="application-footer">

                      <div className="status-section">

                        <label htmlFor={`status-${application.id}`}>
                          Update Status
                        </label>

                        <select
                          id={`status-${application.id}`}
                          value={
                            application.status
                          }
                          onChange={(event) =>
                            updateStatus(
                              application.id,
                              event.target.value
                            )
                          }
                        >
                          <option value="APPLIED">
                            Applied
                          </option>

                          <option value="INTERVIEWING">
                            Interviewing
                          </option>

                          <option value="SELECTED">
                            Selected
                          </option>

                          <option value="REJECTED">
                            Rejected
                          </option>
                        </select>

                      </div>

                      <div className="application-actions">

                        <Link
                          to={`/jobs/${application.jobId}`}
                          className="view-job-button"
                        >
                          View Job
                        </Link>

                        <button
                          type="button"
                          className="remove-button"
                          onClick={() =>
                            deleteApplication(
                              application.id
                            )
                          }
                        >
                          Remove
                        </button>

                      </div>

                    </div>

                  </div>

                )
              )}

            </div>
          )}

        </div>

      </main>
    </>
  );
}

export default Applications;
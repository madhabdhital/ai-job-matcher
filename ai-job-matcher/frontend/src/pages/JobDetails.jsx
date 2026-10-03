import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import "./JobDetails.css";
import Navbar from "../components/Navbar";
import JobDescription from "../components/JobDescription";

function JobDetails() {
  const { jobId } = useParams();
  const navigate = useNavigate();

  const [job, setJob] = useState(null);
  const [alreadySaved, setAlreadySaved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadJob();
    checkApplication();
  }, [jobId]);

  async function loadJob() {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/v1/jobs/${jobId}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to load job");
      }

      const data = await response.json();

      setJob(data);

    } catch (error) {
      console.error(error);
      setError("Unable to load job details.");
    } finally {
      setLoading(false);
    }
  }

  async function checkApplication() {
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
        return;
      }

      const applications = await response.json();

      // Saved application response contains jobId directly
      const exists = applications.some(
        (application) =>
          Number(application.jobId) === Number(jobId)
      );

      setAlreadySaved(exists);

    } catch (error) {
      console.error(
        "Failed to check application status",
        error
      );
    }
  }

  async function saveApplication() {
    if (alreadySaved) {
      return;
    }

    try {
      setSaving(true);

      const token = localStorage.getItem("token");

      const response = await fetch(
        "/api/v1/applications",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            jobId: Number(jobId),
            notes: "",
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message ||
            "Failed to save application"
        );
      }

      setAlreadySaved(true);

      alert(
        "Application saved successfully!"
      );

    } catch (error) {
      console.error(error);

      alert(
        error.message ||
          "Failed to save application"
      );

      // Re-check in case backend says
      // application was already tracked
      await checkApplication();

    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />

        <div className="job-details-page">
          <h2>Loading job details...</h2>
        </div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <Navbar />

        <div className="job-details-page">
          <h2>{error}</h2>
        </div>
      </>
    );
  }

  if (!job) {
    return (
      <>
        <Navbar />

        <div className="job-details-page">
          <h2>Job not found.</h2>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <div className="job-details-page">

        <Link
          to="/dashboard"
          className="back-link"
        >
          ← Back to Jobs
        </Link>

        <div className="job-details-card">

          <div className="job-header">

            <h1>{job.title}</h1>

            <p className="company">
              {job.company ||
                "Company not specified"}
            </p>

          </div>

          <div className="job-info">

            <div>
              <strong>Location</strong>
              <span>
                {job.location || "N/A"}
              </span>
            </div>

            <div>
              <strong>Job Type</strong>
              <span>
                {job.jobType || "N/A"}
              </span>
            </div>

            <div>
              <strong>Work Mode</strong>
              <span>
                {job.workMode || "N/A"}
              </span>
            </div>

            <div>
              <strong>Source</strong>
              <span>
                {job.source || "N/A"}
              </span>
            </div>

          </div>

          <hr />

          <section className="description-section">

            <h2>Job Description</h2>

          <div className="job-description">
              <JobDescription html={job.description} />
          </div>

          </section>

          <div className="job-actions">

            {/* Apply */}
            <a
              href={job.applyUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="action-button apply-button"
            >
              Apply Now
            </a>

            {/* Analyze */}
            <button
              type="button"
              className="action-button analyze-button"
              onClick={() =>
                navigate(
                  `/jobs/${job.id}/analysis`
                )
              }
            >
              Analyze Match
            </button>

            {/* Track */}
            <button
              type="button"
              className="action-button track-button"
              onClick={saveApplication}
              disabled={
                alreadySaved || saving
              }
            >
              {alreadySaved
                ? "Already Saved"
                : saving
                ? "Saving..."
                : "Save Application"}
            </button>

          </div>

        </div>

      </div>
    </>
  );
}

export default JobDetails;
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { getJobs, getRecommendedJobs } from "../services/jobService";
import Navbar from "../components/Navbar";
import "./Dashboard.css";

const PAGE_SIZE = 12;

const JOB_TYPES = [
  { value: "", label: "All types" },
  { value: "INTERNSHIP", label: "Internship" },
  { value: "FULL_TIME", label: "Full time" },
  { value: "CONTRACT", label: "Contract" },
];

function Dashboard() {
  const loggedIn = Boolean(localStorage.getItem("token"));

  const [searchText, setSearchText] = useState("");
  const [query, setQuery] = useState("");
  const [type, setType] = useState("");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);

  const timer = useRef(null);

  // The result remembers which request it belongs to, so "loading" is simply
  // "the stored result is for an older request".
  const requestKey = `${query}|${type}|${page}`;
  const loading = !result || result.key !== requestKey;

  useEffect(() => {
    let active = true;

    const options = { q: query, type, page, size: PAGE_SIZE };
    const request = loggedIn ? getRecommendedJobs(options) : getJobs(options);

    request
      .then((data) => {
        if (active) {
          setResult({ key: requestKey, ...data, error: "", needsResume: false });
        }
      })
      .catch((error) => {
        if (!active) return;

        const needsResume = loggedIn && /resume/i.test(error.message);

        setResult({
          key: requestKey,
          jobs: [],
          totalPages: 0,
          totalElements: 0,
          error: needsResume ? "" : "Unable to load jobs.",
          needsResume,
        });
      });

    return () => {
      active = false;
    };
  }, [query, type, page, requestKey, loggedIn]);

  useEffect(() => () => clearTimeout(timer.current), []);

  function handleSearchChange(event) {
    const value = event.target.value;

    setSearchText(value);
    clearTimeout(timer.current);

    // Wait until the user stops typing before asking the server.
    timer.current = setTimeout(() => {
      setQuery(value.trim());
      setPage(0);
    }, 400);
  }

  function handleTypeChange(event) {
    setType(event.target.value);
    setPage(0);
  }

  const jobs = result ? result.jobs : [];
  const totalPages = result ? result.totalPages : 0;
  const totalElements = result ? result.totalElements : 0;

  return (
    <>
      <Navbar />
      <main className="dashboard-page">
        <div className="dashboard-container">
          <div className="dashboard-header">
            <div>
              <h1>{loggedIn ? "Jobs For You" : "Job Opportunities"}</h1>
              <p>
                {loggedIn
                  ? "Jobs and internships matched to your resume domain."
                  : "Browse available jobs and internships. Login to view details."}
              </p>
            </div>
            {!loading && !result.needsResume && (
              <div className="job-count">
                {totalElements} {totalElements === 1 ? "Job" : "Jobs"}
              </div>
            )}
          </div>

          {!(result && result.needsResume) && (
            <div className="jobs-toolbar">
              <input
                type="search"
                className="search-input"
                placeholder="Search by title or company"
                value={searchText}
                onChange={handleSearchChange}
                aria-label="Search jobs"
              />

              <select
                className="type-select"
                value={type}
                onChange={handleTypeChange}
                aria-label="Filter by job type"
              >
                {JOB_TYPES.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          )}

          {loading && <p className="dashboard-message">Loading jobs...</p>}

          {!loading && result.error && (
            <p className="dashboard-message error">{result.error}</p>
          )}

          {!loading && result.needsResume && (
            <div className="empty-jobs">
              <h2>Upload your resume to find matching opportunities</h2>
              <p>
                We use your resume to detect your domain and show relevant jobs
                and internships.
              </p>
              <Link to="/resume" className="applications-button">
                Upload My Resume
              </Link>
            </div>
          )}

          {!loading && !result.error && !result.needsResume && jobs.length === 0 && (
            <div className="empty-jobs">
              <h2>No matching opportunities found</h2>
              <p>Try a different search or filter, or check again later.</p>
            </div>
          )}

          {!loading && jobs.length > 0 && (
            <>
              <div className="jobs-grid">
                {jobs.map((job) => (
                  <div className="job-card" key={job.id}>
                    <div className="job-card-header">
                      <div>
                        <h2>{job.title}</h2>
                        <p className="job-company">
                          {job.company || "Company not specified"}
                        </p>
                      </div>
                    </div>

                    <div className="job-details">
                      <div className="job-detail">
                        <span className="detail-label">Location</span>
                        <span className="detail-value">{job.location || "N/A"}</span>
                      </div>
                      <div className="job-detail">
                        <span className="detail-label">Job Type</span>
                        <span className="detail-value">{job.jobType || "N/A"}</span>
                      </div>
                      <div className="job-detail">
                        <span className="detail-label">Work Mode</span>
                        <span className="detail-value">{job.workMode || "N/A"}</span>
                      </div>
                      <div className="job-detail">
                        <span className="detail-label">Domain</span>
                        <span className="detail-value">{job.domain || "General"}</span>
                      </div>
                      <div className="job-detail">
                        <span className="detail-label">Source</span>
                        <span className="detail-value">{job.source || "N/A"}</span>
                      </div>
                    </div>

                    <div className="job-card-footer">
                      <Link to={`/jobs/${job.id}`} className="view-details-button">
                        View Details
                      </Link>
                    </div>
                  </div>
                ))}
              </div>

              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    type="button"
                    className="page-button"
                    onClick={() => setPage((current) => current - 1)}
                    disabled={page === 0}
                  >
                    Previous
                  </button>

                  <span className="page-info">
                    Page {page + 1} of {totalPages}
                  </span>

                  <button
                    type="button"
                    className="page-button"
                    onClick={() => setPage((current) => current + 1)}
                    disabled={page + 1 >= totalPages}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>
    </>
  );
}

export default Dashboard;
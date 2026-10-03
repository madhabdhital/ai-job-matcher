
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  analyzeJob,
  getJobAnalysis,
} from "../services/jobAnalysisService";
import "./JobAnalysis.css";
import Navbar from "../components/Navbar";

function JobAnalysis() {
  const { jobId } = useParams();

  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadAnalysis();
  }, [jobId]);

  async function loadAnalysis() {
    try {
      const data = await getJobAnalysis(jobId);
      setAnalysis(data);
    } catch (error) {
      setAnalysis(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleAnalyze() {
    try {
      setAnalyzing(true);
      setError("");

      const data = await analyzeJob(jobId);

      setAnalysis(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setAnalyzing(false);
    }
  }

  function parseSkills(value) {
    if (!value) {
      return [];
    }

    try {
      return JSON.parse(value);
    } catch {
      return [];
    }
  }

  function copyText(text) {
    navigator.clipboard.writeText(text);
    alert("Copied to clipboard!");
  }

  if (loading) {
    return (
      <>
      <Navbar />
      <div className="analysis-page">
        <h2>Loading analysis...</h2>
      </div>
      </>
    );
  }

  const matchedSkills = parseSkills(
    analysis?.matchedSkills
  );

  const missingSkills = parseSkills(
    analysis?.missingSkills
  );

  return (
    <>
    <Navbar />
    <div className="analysis-page">

      <div className="analysis-header">
        <Link to={`/jobs/${jobId}`}>
          ← Back to Job
        </Link>

        <h1>AI Job Analysis</h1>
        <p>
          Compare your master resume with this job
          using AI.
        </p>
      </div>

      {!analysis && (
        <div className="analyze-box">

          <h2>Ready to analyze?</h2>

          <p>
            Our AI will compare your resume with the
            job requirements and identify matching and
            missing skills.
          </p>

          <button
            className="primary-button"
            onClick={handleAnalyze}
            disabled={analyzing}
          >
            {analyzing
              ? "Analyzing with AI..."
              : "Analyze Job"}
          </button>

          {error && (
            <p className="error-message">
              {error}
            </p>
          )}

        </div>
      )}

      {analysis && (
        <>
          <div className="job-summary">

            <div>
              <h2>{analysis.jobTitle}</h2>

              <p>
                {analysis.company}
              </p>
            </div>

            <div className="match-card">
              <span>Match</span>
              <strong>
                {analysis.matchPercentage}%
              </strong>
            </div>

          </div>

          <div className="skills-grid">

            <div className="skill-card matched">

              <h2>Matched Skills</h2>

              {matchedSkills.length === 0 ? (
                <p>No matched skills found.</p>
              ) : (
                <div className="skill-list">
                  {matchedSkills.map(
                    (skill, index) => (
                      <span key={index}>
                        {skill}
                      </span>
                    )
                  )}
                </div>
              )}

            </div>

            <div className="skill-card missing">

              <h2>Missing Skills</h2>

              {missingSkills.length === 0 ? (
                <p>No missing skills found.</p>
              ) : (
                <div className="skill-list">
                  {missingSkills.map(
                    (skill, index) => (
                      <span key={index}>
                        {skill}
                      </span>
                    )
                  )}
                </div>
              )}

            </div>

          </div>

          

          <div className="content-card">

            <div className="section-header">
              <h2>Cover Letter</h2>

              <button
                onClick={() =>
                  copyText(
                    analysis.coverLetter
                  )
                }
              >
                Copy
              </button>
            </div>

            <div className="text-content">
              {analysis.coverLetter}
            </div>

          </div>

          {analysis.outreachMsg && (
            <div className="content-card">

              <div className="section-header">
                <h2>Outreach Message</h2>

                <button
                  onClick={() =>
                    copyText(
                      analysis.outreachMsg
                    )
                  }
                >
                  Copy
                </button>
              </div>

              <div className="text-content">
                {analysis.outreachMsg}
              </div>

            </div>
          )}

          <div className="reanalyze-box">

            <button
              className="secondary-button"
              onClick={handleAnalyze}
              disabled={analyzing}
            >
              {analyzing
                ? "Analyzing..."
                : "Re-analyze Job"}
            </button>

          </div>

        </>
      )}

    </div>
    </>
  );
}

export default JobAnalysis;


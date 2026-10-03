import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import "./Resume.css";
import Navbar from "../components/Navbar";

function Resume() {
  const [file, setFile] = useState(null);
  const [resume, setResume] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [uploading, setUploading] = useState(false);
  const [loadingResume, setLoadingResume] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadResume();
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, []);

  async function loadResume() {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("/api/v1/resumes/current", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!response.ok) {
        setResume(null);
        return;
      }

      const data = await response.json();
      setResume(data);

      if (data.contentType === "application/pdf") {
        const fileResponse = await fetch("/api/v1/resumes/download", {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (fileResponse.ok) {
          const blob = await fileResponse.blob();
          setPreviewUrl(URL.createObjectURL(blob));
        }
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingResume(false);
    }
  }

  function handleFileChange(event) {
    const selectedFile = event.target.files[0];
    setMessage("");
    setError("");

    if (!selectedFile) {
      setFile(null);
      return;
    }

    const allowedTypes = [
      "application/pdf",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ];

    if (!allowedTypes.includes(selectedFile.type)) {
      setError("Only PDF and DOCX files are allowed.");
      setFile(null);
      return;
    }

    if (selectedFile.size > 5 * 1024 * 1024) {
      setError("File size cannot exceed 5 MB.");
      setFile(null);
      return;
    }

    setFile(selectedFile);
  }

  async function uploadResume(event) {
    event.preventDefault();

    if (!file) {
      setError("Please select a PDF or DOCX file.");
      return;
    }

    try {
      setUploading(true);
      setError("");
      setMessage("");

      const token = localStorage.getItem("token");
      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch("/api/v1/resumes/upload", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Resume upload failed");
      }

      setMessage(
        `Resume uploaded successfully. Domain detected: ${data.specialization}.`
      );
      setFile(null);
      document.getElementById("resume-file").value = "";

      await loadResume();
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <Navbar />

      <div className="resume-page">
        <div className="resume-card">
          <Link to="/">← Back to Jobs</Link>

          <h1>My Resume</h1>

          <p className="resume-description">
            Upload your resume once. We extract your domain and skills so
            relevant jobs and internships can be shown to you.
          </p>

          {loadingResume ? (
            <p>Loading your resume...</p>
          ) : resume ? (
            <div className="resume-info">
              <h2>Your Uploaded Resume</h2>
              <p><strong>File:</strong> {resume.filename}</p>
              <p><strong>Domain:</strong> {resume.specialization?.replaceAll('"', "")}</p>
              <p><strong>Uploaded:</strong> {new Date(resume.uploadedAt).toLocaleString()}</p>

              {previewUrl && (
                <div style={{ marginTop: 20 }}>
                  <h3>Resume Preview</h3>
                  <iframe
                    title="My Resume"
                    src={previewUrl}
                    style={{ width: "100%", height: "700px", border: "1px solid #ddd", borderRadius: "12px" }}
                  />
                </div>
              )}

              {!previewUrl && (
                <div className="resume-text-preview">
                  <h3>Extracted Resume Text</h3>
                  <pre style={{ whiteSpace: "pre-wrap" }}>{resume.extractedText}</pre>
                </div>
              )}
            </div>
          ) : (
            <div className="resume-info">
              <strong>No resume uploaded yet.</strong>
            </div>
          )}

          <hr />

          <div className="resume-info">
            <strong>Supported formats:</strong> PDF, DOCX
            <br />
            <strong>Maximum size:</strong> 5 MB
          </div>

          <form onSubmit={uploadResume}>
            <label htmlFor="resume-file">
              {resume ? "Replace Resume" : "Select Resume"}
            </label>

            <input
              id="resume-file"
              type="file"
              accept=".pdf,.docx"
              onChange={handleFileChange}
            />

            {file && <p className="selected-file">Selected: {file.name}</p>}
            {error && <p className="error-message">{error}</p>}
            {message && <p className="success-message">{message}</p>}

            <button type="submit" disabled={uploading || !file}>
              {uploading ? "Uploading..." : resume ? "Replace Resume" : "Upload Resume"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default Resume;

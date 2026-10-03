
const API_URL = "/api/v1/job-analysis";

export async function analyzeJob(jobId) {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_URL}/${jobId}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(
      data.message || "Failed to analyze job"
    );
  }

  return data;
}

export async function getJobAnalysis(jobId) {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_URL}/${jobId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(
      data.message || "Failed to load job analysis"
    );
  }

  return data;
}


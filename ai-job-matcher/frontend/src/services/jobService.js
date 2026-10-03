const API_URL = "/api/v1/jobs";

export async function getJobs() {
  const response = await fetch(API_URL, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  if (!response.ok) throw new Error("Failed to fetch jobs");
  return response.json();
}

export async function getRecommendedJobs() {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_URL}/recommended`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Please upload your resume first.");
  }

  return data;
}

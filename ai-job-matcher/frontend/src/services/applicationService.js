import { apiRequest } from "./api";

export async function getApplications() {
  return apiRequest("/applications", {
    method: "GET",
  });
}

export async function deleteApplication(id) {
  return apiRequest(`/applications/${id}`, {
    method: "DELETE",
  });
}

export async function updateApplicationStatus(id, status) {
  return apiRequest(
    `/applications/${id}/status?status=${status}`,
    {
      method: "PUT",
    }
  );
}
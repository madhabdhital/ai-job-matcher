import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Applications from "./pages/Applications";
import JobDetails from "./pages/JobDetails";
import JobAnalysis from "./pages/JobAnalysis";
import Resume from "./pages/Resume";
import ProtectedRoute from "./components/ProtectedRoute";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Public homepage: all jobs are visible before login. */}
        <Route path="/" element={<Dashboard />} />
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/applications" element={
          <ProtectedRoute><Applications /></ProtectedRoute>
        } />

        {/* Clicking View Details requires authentication. */}
        <Route path="/jobs/:jobId" element={
          <ProtectedRoute><JobDetails /></ProtectedRoute>
        } />

        <Route path="/jobs/:jobId/analysis" element={
          <ProtectedRoute><JobAnalysis /></ProtectedRoute>
        } />

        <Route path="/resume" element={
          <ProtectedRoute><Resume /></ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

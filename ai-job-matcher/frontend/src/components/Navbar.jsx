import { NavLink, Link, useNavigate } from "react-router-dom";
import "./Navbar.css";

function Navbar() {
  const navigate = useNavigate();
  const isLoggedIn = Boolean(localStorage.getItem("token"));

  function handleLogout() {
    localStorage.removeItem("token");
    navigate("/", { replace: true });
    window.location.reload();
  }

  const linkClass = ({ isActive }) =>
    isActive ? "navbar-link active" : "navbar-link";

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-logo">
        <span className="navbar-mark">AJ</span>
        <span>AI Job Matcher</span>
      </Link>

      <div className="navbar-links">
        <NavLink to="/" className={linkClass}>Jobs</NavLink>

        {isLoggedIn ? (
          <>
            <NavLink to="/applications" className={linkClass}>Applications</NavLink>
            <NavLink to="/resume" className={linkClass}>My Resume</NavLink>
            <button type="button" className="navbar-logout" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="navbar-link">Login</Link>
            <Link to="/register" className="navbar-link">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;

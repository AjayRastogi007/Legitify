import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { signOut } from "../service/authService";
import ThemeToggle from "./ThemeToggle";

const Header = () => {
  const { auth } = useAuth();
  const navigate = useNavigate();

  return (
    <nav className="navbar bg-body-tertiary border-bottom shadow-sm sticky-top">
      <div className="container-fluid px-3 py-1 d-flex justify-content-between align-items-center">

        <Link className="navbar-brand fw-bold fs-4 d-flex align-items-center gap-2" to="/home">
          <i className="bi bi-shield-check text-primary fs-4"></i>
          Legitify
        </Link>

        <div className="d-flex align-items-center gap-3">
          <ThemeToggle />

          {auth?.user ? (
            <button
              className="btn btn-outline-danger btn-sm px-3"
              onClick={signOut}
            >
              Sign Out
            </button>
          ) : (
            <button
              className="btn btn-primary btn-sm px-3"
              onClick={() => navigate("/sign-in")}
            >
              Sign In
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Header;

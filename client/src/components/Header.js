import { Shield } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "./ui/button";
import ThemeToggle from "./ThemeToggle";
import { useAuth } from "../context/AuthContext";

const Header = () => {
  const { auth, handleSignOut } = useAuth();
  const navigate = useNavigate();

  return (
    <nav className="legitify-navbar sticky top-0 z-50 bg-card border-b border-border shadow-sm transition-all duration-300">
      <div className="header-container w-full px-4 md:px-6 lg:px-8 flex items-center justify-between h-14">
        <Link to="/home" className="legitify-brand flex items-center gap-2.5 no-underline">
          <div className="logo-badge w-9 h-9 rounded-xl bg-linear-to-br from-blue-700 to-primary flex items-center justify-center shadow-md">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <span className="logo-text text-lg font-bold text-foreground tracking-tight hover:text-primary transition-colors hover-text-accent duration-200">
            Legitify
          </span>
        </Link>

        <div className="flex items-center gap-3">
          <ThemeToggle />

          {auth?.user ? (
            <Button
              variant="outline"
              size="sm"
              onClick={handleSignOut}
              className="btn-outline-danger border-destructive text-destructive hover:bg-destructive hover:text-destructive-foreground font-semibold"
            >
              Sign Out
            </Button>
          ) : (
            <Button
              size="sm"
              onClick={() => navigate("/sign-in")}
              className="btn-primary gradient-primary font-semibold shadow-md hover:shadow-lg transition-all duration-200"
            >
              Sign In
            </Button>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Header;
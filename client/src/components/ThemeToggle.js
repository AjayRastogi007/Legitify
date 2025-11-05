import { useEffect, useState, useRef } from "react";
import { Tooltip } from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";

const ThemeToggle = () => {
  const [theme, setTheme] = useState(() => localStorage.getItem("theme") || "light");
  const buttonRef = useRef(null);
  const tooltipInstance = useRef(null);

  useEffect(() => {
    if (buttonRef.current) {
      tooltipInstance.current = new Tooltip(buttonRef.current, {
        title: theme === "light" ? "Switch to Dark Mode" : "Switch to Light Mode",
        placement: "bottom",
        trigger: "hover",
      });
    }

    return () => {
      if (tooltipInstance.current) {
        tooltipInstance.current.dispose();
        tooltipInstance.current = null;
      }
    };
  }, []);

  useEffect(() => {
    document.body.setAttribute("data-bs-theme", theme);
    localStorage.setItem("theme", theme);

    if (tooltipInstance.current) {
      tooltipInstance.current.setContent({
        ".tooltip-inner":
          theme === "light" ? "Switch to Dark Mode" : "Switch to Light Mode",
      });
    }
  }, [theme]);

  const toggleTheme = () => {
    tooltipInstance.current?.hide();
    setTheme((prev) => (prev === "light" ? "dark" : "light"));
  };

  return (
    <button
      ref={buttonRef}
      type="button"
      className="theme-toggle-btn"
      onClick={toggleTheme}
    >
      <i
        className={`bi ${theme === "light" ? "bi-moon-stars-fill" : "bi-sun-fill"}`}
        style={{ fontSize: "1.25rem", lineHeight: 1 }}
      ></i>
    </button>
  );
};

export default ThemeToggle;

import { useLayoutEffect, useState } from "react";
import { Shield } from "lucide-react";

const LoadingPage = () => {
  const [theme] = useState(
    () => localStorage.getItem("theme") || "light"
  );

  useLayoutEffect(() => {
    if (theme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [theme]);

  return (
    <div className="flex flex-col justify-center items-center text-center min-h-screen bg-background text-foreground">
      <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center animate-pulse">
        <Shield className="w-6 h-6 text-primary-foreground" />
      </div>
    </div>
  );
};

export default LoadingPage;
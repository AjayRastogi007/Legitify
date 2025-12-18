import { useRouteError, isRouteErrorResponse, Link } from "react-router-dom";
import { useLayoutEffect, useState } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "../components/ui/button";
import { Loader2 } from "lucide-react";

const ErrorPage = () => {
  const error = useRouteError();

  const [theme] = useState(
    () => localStorage.getItem("theme") || "light"
  );

  const [isReloading, setIsReloading] = useState(false);

  useLayoutEffect(() => {
    if (theme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [theme]);

  let title = "Something went wrong";
  let message = "An unexpected error occurred. Please try again.";
  let status;

  if (isRouteErrorResponse(error)) {
    status = error.status;

    if (error.status === 404) {
      title = "Page not found";
      message = "The page you are looking for does not exist.";
    } else if (error.status === 401) {
      title = "Unauthorized";
      message = "You are not authorized to view this page.";
    } else if (error.status >= 500) {
      title = "Server error";
      message = "The server encountered an error. Please try again later.";
    }
  }

  return (
    <div className="flex flex-col justify-center items-center text-center px-4 min-h-screen bg-background">
      <div className="max-w-md">
        <AlertTriangle
          className={`w-16 h-16 mx-auto mb-6 ${theme === "dark" ? "text-red-400" : "text-destructive"}`}
        />

        <h1 className="text-2xl font-bold text-foreground mb-2">
          {status ? `${status} – ${title}` : title}
        </h1>

        <p className="text-muted-foreground mb-6">
          {message}
        </p>

        <div className="flex gap-3 justify-center">
          <Link to="/home">
            <Button className="gradient-primary font-semibold">
              Go Home
            </Button>
          </Link>

          <Button
            variant="outline"
            disabled={isReloading}
            onClick={() => {
              setIsReloading(true);
              setTimeout(() => {
                window.location.reload();
              }, 150);
            }}
            className="flex items-center gap-2"
          >
            {isReloading && <Loader2 className="w-4 h-4 animate-spin" />}
            {isReloading ? "Reloading…" : "Reload"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;
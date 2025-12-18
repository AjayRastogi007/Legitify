import React from "react";

export const Input = ({ className = "", ...props }) => {
  const finalClassName = [
    "w-full h-12 rounded-lg border border-border bg-background px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary",
    className,
  ].join(" ");

  return <input className={finalClassName} {...props} />;
};

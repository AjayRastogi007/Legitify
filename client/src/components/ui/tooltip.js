import React, { useState } from "react";

export const TooltipProvider = ({ children }) => children;

export const Tooltip = ({ children }) => {
  const [open, setOpen] = useState(false);

  let trigger = null;
  let content = null;

  React.Children.forEach(children, (child) => {
    if (child.type === TooltipTrigger) {
      trigger = React.cloneElement(child, {
        onMouseEnter: () => setOpen(true),
        onMouseLeave: () => setOpen(false),
      });
    }

    if (child.type === TooltipContent) {
      content = child;
    }
  });

  return (
    <div className="relative inline-flex">
      {trigger}
      {open && content}
    </div>
  );
};

export const TooltipTrigger = ({ children, onMouseEnter, onMouseLeave }) =>
  React.cloneElement(children, {
    onMouseEnter,
    onMouseLeave,
  });

export const TooltipContent = ({ children }) => (
  <div className="absolute left-1/2 top-full mt-2 -translate-x-1/2 z-50 rounded-md bg-card border border-border px-3 py-1.5 text-xs shadow-lg whitespace-nowrap pointer-events-none">
    {children}
  </div>
);
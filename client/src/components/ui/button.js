import React from "react";

export const Button = ({
    className = "",
    variant = "default",
    size = "md",
    ...props
}) => {
    const variants = {
        default: "bg-primary text-primary-foreground hover:opacity-90",
        outline:
            "border border-border bg-transparent hover:bg-accent text-foreground",
        ghost: "bg-transparent hover:bg-accent",
    };

    const sizes = {
        sm: "h-9 px-3 text-sm",
        md: "h-11 px-4",
        icon: "h-10 w-10 p-0",
    };

    const finalClassName = [
        "inline-flex items-center justify-center rounded-lg font-medium transition-colors",
        "focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background",
        variants[variant] || "",
        sizes[size] || "",
        className,
    ].join(" ");


    return <button className={finalClassName} {...props} />;
};

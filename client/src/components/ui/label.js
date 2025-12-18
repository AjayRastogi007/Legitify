export const Label = ({ className = "", ...props }) => {
  return (
    <label
      className={`
        block text-xs font-semibold uppercase tracking-wide
        text-foreground/80
        dark:text-foreground/70
        ${className}
      `}
      {...props}
    />
  );
};

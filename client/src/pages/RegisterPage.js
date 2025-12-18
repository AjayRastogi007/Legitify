import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../service/authService";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";

const initialErrorState = {
  username: "",
  email: "",
  password: "",
  general: ""
};

const InputField = ({ label, type, value, onChange, error }) => (
  <div className="mb-4 group">
    <Label className="text-xs font-semibold uppercase mb-2 block group-focus-within:text-primary transition-colors">
      {label}
    </Label>
    <Input
      type={type}
      className={`${error ? "border-destructive focus-visible:ring-destructive" : ""}`}
      value={value}
      onChange={onChange}
      autoComplete={type === "password" ? "new-password" : "off"}
    />
    {error && (
      <p className="text-destructive animate-error text-sm mt-1">{error}</p>
    )}
  </div>
);

const RegisterPage = () => {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState(initialErrorState);

  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFieldErrors(initialErrorState);

    const errors = {};
    if (!username.trim()) errors.username = "Username is required.";
    if (!email.includes("@") || !email.includes("."))
      errors.email = "Please enter a valid email.";
    if (password.length < 8)
      errors.password = "Password must be at least 8 characters.";

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      await register({ username, email, password });
      navigate("/sign-in");
    } catch (error) {
      const newErrors = { ...initialErrorState };
      if (error.response?.data?.fieldErrors)
        Object.assign(newErrors, error.response.data.fieldErrors);
      else if (error.response?.data?.message)
        newErrors.general = error.response.data.message;
      else newErrors.general = "Registration failed. Please try again.";
      setFieldErrors(newErrors);
    }
  };

  return (
    <div className="flex flex-1 items-center justify-center px-4">
      <div className="relative w-full max-w-md">
        <div className="absolute -inset-1 rounded-2xl bg-linear-to-r from-primary/30 to-blue-500/30 blur-lg opacity-40" />

        <div className="relative bg-card rounded-2xl p-8 shadow-xl border border-border">
          <h2 className="text-2xl font-bold text-center text-primary mb-1">
            Create account
          </h2>
          <p className="text-sm text-foreground/70 text-center mb-6">
            Get started with Legitify today
          </p>

          <form onSubmit={handleSubmit} noValidate>
            <InputField
              label="Username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              error={fieldErrors.username}
            />

            <InputField
              label="Email address"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={fieldErrors.email}
            />

            <InputField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={fieldErrors.password}
            />

            {fieldErrors.general && (
              <p className="text-destructive text-sm text-center mb-3">
                {fieldErrors.general}
              </p>
            )}

            <Button className="w-full mt-4 gradient-primary py-3 font-semibold">
              Create Account
            </Button>
          </form>

          <p className="text-sm text-center text-foreground/70 mt-6">
            Already have an account?{" "}
            <Link to="/sign-in" className="text-primary font-medium">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );

};

export default RegisterPage;
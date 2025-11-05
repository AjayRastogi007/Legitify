import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { signIn } from "../service/authService";
import { useAuth } from "../context/AuthContext";
import useAutoScrollLock from "../hooks/useAutoScrollLock";

const initialErrorState = {
    email: "",
    password: "",
    general: ""
};

const InputField = ({ label, type, value, onChange, error }) => (
    <div className="mb-3">
        <label className="form-label fw-semibold small text-uppercase text-secondary">
            {label}
        </label>
        <input
            type={type}
            className={`form-control ${error ? "is-invalid" : ""}`}
            value={value}
            onChange={onChange}
            autoComplete={type === "password" ? "current-password" : "email"}
        />
        {error && (
            <div className="invalid-feedback" style={{ fontSize: "0.85rem" }}>
                {error}
            </div>
        )}
    </div>
);

const SignInPage = () => {
    useAutoScrollLock();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fieldErrors, setFieldErrors] = useState(initialErrorState);
    const { handleSignIn } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setFieldErrors(initialErrorState);

        const errors = {};
        if (!email.includes("@") || !email.includes(".")) {
            errors.email = "Please enter a valid email.";
        }
        if (password.length < 8) {
            errors.password = "Password must be at least 8 characters.";
        }

        if (Object.keys(errors).length > 0) {
            setFieldErrors((prev) => ({ ...prev, ...errors }));
            return;
        }

        try {
            const data = await signIn({ email, password });
            handleSignIn(data);
            navigate("/home");
        } catch (error) {
            const newErrors = { ...initialErrorState };
            if (error.response?.data?.fieldErrors) {
                Object.assign(newErrors, error.response.data.fieldErrors);
            } else if (error.response?.data?.message) {
                newErrors.general = error.response.data.message;
            } else {
                newErrors.general = "Invalid email or password.";
            }
            setFieldErrors(newErrors);
        }
    };

    return (
        <div
            className="d-flex flex-column align-items-center justify-content-center px-3"
            style={{ minHeight: "100vh" }}
        >
            <div
                className="form-card p-4 p-md-5 shadow-sm w-100"
                style={{
                    maxWidth: "420px",
                    borderTop: "5px solid var(--brand-accent)",
                    transition: "all var(--transition-slow)"
                }}
            >
                <h2
                    className="fw-bold mb-4 text-center"
                    style={{
                        color: "var(--brand-primary)",
                        letterSpacing: "0.5px"
                    }}
                >
                    Sign In
                </h2>

                <form className="d-flex flex-column" onSubmit={handleSubmit} noValidate>
                    <InputField
                        label="Email Address"
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
                        <div className="text-danger text-center fw-semibold mb-2">
                            {fieldErrors.general}
                        </div>
                    )}

                    <button type="submit" className="btn mt-3 fw-semibold py-2 fs-6">
                        Sign In
                    </button>
                </form>

                <div className="mt-4 text-center">
                    <span className="text-secondary me-1">New user?</span>
                    <Link
                        to="/register"
                        className="fw-bold"
                        style={{
                            color: "var(--brand-primary)",
                            textDecoration: "none",
                            transition: "color var(--transition-fast)"
                        }}
                        onMouseEnter={(e) => (e.target.style.color = "var(--brand-primary-light)")}
                        onMouseLeave={(e) => (e.target.style.color = "var(--brand-primary)")}
                    >
                        Register
                    </Link>

                </div>
            </div>
        </div>
    );
};

export default SignInPage;

import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../service/authService";
import useAutoScrollLock from "../hooks/useAutoScrollLock";

const initialErrorState = {
    username: "",
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
            autoComplete={type === "password" ? "new-password" : "off"}
        />
        {error && (
            <div className="invalid-feedback" style={{ fontSize: "0.85rem" }}>
                {error}
            </div>
        )}
    </div>
);

const RegisterPage = () => {
    useAutoScrollLock();
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
        <div
            className="d-flex flex-column align-items-center justify-content-center px-3 flex-grow-1"
            style={{
                flex: 1,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
            }}
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
                    Create Account
                </h2>

                <form className="d-flex flex-column" onSubmit={handleSubmit} noValidate>
                    <InputField
                        label="Username"
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        error={fieldErrors.username}
                    />

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

                    <button
                        type="submit"
                        className="btn mt-3 fw-semibold py-2 fs-6 btn-primary"
                    >
                        Register
                    </button>
                </form>

                <div className="mt-4 text-center">
                    <span className="text-secondary me-1">Already have an account?</span>
                    <Link
                        to="/sign-in"
                        className="fw-bold"
                        style={{
                            color: "var(--brand-primary)",
                            textDecoration: "none",
                            transition: "color var(--transition-fast)"
                        }}
                        onMouseEnter={(e) =>
                            (e.target.style.color = "var(--brand-primary-light)")
                        }
                        onMouseLeave={(e) =>
                            (e.target.style.color = "var(--brand-primary)")
                        }
                    >
                        Sign In
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;

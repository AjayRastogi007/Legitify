import { useState } from "react";

const SignIn = () => {

    function handleSubmit(event) {
        event.preventDefault();

        const newErrors = {};
        if (!formData.email.includes('@')) {
            newErrors.email = "Invalid email address.";
        }

        if (formData.password.length < 8) {
            newErrors.password = "Invalid password: minimum length 8.";
        }
        setErrors(newErrors);

        if (Object.keys(newErrors).length === 0) {
            console.log("✅ Valid data, send to backend:", formData);
        }
    }

    function handleChange(event) {
        const { name, value } = event.target;
        if (!name) return;
        setFormData((prev) => ({ ...prev, [name]: value }));
    }

    const [formData, setFormData] = useState({});
    const [errors, setErrors] = useState({});

    return (
        <div>
            <div>
                <div className="logo-container"></div>
                <div className="name-conatiner"></div>
            </div>

            <div className="authentication-conatiner">
                <div>
                    <span>Sign in to your account</span>
                </div>

                <form noValidate onSubmit={handleSubmit}>
                    <label htmlFor="email">Email:</label>
                    <input
                        type="email"
                        name="email"
                        onChange={handleChange}
                    />
                    {errors.email && <div style={{ color: "red" }}>{errors.email}</div>}

                    <label htmlFor="password">Password:</label>
                    <input
                        type="password"
                        name="password"
                        onChange={handleChange}
                    />
                    {errors.password && <div style={{ color: "red" }}>{errors.password}</div>}

                    <button type="submit">Sign In</button>
                </form>

                <div>
                    <span>New user?</span>
                    
                </div>
            </div>
        </div>
    );
}

export default SignIn;
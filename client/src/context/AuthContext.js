import { createContext, useContext, useEffect, useState } from "react";
import { signOut, getCurrentUser, refreshAccessToken } from "../service/authService";
import { useAlert } from "./AlertContext";
import api from "../api/axios";
import LoadingPage from "../pages/LoadingPage";

const AuthContext = createContext(null);
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export const AuthProvider = ({ children }) => {
    const { showAlert } = useAlert();

    const [auth, setAuth] = useState({
        user: null,
        accessToken: null,
        isLoggingOut: false
    });

    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let active = true;

        (async () => {
            const MIN_LOADING_TIME = 1200;

            try {
                await Promise.all([
                    (async () => {
                        const { accessToken } = await refreshAccessToken();
                        api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
                        const user = await getCurrentUser();
                        if (!active) return;
                        setAuth({
                            user,
                            accessToken,
                            isLoggingOut: false
                        });
                    })(),
                    delay(MIN_LOADING_TIME),
                ]);
            } catch (e) {
                if (!active) return;
                setAuth({
                    user: null,
                    accessToken: null,
                    isLoggingOut: false
                });
                delete api.defaults.headers.common.Authorization;
            } finally {
                if (active) setLoading(false);
            }
        })();

        return () => {
            active = false;
        };
    }, []);

    const handleSignIn = (data) => {
        setAuth({
            user: data,
            accessToken: data.accessToken,
            isLoggingOut: false
        });

        showAlert({
            title: "Welcome back!",
            message: "You have successfully signed in.",
            type: "success",
        });
    };

    const handleSignOut = async () => {
        setAuth((prev) => ({ ...prev, isLoggingOut: true }));

        try {
            await signOut();

            showAlert({
                title: "Signed out",
                message: "You have been logged out successfully.",
                type: "info",
            });
        } catch (e) {
            showAlert({
                title: "Logout failed",
                message: "You were logged out locally, but the server did not respond.",
                type: "warning",
            });
        } finally {
            setAuth({
                user: null,
                accessToken: null,
                isLoggingOut: false,
            });
            delete api.defaults.headers.common.Authorization;
        }
    };

    return (
        <AuthContext.Provider value={{ auth, setAuth, handleSignIn, handleSignOut }}>
            {loading ? (
                <div className={`transition-opacity duration-300 ${!loading ? "opacity-0" : "opacity-100"}`}>
                    <LoadingPage />
                </div>
            ) : children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const authContext = useContext(AuthContext);

    if (!authContext) {
        throw new Error('useAuth must be used within a AuthProvider.');
    }

    return authContext;
};
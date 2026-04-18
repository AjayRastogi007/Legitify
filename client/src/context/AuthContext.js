import { createContext, useContext, useEffect, useState } from "react";
import { signOut, getCurrentUser, refreshAccessToken } from "../service/authService";
import { useAlert } from "./AlertContext";
import api from "../api/axios";

const AuthContext = createContext(null);

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

        const isFirstLoad = !sessionStorage.getItem("app_loaded");
        const MIN_LOADING_TIME = isFirstLoad ? 5000 : 0;

        if (isFirstLoad) {
            sessionStorage.setItem("app_loaded", "true");
        }

        (async () => {
            const start = Date.now();

            try {
                const { accessToken } = await refreshAccessToken();

                api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;

                const user = await getCurrentUser();

                if (!active) return;

                setAuth({
                    user,
                    accessToken,
                    isLoggingOut: false
                });

            } catch (e) {
                if (!active) return;

                setAuth({
                    user: null,
                    accessToken: null,
                    isLoggingOut: false
                });

                delete api.defaults.headers.common.Authorization;
            } finally {
                const elapsed = Date.now() - start;
                const remaining = MIN_LOADING_TIME - elapsed;

                if (remaining > 0) {
                    setTimeout(() => {
                        if (active) setLoading(false);
                    }, remaining);
                } else {
                    if (active) setLoading(false);
                }
            }
        })();

        return () => {
            active = false;
        };
    }, []);

    const handleSignIn = (data) => {
        setAuth({
            user: data.user,
            accessToken: data.accessToken,
            isLoggingOut: false
        });

        api.defaults.headers.common.Authorization = `Bearer ${data.accessToken}`;

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
        <AuthContext.Provider value={{ auth, loading, setAuth, handleSignIn, handleSignOut }}>
            {loading ? children /* you will wrap this outside */ : children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const authContext = useContext(AuthContext);

    if (!authContext) {
        throw new Error('useAuth must be used within a AuthProvider.');
    }

    return authContext;
};
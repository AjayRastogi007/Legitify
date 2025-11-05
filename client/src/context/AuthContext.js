import { createContext, useContext, useEffect, useState } from "react";
import { signOut, getCurrentUser, refreshAccessToken } from "../service/authService";
import api from "../api/axios";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [auth, setAuth] = useState({
        user: null,
        accessToken: null
    });

    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let active = true;
        (async () => {
            try {
                const { accessToken } = await refreshAccessToken();
                api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
                const user = await getCurrentUser();
                if (!active) return;
                setAuth({ user, accessToken });
            } catch (e) {
                if (!active) return;
                setAuth({ user: null, accessToken: null });
                delete api.defaults.headers.common.Authorization;
            } finally {
                if (active) setLoading(false);
            }
        })();
        return () => { active = false; };
    }, []);


    const handleSignIn = (data) => {
        setAuth({
            user: data,
            accessToken: data.accessToken
        });
    };


    const handleSignOut = async () => {
        try {
            await signOut();
        } catch (e) {
            console.error("Logout failed:", e);
        } finally {
            setAuth({ user: null, accessToken: null });
        }
    }

    return (
        <AuthContext.Provider value={{ auth, setAuth, handleSignIn, handleSignOut }}>
            {loading ? <div>Loading session...</div> : children}
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
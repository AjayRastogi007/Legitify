import { useEffect, useRef } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { refreshAccessToken } from "../service/authService";
import { useAlert } from "../context/AlertContext";

const AUTH_EXCLUDED_ENDPOINTS = [
    "/auth/sign-in",
    "/auth/sign-out",
    "/auth/refresh",
];

let refreshPromise = null;

const getFreshToken = async () => {
    if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
};

const useAxios = () => {
    const { showAlert } = useAlert();

    const { auth, setAuth } = useAuth();
    const accessTokenRef = useRef(auth.accessToken);
    const isLoggingOutRef = useRef(auth.isLoggingOut);

    useEffect(() => {
        isLoggingOutRef.current = auth.isLoggingOut;
    }, [auth.isLoggingOut]);


    useEffect(() => {
        accessTokenRef.current = auth.accessToken;
        if (auth.accessToken) {
            api.defaults.headers.common.Authorization = `Bearer ${auth.accessToken}`;
        } else {
            delete api.defaults.headers.common.Authorization;
        }
    }, [auth.accessToken]);

    useEffect(() => {
        const reqId = api.interceptors.request.use(
            (config) => {
                const token = accessTokenRef.current;
                if (!config.headers) config.headers = {};

                if (!config.headers.Authorization && token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            }
        );

        const resId = api.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error?.config;

                if (!originalRequest || !error?.response) {
                    return Promise.reject(error);
                }

                const isAuthExcluded = AUTH_EXCLUDED_ENDPOINTS.some((url) =>
                    originalRequest.url?.includes(url)
                );


                if (
                    error.response.status === 401 &&
                    !originalRequest._retry &&
                    !isAuthExcluded &&
                    !isLoggingOutRef.current
                ) {

                    originalRequest._retry = true;

                    try {
                        const res = await getFreshToken();
                        const newAccessToken = res?.accessToken;

                        if (!newAccessToken) {
                            throw new Error("No access token received");
                        }

                        setAuth((prev) => ({ ...prev, accessToken: newAccessToken }));

                        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;

                        originalRequest.headers = originalRequest.headers;

                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

                        return api(originalRequest);
                    } catch {
                        showAlert({
                            title: "Session expired",
                            message: "Please sign in again.",
                            type: "warning",
                        });

                        setAuth({ user: null, accessToken: null });
                        delete api.defaults.headers.common.Authorization;
                        return Promise.reject(error);
                    }
                }

                return Promise.reject(error);
            }
        );

        return () => {
            api.interceptors.request.eject(reqId);
            api.interceptors.response.eject(resId);
        };

    }, []);

    return api;
};

export default useAxios;
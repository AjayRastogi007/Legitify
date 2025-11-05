import { useEffect, useRef } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { refreshAccessToken } from "../service/authService";

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
    const { auth, setAuth } = useAuth();
    const accessTokenRef = useRef(auth.accessToken);

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
            },
            (error) => Promise.reject(error)
        );

        const resId = api.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error?.config;

                if (!originalRequest || !error?.response) {
                    return Promise.reject(error);
                }

                const isRefreshCall =
                    originalRequest.url?.includes("/auth/refresh") ||
                    originalRequest._isRefresh;

                if (error.response.status === 401 && !originalRequest._retry && !isRefreshCall) {
                    originalRequest._retry = true;

                    try {
                        const res = await getFreshToken();
                        const newAccessToken = res?.accessToken;

                        if (!newAccessToken) {
                            setAuth({ user: null, accessToken: null });
                            delete api.defaults.headers.common.Authorization;
                            return Promise.reject(error);
                        }

                        setAuth((prev) => ({ ...prev, accessToken: newAccessToken }));
                        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;


                        originalRequest.headers = originalRequest.headers || {};
                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        return api(originalRequest);
                    } catch (refreshError) {
                        setAuth({ user: null, accessToken: null });
                        delete api.defaults.headers.common.Authorization;
                        return Promise.reject(refreshError);
                    }
                }

                return Promise.reject(error);
            }
        );

        return () => {
            api.interceptors.request.eject(requestIntercept);
            api.interceptors.response.eject(responseIntercept);
        };
    }, [auth, setAuth]);

    return api;
};

export default useAxios;
import api from "../api/axios";

export const register = async (userDetails) => {
    const response = await api.post("/auth/register", userDetails);
    return response.data;
};

export const signIn = async (credentials) => {
    const response = await api.post("/auth/sign-in", credentials);
    return response.data;
};

export const refreshAccessToken = async () => {
  const response = await api.post("/auth/refresh");
  return response.data;
};

export const getCurrentUser = async () => {
    console.log("me is called");
    const response = await api.get("/auth/me");
    console.log("me was called");
    return response.data;
}

export const signOut = async () => {
    const response = await api.post("/auth/sign-out");
    return response.data;
}
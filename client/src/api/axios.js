import axios from "axios";

const api = axios.create({
    baseURL: `${process.env.PARCEL_API_BASE_URL}/legitify`,
    withCredentials: true,
    timeout: 120000,
});

export default api;
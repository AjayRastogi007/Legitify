import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:9191/legitify",
    withCredentials: true,
});

export default api;

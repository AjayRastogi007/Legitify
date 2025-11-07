import React from "react";
import ReactDOM from "react-dom/client";
import {
    createBrowserRouter,
    RouterProvider,
    Route,
    createRoutesFromElements,
} from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

import App from "./App";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/RegisterPage";
import SignInPage from "./pages/SignInPage";
import ErrorPage from "./pages/ErrorPage";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./styles.css";

import { setScrollbarWidth } from "./utility/setScrollbarWidth";
import { fixViewportWidth } from "./utility/fixViewportWidth";

setScrollbarWidth();
fixViewportWidth();

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route element={<AuthProvider><App /></AuthProvider>} errorElement={<ErrorPage />}>
            <Route path="/home" element={<HomePage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/sign-in" element={<SignInPage />} />
            <Route index element={<HomePage />} />
        </Route>
    )
);

ReactDOM.createRoot(document.getElementById("root")).render(
    <RouterProvider router={router} />
);

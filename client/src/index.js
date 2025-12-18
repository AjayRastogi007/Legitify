import React from "react";
import ReactDOM from "react-dom/client";
import {
  createBrowserRouter,
  RouterProvider,
  createRoutesFromElements,
  Route,
} from "react-router-dom";
import { AlertProvider } from "./context/AlertContext";
import { AuthProvider } from "./context/AuthContext";

import App from "./App";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/RegisterPage";
import SignInPage from "./pages/SignInPage";
import ErrorPage from "./pages/ErrorPage";

import "./index.css";

const router = createBrowserRouter(
  createRoutesFromElements(
    <Route
      element={
        <AlertProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </AlertProvider>
      }
      errorElement={<ErrorPage />}
    >
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

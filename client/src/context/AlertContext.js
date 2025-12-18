import { createContext, useContext, useState } from "react";
import AlertStack from "../components/AlertStack";
import { ALERT_STYLES } from "../utility/AlertStyles";

const AlertContext = createContext(null);

export const AlertProvider = ({ children }) => {
    const [alerts, setAlerts] = useState([]);

    const showAlert = ({ title, message, type = "error", timeout = 5000 }) => {
        const signature = `${type}-${title}-${message}`;

        const id = `${signature}-${Date.now()}`;

        const alert = {
            id,
            signature,
            title,
            message,
            ...ALERT_STYLES[type],
        };

        setAlerts((prev) => {
            const filtered = prev.filter(a => a.signature !== signature);
            return [alert, ...filtered];
        });

        if (timeout) {
            setTimeout(() => {
                setAlerts((prev) => prev.filter((a) => a.id !== id));
            }, timeout);
        }
    };


    const removeAlert = (id) => {
        setAlerts((prev) => prev.filter((a) => a.id !== id));
    };

    return (
        <AlertContext.Provider value={{ showAlert, removeAlert }}>
            {children}
            <AlertStack alerts={alerts} onRemove={removeAlert} />
        </AlertContext.Provider>
    );
};

export const useAlert = () => {
    const ctx = useContext(AlertContext);
    if (!ctx) {
        throw new Error("useAlert must be used inside AlertProvider");
    }
    return ctx;
};
import React from 'react';
import ReactDOM from 'react-dom/client';
import Header from './components/Header';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './styles.css';
import SignIn from './components/SignIn';
import Register from './components/Register';

const AppLayout = () => {
    return <Register />;
}

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<AppLayout />);
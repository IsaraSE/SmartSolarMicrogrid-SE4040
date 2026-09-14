/*
 * File Name: main.jsx
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Application entry point.
 * Date: 2026-09-14
 */

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
);

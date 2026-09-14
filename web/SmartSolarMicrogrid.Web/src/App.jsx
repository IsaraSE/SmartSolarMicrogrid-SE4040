/*
 * File Name: App.jsx
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Main application component with routing configuration.
 * Date: 2026-09-14
 */

import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';

/* Protected Route Component */
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontFamily: 'Inter, sans-serif',
        color: '#64748b',
      }}>
        Loading...
      </div>
    );
  }

  return user ? children : <Navigate to="/login" replace />;
};

/* Public Route - redirects to dashboard if already logged in */
const PublicRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  return user ? <Navigate to="/dashboard" replace /> : children;
};

/* Placeholder Dashboard */
const Dashboard = () => {
  const { user, logout } = useAuth();
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh',
      fontFamily: 'Inter, sans-serif',
      gap: '16px',
    }}>
      <h1 style={{ color: '#0f2b46' }}>Welcome, {user?.fullName}!</h1>
      <p style={{ color: '#64748b' }}>Role: {user?.role}</p>
      <button
        onClick={() => { logout(); window.location.href = '/login'; }}
        style={{
          padding: '12px 24px',
          background: '#ef4444',
          color: '#fff',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
          fontSize: '0.9rem',
          fontWeight: '600',
        }}
      >
        Logout
      </button>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

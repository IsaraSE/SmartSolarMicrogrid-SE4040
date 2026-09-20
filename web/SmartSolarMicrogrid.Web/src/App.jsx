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
import Login from './pages/Login/Login';
import MainLayout from './components/layout/MainLayout';
import Dashboard from './pages/Dashboard/Dashboard';
import Users from './pages/Users/Users';
import UserForm from './pages/Users/UserForm';
import Prosumers from './pages/Prosumers/Prosumers';
import Stations from './pages/Stations/Stations';
import AddStation from './pages/Stations/AddStation';
import EditStation from './pages/Stations/EditStation';
import Slots from './pages/Slots/Slots';
import AddSlot from './pages/Slots/AddSlot';
import EditSlot from './pages/Slots/EditSlot';
import Reservations from './pages/Reservations/Reservations';
import Settings from './pages/Settings/Settings';

/* Protected Route Component */
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontFamily: 'Inter, sans-serif',
        color: '#64748b',
        background: '#f8fafc',
        gap: '20px',
      }}>
        <div style={{
          width: '44px',
          height: '44px',
          border: '3px solid #e2e8f0',
          borderTopColor: '#10b981',
          borderRadius: '50%',
          animation: 'appSpin 0.8s linear infinite',
        }} />
        <span style={{ fontSize: '0.85rem', fontWeight: '500', letterSpacing: '0.3px', color: '#94a3b8' }}>Loading HelioGrid...</span>
        <style>{`@keyframes appSpin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <MainLayout>{children}</MainLayout>;
};

/* Public Route - redirects to dashboard if already logged in */
const PublicRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  return user ? <Navigate to="/dashboard" replace /> : children;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Route */}
          <Route
            path="/login"
            element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            }
          />

          {/* Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/users"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <Users />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/prosumers"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <Prosumers />
              </ProtectedRoute>
            }
          />

          <Route
            path="/stations"
            element={
              <ProtectedRoute>
                <Stations />
              </ProtectedRoute>
            }
          />

          <Route
            path="/stations/add"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <AddStation />
              </ProtectedRoute>
            }
          />

          <Route
            path="/stations/edit/:id"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <EditStation />
              </ProtectedRoute>
            }
          />

          <Route
            path="/slots"
            element={
              <ProtectedRoute>
                <Slots />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/slots/add"
            element={
              <ProtectedRoute allowedRoles={['GRID_OPERATOR']}>
                <AddSlot />
              </ProtectedRoute>
            }
          />

          <Route
            path="/slots/edit/:id"
            element={
              <ProtectedRoute allowedRoles={['GRID_OPERATOR']}>
                <EditSlot />
              </ProtectedRoute>
            }
          />

          <Route
            path="/reservations"
            element={
              <ProtectedRoute>
                <Reservations />
              </ProtectedRoute>
            }
          />

          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <Settings />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/users/new"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <UserForm />
              </ProtectedRoute>
            }
          />

          <Route
            path="/users/edit/:id"
            element={
              <ProtectedRoute allowedRoles={['BACKOFFICE']}>
                <UserForm />
              </ProtectedRoute>
            }
          />

          {/* Default Redirect */}
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

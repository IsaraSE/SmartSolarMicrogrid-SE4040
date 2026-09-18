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

  if (!user) {
    return <Navigate to="/login" replace />;
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
              <ProtectedRoute>
                <Users />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/prosumers"
            element={
              <ProtectedRoute>
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
              <ProtectedRoute>
                <AddStation />
              </ProtectedRoute>
            }
          />

          <Route
            path="/stations/edit/:id"
            element={
              <ProtectedRoute>
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
              <ProtectedRoute>
                <AddSlot />
              </ProtectedRoute>
            }
          />

          <Route
            path="/slots/edit/:id"
            element={
              <ProtectedRoute>
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
              <ProtectedRoute>
                <UserForm />
              </ProtectedRoute>
            }
          />

          <Route
            path="/users/edit/:id"
            element={
              <ProtectedRoute>
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

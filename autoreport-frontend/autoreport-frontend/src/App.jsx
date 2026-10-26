import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Reports from './pages/Reports';
import Users from './pages/Users';
import RolesPermissions from './pages/RolesPermissions';
import Schedules from './pages/Schedules';
import ImportExcel from './pages/ImportExcel';
import Objectives from './pages/Objectives';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import About from './pages/About';
import AuditLog from './pages/AuditLog';
import AIChat from './pages/AIChat';
import NotFound from './pages/NotFound';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports"
        element={
          <ProtectedRoute>
            <Reports />
          </ProtectedRoute>
        }
      />
      <Route
        path="/import"
        element={
          <ProtectedRoute roles={['ADMIN', 'MANAGER']}>
            <ImportExcel />
          </ProtectedRoute>
        }
      />
      <Route
        path="/schedules"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <Schedules />
          </ProtectedRoute>
        }
      />
      <Route
        path="/objectives"
        element={
          <ProtectedRoute roles={['ADMIN', 'MANAGER']}>
            <Objectives />
          </ProtectedRoute>
        }
      />
      <Route
        path="/users"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <Users />
          </ProtectedRoute>
        }
      />
      <Route
        path="/roles-permissions"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <RolesPermissions />
          </ProtectedRoute>
        }
      />
      <Route
        path="/audit"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <AuditLog />
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <Settings />
          </ProtectedRoute>
        }
      />
      <Route
        path="/chat"
        element={
          <ProtectedRoute>
            <AIChat />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        }
      />
      <Route
        path="/about"
        element={
          <ProtectedRoute>
            <About />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

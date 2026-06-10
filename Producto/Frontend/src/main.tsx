import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import Login from './pages/Login';
import Registro from './pages/Registro';
import Dashboard from './pages/Dashboard';
import Clientes from './pages/Clientes';
import Profesionales from './pages/Profesionales';
import Visitas from './pages/Visitas';
import Pagos from './pages/Pagos';
import MisPagos from './pages/MisPagos';
import MisActividades from './pages/MisActividades';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import './index.css';


const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/registro',
    element: <Registro />,
  },
  {
    path: '/recuperar-contrasena',
    element: <ForgotPassword />,
  },
  {
    path: '/restablecer-contrasena',
    element: <ResetPassword />,
  },
  {
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dashboard',      element: <Dashboard /> },
      {
        path: '/clientes',
        element: <ProtectedRoute roles={['ADMIN']}><Clientes /></ProtectedRoute>,
      },
      {
        path: '/profesionales',
        element: <ProtectedRoute roles={['ADMIN']}><Profesionales /></ProtectedRoute>,
      },
      {
        path: '/visitas',
        element: <ProtectedRoute roles={['ADMIN', 'PROFESIONAL']}><Visitas /></ProtectedRoute>,
      },
      {
        path: '/pagos',
        element: <ProtectedRoute roles={['ADMIN']}><Pagos /></ProtectedRoute>,
      },
      {
        path: '/mis-pagos',
        element: <ProtectedRoute roles={['CLIENTE']}><MisPagos /></ProtectedRoute>,
      },
      {
        path: '/mis-actividades',
        element: <ProtectedRoute roles={['CLIENTE']}><MisActividades /></ProtectedRoute>,
      },
    ],
  },
  {
    path: '/',
    element: <Navigate to="/dashboard" replace />,
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>
);
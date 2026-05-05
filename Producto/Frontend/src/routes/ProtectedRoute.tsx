import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: ReactNode;
  roles?: string[];
}

export default function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { token, rol, cargando } = useAuth();

  if (cargando) return <div>Cargando...</div>;
  if (!token) return <Navigate to="/login" replace />;
  if (roles && rol && !roles.includes(rol)) {
    return <div>No tienes permiso para esta acción.</div>;
  }

  return <>{children}</>;
}

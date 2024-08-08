import React, { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthContext } from '@contexts/AuthContext';

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isLoggedIn } = useAuthContext();
  const location = useLocation();

  // Verificación si el usuario ha iniciado sesión
  if (!isLoggedIn && location.pathname !== '/logIn') {
    // De no haber, redirecciona a logIn automáticamente
    return <Navigate to="/logIn" />;
  }

  return (
    <>
      {children}
    </>
  );
}

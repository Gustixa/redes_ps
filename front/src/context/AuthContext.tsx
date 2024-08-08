import React, {
  createContext, useState, useContext, useMemo, useEffect, ReactNode,
} from 'react';

interface AuthUser {
  correo: string;
}

interface AuthContextType {
  authUser: AuthUser | null;
  setAuthUser: React.Dispatch<React.SetStateAction<AuthUser | null>>;
  isLoggedIn: boolean;
  setIsLoggedIn: React.Dispatch<React.SetStateAction<boolean>>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
}

export function useAuthContext() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext debe ser utilizado dentro de un AuthProvider');
  }
  return context;
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [authUser, setAuthUser] = useState<AuthUser | null>({
    correo: '',
  });
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // Verificar si hay información de usuario almacenada en localStorage al cargar la página
    const storedUser = localStorage.getItem('authUser');
    if (storedUser) {
      const parsedUser = JSON.parse(storedUser) as AuthUser;
      setAuthUser(parsedUser);
      setIsLoggedIn(true);
    }
  }, []);

  const logout = () => {
    // Restablecer la información de usuario y el estado de inicio de sesión al cerrar sesión
    localStorage.removeItem('authUser');
    setAuthUser(null);
    setIsLoggedIn(false);
  };

  const value = useMemo(() => ({
    authUser,
    setAuthUser,
    isLoggedIn,
    setIsLoggedIn,
    logout,
  }), [authUser, isLoggedIn]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

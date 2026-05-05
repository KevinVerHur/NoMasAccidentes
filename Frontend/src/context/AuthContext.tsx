import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';
import type { Rol } from '../types';

interface DatosJwt {
  email: string;
  rol: Rol;
}

interface AuthContextValue {
  token: string | null;
  email: string | null;
  rol: Rol | null;
  cargando: boolean;
  iniciarSesion: (token: string) => void;
  cerrarSesion: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function decodificarJwt(token: string): DatosJwt | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      email: payload.sub,
      rol: payload.rol,
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem('nma_token')
  );

  const datos = token ? decodificarJwt(token) : null;
  const email = datos?.email ?? null;
  const rol = datos?.rol ?? null;
  const cargando = false;

  function iniciarSesion(nuevoToken: string) {
    localStorage.setItem('nma_token', nuevoToken);
    setToken(nuevoToken);
  }

  function cerrarSesion() {
    localStorage.removeItem('nma_token');
    setToken(null);
  }

  return (
    <AuthContext.Provider
      value={{ token, email, rol, cargando, iniciarSesion, cerrarSesion }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);

  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }

  return ctx;
}

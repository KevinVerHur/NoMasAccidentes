import { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import type { LoginRequest } from '../types';
import { login } from '../api/auth';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginRequest>();
  const { iniciarSesion } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const registroExitoso = (location.state as { registroExitoso?: boolean } | null)?.registroExitoso ?? false;
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);

  async function onSubmit(data: LoginRequest) {
    setError(null);
    setCargando(true);
    try {
      const { token } = await login(data);
      iniciarSesion(token);
      navigate('/dashboard');
    } catch {
      setError('Credenciales incorrectas. Verifica tu email y contraseña.');
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="min-h-screen bg-fondo flex items-center justify-center px-4">
      <div className="w-full max-w-[400px]">

        {/* Logo / Header */}
        <div className="text-center mb-8">
          <div className="text-[32px] mb-2">🦺</div>
          <h1 className="text-[24px] font-bold text-azul m-0">
            <span className="text-dorado">No Más</span> Accidentes
          </h1>
          <p className="text-[13px] text-gray-400 mt-1">Sistema de gestión de seguridad laboral</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-xl shadow-md px-8 py-8">
          <h2 className="text-[16px] font-bold text-azul mb-4 m-0">Iniciar sesión</h2>

          {registroExitoso && (
            <div className="bg-green-50 text-green-700 text-[12px] px-3 py-2 rounded-md mb-4 border border-green-200 flex items-center gap-2">
              ✅ <span>¡Registro exitoso! Ya puedes iniciar sesión.</span>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            {/* Email */}
            <div className="mb-4">
              <label className="block text-[11px] font-bold text-gray-500 mb-1">
                Email
              </label>
              <input
                type="email"
                placeholder="usuario@empresa.cl"
                className={`w-full px-3 py-2 border rounded-md text-[13px] outline-none transition-colors
                  ${errors.email
                    ? 'border-peligro focus:border-peligro'
                    : 'border-gray-300 focus:border-azul'
                  }`}
                {...register('email', { required: 'El email es obligatorio' })}
              />
              {errors.email && (
                <span className="text-[11px] text-peligro mt-1 block">{errors.email.message}</span>
              )}
            </div>

            {/* Contraseña */}
            <div className="mb-6">
              <label className="block text-[11px] font-bold text-gray-500 mb-1">
                Contraseña
              </label>
              <input
                type="password"
                placeholder="••••••••"
                className={`w-full px-3 py-2 border rounded-md text-[13px] outline-none transition-colors
                  ${errors.password
                    ? 'border-peligro focus:border-peligro'
                    : 'border-gray-300 focus:border-azul'
                  }`}
                {...register('password', { required: 'La contraseña es obligatoria' })}
              />
              {errors.password && (
                <span className="text-[11px] text-peligro mt-1 block">{errors.password.message}</span>
              )}
            </div>

            {/* Error de servidor */}
            {error && (
              <div className="bg-red-50 text-red-700 text-[12px] px-3 py-2 rounded-md mb-4 border border-red-200">
                {error}
              </div>
            )}

            {/* Botón */}
            <button
              type="submit"
              disabled={cargando}
              className="w-full bg-azul hover:bg-[#162d4a] text-white font-bold py-2.5 rounded-md text-[13px] cursor-pointer border-none transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {cargando ? 'Ingresando...' : 'Ingresar'}
            </button>
          </form>
        </div>

        <p className="text-center text-[12px] text-gray-500 mt-5">
          ¿No tienes cuenta?{' '}
          <Link to="/registro" className="text-azul font-bold hover:underline">
            Regístrate
          </Link>
        </p>

        <p className="text-center text-[11px] text-gray-400 mt-3">
          © 2026 No Más Accidentes
        </p>
      </div>
    </div>
  );
}

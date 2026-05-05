import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import type { RegistroRequest } from '../types';
import { registro } from '../api/auth';

export default function Registro() {
  const { register, handleSubmit, formState: { errors }, watch } = useForm<RegistroRequest>();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);

  async function onSubmit(data: RegistroRequest) {
    setError(null);
    setCargando(true);
    try {
      await registro({ ...data, idRol: 3 });
      navigate('/login', { state: { registroExitoso: true } });
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })
        ?.response?.data?.mensaje;
      setError(msg ?? 'No se pudo completar el registro. Intenta nuevamente.');
    } finally {
      setCargando(false);
    }
  }

  const inputClass = (hasError: boolean) =>
    `w-full px-3 py-2 border rounded-md text-[13px] outline-none transition-colors ${
      hasError ? 'border-peligro' : 'border-gray-300 focus:border-azul'
    }`;

  return (
    <div className="min-h-screen bg-fondo flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-[480px]">

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="text-[32px] mb-2">🦺</div>
          <h1 className="text-[24px] font-bold text-azul m-0">
            <span className="text-dorado">No Más</span> Accidentes
          </h1>
          <p className="text-[13px] text-gray-400 mt-1">Sistema de gestión de seguridad laboral</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-xl shadow-md px-8 py-8">
          <h2 className="text-[16px] font-bold text-azul mb-6 m-0">Crear cuenta</h2>

          <form onSubmit={handleSubmit(onSubmit)} noValidate>

            {/* Nombre + Apellido */}
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div>
                <label className="block text-[11px] font-bold text-gray-500 mb-1">Nombre</label>
                <input
                  type="text"
                  placeholder="Juan"
                  className={inputClass(!!errors.nombre)}
                  {...register('nombre', { required: 'Obligatorio', maxLength: { value: 120, message: 'Máx. 120 caracteres' } })}
                />
                {errors.nombre && <span className="text-[11px] text-peligro mt-1 block">{errors.nombre.message}</span>}
              </div>
              <div>
                <label className="block text-[11px] font-bold text-gray-500 mb-1">Apellido</label>
                <input
                  type="text"
                  placeholder="Pérez"
                  className={inputClass(!!errors.apellido)}
                  {...register('apellido', { required: 'Obligatorio', maxLength: { value: 120, message: 'Máx. 120 caracteres' } })}
                />
                {errors.apellido && <span className="text-[11px] text-peligro mt-1 block">{errors.apellido.message}</span>}
              </div>
            </div>

            {/* Email */}
            <div className="mb-4">
              <label className="block text-[11px] font-bold text-gray-500 mb-1">Email</label>
              <input
                type="email"
                placeholder="usuario@empresa.cl"
                className={inputClass(!!errors.email)}
                {...register('email', {
                  required: 'El email es obligatorio',
                  pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email no válido' },
                  maxLength: { value: 120, message: 'Máx. 120 caracteres' },
                })}
              />
              {errors.email && <span className="text-[11px] text-peligro mt-1 block">{errors.email.message}</span>}
            </div>

            {/* Contraseña */}
            <div className="mb-6">
              <label className="block text-[11px] font-bold text-gray-500 mb-1">Contraseña</label>
              <input
                type="password"
                placeholder="Mínimo 8 caracteres"
                className={inputClass(!!errors.password)}
                {...register('password', {
                  required: 'La contraseña es obligatoria',
                  minLength: { value: 8, message: 'Mínimo 8 caracteres' },
                  maxLength: { value: 100, message: 'Máx. 100 caracteres' },
                })}
              />
              {errors.password && <span className="text-[11px] text-peligro mt-1 block">{errors.password.message}</span>}
              {!errors.password && watch('password') && watch('password').length < 8 && (
                <span className="text-[11px] text-gray-400 mt-1 block">{watch('password').length}/8 caracteres mínimos</span>
              )}
            </div>

            {/* Error servidor */}
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
              {cargando ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>
        </div>

        {/* Link al login */}
        <p className="text-center text-[12px] text-gray-500 mt-5">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-azul font-bold hover:underline">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
}

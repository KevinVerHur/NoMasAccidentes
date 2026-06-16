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
    const state = location.state as { registroExitoso?: boolean; passwordRestablecida?: boolean } | null;
    const registroExitoso = state?.registroExitoso ?? false;
    const passwordRestablecida = state?.passwordRestablecida ?? false;
    const [error, setError] = useState<string | null>(null);
    const [cargando, setCargando] = useState(false);
    const [mostrarPassword, setMostrarPassword] = useState(false);

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
        <div className="auth-page">
            <div className="auth-container">

                <div className="auth-header">
                    <div className="auth-logo">🦺</div>
                    <h1 className="auth-title"><span>No Más</span> Accidentes</h1>
                    <p className="auth-subtitle">Sistema de gestión de seguridad laboral</p>
                </div>

                <div className="auth-card">
                    <h2 className="auth-card-title">Iniciar sesión</h2>

                    {registroExitoso && (
                        <div className="auth-alert auth-alert--success">
                            ✅ <span>¡Registro exitoso! Ya puedes iniciar sesión.</span>
                        </div>
                    )}

                    {passwordRestablecida && (
                        <div className="auth-alert auth-alert--success">
                            ✅ <span>¡Contraseña actualizada! Ya puedes iniciar sesión.</span>
                        </div>
                    )}

                    <form onSubmit={handleSubmit(onSubmit)} noValidate>
                        <div className="auth-field">
                            <label className="auth-label">Email</label>
                            <input
                                type="email"
                                placeholder="usuario@empresa.cl"
                                className={`auth-input ${errors.email ? 'auth-input--error' : ''}`}
                                {...register('email', { required: 'El email es obligatorio' })}
                            />
                            {errors.email && <span className="auth-field-error">{errors.email.message}</span>}
                        </div>

                        <div className="auth-field">
                            <label className="auth-label">Contraseña</label>

                            <div className="auth-password-wrapper">
                                <input
                                    type={mostrarPassword ? 'text' : 'password'}
                                    placeholder="••••••••"
                                    className={`auth-input auth-input--password ${errors.password ? 'auth-input--error' : ''}`}
                                    {...register('password', { required: 'La contraseña es obligatoria' })}
                                />

                               <button
                                    type="button"
                                    className="auth-password-toggle"
                                    onClick={() => setMostrarPassword(!mostrarPassword)}
                                    aria-label={mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                                >
                                    {mostrarPassword ? (
                                        // Ojo cerrado
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                            <path
                                                d="M3 3L21 21"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M10.6 10.6C10.2 11 10 11.5 10 12C10 13.1 10.9 14 12 14C12.5 14 13 13.8 13.4 13.4"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M9.9 5.2C10.6 5.1 11.3 5 12 5C16.5 5 20.3 7.9 22 12C21.5 13.2 20.8 14.3 19.9 15.2"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                            <path
                                                d="M6.1 6.8C4.4 8 3 9.8 2 12C3.7 16.1 7.5 19 12 19C13.8 19 15.5 18.5 16.9 17.7"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                        </svg>
                                    ) : (
                                        // Ojo abierto
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                            <path
                                                d="M2 12C3.7 7.9 7.5 5 12 5C16.5 5 20.3 7.9 22 12C20.3 16.1 16.5 19 12 19C7.5 19 3.7 16.1 2 12Z"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                            <path
                                                d="M12 15C13.7 15 15 13.7 15 12C15 10.3 13.7 9 12 9C10.3 9 9 10.3 9 12C9 13.7 10.3 15 12 15Z"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                        </svg>
                                    )}
                                </button>
                            </div>

                            {errors.password && <span className="auth-field-error">{errors.password.message}</span>}
                        </div>

                        {error && (
                            <div className="auth-alert auth-alert--error auth-alert--closable">
                                <span>{error}</span>

                                <button
                                    type="button"
                                    className="auth-alert-close"
                                    onClick={() => setError(null)}
                                    aria-label="Cerrar mensaje de error"
                                >
                                    ×
                                </button>
                            </div>
                        )}

                        <button type="submit" disabled={cargando} className="auth-btn">
                            {cargando ? 'Ingresando...' : 'Ingresar'}
                        </button>
                    </form>

                    <Link to="/recuperar-contrasena" className="auth-link-secondary">
                        ¿Olvidaste tu contraseña?
                    </Link>
                </div>

                <p className="auth-footer">
                    ¿No tienes cuenta?{' '}
                    <Link to="/registro">Regístrate</Link>
                </p>

                <p className="auth-copyright">© 2026 No Más Accidentes</p>
            </div>
        </div>
    );
}

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
                            <input
                                type="password"
                                placeholder="••••••••"
                                className={`auth-input ${errors.password ? 'auth-input--error' : ''}`}
                                {...register('password', { required: 'La contraseña es obligatoria' })}
                            />
                            {errors.password && <span className="auth-field-error">{errors.password.message}</span>}
                        </div>

                        {error && <div className="auth-alert auth-alert--error">{error}</div>}

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

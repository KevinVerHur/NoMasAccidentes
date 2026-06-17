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

    return (
        <div className="auth-page">
            <div className="auth-container">

                <div className="auth-header">
                    <div className="auth-logo">🦺</div>
                    <h1 className="auth-title"><span>No Más</span> Accidentes</h1>
                    <p className="auth-subtitle">Sistema de gestión de seguridad laboral</p>
                </div>

                <div className="auth-card">
                    <h2 className="auth-card-title">Crear cuenta</h2>

                    <form onSubmit={handleSubmit(onSubmit)} noValidate>

                        <div className="auth-grid-2">
                            <div>
                                <label className="auth-label">Nombre</label>
                                <input
                                    type="text"
                                    placeholder="Juan"
                                    className={`auth-input ${errors.nombre ? 'auth-input--error' : ''}`}
                                    {...register('nombre', { required: 'Obligatorio', maxLength: { value: 120, message: 'Máx. 120 caracteres' } })}
                                />
                                {errors.nombre && <span className="auth-field-error">{errors.nombre.message}</span>}
                            </div>
                            <div>
                                <label className="auth-label">Apellido</label>
                                <input
                                    type="text"
                                    placeholder="Pérez"
                                    className={`auth-input ${errors.apellido ? 'auth-input--error' : ''}`}
                                    {...register('apellido', { required: 'Obligatorio', maxLength: { value: 120, message: 'Máx. 120 caracteres' } })}
                                />
                                {errors.apellido && <span className="auth-field-error">{errors.apellido.message}</span>}
                            </div>
                        </div>

                        <div className="auth-field">
                            <label className="auth-label">Email</label>
                            <input
                                type="email"
                                placeholder="usuario@empresa.cl"
                                className={`auth-input ${errors.email ? 'auth-input--error' : ''}`}
                                {...register('email', {
                                    required: 'El email es obligatorio',
                                    pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email no válido' },
                                    maxLength: { value: 120, message: 'Máx. 120 caracteres' },
                                })}
                            />
                            {errors.email && <span className="auth-field-error">{errors.email.message}</span>}
                        </div>

                        <div className="auth-field">
                            <label className="auth-label">Contraseña</label>
                            <input
                                type="password"
                                placeholder="Mínimo 8 caracteres, mayúscula, número y símbolo"
                                className={`auth-input ${errors.password ? 'auth-input--error' : ''}`}
                                {...register('password', {
                                    required: 'La contraseña es obligatoria',
                                    minLength: { value: 8, message: 'Mínimo 8 caracteres' },
                                    maxLength: { value: 100, message: 'Máx. 100 caracteres' },
                                    pattern: {
                                        value: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                                        message: 'Debe incluir una mayúscula, un número y un símbolo',
                                    },
                                })}
                            />
                            {errors.password && <span className="auth-field-error">{errors.password.message}</span>}
                            {!errors.password && watch('password') && watch('password').length < 8 && (
                                <span className="auth-field-error" style={{ color: '#9ca3af' }}>
                  {watch('password').length}/8 caracteres mínimos
                </span>
                            )}
                        </div>

                        {error && <div className="auth-alert auth-alert--error">{error}</div>}

                        <button type="submit" disabled={cargando} className="auth-btn">
                            {cargando ? 'Creando cuenta...' : 'Crear cuenta'}
                        </button>
                    </form>
                </div>

                <p className="auth-footer">
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/login">Inicia sesión</Link>
                </p>

                <p className="auth-copyright">© 2026 No Más Accidentes</p>
            </div>
        </div>
    );
}

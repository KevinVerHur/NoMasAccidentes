import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { restablecerPassword } from '../api/auth';

interface FormData {
    nuevaPassword: string;
    confirmarPassword: string;
}

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';
    const navigate = useNavigate();
    const { register, handleSubmit, watch, formState: { errors } } = useForm<FormData>();
    const [error, setError] = useState<string | null>(null);
    const [cargando, setCargando] = useState(false);

    async function onSubmit(data: FormData) {
        setError(null);
        setCargando(true);
        try {
            await restablecerPassword(token, data.nuevaPassword);
            navigate('/login', { state: { passwordRestablecida: true } });
        } catch {
            setError('El enlace es inválido o ya expiró. Solicita uno nuevo.');
        } finally {
            setCargando(false);
        }
    }

    if (!token) {
        return (
            <div className="auth-page">
                <div className="auth-container">
                    <div className="auth-card" style={{ textAlign: 'center' }}>
                        <p style={{ fontSize: 13, color: '#6b7280', marginBottom: 16 }}>
                            Enlace inválido. Solicita un nuevo correo de recuperación.
                        </p>
                        <Link to="/recuperar-contrasena" className="auth-btn" style={{ display: 'inline-block', width: 'auto', padding: '9px 20px' }}>
                            Solicitar recuperación
                        </Link>
                    </div>
                </div>
            </div>
        );
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
                    <h2 className="auth-card-title">Nueva contraseña</h2>
                    <p className="auth-info">Elige una contraseña segura de al menos 6 caracteres.</p>

                    <form onSubmit={handleSubmit(onSubmit)} noValidate>
                        <div className="auth-field">
                            <label className="auth-label">Nueva contraseña</label>
                            <input
                                type="password"
                                placeholder="••••••••"
                                className={`auth-input ${errors.nuevaPassword ? 'auth-input--error' : ''}`}
                                {...register('nuevaPassword', {
                                    required: 'La contraseña es obligatoria',
                                    minLength: { value: 6, message: 'Mínimo 6 caracteres' },
                                })}
                            />
                            {errors.nuevaPassword && <span className="auth-field-error">{errors.nuevaPassword.message}</span>}
                        </div>

                        <div className="auth-field">
                            <label className="auth-label">Confirmar contraseña</label>
                            <input
                                type="password"
                                placeholder="••••••••"
                                className={`auth-input ${errors.confirmarPassword ? 'auth-input--error' : ''}`}
                                {...register('confirmarPassword', {
                                    required: 'Confirma la contraseña',
                                    validate: (val) => val === watch('nuevaPassword') || 'Las contraseñas no coinciden',
                                })}
                            />
                            {errors.confirmarPassword && <span className="auth-field-error">{errors.confirmarPassword.message}</span>}
                        </div>

                        {error && <div className="auth-alert auth-alert--error">{error}</div>}

                        <button type="submit" disabled={cargando} className="auth-btn">
                            {cargando ? 'Guardando...' : 'Guardar nueva contraseña'}
                        </button>
                    </form>
                </div>

                <p className="auth-footer">
                    <Link to="/login">← Volver al inicio de sesión</Link>
                </p>

                <p className="auth-copyright">© 2026 No Más Accidentes</p>
            </div>
        </div>
    );
}

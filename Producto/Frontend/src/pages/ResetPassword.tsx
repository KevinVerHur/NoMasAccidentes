import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { restablecerPassword, validarTokenReset } from '../api/auth';

type EstadoToken = 'validando' | 'valido' | 'invalido';

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
    const [estadoToken, setEstadoToken] = useState<EstadoToken>('validando');
    const [mostrarNuevaPassword, setMostrarNuevaPassword] = useState(false);
    const [mostrarConfirmarPassword, setMostrarConfirmarPassword] = useState(false);

    // Valida el enlace apenas se abre la página, para avisar de inmediato si ya expiró
    // o fue usado, sin esperar a que el usuario intente cambiar la contraseña.
    useEffect(() => {
        if (!token) {
            setEstadoToken('invalido');
            return;
        }
        let activo = true;
        validarTokenReset(token)
            .then((valido) => {
                if (activo) setEstadoToken(valido ? 'valido' : 'invalido');
            })
            .catch(() => {
                if (activo) setEstadoToken('invalido');
            });
        return () => {
            activo = false;
        };
    }, [token]);

    const iconoOjoAbierto = (
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
);

const iconoOjoCerrado = (
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
);

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

    if (estadoToken === 'validando') {
        return (
            <div className="auth-page">
                <div className="auth-container">
                    <div className="auth-card" style={{ textAlign: 'center' }}>
                        <p style={{ fontSize: 13, color: '#6b7280', margin: 0 }}>
                            Validando enlace...
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    if (estadoToken === 'invalido') {
        return (
            <div className="auth-page">
                <div className="auth-container">
                    <div className="auth-card" style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: 32, marginBottom: 12 }}>⏳</div>
                        <p style={{ fontWeight: 'bold', color: '#374151', marginBottom: 6 }}>Enlace no válido</p>
                        <p style={{ fontSize: 13, color: '#6b7280', marginBottom: 16 }}>
                            Este enlace de recuperación expiró o ya fue usado. Solicita un nuevo correo.
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
                    <p className="auth-info">Elige una contraseña segura de al menos 8 caracteres, con una mayúscula, un número y un símbolo.</p>

                    <form onSubmit={handleSubmit(onSubmit)} noValidate>
                        <div className="auth-field">
                            <label className="auth-label">Nueva contraseña</label>

                            <div className="auth-password-wrapper">
                                <input
                                    type={mostrarNuevaPassword ? 'text' : 'password'}
                                    placeholder="••••••••"
                                    className={`auth-input auth-input--password ${errors.nuevaPassword ? 'auth-input--error' : ''}`}
                                    {...register('nuevaPassword', {
                                        required: 'La contraseña es obligatoria',
                                        minLength: { value: 8, message: 'Mínimo 8 caracteres' },
                                        pattern: {
                                            value: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                                            message: 'Debe incluir una mayúscula, un número y un símbolo',
                                        },
                                    })}
                                />

                                <button
                                    type="button"
                                    className="auth-password-toggle"
                                    onClick={() => setMostrarNuevaPassword(!mostrarNuevaPassword)}
                                    aria-label={mostrarNuevaPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                                >
                                    {mostrarNuevaPassword ? iconoOjoCerrado : iconoOjoAbierto}
                                </button>
                            </div>

                            {errors.nuevaPassword && <span className="auth-field-error">{errors.nuevaPassword.message}</span>}
                        </div>

                        <div className="auth-field">
                            <label className="auth-label">Confirmar contraseña</label>

                            <div className="auth-password-wrapper">
                                <input
                                    type={mostrarConfirmarPassword ? 'text' : 'password'}
                                    placeholder="••••••••"
                                    className={`auth-input auth-input--password ${errors.confirmarPassword ? 'auth-input--error' : ''}`}
                                    {...register('confirmarPassword', {
                                        required: 'Confirma la contraseña',
                                        validate: (val) => val === watch('nuevaPassword') || 'Las contraseñas no coinciden',
                                    })}
                                />

                                <button
                                    type="button"
                                    className="auth-password-toggle"
                                    onClick={() => setMostrarConfirmarPassword(!mostrarConfirmarPassword)}
                                    aria-label={mostrarConfirmarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                                >
                                    {mostrarConfirmarPassword ? iconoOjoCerrado : iconoOjoAbierto}
                                </button>
                            </div>

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

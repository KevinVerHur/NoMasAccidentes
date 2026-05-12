import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { solicitarRecuperacion } from '../api/auth';

interface FormData {
  email: string;
}

export default function ForgotPassword() {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>();
  const [enviado, setEnviado] = useState(false);
  const [cargando, setCargando] = useState(false);

  async function onSubmit(data: FormData) {
    setCargando(true);
    try {
      await solicitarRecuperacion(data.email);
      setEnviado(true);
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
          <h2 className="auth-card-title">Recuperar contraseña</h2>

          {!enviado ? (
            <>
              <p className="auth-info">
                Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña.
              </p>

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

                <button type="submit" disabled={cargando} className="auth-btn">
                  {cargando ? 'Enviando...' : 'Enviar enlace'}
                </button>
              </form>
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '16px 0' }}>
              <div style={{ fontSize: 32, marginBottom: 12 }}>📧</div>
              <p style={{ fontWeight: 'bold', color: '#374151', marginBottom: 6 }}>¡Correo enviado!</p>
              <p className="auth-info" style={{ margin: 0 }}>
                Si tu email está registrado, recibirás un enlace para restablecer tu contraseña.
                Revisa también la carpeta de spam.
              </p>
            </div>
          )}
        </div>

        <p className="auth-footer">
          <Link to="/login">← Volver al inicio de sesión</Link>
        </p>

        <p className="auth-copyright">© 2026 No Más Accidentes</p>
      </div>
    </div>
  );
}

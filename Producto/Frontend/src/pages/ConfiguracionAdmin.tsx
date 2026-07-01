import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import Panel from '../components/ui/Panel';
import {
  obtenerMiPerfil,
  actualizarMiPerfil,
  cambiarMiPassword,
} from '../api/auth';
import type {
  ActualizarPerfilRequest,
  CambiarPasswordRequest,
  UsuarioResponse,
} from '../types';





function formatearFecha(valor: string | null) {
  if (!valor) return 'Sin registro';

  return new Date(valor).toLocaleDateString('es-CL', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ConfiguracionAdmin() {
  const [perfil, setPerfil] = useState<UsuarioResponse | null>(null);
  const [cargando, setCargando] = useState(true);
  const [guardandoPerfil, setGuardandoPerfil] = useState(false);
  const [guardandoPassword, setGuardandoPassword] = useState(false);
  const [mensajePerfil, setMensajePerfil] = useState<string | null>(null);
  const [mensajePassword, setMensajePassword] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const formPerfil = useForm<ActualizarPerfilRequest>();
  const formPassword = useForm<CambiarPasswordRequest & { confirmarPassword: string }>();

  useEffect(() => {
    async function cargarPerfil() {
      setCargando(true);
      setError(null);

      try {
        const data = await obtenerMiPerfil();
        setPerfil(data);

        formPerfil.reset({
          nombre: data.nombre,
          apellido: data.apellido,
          email: data.email,
        });
      } catch {
        setError('No se pudo cargar la información de la cuenta.');
      } finally {
        setCargando(false);
      }
    }

    cargarPerfil();
  }, [formPerfil]);

  async function onGuardarPerfil(data: ActualizarPerfilRequest) {
    setGuardandoPerfil(true);
    setMensajePerfil(null);
    setError(null);

    try {
      const actualizado = await actualizarMiPerfil(data);
      setPerfil(actualizado);
      setMensajePerfil('Perfil actualizado correctamente.');
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'No se pudo actualizar el perfil.');
    } finally {
      setGuardandoPerfil(false);
    }
  }

  async function onCambiarPassword(
    data: CambiarPasswordRequest & { confirmarPassword: string }
  ) {
    setMensajePassword(null);
    setError(null);

    if (data.passwordNueva !== data.confirmarPassword) {
      setError('La nueva contraseña y la confirmación no coinciden.');
      return;
    }

    setGuardandoPassword(true);

    try {
      await cambiarMiPassword({
        passwordActual: data.passwordActual,
        passwordNueva: data.passwordNueva,
      });

      formPassword.reset();
      setMensajePassword('Contraseña actualizada correctamente.');
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'No se pudo cambiar la contraseña.');
    } finally {
      setGuardandoPassword(false);
    }
  }

 

  return (
    <>
      <div className="page-title">Configuración</div>
      <div className="page-subtitle">Cuenta administrativa y preferencias del sistema</div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          <span>●</span>
          <div>{error}</div>
        </div>
      )}

      <div className="grid-2">
        <Panel titulo="Perfil de administrador">
          {cargando ? (
            <div className="placeholder">Cargando perfil...</div>
          ) : (
            <form
              onSubmit={formPerfil.handleSubmit(onGuardarPerfil)}
              style={{ padding: 16 }}
              noValidate
            >
              <div className="form-grid">
                <div>
                  <label className="auth-label">Nombre</label>
                  <input
                    className={`auth-input ${formPerfil.formState.errors.nombre ? 'auth-input--error' : ''}`}
                    {...formPerfil.register('nombre', { required: 'El nombre es obligatorio' })}
                  />
                  {formPerfil.formState.errors.nombre && (
                    <span className="auth-field-error">
                      {formPerfil.formState.errors.nombre.message}
                    </span>
                  )}
                </div>

                <div>
                  <label className="auth-label">Apellido</label>
                  <input
                    className={`auth-input ${formPerfil.formState.errors.apellido ? 'auth-input--error' : ''}`}
                    {...formPerfil.register('apellido', { required: 'El apellido es obligatorio' })}
                  />
                  {formPerfil.formState.errors.apellido && (
                    <span className="auth-field-error">
                      {formPerfil.formState.errors.apellido.message}
                    </span>
                  )}
                </div>

                <div className="span2">
                  <label className="auth-label">Email</label>
                  <input
                    type="email"
                    className={`auth-input ${formPerfil.formState.errors.email ? 'auth-input--error' : ''}`}
                    {...formPerfil.register('email', {
                      required: 'El email es obligatorio',
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: 'Email inválido',
                      },
                    })}
                  />
                  {formPerfil.formState.errors.email && (
                    <span className="auth-field-error">
                      {formPerfil.formState.errors.email.message}
                    </span>
                  )}
                </div>
              </div>

              <div style={{ marginTop: 14, display: 'flex', justifyContent: 'flex-end' }}>
                <button className="btn btn-primary" type="submit" disabled={guardandoPerfil}>
                  {guardandoPerfil ? 'Guardando...' : 'Guardar perfil'}
                </button>
              </div>

              {mensajePerfil && (
                <div className="auth-alert auth-alert--success" style={{ marginTop: 12 }}>
                  {mensajePerfil}
                </div>
              )}
            </form>
          )}
        </Panel>

        <Panel titulo="Información de la cuenta">
          <div style={{ padding: 16, display: 'grid', gap: 12, fontSize: 13 }}>
            <div>
              <div style={{ color: '#9ca3af', fontSize: 11 }}>Rol</div>
              <strong>{perfil?.rol ?? 'ADMIN'}</strong>
            </div>

            <div>
              <div style={{ color: '#9ca3af', fontSize: 11 }}>Estado</div>
              <strong>{perfil?.activo ? 'Activo' : 'Inactivo'}</strong>
            </div>

            <div>
              <div style={{ color: '#9ca3af', fontSize: 11 }}>Último acceso</div>
              <strong>{formatearFecha(perfil?.ultimoAcceso ?? null)}</strong>
            </div>

            <div>
              <div style={{ color: '#9ca3af', fontSize: 11 }}>Fecha de creación</div>
              <strong>{formatearFecha(perfil?.fechaCreacion ?? null)}</strong>
            </div>
          </div>
        </Panel>
      </div>

      <div className="grid-2">
        <Panel titulo="Cambiar contraseña">
          <form
            onSubmit={formPassword.handleSubmit(onCambiarPassword)}
            style={{ padding: 16 }}
            noValidate
          >
            <div style={{ display: 'grid', gap: 12 }}>
              <div>
                <label className="auth-label">Contraseña actual</label>
                <input
                  type="password"
                  className={`auth-input ${formPassword.formState.errors.passwordActual ? 'auth-input--error' : ''}`}
                  {...formPassword.register('passwordActual', {
                    required: 'La contraseña actual es obligatoria',
                  })}
                />
                {formPassword.formState.errors.passwordActual && (
                  <span className="auth-field-error">
                    {formPassword.formState.errors.passwordActual.message}
                  </span>
                )}
              </div>

              <div>
                <label className="auth-label">Nueva contraseña</label>
                <input
                  type="password"
                  className={`auth-input ${formPassword.formState.errors.passwordNueva ? 'auth-input--error' : ''}`}
                  {...formPassword.register('passwordNueva', {
                    required: 'La nueva contraseña es obligatoria',
                    minLength: {
                      value: 8,
                      message: 'Mínimo 8 caracteres',
                    },
                    pattern: {
                      value: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                      message: 'Debe incluir mayúscula, número y símbolo',
                    },
                  })}
                />
                {formPassword.formState.errors.passwordNueva && (
                  <span className="auth-field-error">
                    {formPassword.formState.errors.passwordNueva.message}
                  </span>
                )}
              </div>

              <div>
                <label className="auth-label">Confirmar nueva contraseña</label>
                <input
                  type="password"
                  className={`auth-input ${formPassword.formState.errors.confirmarPassword ? 'auth-input--error' : ''}`}
                  {...formPassword.register('confirmarPassword', {
                    required: 'Confirma la nueva contraseña',
                  })}
                />
                {formPassword.formState.errors.confirmarPassword && (
                  <span className="auth-field-error">
                    {formPassword.formState.errors.confirmarPassword.message}
                  </span>
                )}
              </div>
            </div>

            <div style={{ marginTop: 14, display: 'flex', justifyContent: 'flex-end' }}>
              <button className="btn btn-primary" type="submit" disabled={guardandoPassword}>
                {guardandoPassword ? 'Actualizando...' : 'Cambiar contraseña'}
              </button>
            </div>

            {mensajePassword && (
              <div className="auth-alert auth-alert--success" style={{ marginTop: 12 }}>
                {mensajePassword}
              </div>
            )}
          </form>
        </Panel>

      </div>
    </>
  );
}
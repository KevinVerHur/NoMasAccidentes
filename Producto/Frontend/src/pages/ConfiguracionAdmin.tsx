import { useEffect,  useState } from 'react';
import { useForm } from 'react-hook-form';

import Modal from '../components/ui/Modal';
import Panel from '../components/ui/Panel';
import {
  actualizarMiPerfil,
  cambiarMiPassword,
  obtenerMiPerfil,
} from '../api/auth';
import {
  actualizarConfiguracionEmpresa,
  obtenerConfiguracionEmpresa,
} from '../api/configuracion';
import type {
  ActualizarPerfilRequest,
  UsuarioResponse,
} from '../types';

interface EmpresaAdmin {
  nombreEmpresa: string;
  rut: string;
  emailContacto: string;
  telefono: string;
  direccion: string;
  region: string;
}

interface PasswordForm {
  passwordActual: string;
  passwordNueva: string;
  confirmarPassword: string;
}





function mensajeError(e: unknown, fallback: string): string {
  const data = (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data;
  return data?.mensaje ?? data?.message ?? fallback;
}

function nombreCompleto(perfil: UsuarioResponse | null) {
  if (!perfil) return 'Sin registro';
  return `${perfil.nombre} ${perfil.apellido}`.trim();
}

function normalizarEmpresa(data: {
  nombreEmpresa: string;
  rut: string;
  emailContacto: string;
  telefono: string | null;
  direccion: string | null;
  region: string | null;
}): EmpresaAdmin {
  return {
    nombreEmpresa: data.nombreEmpresa,
    rut: data.rut,
    emailContacto: data.emailContacto,
    telefono: data.telefono ?? '',
    direccion: data.direccion ?? '',
    region: data.region ?? '',
  };
}

export default function ConfiguracionAdmin() {
  const [perfil, setPerfil] = useState<UsuarioResponse | null>(null);
  const [empresa, setEmpresa] = useState<EmpresaAdmin | null>(null);

  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [modalPerfil, setModalPerfil] = useState(false);
  const [modalPassword, setModalPassword] = useState(false);
  const [modalEmpresa, setModalEmpresa] = useState(false);

  const formPerfil = useForm<ActualizarPerfilRequest>();
  const formPassword = useForm<PasswordForm>();
  const formEmpresa = useForm<EmpresaAdmin>();


  useEffect(() => {
    async function cargarConfiguracion() {
      setCargando(true);
      setError(null);

      try {
        const [datosPerfil, datosEmpresa] = await Promise.all([
          obtenerMiPerfil(),
          obtenerConfiguracionEmpresa(),
        ]);

        const empresaNormalizada = normalizarEmpresa(datosEmpresa);

        setPerfil(datosPerfil);
        setEmpresa(empresaNormalizada);

        formPerfil.reset({
          nombre: datosPerfil.nombre,
          apellido: datosPerfil.apellido,
          email: datosPerfil.email,
        });

        formEmpresa.reset(empresaNormalizada);
      } catch (e: unknown) {
        setError(mensajeError(e, 'No se pudo cargar la configuración.'));
      } finally {
        setCargando(false);
      }
    }

    cargarConfiguracion();
  }, [formEmpresa, formPerfil]);



  function abrirPerfil() {
    if (perfil) {
      formPerfil.reset({
        nombre: perfil.nombre,
        apellido: perfil.apellido,
        email: perfil.email,
      });
    }

    setModalPerfil(true);
  }

  function abrirEmpresa() {
    if (empresa) {
      formEmpresa.reset(empresa);
    }

    setModalEmpresa(true);
  }

  async function onGuardarPerfil(data: ActualizarPerfilRequest) {
    setGuardando(true);
    setMensaje(null);
    setError(null);

    try {
      const actualizado = await actualizarMiPerfil(data);
      setPerfil(actualizado);
      setModalPerfil(false);
      setMensaje('Perfil actualizado correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo actualizar el perfil.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onCambiarPassword(data: PasswordForm) {
    setMensaje(null);
    setError(null);

    if (data.passwordNueva !== data.confirmarPassword) {
      setError('La nueva contraseña y la confirmación no coinciden.');
      return;
    }

    setGuardando(true);

    try {
      await cambiarMiPassword({
        passwordActual: data.passwordActual,
        passwordNueva: data.passwordNueva,
      });

      formPassword.reset();
      setModalPassword(false);
      setMensaje('Contraseña actualizada correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo cambiar la contraseña.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onGuardarEmpresa(data: EmpresaAdmin) {
    setGuardando(true);
    setMensaje(null);
    setError(null);

    try {
      const actualizada = await actualizarConfiguracionEmpresa(data);
      const empresaNormalizada = normalizarEmpresa(actualizada);

      setEmpresa(empresaNormalizada);
      formEmpresa.reset(empresaNormalizada);
      setModalEmpresa(false);
      setMensaje('Datos de empresa actualizados correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudieron actualizar los datos de empresa.'));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Configuración</div>
      <div className="page-subtitle">Datos de tu cuenta y empresa.</div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          <div>{error}</div>
        </div>
      )}

      {mensaje && (
        <div className="alert-item alert-item--ok" style={{ marginBottom: 12 }}>
          <div>{mensaje}</div>
        </div>
      )}

      {cargando ? (
        <div className="placeholder">Cargando configuración...</div>
      ) : (
        <>
          <div className="grid-2">
          
            <Panel titulo="Seguridad">
              <div style={{ padding: 16, display: 'grid', gap: 8, fontSize: 13 }}>
                <div>
                  <strong style={{ display: 'block', color: '#6b7280', fontSize: 11 }}>
                    Contraseña actual
                  </strong>
                  <span>••••••••••••</span>
                </div>

                <div>
                  <strong style={{ display: 'block', color: '#6b7280', fontSize: 11 }}>
                    Nueva contraseña
                  </strong>
                  <span>Sin cambios pendientes</span>
                </div>

                <div>
                  <strong style={{ display: 'block', color: '#6b7280', fontSize: 11 }}>
                    Confirmar contraseña
                  </strong>
                  <span>Sin cambios pendientes</span>
                </div>

                <div className="responsive-inline-fields" style={{ justifyContent: 'flex-end', marginTop: 6 }}>
                  <button className="btn btn-outline btn-sm" onClick={() => setModalPassword(true)}>
                    Cambiar contraseña
                  </button>
                </div>
              </div>
            </Panel>

            <Panel titulo="Perfil de usuario">
              <div style={{ padding: 16, display: 'grid', gap: 12 }}>
                <div className="form-grid">
                  <div>
                    <label className="auth-label">Nombre completo</label>
                    <input className="auth-input" value={nombreCompleto(perfil)} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Email</label>
                    <input className="auth-input" value={perfil?.email ?? 'Sin registro'} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Rol</label>
                    <input className="auth-input" value={perfil?.rol ?? 'ADMIN'} readOnly />
                  </div>
                </div>

                <div className="responsive-inline-fields" style={{ justifyContent: 'flex-end' }}>
                  <button className="btn btn-primary btn-sm" onClick={abrirPerfil}>
                    Guardar cambios
                  </button>
                </div>
              </div>
            </Panel>

            <Panel
              titulo="Datos empresa"
              accion={
                <button className="btn btn-outline btn-sm" onClick={abrirEmpresa}>
                  Editar
                </button>
              }
            >
              <div style={{ padding: 16 }}>
                <div className="form-grid">
                  <div>
                    <label className="auth-label">Nombre empresa</label>
                    <input className="auth-input" value={empresa?.nombreEmpresa ?? ''} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">RUT</label>
                    <input className="auth-input" value={empresa?.rut ?? ''} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Email contacto</label>
                    <input className="auth-input" value={empresa?.emailContacto ?? ''} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Teléfono</label>
                    <input className="auth-input" value={empresa?.telefono ?? ''} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Dirección</label>
                    <input className="auth-input" value={empresa?.direccion ?? ''} readOnly />
                  </div>

                  <div>
                    <label className="auth-label">Región</label>
                    <input className="auth-input" value={empresa?.region ?? ''} readOnly />
                  </div>
                </div>
              </div>
            </Panel>
          </div>

         
        </>
      )}

      <Modal
        abierto={modalPassword}
        titulo="Cambiar contraseña"
        ancho="sm"
        onCerrar={() => setModalPassword(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPassword(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-password-admin" type="submit" disabled={guardando}>
              {guardando ? 'Actualizando...' : 'Actualizar'}
            </button>
          </>
        }
      >
        <form id="form-password-admin" onSubmit={formPassword.handleSubmit(onCambiarPassword)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Contraseña actual</label>
              <input
                type="password"
                className="auth-input"
                {...formPassword.register('passwordActual', { required: true })}
              />
            </div>

            <div>
              <label className="auth-label">Nueva contraseña</label>
              <input
                type="password"
                className="auth-input"
                {...formPassword.register('passwordNueva', {
                  required: true,
                  minLength: 8,
                  pattern: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Confirmar contraseña</label>
              <input
                type="password"
                className="auth-input"
                {...formPassword.register('confirmarPassword', { required: true })}
              />
            </div>
          </div>
        </form>
      </Modal>

      <Modal
        abierto={modalEmpresa}
        titulo="Editar Datos Empresa"
        onCerrar={() => setModalEmpresa(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEmpresa(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-empresa-admin" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar configuración'}
            </button>
          </>
        }
      >
        <form id="form-empresa-admin" onSubmit={formEmpresa.handleSubmit(onGuardarEmpresa)} noValidate>
          <div className="form-grid">
            <div>
              <label className="auth-label">Nombre empresa</label>
              <input className="auth-input" {...formEmpresa.register('nombreEmpresa', { required: true })} />
            </div>

            <div>
              <label className="auth-label">RUT</label>
              <input
                className="auth-input"
                {...formEmpresa.register('rut', {
                  required: true,
                  pattern: /^\d{7,8}-[\dkK]$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Email contacto</label>
              <input
                type="email"
                className="auth-input"
                {...formEmpresa.register('emailContacto', {
                  required: true,
                  pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Teléfono</label>
              <input className="auth-input" {...formEmpresa.register('telefono')} />
            </div>

            <div className="span2">
              <label className="auth-label">Dirección</label>
              <input className="auth-input" {...formEmpresa.register('direccion')} />
            </div>

            <div>
              <label className="auth-label">Región</label>
              <select className="auth-input" {...formEmpresa.register('region')}>
                <option value="Metropolitana">Metropolitana</option>
                <option value="Valparaíso">Valparaíso</option>
                <option value="Biobío">Biobío</option>
                <option value="Coquimbo">Coquimbo</option>
                <option value="Antofagasta">Antofagasta</option>
                <option value="Los Lagos">Los Lagos</option>
              </select>
            </div>
          </div>
        </form>
      </Modal>

      <Modal
        abierto={modalPerfil}
        titulo="Editar Perfil Usuario"
        onCerrar={() => setModalPerfil(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPerfil(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-perfil-admin" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </>
        }
      >
        <form id="form-perfil-admin" onSubmit={formPerfil.handleSubmit(onGuardarPerfil)} noValidate>
          <div className="form-grid">
            <div>
              <label className="auth-label">Nombre</label>
              <input className="auth-input" {...formPerfil.register('nombre', { required: true })} />
            </div>

            <div>
              <label className="auth-label">Apellido</label>
              <input className="auth-input" {...formPerfil.register('apellido', { required: true })} />
            </div>

            <div className="span2">
              <label className="auth-label">Email</label>
              <input
                type="email"
                className="auth-input"
                {...formPerfil.register('email', {
                  required: true,
                  pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                })}
              />
            </div>
          </div>
        </form>
      </Modal>

    </>
  );
}

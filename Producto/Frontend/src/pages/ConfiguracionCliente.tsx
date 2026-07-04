import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { miCliente, miContacto, actualizarMiContacto } from '../api/clientes';
import { cambiarMiPassword } from '../api/auth';
import type {
  ActualizarMiContactoRequest,
  CambiarPasswordRequest,
  EmpresaResponse,
  EstadoEmpresa,
  MiContactoResponse,
  VarianteBadge,
} from '../types';

type PasswordForm = CambiarPasswordRequest & { confirmarPassword: string };

const badgePorEstado: Record<EstadoEmpresa, VarianteBadge> = {
  ACTIVO:     'green',
  MOROSO:     'yellow',
  SUSPENDIDO: 'red',
};

function mensajeError(e: unknown, fallback: string): string {
  const data = (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data;
  return data?.mensaje ?? data?.message ?? fallback;
}

export default function ConfiguracionCliente() {
  const [empresa, setEmpresa] = useState<EmpresaResponse | null>(null);
  const [contacto, setContacto] = useState<MiContactoResponse | null>(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [modalContacto, setModalContacto] = useState(false);
  const [modalPassword, setModalPassword] = useState(false);

  const form = useForm<ActualizarMiContactoRequest>();
  const formPassword = useForm<PasswordForm>();

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [datosEmpresa, datosContacto] = await Promise.all([miCliente(), miContacto()]);
      setEmpresa(datosEmpresa);
      setContacto(datosContacto);
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudieron cargar los datos de tu empresa.'));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  function abrirEditarContacto() {
    if (contacto) {
      form.reset({
        nombre: contacto.nombre,
        cargo: contacto.cargo ?? '',
        telefono: contacto.telefono ?? '',
      });
    }
    setMensaje(null);
    setError(null);
    setModalContacto(true);
  }

  async function onGuardarContacto(data: ActualizarMiContactoRequest) {
    setGuardando(true);
    setMensaje(null);
    setError(null);
    try {
      const actualizado = await actualizarMiContacto({
        nombre: data.nombre,
        cargo: data.cargo,
        telefono: data.telefono,
      });
      setContacto(actualizado);
      setModalContacto(false);
      setMensaje('Tus datos de contacto se actualizaron correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudieron actualizar tus datos de contacto.'));
    } finally {
      setGuardando(false);
    }
  }

  function abrirCambiarPassword() {
    formPassword.reset();
    setMensaje(null);
    setError(null);
    setModalPassword(true);
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
      setError(mensajeError(e, 'No se pudo actualizar la contraseña.'));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Configuración</div>
      <div className="page-subtitle">Datos de tu empresa, contacto y acceso</div>

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
        <div className="placeholder">Cargando...</div>
      ) : (
        <div className="grid-2">
          {/* ---- Datos de la empresa (solo lectura) ---- */}
          <section className="responsive-panel-section">
            <div className="flex items-center justify-between px-4 py-2 border-b border-gray-200">
              <strong className="text-azul text-[14px]">🏢 Datos de la empresa</strong>
              {empresa && (
                <Badge variante={badgePorEstado[empresa.estado]}>{empresa.estado}</Badge>
              )}
            </div>

            <div style={{ padding: '18px 20px 20px' }}>
              <div className="responsive-detail-grid responsive-detail-grid--client">
                <span>Razón social</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.razonSocial ?? '—'}</strong>

                <span>RUT</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.rut ?? '—'}</strong>

                <span>Rubro</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.nombreRubro ?? '—'}</strong>

                <span>Dirección</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.direccion ?? '—'}</strong>

                <span>Comuna</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.comuna ?? '—'}</strong>

                <span>Trabajadores</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.cantidadTrabajadores ?? '—'}</strong>

                <span>Plan</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.plan ?? '—'}</strong>

                <span>Profesional asignado</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{empresa?.nombreProfesional ?? 'Sin asignar'}</strong>
              </div>

              <p style={{ marginTop: 16, fontSize: 12, color: '#6b7280' }}>
                Estos datos los administra la consultora. Si necesitas corregir alguno, contáctala.
              </p>
            </div>
          </section>

          {/* ---- Mis datos de contacto (editable) ---- */}
          <section className="responsive-panel-section">
            <div className="flex items-center justify-between px-4 py-2 border-b border-gray-200">
              <strong className="text-azul text-[14px]">👤 Mis datos de contacto</strong>
              <button className="btn btn-sm btn-outline" onClick={abrirEditarContacto}>
                Editar contacto
              </button>
            </div>

            <div style={{ padding: '18px 20px 20px' }}>
              <div className="responsive-detail-grid responsive-detail-grid--compact">
                <span>Nombre</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{contacto?.nombre ?? '—'}</strong>

                <span>Cargo</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{contacto?.cargo ?? '—'}</strong>

                <span>Teléfono</span>
                <strong style={{ textAlign: 'right', fontWeight: 500 }}>{contacto?.telefono ?? '—'}</strong>

                <span>Email</span>
                <strong style={{ textAlign: 'right', fontWeight: 500, color: '#2563eb' }}>{contacto?.email ?? '—'}</strong>
              </div>

              <p style={{ marginTop: 16, fontSize: 12, color: '#6b7280' }}>
                Tu email es tu usuario de acceso y no puede modificarse aquí.
              </p>

              <button
                className="btn btn-sm btn-outline"
                style={{ marginTop: 12 }}
                onClick={abrirCambiarPassword}
              >
                Cambiar contraseña
              </button>
            </div>
          </section>
        </div>
      )}

      <Modal
        abierto={modalContacto}
        titulo="Editar mis datos de contacto"
        ancho="sm"
        onCerrar={() => setModalContacto(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalContacto(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-mi-contacto" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </>
        }
      >
        <form id="form-mi-contacto" onSubmit={form.handleSubmit(onGuardarContacto)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Nombre</label>
              <input className="auth-input" {...form.register('nombre', { required: true })} />
            </div>

            <div>
              <label className="auth-label">Cargo</label>
              <input className="auth-input" placeholder="Ej: Jefe de RRHH" {...form.register('cargo')} />
            </div>

            <div>
              <label className="auth-label">Teléfono</label>
              <input className="auth-input" placeholder="Ej: +56 9 1234 5678" {...form.register('telefono')} />
            </div>

            <div>
              <label className="auth-label">Email</label>
              <input className="auth-input" value={contacto?.email ?? ''} disabled readOnly />
              <span style={{ fontSize: 12, color: '#6b7280' }}>
                El email es tu usuario de acceso y no puede modificarse.
              </span>
            </div>
          </div>
        </form>
      </Modal>

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
            <button className="btn btn-primary" form="form-password-cliente" type="submit" disabled={guardando}>
              {guardando ? 'Actualizando...' : 'Actualizar contraseña'}
            </button>
          </>
        }
      >
        <form id="form-password-cliente" onSubmit={formPassword.handleSubmit(onCambiarPassword)} noValidate>
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
                placeholder="Mínimo 8 caracteres"
                {...formPassword.register('passwordNueva', {
                  required: true,
                  minLength: 8,
                  pattern: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                })}
              />
              <span style={{ fontSize: 12, color: '#6b7280' }}>
                Al menos una mayúscula, un número y un símbolo (RNF09).
              </span>
            </div>

            <div>
              <label className="auth-label">Confirmar contraseña</label>
              <input
                type="password"
                className="auth-input"
                placeholder="Repite la nueva contraseña"
                {...formPassword.register('confirmarPassword', { required: true })}
              />
            </div>
          </div>
        </form>
      </Modal>
    </>
  );
}

import { useCallback, useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';

import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import {
  actualizarMiPerfil,
  cambiarMiPassword,
  obtenerMiPerfil,
} from '../api/auth';
import {
  actualizarMiPerfilProfesional,
  obtenerMiPerfilProfesional,
} from '../api/profesionales';
import { rendimientoProfesional } from '../api/reportes';
import type {
  ActualizarPerfilRequest,
  ActualizarProfesionalRequest,
  CambiarPasswordRequest,
  ProfesionalResponse,
  RendimientoProfesionalResponse,
  UsuarioResponse,
  VarianteBadge,
} from '../types';

type DatosForm = ActualizarPerfilRequest & ActualizarProfesionalRequest;
type PasswordForm = CambiarPasswordRequest & { confirmarPassword: string };

function mensajeError(e: unknown, fallback: string): string {
  const data = (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data;
  return data?.mensaje ?? data?.message ?? fallback;
}

function nombreCompleto(usuario: UsuarioResponse | null, profesional: ProfesionalResponse | null) {
  if (profesional?.nombreCompleto) return profesional.nombreCompleto;
  if (!usuario) return 'Sin registro';
  return `${usuario.nombre} ${usuario.apellido}`.trim();
}

function estadoIndicador(estado: string): VarianteBadge {
  if (estado === 'Al dia') return 'green';
  if (estado === 'Dentro de meta') return 'green';
  if (estado.includes('pendiente')) return 'yellow';
  return 'gray';
}

function acumulado(datos: RendimientoProfesionalResponse[]) {
  return datos.reduce(
    (acc, item) => ({
      visitasRealizadas: acc.visitasRealizadas + item.visitasRealizadas,
      capacitacionesDictadas: acc.capacitacionesDictadas + item.capacitacionesDictadas,
      asesoriasAtendidas: acc.asesoriasAtendidas + item.asesoriasAtendidas,
      visitasProgramadas: acc.visitasProgramadas + item.visitasProgramadas,
    }),
    {
      visitasRealizadas: 0,
      capacitacionesDictadas: 0,
      asesoriasAtendidas: 0,
      visitasProgramadas: 0,
    }
  );
}

export default function ConfiguracionProfesional() {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null);
  const [profesional, setProfesional] = useState<ProfesionalResponse | null>(null);
  const [rendimientoMes, setRendimientoMes] = useState<RendimientoProfesionalResponse | null>(null);
  const [rendimientoAnual, setRendimientoAnual] = useState<RendimientoProfesionalResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [modalDatos, setModalDatos] = useState(false);
  const [modalPassword, setModalPassword] = useState(false);

  const formDatos = useForm<DatosForm>();
  const formPassword = useForm<PasswordForm>();

  const fechaBase = useMemo(() => new Date(), []);
  const mesActual = fechaBase.getMonth() + 1;
  const anioActual = fechaBase.getFullYear();

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);

    try {
      const [datosUsuario, datosProfesional, rendimientoActual, rendimientoMeses] =
        await Promise.all([
          obtenerMiPerfil(),
          obtenerMiPerfilProfesional(),
          rendimientoProfesional(mesActual, anioActual),
          Promise.all(
            Array.from({ length: mesActual }, (_, index) =>
              rendimientoProfesional(index + 1, anioActual)
            )
          ),
        ]);

      const rendimientoDelProfesional =
        rendimientoActual.find((item) => item.idProfesional === datosProfesional.id) ?? null;

      const rendimientoAnualProfesional = rendimientoMeses
        .flat()
        .filter((item) => item.idProfesional === datosProfesional.id);

      setUsuario(datosUsuario);
      setProfesional(datosProfesional);
      setRendimientoMes(rendimientoDelProfesional);
      setRendimientoAnual(rendimientoAnualProfesional);

      formDatos.reset({
        nombre: datosUsuario.nombre,
        apellido: datosUsuario.apellido,
        email: datosUsuario.email,
        rut: datosProfesional.rut,
        telefono: datosProfesional.telefono ?? '',
        especialidad: datosProfesional.especialidad ?? '',
      });

    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo cargar el perfil.'));
    } finally {
      setCargando(false);
    }
  }, [anioActual, formDatos, mesActual]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const indicadores = useMemo(() => {
    const total = acumulado(rendimientoAnual);
    const cumplimiento = rendimientoMes?.cumplimientoVisitas ?? null;

    return [
      {
        indicador: 'Visitas realizadas',
        mes: String(rendimientoMes?.visitasRealizadas ?? 0),
        acumulado: String(total.visitasRealizadas),
        estado: 'Al dia',
      },
      {
        indicador: 'Capacitaciones dictadas',
        mes: String(rendimientoMes?.capacitacionesDictadas ?? 0),
        acumulado: String(total.capacitacionesDictadas),
        estado: 'Al dia',
      },
      {
        indicador: 'Asesorias cerradas',
        mes: String(rendimientoMes?.asesoriasAtendidas ?? 0),
        acumulado: String(total.asesoriasAtendidas),
        estado: 'Al dia',
      },
      {
        indicador: 'Informes de visita publicados',
        mes: '0',
        acumulado: '0',
        estado: '1 pendiente',
      },
      {
        indicador: 'Tiempo de respuesta promedio',
        mes: cumplimiento == null ? 'N/D' : `${Math.round(cumplimiento)}%`,
        acumulado: total.visitasProgramadas > 0
          ? `${total.visitasRealizadas}/${total.visitasProgramadas}`
          : 'N/D',
        estado: cumplimiento != null && cumplimiento >= 80 ? 'Dentro de meta' : '1 pendiente',
      },
    ];
  }, [rendimientoAnual, rendimientoMes]);

  function abrirEditarDatos() {
    if (usuario && profesional) {
      formDatos.reset({
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        email: usuario.email,
        rut: profesional.rut,
        telefono: profesional.telefono ?? '',
        especialidad: profesional.especialidad ?? '',
      });
    }

    setModalDatos(true);
  }

  async function onGuardarDatos(data: DatosForm) {
    setGuardando(true);
    setMensaje(null);
    setError(null);

    try {
      const [usuarioActualizado, profesionalActualizado] = await Promise.all([
        actualizarMiPerfil({
          nombre: data.nombre,
          apellido: data.apellido,
          email: data.email,
        }),
        actualizarMiPerfilProfesional({
          rut: data.rut,
          telefono: data.telefono,
          especialidad: data.especialidad,
        }),
      ]);

      setUsuario(usuarioActualizado);
      setProfesional(profesionalActualizado);
      setModalDatos(false);
      setMensaje('Datos actualizados correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudieron actualizar los datos.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onCambiarPassword(data: PasswordForm) {
    setMensaje(null);
    setError(null);

    if (data.passwordNueva !== data.confirmarPassword) {
      setError('La nueva contrasena y la confirmacion no coinciden.');
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
      setMensaje('Contrasena actualizada correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo actualizar la contrasena.'));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
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
        <div className="placeholder">Cargando perfil...</div>
      ) : (
        <>
          <div>
            <section className="responsive-panel-section">
              <div className="flex items-center justify-between px-4 py-2 border-b border-gray-200">
                <strong className="text-azul text-[14px]">Informacion personal</strong>
                <button className="btn btn-sm btn-outline" onClick={abrirEditarDatos}>
                  Editar datos
                </button>
              </div>

              <div style={{ padding: '18px 20px 20px' }}>
                <div className="responsive-detail-grid responsive-detail-grid--profile">
                  <span>Nombre</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500 }}>
                    {nombreCompleto(usuario, profesional)}
                  </strong>

                  <span>RUT</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500 }}>
                    {profesional?.rut ?? 'Sin registro'}
                  </strong>

                  <span>Especialidad</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500 }}>
                    {profesional?.especialidad ?? 'Sin registro'}
                  </strong>

                  <span>Email</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500, color: '#2563eb' }}>
                    {usuario?.email ?? profesional?.email ?? 'Sin registro'}
                  </strong>

                  <span>Telefono</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500 }}>
                    {profesional?.telefono ?? 'Sin registro'}
                  </strong>

                  <span>Clientes asignados</span>
                  <strong style={{ textAlign: 'right', fontWeight: 500 }}>
                    {profesional?.cantidadClientes ?? 0}
                  </strong>
                </div>

                <button
                  className="btn btn-sm btn-outline"
                  style={{ marginTop: 34 }}
                  onClick={() => setModalPassword(true)}
                >
                  Cambiar contrasena
                </button>
              </div>
            </section>
          </div>

          <section className="responsive-panel-section">
            <div className="px-4 py-3 border-b border-gray-200">
              <strong className="text-azul text-[14px]">Mis indicadores de rendimiento</strong>
            </div>

            <table className="app-table">
              <thead>
                <tr>
                  <th>Indicador</th>
                  <th>Mes actual</th>
                  <th>Acumulado {anioActual}</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {indicadores.map((item) => (
                  <tr key={item.indicador}>
                    <td>{item.indicador}</td>
                    <td>{item.mes}</td>
                    <td>{item.acumulado}</td>
                    <td>
                      <Badge variante={estadoIndicador(item.estado)}>
                        {item.estado}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        </>
      )}

      <Modal
        abierto={modalDatos}
        titulo="Editar mis datos"
        onCerrar={() => setModalDatos(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalDatos(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-editar-datos" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </>
        }
      >
        <form id="form-editar-datos" onSubmit={formDatos.handleSubmit(onGuardarDatos)} noValidate>
          <div className="form-grid">
            <div>
              <label className="auth-label">Nombre</label>
              <input className="auth-input" {...formDatos.register('nombre', { required: true })} />
            </div>

            <div>
              <label className="auth-label">Apellido</label>
              <input className="auth-input" {...formDatos.register('apellido', { required: true })} />
            </div>

            <div>
              <label className="auth-label">Email</label>
              <input
                type="email"
                className="auth-input"
                {...formDatos.register('email', {
                  required: true,
                  pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Telefono</label>
              <input className="auth-input" {...formDatos.register('telefono')} />
            </div>

            <div>
              <label className="auth-label">RUT</label>
              <input
                className="auth-input"
                {...formDatos.register('rut', {
                  required: true,
                  pattern: /^\d{7,8}-[\dkK]$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Especialidad</label>
              <input className="auth-input" {...formDatos.register('especialidad')} />
            </div>
          </div>
        </form>
      </Modal>

      <Modal
        abierto={modalPassword}
        titulo="Cambiar contrasena"
        ancho="sm"
        onCerrar={() => setModalPassword(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPassword(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-password" type="submit" disabled={guardando}>
              {guardando ? 'Actualizando...' : 'Actualizar contrasena'}
            </button>
          </>
        }
      >
        <form id="form-password" onSubmit={formPassword.handleSubmit(onCambiarPassword)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Contrasena actual</label>
              <input
                type="password"
                className="auth-input"
                {...formPassword.register('passwordActual', { required: true })}
              />
            </div>

            <div>
              <label className="auth-label">Nueva contrasena</label>
              <input
                type="password"
                className="auth-input"
                placeholder="Minimo 8 caracteres"
                {...formPassword.register('passwordNueva', {
                  required: true,
                  minLength: 8,
                  pattern: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                })}
              />
            </div>

            <div>
              <label className="auth-label">Confirmar contrasena</label>
              <input
                type="password"
                className="auth-input"
                placeholder="Repite la nueva contrasena"
                {...formPassword.register('confirmarPassword', { required: true })}
              />
            </div>
          </div>
        </form>
      </Modal>
    </>
  );
}

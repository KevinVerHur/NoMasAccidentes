import { useCallback, useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';

import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import {
  actualizarMiPerfil,
  cambiarMiPassword,
  obtenerMiPerfil,
} from '../api/auth';
import {
  actualizarMiEstadoProfesional,
  actualizarMiPerfilProfesional,
  obtenerMiPerfilProfesional,
} from '../api/profesionales';
import { rendimientoProfesional } from '../api/reportes';
import type {
  ActualizarEstadoProfesionalRequest,
  ActualizarPerfilRequest,
  ActualizarProfesionalRequest,
  CambiarPasswordRequest,
  EstadoProfesional,
  ProfesionalResponse,
  RendimientoProfesionalResponse,
  UsuarioResponse,
  VarianteBadge,
} from '../types';

type PasswordForm = CambiarPasswordRequest & { confirmarPassword: string };
type IndicadorPerfil = {
  nombre: string;
  mesActual: string;
  acumulado: string;
  estado: string;
};

const labelEstado: Record<EstadoProfesional, string> = {
  DISPONIBLE: 'Disponible',
  EN_VISITA: 'En visita',
  EN_CAPACITACION: 'En capacitacion',
};

const badgeEstado: Record<EstadoProfesional, VarianteBadge> = {
  DISPONIBLE: 'green',
  EN_VISITA: 'blue',
  EN_CAPACITACION: 'yellow',
};

function mensajeError(e: unknown, fallback: string): string {
  const data = (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data;
  return data?.mensaje ?? data?.message ?? fallback;
}

function nombreCompleto(usuario: UsuarioResponse | null, profesional: ProfesionalResponse | null): string {
  if (profesional?.nombreCompleto) return profesional.nombreCompleto;
  if (!usuario) return 'Sin registro';
  return `${usuario.nombre} ${usuario.apellido}`.trim();
}

function badgePorEstadoTexto(estado: string): VarianteBadge {
  if (estado === 'Al dia') return 'green';
  if (estado === 'Dentro de meta') return 'blue';
  if (estado === 'Pendiente') return 'yellow';
  return 'gray';
}

function acumuladoRendimiento(datos: RendimientoProfesionalResponse[]) {
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
  const { cerrarSesion } = useAuth();
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null);
  const [profesional, setProfesional] = useState<ProfesionalResponse | null>(null);
  const [rendimientoMes, setRendimientoMes] = useState<RendimientoProfesionalResponse | null>(null);
  const [rendimientoAnual, setRendimientoAnual] = useState<RendimientoProfesionalResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [modalDatos, setModalDatos] = useState(false);
  const [modalEstado, setModalEstado] = useState(false);
  const [modalPassword, setModalPassword] = useState(false);

  const formDatos = useForm<ActualizarPerfilRequest & ActualizarProfesionalRequest>();
  const formEstado = useForm<ActualizarEstadoProfesionalRequest & { observacion?: string }>();
  const formPassword = useForm<PasswordForm>();

  const hoy = useMemo(() => new Date(), []);
  const mesActual = hoy.getMonth() + 1;
  const anioActual = hoy.getFullYear();

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);

    try {
      const [datosUsuario, datosProfesional, rendimientoPeriodo, rendimientoPorMes] =
        await Promise.all([
          obtenerMiPerfil(),
          obtenerMiPerfilProfesional(),
          rendimientoProfesional(mesActual, anioActual),
          Promise.all(
            Array.from({ length: mesActual }, (_, i) =>
              rendimientoProfesional(i + 1, anioActual)
            )
          ),
        ]);

      const actual = rendimientoPeriodo.find((item) => item.idProfesional === datosProfesional.id) ?? null;
      const anual = rendimientoPorMes
        .flat()
        .filter((item) => item.idProfesional === datosProfesional.id);

      setUsuario(datosUsuario);
      setProfesional(datosProfesional);
      setRendimientoMes(actual);
      setRendimientoAnual(anual);

      formDatos.reset({
        nombre: datosUsuario.nombre,
        apellido: datosUsuario.apellido,
        email: datosUsuario.email,
        rut: datosProfesional.rut,
        telefono: datosProfesional.telefono ?? '',
        especialidad: datosProfesional.especialidad ?? '',
      });

      formEstado.reset({
        estado: datosProfesional.estado,
        observacion: '',
      });
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo cargar la informacion de tu perfil.'));
    } finally {
      setCargando(false);
    }
  }, [anioActual, formDatos, formEstado, mesActual]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const indicadores = useMemo<IndicadorPerfil[]>(() => {
    const anual = acumuladoRendimiento(rendimientoAnual);
    const cumplimiento = rendimientoMes?.cumplimientoVisitas ?? null;
    const estadoCumplimiento =
      cumplimiento == null ? 'Pendiente' : cumplimiento >= 80 ? 'Al dia' : 'Revisar';

    return [
      {
        nombre: 'Visitas realizadas',
        mesActual: String(rendimientoMes?.visitasRealizadas ?? 0),
        acumulado: String(anual.visitasRealizadas),
        estado: estadoCumplimiento,
      },
      {
        nombre: 'Capacitaciones dictadas',
        mesActual: String(rendimientoMes?.capacitacionesDictadas ?? 0),
        acumulado: String(anual.capacitacionesDictadas),
        estado: 'Al dia',
      },
      {
        nombre: 'Asesorias cerradas',
        mesActual: String(rendimientoMes?.asesoriasAtendidas ?? 0),
        acumulado: String(anual.asesoriasAtendidas),
        estado: 'Al dia',
      },
      {
        nombre: 'Informes de visita publicados',
        mesActual: 'N/D',
        acumulado: 'N/D',
        estado: 'Pendiente',
      },
      {
        nombre: 'Tiempo de respuesta promedio',
        mesActual: cumplimiento == null ? 'N/D' : `${Math.round(cumplimiento)}%`,
        acumulado: anual.visitasProgramadas > 0 ? `${anual.visitasRealizadas}/${anual.visitasProgramadas}` : 'N/D',
        estado: cumplimiento != null && cumplimiento >= 80 ? 'Dentro de meta' : 'Pendiente',
      },
    ];
  }, [rendimientoAnual, rendimientoMes]);

  async function onGuardarDatos(data: ActualizarPerfilRequest & ActualizarProfesionalRequest) {
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
      setError(mensajeError(e, 'No se pudieron guardar los datos.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onGuardarEstado(data: ActualizarEstadoProfesionalRequest & { observacion?: string }) {
    setGuardando(true);
    setMensaje(null);
    setError(null);

    try {
      const actualizado = await actualizarMiEstadoProfesional({ estado: data.estado });
      setProfesional(actualizado);
      setModalEstado(false);
      setMensaje('Estado actualizado correctamente.');
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo actualizar el estado.'));
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
      cerrarSesion();
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo actualizar la contrasena.'));
    } finally {
      setGuardando(false);
    }
  }

  function abrirModalDatos() {
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

  function abrirModalEstado() {
    formEstado.reset({
      estado: profesional?.estado ?? 'DISPONIBLE',
      observacion: '',
    });
    setModalEstado(true);
  }

  return (
    <>
      <div className="page-title">Mi perfil</div>
      <div className="page-subtitle">Gestion de datos personales, estado en terreno e indicadores de rendimiento</div>

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
          <div className="grid-2">
            <Panel
              titulo="Informacion personal"
              accion={
                <div className="btn-group">
                  <button className="btn btn-sm btn-outline" onClick={abrirModalDatos}>
                    Editar datos
                  </button>
                  <button className="btn btn-sm btn-primary" onClick={abrirModalEstado}>
                    Actualizar estado
                  </button>
                </div>
              }
            >
              <div style={{ padding: 16 }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 14 }}>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>Nombre</div>
                    <strong>{nombreCompleto(usuario, profesional)}</strong>
                  </div>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>RUT</div>
                    <strong>{profesional?.rut ?? 'Sin registro'}</strong>
                  </div>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>Especialidad</div>
                    <strong>{profesional?.especialidad ?? 'Sin registro'}</strong>
                  </div>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>Email</div>
                    <strong>{usuario?.email ?? profesional?.email ?? 'Sin registro'}</strong>
                  </div>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>Telefono</div>
                    <strong>{profesional?.telefono ?? 'Sin registro'}</strong>
                  </div>
                  <div>
                    <div style={{ color: '#9ca3af', fontSize: 11 }}>Clientes asignados</div>
                    <strong>{profesional?.cantidadClientes ?? 0}</strong>
                  </div>
                </div>
              </div>
            </Panel>

            <Panel
              titulo="Mi ubicacion en terreno"
              accion={
                <button className="btn btn-sm btn-outline" onClick={abrirModalEstado}>
                  Actualizar estado
                </button>
              }
            >
              <div style={{ padding: 16, display: 'grid', gap: 12 }}>
                <div>
                  <div style={{ color: '#9ca3af', fontSize: 11 }}>Estado actual</div>
                  {profesional && (
                    <Badge variante={badgeEstado[profesional.estado]}>
                      {labelEstado[profesional.estado]}
                    </Badge>
                  )}
                </div>
                <div>
                  <div style={{ color: '#9ca3af', fontSize: 11 }}>Latitud</div>
                  <strong>{profesional?.latitud ?? 'Sin registro'}</strong>
                </div>
                <div>
                  <div style={{ color: '#9ca3af', fontSize: 11 }}>Longitud</div>
                  <strong>{profesional?.longitud ?? 'Sin registro'}</strong>
                </div>
              </div>
            </Panel>
          </div>

          <Panel
            titulo="Mis indicadores de rendimiento"
            accion={
              <button className="btn btn-sm btn-outline" onClick={() => setModalPassword(true)}>
                Cambiar contrasena
              </button>
            }
          >
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
                {indicadores.map((indicador) => (
                  <tr key={indicador.nombre}>
                    <td>{indicador.nombre}</td>
                    <td>{indicador.mesActual}</td>
                    <td>{indicador.acumulado}</td>
                    <td>
                      <Badge variante={badgePorEstadoTexto(indicador.estado)}>
                        {indicador.estado}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Panel>
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
            <button className="btn btn-primary" form="form-editar-mis-datos" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </>
        }
      >
        <form id="form-editar-mis-datos" onSubmit={formDatos.handleSubmit(onGuardarDatos)} noValidate>
          <div className="form-grid">
            <div>
              <label className="auth-label">Nombre</label>
              <input
                className={`auth-input ${formDatos.formState.errors.nombre ? 'auth-input--error' : ''}`}
                {...formDatos.register('nombre', { required: 'El nombre es obligatorio' })}
              />
            </div>
            <div>
              <label className="auth-label">Apellido</label>
              <input
                className={`auth-input ${formDatos.formState.errors.apellido ? 'auth-input--error' : ''}`}
                {...formDatos.register('apellido', { required: 'El apellido es obligatorio' })}
              />
            </div>
            <div>
              <label className="auth-label">Email</label>
              <input
                type="email"
                className={`auth-input ${formDatos.formState.errors.email ? 'auth-input--error' : ''}`}
                {...formDatos.register('email', {
                  required: 'El email es obligatorio',
                  pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email invalido' },
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
                className={`auth-input ${formDatos.formState.errors.rut ? 'auth-input--error' : ''}`}
                {...formDatos.register('rut', {
                  required: 'El RUT es obligatorio',
                  pattern: { value: /^\d{7,8}-[\dkK]$/, message: 'RUT invalido. Ej: 12345678-9' },
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
        abierto={modalEstado}
        titulo="Cambiar mi estado"
        ancho="sm"
        onCerrar={() => setModalEstado(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEstado(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-cambiar-estado" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar estado'}
            </button>
          </>
        }
      >
        <form id="form-cambiar-estado" onSubmit={formEstado.handleSubmit(onGuardarEstado)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Nuevo estado</label>
              <select className="auth-input" {...formEstado.register('estado', { required: true })}>
                <option value="DISPONIBLE">Disponible</option>
                <option value="EN_VISITA">En visita</option>
                <option value="EN_CAPACITACION">En capacitacion</option>
              </select>
            </div>
            <div>
              <label className="auth-label">Observacion opcional</label>
              <textarea
                className="auth-input"
                rows={3}
                placeholder="Ej: en ruta hacia Minera Andes."
                {...formEstado.register('observacion')}
              />
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
            <button className="btn btn-primary" form="form-cambiar-password" type="submit" disabled={guardando}>
              {guardando ? 'Actualizando...' : 'Actualizar contrasena'}
            </button>
          </>
        }
      >
        <form id="form-cambiar-password" onSubmit={formPassword.handleSubmit(onCambiarPassword)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Contrasena actual</label>
              <input
                type="password"
                className={`auth-input ${formPassword.formState.errors.passwordActual ? 'auth-input--error' : ''}`}
                {...formPassword.register('passwordActual', { required: 'La contrasena actual es obligatoria' })}
              />
            </div>
            <div>
              <label className="auth-label">Nueva contrasena</label>
              <input
                type="password"
                className={`auth-input ${formPassword.formState.errors.passwordNueva ? 'auth-input--error' : ''}`}
                placeholder="Minimo 8 caracteres"
                {...formPassword.register('passwordNueva', {
                  required: 'La nueva contrasena es obligatoria',
                  minLength: { value: 8, message: 'Minimo 8 caracteres' },
                  pattern: {
                    value: /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                    message: 'Debe incluir mayuscula, numero y simbolo',
                  },
                })}
              />
            </div>
            <div>
              <label className="auth-label">Confirmar contrasena</label>
              <input
                type="password"
                className={`auth-input ${formPassword.formState.errors.confirmarPassword ? 'auth-input--error' : ''}`}
                placeholder="Repite la nueva contrasena"
                {...formPassword.register('confirmarPassword', { required: 'Confirma la nueva contrasena' })}
              />
            </div>
            <div className="warning-box" style={{ marginBottom: 0 }}>
              La sesion se cerrara y deberas volver a iniciar con la nueva contrasena.
            </div>
          </div>
        </form>
      </Modal>
    </>
  );
}

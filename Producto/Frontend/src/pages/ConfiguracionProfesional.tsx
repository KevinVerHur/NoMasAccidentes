import { useCallback, useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
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
import { obtenerMiUltimaUbicacion } from '../api/ubicaciones';
import type {
  ActualizarEstadoProfesionalRequest,
  ActualizarPerfilRequest,
  ActualizarProfesionalRequest,
  CambiarPasswordRequest,
  EstadoProfesional,
  ProfesionalResponse,
  RendimientoProfesionalResponse,
  UbicacionProfesionalResponse,
  UsuarioResponse,
  VarianteBadge,
} from '../types';

type DatosForm = ActualizarPerfilRequest & ActualizarProfesionalRequest;
type PasswordForm = CambiarPasswordRequest & { confirmarPassword: string };
type EstadoForm = ActualizarEstadoProfesionalRequest & { observacion?: string };

const CENTRO_FALLBACK: [number, number] = [-33.4489, -70.6693];

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

const colorPorEstado: Record<EstadoProfesional, string> = {
  DISPONIBLE: '#27ae60',
  EN_VISITA: '#2563eb',
  EN_CAPACITACION: '#e07b00',
};

function crearIcono(estado: EstadoProfesional) {
  return L.divIcon({
    className: 'estado-marker',
    html: `<span style="background:${colorPorEstado[estado]}"></span>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  });
}

function AjustarMapaMiUbicacion({ centro }: { centro: [number, number] }) {
  const map = useMap();

  useEffect(() => {
    const id = window.setTimeout(() => {
      map.invalidateSize();
      map.setView(centro, 14, { animate: true });
    }, 150);

    return () => window.clearTimeout(id);
  }, [map, centro]);

  return null;
}

function MapaMiUbicacion({
  ubicacion,
  profesional,
}: {
  ubicacion: UbicacionProfesionalResponse | null;
  profesional: ProfesionalResponse | null;
}) {
  const tieneUbicacion = ubicacion != null;

  const centro: [number, number] = tieneUbicacion
    ? [Number(ubicacion.latitud), Number(ubicacion.longitud)]
    : CENTRO_FALLBACK;

  const estado = ubicacion?.estado ?? profesional?.estado ?? 'DISPONIBLE';

  return (
    <div style={{ height: 195, width: '100%', position: 'relative', zIndex: 0 }}>
      <MapContainer
        center={centro}
        zoom={tieneUbicacion ? 14 : 11}
        style={{ height: '100%', width: '100%', zIndex: 0 }}
        scrollWheelZoom
      >
        <AjustarMapaMiUbicacion centro={centro} />

        <TileLayer
          attribution="&copy; OpenStreetMap"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {tieneUbicacion && (
          <Marker position={centro} icon={crearIcono(estado)}>
            <Popup>
              <strong>{ubicacion.nombreProfesional}</strong>
              <br />
              {ubicacion.email}
              <br />
              Estado: {labelEstado[ubicacion.estado]}
              <br />
              Ultima actualizacion: {new Date(ubicacion.fechaRegistro).toLocaleString('es-CL')}
            </Popup>
          </Marker>
        )}
      </MapContainer>

      {!tieneUbicacion && (
        <div
          style={{
            position: 'absolute',
            left: 12,
            bottom: 12,
            zIndex: 500,
            background: 'rgba(255,255,255,.94)',
            border: '1px solid #d1d5db',
            borderRadius: 6,
            padding: '6px 8px',
            fontSize: 12,
            color: '#4b5563',
          }}
        >
          Ubicacion sin registrar
        </div>
      )}
    </div>
  );
}

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
  const [ubicacion, setUbicacion] = useState<UbicacionProfesionalResponse | null>(null);
  const [rendimientoMes, setRendimientoMes] = useState<RendimientoProfesionalResponse | null>(null);
  const [rendimientoAnual, setRendimientoAnual] = useState<RendimientoProfesionalResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [errorUbicacion, setErrorUbicacion] = useState<string | null>(null);

  const [modalDatos, setModalDatos] = useState(false);
  const [modalEstado, setModalEstado] = useState(false);
  const [modalPassword, setModalPassword] = useState(false);

  const formDatos = useForm<DatosForm>();
  const formEstado = useForm<EstadoForm>();
  const formPassword = useForm<PasswordForm>();

  const fechaBase = useMemo(() => new Date(), []);
  const mesActual = fechaBase.getMonth() + 1;
  const anioActual = fechaBase.getFullYear();

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    setErrorUbicacion(null);

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

      formEstado.reset({
        estado: datosProfesional.estado,
        observacion: '',
      });

      try {
        const ultimaUbicacion = await obtenerMiUltimaUbicacion();
        setUbicacion(ultimaUbicacion);
      } catch {
        setUbicacion(null);
        setErrorUbicacion('Ubicacion sin registrar');
      }
    } catch (e: unknown) {
      setError(mensajeError(e, 'No se pudo cargar el perfil.'));
    } finally {
      setCargando(false);
    }
  }, [anioActual, formDatos, formEstado, mesActual]);

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

  function abrirActualizarEstado() {
    formEstado.reset({
      estado: profesional?.estado ?? 'DISPONIBLE',
      observacion: '',
    });

    setModalEstado(true);
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

  async function onGuardarEstado(data: EstadoForm) {
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
          <div className="grid-2">
            <section className="bg-white rounded-lg shadow-sm overflow-hidden mb-4">
              <div className="flex items-center justify-between px-4 py-2 border-b border-gray-200">
                <strong className="text-azul text-[14px]">Informacion personal</strong>
                <button className="btn btn-sm btn-outline" onClick={abrirEditarDatos}>
                  Editar datos
                </button>
              </div>

              <div style={{ padding: '18px 20px 20px' }}>
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: '160px 1fr',
                    rowGap: 4,
                    columnGap: 16,
                    fontSize: 16,
                    lineHeight: 1.35,
                  }}
                >
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

            <section className="bg-white rounded-lg shadow-sm overflow-hidden mb-4">
              <div className="flex items-center justify-between px-4 py-2 border-b border-gray-200">
                <strong className="text-azul text-[14px]">Mi ubicacion en terreno</strong>
                <button className="btn btn-sm btn-outline" onClick={abrirActualizarEstado}>
                  Actualizar estado
                </button>
              </div>

              <div style={{ padding: 16 }}>
                <MapaMiUbicacion ubicacion={ubicacion} profesional={profesional} />
                {errorUbicacion && (
                  <div style={{ marginTop: 8, fontSize: 12, color: '#6b7280' }}>
                    {errorUbicacion}
                  </div>
                )}
              </div>
            </section>
          </div>

          <section className="bg-white rounded-lg shadow-sm overflow-hidden mb-4">
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
        abierto={modalEstado}
        titulo="Cambiar mi estado"
        ancho="sm"
        onCerrar={() => setModalEstado(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEstado(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-estado" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar estado'}
            </button>
          </>
        }
      >
        <form id="form-estado" onSubmit={formEstado.handleSubmit(onGuardarEstado)} noValidate>
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
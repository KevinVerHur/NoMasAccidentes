import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Circle, MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
// import SeguimientoPreventivoPanel from '../components/actividades/SeguimientoPreventivoPanel';
import { useAuth } from '../context/AuthContext';
import { registrarMiUbicacion } from '../api/ubicaciones';
import { obtenerMiPerfilProfesional, actualizarMiEstadoProfesional } from '../api/profesionales';
import { finalizarMiVisita, iniciarMiVisita, listarMisVisitas } from '../api/visitas';
import { obtenerDashboardProfesional } from '../api/dashboard';
import type {
  DashboardClienteAsignado,
  DashboardProfesionalResponse,
  EstadoProfesional,
  EstadoVisitaBackend,
  ProfesionalResponse,
  VarianteBadge,
  VisitaResponse,
  AsesoriaResponse,
  CapacitacionResponse,
} from '../types';

import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import esLocale from '@fullcalendar/core/locales/es';
import type { EventInput } from '@fullcalendar/core';

import { misAsesorias } from '../api/asesorias';
import { listarCapacitacionesPorRelator } from '../api/capacitaciones';

interface UbicacionActual {
  latitud: number;
  longitud: number;
  precision: number;
  fecha: Date;
}

const PRECISION_MAXIMA_METROS = 3000;

const fmtUltimaVisita = (iso: string | null) =>
  iso
    ? new Date(`${iso}T00:00:00`).toLocaleDateString('es-CL', { day: '2-digit', month: 'short' })
    : '—';

const mesActual = new Date().toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });

const badgePorEstadoVisita: Record<EstadoVisitaBackend, VarianteBadge> = {
  PROGRAMADA: 'blue',
  EN_CURSO: 'yellow',
  REALIZADA: 'green',
  CANCELADA: 'red',
};

const labelEstadoVisita: Record<EstadoVisitaBackend, string> = {
  PROGRAMADA: 'Programada',
  EN_CURSO: 'En curso',
  REALIZADA: 'Realizada',
  CANCELADA: 'Cancelada',
};

const badgePorEstadoCliente: Record<DashboardClienteAsignado['estado'], VarianteBadge> = {
  ACTIVO: 'green',
  MOROSO: 'yellow',
  SUSPENDIDO: 'red',
};

const labelEstadoProfesional: Record<EstadoProfesional, string> = {
  DISPONIBLE: 'Disponible',
  EN_VISITA: 'En visita',
  EN_CAPACITACION: 'En capacitacion',
};

const iconoConfiable = L.divIcon({
  className: 'estado-marker',
  html: '<span style="background:#27ae60"></span>',
  iconSize: [18, 18],
  iconAnchor: [9, 9],
});

const iconoImpreciso = L.divIcon({
  className: 'estado-marker',
  html: '<span style="background:#e07b00"></span>',
  iconSize: [18, 18],
  iconAnchor: [9, 9],
});

const HORA_INICIO_JORNADA = 8;
const HORA_FIN_JORNADA = 18;

function estaDentroDeJornadaLaboralChile() {
  const ahoraChile = new Date(
    new Date().toLocaleString('en-US', { timeZone: 'America/Santiago' })
  );

  const dia = ahoraChile.getDay(); // 0 domingo, 6 sabado
  const hora = ahoraChile.getHours();

  return dia >= 1 && dia <= 5 && hora >= HORA_INICIO_JORNADA && hora < HORA_FIN_JORNADA;
}

function distanciaMetros(a: UbicacionActual, b: UbicacionActual) {
  const radioTierra = 6371000;
  const lat1 = (a.latitud * Math.PI) / 180;
  const lat2 = (b.latitud * Math.PI) / 180;
  const deltaLat = ((b.latitud - a.latitud) * Math.PI) / 180;
  const deltaLng = ((b.longitud - a.longitud) * Math.PI) / 180;

  const h =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) *
    Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

  return 2 * radioTierra * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
}

function formatearFecha(fechaIso: string) {
  return new Date(fechaIso).toLocaleDateString('es-CL', {
    day: '2-digit',
    month: 'short',
  });
}

function formatearHora(fechaIso: string) {
  return new Date(fechaIso).toLocaleTimeString('es-CL', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function AjustarMapa({ posicion }: { posicion: [number, number] }) {
  const map = useMap();

  useEffect(() => {
    const id = window.setTimeout(() => {
      map.invalidateSize();
      map.setView(posicion, map.getZoom(), { animate: true });
    }, 150);

    return () => window.clearTimeout(id);
  }, [map, posicion]);

  return null;
}

function MapaProfesional({
  posicion,
  precision,
  confiable,
  grande = false,
}: {
  posicion: [number, number] | null;
  precision?: number;
  confiable: boolean;
  grande?: boolean;
}) {
  if (!posicion) {
    return (
      <div
        style={{
          height: '100%',
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#eef4f9',
          color: '#5a6b7f',
          fontSize: 12,
          textAlign: 'center',
          padding: 16,
        }}
      >
        Presiona "Actualizar ahora" o "Iniciar seguimiento" para leer tu ubicacion.
      </div>
    );
  }

  return (
    <MapContainer
      center={posicion}
      zoom={grande ? 15 : 14}
      style={{ height: '100%', width: '100%', zIndex: 0 }}
      scrollWheelZoom={grande}
    >
      <AjustarMapa posicion={posicion} />

      <TileLayer
        attribution="&copy; OpenStreetMap"
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {typeof precision === 'number' && (
        <Circle
          center={posicion}
          radius={precision}
          pathOptions={{
            color: confiable ? '#2563eb' : '#e07b00',
            fillColor: confiable ? '#2563eb' : '#e07b00',
            fillOpacity: 0.12,
            weight: 1,
          }}
        />
      )}

      <Marker position={posicion} icon={confiable ? iconoConfiable : iconoImpreciso}>
        <Popup>
          <strong>{confiable ? 'Ubicacion confiable' : 'Ubicacion aproximada'}</strong>
          <br />
          {confiable ? 'Sincronizable con el sistema' : 'No se enviara al administrador'}
          {typeof precision === 'number' && (
            <>
              <br />
              Precision aproximada: {Math.round(precision)} m
            </>
          )}
        </Popup>
      </Marker>
    </MapContainer>
  );
}

function esFechaPasada(fecha: string) {
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);

  const f = new Date(`${fecha}T00:00:00`);
  f.setHours(0, 0, 0, 0);

  return f < hoy;
}

function colorEvento(tipo: 'visita' | 'asesoria' | 'capacitacion', estado: string, fecha: string) {
  if (estado === 'CANCELADA') return '#9ca3af';

  const pasado = esFechaPasada(fecha);

  if (tipo === 'visita') {
    if (estado === 'REALIZADA') return '#16a34a';
    if (estado === 'EN_CURSO') return '#f59e0b';
    return pasado ? '#93c5fd' : '#2563eb';
  }

  if (tipo === 'asesoria') {
    if (estado === 'CERRADA') return '#15803d';
    if (estado === 'EN_PROCESO') return '#f97316';
    return pasado ? '#fed7aa' : '#ea580c';
  }

  if (estado === 'REALIZADA') return '#16a34a';
  if (estado === 'EN_CURSO') return '#0891b2';
  return pasado ? '#a5f3fc' : '#06b6d4';
}

function eventosVisitasProfesional(visitas: VisitaResponse[]): EventInput[] {
  return visitas.map((v) => {
    const color = colorEvento('visita', v.estado, v.fechaProgramada);

    return {
      id: `visita-${v.id}`,
      title: `Visita - ${v.razonSocialEmpresa}`,
      start: v.fechaProgramada,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Visita',
        estado: v.estado,
        cliente: v.razonSocialEmpresa,
        detalle: v.tipoRevision ?? 'Sin tipo',
      },
    };
  });
}

function eventosAsesoriasProfesional(asesorias: AsesoriaResponse[]): EventInput[] {
  return asesorias.map((a) => {
    const fecha = a.fechaAtencion ?? a.fechaSolicitud;
    const color = colorEvento('asesoria', a.estado, fecha);

    return {
      id: `asesoria-${a.id}`,
      title: `Asesoria - ${a.razonSocialEmpresa}`,
      start: fecha,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Asesoria',
        estado: a.estado,
        cliente: a.razonSocialEmpresa,
        detalle: a.motivo,
      },
    };
  });
}

function eventosCapacitacionesProfesional(capacitaciones: CapacitacionResponse[]): EventInput[] {
  return capacitaciones.map((c) => {
    const color = colorEvento('capacitacion', c.estado, c.fechaProgramada);

    return {
      id: `capacitacion-${c.id}`,
      title: `Capacitacion - ${c.curso}`,
      start: c.fechaProgramada,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Capacitacion',
        estado: c.estado,
        cliente: c.razonSocialEmpresa,
        detalle: c.lugar,
      },
    };
  });
}

export default function DashboardProfesional() {
  const { email } = useAuth();

  const [modalMapa, setModalMapa] = useState(false);
  const [ubicacion, setUbicacion] = useState<UbicacionActual | null>(null);
  const [siguiendo, setSiguiendo] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [mensajeUbicacion, setMensajeUbicacion] = useState<string | null>(null);
  const [miProfesional, setMiProfesional] = useState<ProfesionalResponse | null>(null);

  const [visitas, setVisitas] = useState<VisitaResponse[]>([]);
  const [cargandoVisitas, setCargandoVisitas] = useState(true);
  const [operandoVisitaId, setOperandoVisitaId] = useState<number | null>(null);
  const [mensajeVisita, setMensajeVisita] = useState<string | null>(null);
  const [resumenClientes, setResumenClientes] = useState<DashboardProfesionalResponse | null>(null);

  const watchIdRef = useRef<number | null>(null);
  const ultimaUbicacionEnviadaRef = useRef<UbicacionActual | null>(null);
  const ultimoEnvioRef = useRef(0);

  const proximaVisita = visitas.find((v) => v.estado === 'PROGRAMADA') ?? visitas[0] ?? null;

  const posicionProfesional = useMemo<[number, number] | null>(() => {
    if (!ubicacion) return null;
    return [ubicacion.latitud, ubicacion.longitud];
  }, [ubicacion]);

  const ubicacionConfiable = ubicacion
    ? ubicacion.precision <= PRECISION_MAXIMA_METROS
    : false;

  const estadoActual = miProfesional
    ? labelEstadoProfesional[miProfesional.estado]
    : 'Sin estado';

  const varianteEstado = miProfesional?.estado === 'DISPONIBLE'
    ? 'ok'
    : miProfesional?.estado === 'EN_CAPACITACION'
      ? 'warn'
      : undefined;


  const cargarMiEstado = useCallback(async () => {
    try {
      const actual = await obtenerMiPerfilProfesional();
      setMiProfesional(actual);
    } catch {
      setMiProfesional(null);
    }
  }, []);

  const [asesorias, setAsesorias] = useState<AsesoriaResponse[]>([]);
  const [capacitaciones, setCapacitaciones] = useState<CapacitacionResponse[]>([]);
  const [errorAgenda, setErrorAgenda] = useState<string | null>(null);

  const eventosAgenda = useMemo<EventInput[]>(
    () => [
      ...eventosVisitasProfesional(visitas),
      ...eventosAsesoriasProfesional(asesorias),
      ...eventosCapacitacionesProfesional(capacitaciones),
    ],
    [visitas, asesorias, capacitaciones]
  );
  const cargarMisVisitas = useCallback(async () => {
    setCargandoVisitas(true);

    try {
      const data = await listarMisVisitas();
      setVisitas(data);
    } catch {
      setVisitas([]);
      setMensajeVisita('No se pudieron cargar tus visitas asignadas.');
    } finally {
      setCargandoVisitas(false);
    }
  }, []);

  useEffect(() => {
    cargarMiEstado();

    const id = window.setInterval(cargarMiEstado, 5000);
    return () => window.clearInterval(id);
  }, [cargarMiEstado]);

  useEffect(() => {
    cargarMisVisitas();
  }, [cargarMisVisitas]);

  useEffect(() => {
    async function cargarAgendaProfesional() {
      setErrorAgenda(null);

      try {
        const misAsesoriasData = await misAsesorias();
        setAsesorias(misAsesoriasData);
      } catch {
        setAsesorias([]);
        setErrorAgenda('No se pudieron cargar tus asesorias asignadas.');
      }
    }

    cargarAgendaProfesional();
  }, []);

  useEffect(() => {
    async function cargarCapacitacionesRelator() {
      if (!miProfesional?.id) return;

      try {
        const data = await listarCapacitacionesPorRelator(miProfesional.id);
        setCapacitaciones(data);
      } catch {
        setCapacitaciones([]);
        setErrorAgenda('No se pudieron cargar tus capacitaciones como relator.');
      }
    }

    cargarCapacitacionesRelator();
  }, [miProfesional?.id]);

  useEffect(() => {
    obtenerDashboardProfesional()
      .then(setResumenClientes)
      .catch(() => setResumenClientes(null));
  }, []);

  async function cambiarMiEstado(estado: EstadoProfesional) {
    try {
      const actualizado = await actualizarMiEstadoProfesional({ estado });
      setMiProfesional(actualizado);
      setMensajeUbicacion('Estado operativo actualizado.');
    } catch {
      setMensajeUbicacion('No se pudo actualizar tu estado operativo.');
    }
  }

  async function iniciarVisita(id: number) {
    setOperandoVisitaId(id);
    setMensajeVisita(null);

    try {
      await iniciarMiVisita(id);
      const actualizado = await actualizarMiEstadoProfesional({ estado: 'EN_VISITA' });
      setMiProfesional(actualizado);
      await cargarMisVisitas();
      setMensajeVisita('Visita iniciada. Tu estado cambio a En visita.');
    } catch {
      setMensajeVisita('No se pudo iniciar la visita.');
    } finally {
      setOperandoVisitaId(null);
    }
  }

  async function finalizarVisita(id: number) {
    setOperandoVisitaId(id);
    setMensajeVisita(null);

    try {
      await finalizarMiVisita(id);
      const actualizado = await actualizarMiEstadoProfesional({ estado: 'DISPONIBLE' });
      setMiProfesional(actualizado);
      await cargarMisVisitas();
      setMensajeVisita('Visita finalizada. Tu estado volvio a Disponible.');
    } catch {
      setMensajeVisita('No se pudo finalizar la visita.');
    } finally {
      setOperandoVisitaId(null);
    }
  }

  const enviarUbicacion = useCallback(async (nuevaUbicacion: UbicacionActual, forzar = false) => {

    if (!estaDentroDeJornadaLaboralChile()) {
      setMensajeUbicacion('Fuera de jornada laboral. La ubicacion no se enviara al sistema.')
      return;
    }

    const ahora = Date.now();
    const ultimaEnviada = ultimaUbicacionEnviadaRef.current;
    const pasoTiempoMinimo = ahora - ultimoEnvioRef.current >= 10000;
    const seMovioSuficiente = !ultimaEnviada || distanciaMetros(ultimaEnviada, nuevaUbicacion) >= 20;

    if (!forzar && (!pasoTiempoMinimo || !seMovioSuficiente)) return;

    if (nuevaUbicacion.precision > PRECISION_MAXIMA_METROS) {
      setMensajeUbicacion(
        `Ubicacion aproximada detectada (${Math.round(nuevaUbicacion.precision)} m). Se muestra localmente, pero no se enviara al administrador.`
      );
      return;
    }

    setEnviando(true);

    try {
      await registrarMiUbicacion({
        latitud: nuevaUbicacion.latitud,
        longitud: nuevaUbicacion.longitud,
      });

      ultimoEnvioRef.current = ahora;
      ultimaUbicacionEnviadaRef.current = nuevaUbicacion;
      setMensajeUbicacion('Ubicacion confiable sincronizada con el sistema.');
    } catch {
      setMensajeUbicacion('No se pudo sincronizar la ubicacion con el servidor.');
    } finally {
      setEnviando(false);
    }
  }, []);

  const aplicarLecturaUbicacion = useCallback(async (position: GeolocationPosition, forzarEnvio = false) => {
    const nuevaUbicacion: UbicacionActual = {
      latitud: position.coords.latitude,
      longitud: position.coords.longitude,
      precision: position.coords.accuracy,
      fecha: new Date(),
    };

    setUbicacion(nuevaUbicacion);
    await enviarUbicacion(nuevaUbicacion, forzarEnvio);
  }, [enviarUbicacion]);

  const actualizarUnaVez = useCallback(() => {

    if (!estaDentroDeJornadaLaboralChile()) {
      setMensajeUbicacion('El mapa solo funciona de lunes a viernes entre las 08:00 y las 18:00, hora de Chile.');
      return;
    }

    setMensajeUbicacion(null);

    if (!navigator.geolocation) {
      setMensajeUbicacion('Este navegador no soporta geolocalizacion.');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        aplicarLecturaUbicacion(position, true);
      },
      () => {
        setMensajeUbicacion('No se pudo obtener la ubicacion. Revisa los permisos del navegador.');
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 5000,
      }
    );
  }, [aplicarLecturaUbicacion]);

  const iniciarSeguimiento = useCallback(() => {

    if (!estaDentroDeJornadaLaboralChile()) {
      setMensajeUbicacion('El seguimiento solo puede iniciarse de lunes a viernes entre las 08:00 y las 18:00, hora de Chile.');
      return;
    }

    setMensajeUbicacion(null);

    if (!navigator.geolocation) {
      setMensajeUbicacion('Este navegador no soporta geolocalizacion.');
      return;
    }

    if (watchIdRef.current !== null) return;

    const id = navigator.geolocation.watchPosition(
      (position) => {
        aplicarLecturaUbicacion(position);
      },
      () => {
        setMensajeUbicacion('No se pudo seguir la ubicacion. Revisa los permisos del navegador.');
        setSiguiendo(false);

        if (watchIdRef.current !== null) {
          navigator.geolocation.clearWatch(watchIdRef.current);
          watchIdRef.current = null;
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 5000,
      }
    );

    watchIdRef.current = id;
    setSiguiendo(true);
    setMensajeUbicacion('Seguimiento activo. Solo las lecturas confiables se enviaran al administrador.');
  }, [aplicarLecturaUbicacion]);

  const detenerSeguimiento = useCallback(() => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }

    setSiguiendo(false);
    setMensajeUbicacion('Seguimiento detenido.');
  }, []);

  useEffect(() => {
    return () => {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
      }
    };
  }, []);

  return (
    <>
      <div className="page-title">Mi Panel</div>
      <div className="page-subtitle" style={{ textTransform: 'capitalize' }}>
        {email} - Prevencionista de riesgos - {mesActual}
      </div>

      <div className="kpi-row">
        <KpiCard
          label="Visitas hoy"
          value={
            visitas.filter((v) =>
              new Date(v.fechaProgramada).toDateString() === new Date().toDateString()
            ).length
          }
          sub={proximaVisita ? `Proxima: ${formatearHora(proximaVisita.fechaProgramada)}` : 'Sin proximas visitas'}
          variante="warn"
        />

        <KpiCard
          label="Visitas pendientes"
          value={visitas.filter((v) => v.estado === 'PROGRAMADA').length}
          sub="Asignadas a mi usuario"
        />

        <KpiCard
          label="Mis clientes asignados"
          value={resumenClientes?.clientesAsignados ?? 0}
          sub={
            resumenClientes && resumenClientes.clientesMorosos > 0
              ? `${resumenClientes.clientesMorosos} moroso(s) / suspendido(s)`
              : 'Todos al día'
          }
          variante={resumenClientes && resumenClientes.clientesMorosos > 0 ? 'warn' : undefined}
        />

        <KpiCard
          label="Mi estado"
          value={estadoActual}
          sub={
            ubicacion
              ? `${ubicacionConfiable ? 'Ubicacion confiable' : 'Ubicacion aproximada'} - ${ubicacion.fecha.toLocaleTimeString('es-CL')}`
              : 'Sin lectura de ubicacion'
          }
          variante={varianteEstado}
        />
      </div>
      <Panel titulo="Agenda semanal">
        {errorAgenda && (
          <div className="alert-item alert-item--warn" style={{ margin: 12 }}>
            {errorAgenda}
          </div>
        )}

        <div className="agenda-semanal">
          <FullCalendar
            plugins={[dayGridPlugin, interactionPlugin]}
            initialView="dayGridWeek"
            locale={esLocale}
            firstDay={1}
            height="auto"
            events={eventosAgenda}
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridWeek,dayGridMonth',
            }}
            buttonText={{
              today: 'Hoy',
              week: 'Semana',
              month: 'Mes',
            }}
            eventClick={(info) => {
              const props = info.event.extendedProps;
              alert(
                `${props.tipo}\n${info.event.title}\nEstado: ${props.estado}\nCliente: ${props.cliente}\nDetalle: ${props.detalle ?? '-'}`
              );
            }}
          />
        </div>
      </Panel>
      <div className="grid-2">
        <Panel
          titulo="Mis proximas visitas"
          accion={<button className="btn btn-sm btn-outline" onClick={cargarMisVisitas}>Refrescar</button>}
        >
          {mensajeVisita && (
            <div className="alert-item alert-item--info" style={{ marginBottom: 12 }}>
              {mensajeVisita}
            </div>
          )}

          {cargandoVisitas ? (
            <div className="placeholder">Cargando visitas asignadas...</div>
          ) : visitas.length === 0 ? (
            <div className="placeholder">No tienes visitas asignadas.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  {['Cliente', 'Fecha', 'Hora', 'Tipo', 'Estado', 'Accion'].map((h) => (
                    <th key={h}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {visitas.map((v) => (
                  <tr key={v.id}>
                    <td>{v.razonSocialEmpresa}</td>
                    <td>{formatearFecha(v.fechaProgramada)}</td>
                    <td>{formatearHora(v.fechaProgramada)}</td>
                    <td>{v.tipoRevision ?? '-'}</td>
                    <td>
                      <Badge variante={badgePorEstadoVisita[v.estado]}>
                        {labelEstadoVisita[v.estado]}
                      </Badge>
                    </td>
                    <td>
                      <div className="btn-group">
                        {v.estado === 'PROGRAMADA' && (
                          <button
                            className="btn btn-sm btn-primary"
                            disabled={operandoVisitaId === v.id}
                            onClick={() => iniciarVisita(v.id)}
                          >
                            Iniciar
                          </button>
                        )}

                        {v.estado === 'EN_CURSO' && (
                          <button
                            className="btn btn-sm btn-success"
                            disabled={operandoVisitaId === v.id}
                            onClick={() => finalizarVisita(v.id)}
                          >
                            Finalizar
                          </button>
                        )}

                        {v.estado !== 'PROGRAMADA' && v.estado !== 'EN_CURSO' && (
                          <span style={{ color: '#9ca3af', fontSize: 12 }}>Sin accion</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>

        <Panel
          titulo="Mi ubicacion en terreno"
          accion={
            <div className="btn-group">
              <select
                className="estado-select"
                value={miProfesional?.estado ?? 'DISPONIBLE'}
                onChange={(e) => cambiarMiEstado(e.target.value as EstadoProfesional)}
              >
                <option value="DISPONIBLE">Disponible</option>
                <option value="EN_VISITA">En visita</option>
                <option value="EN_CAPACITACION">En capacitacion</option>
              </select>

              <button className="btn btn-sm btn-outline" onClick={actualizarUnaVez} disabled={enviando}>
                {enviando ? 'Sincronizando...' : 'Actualizar ahora'}
              </button>

              {siguiendo ? (
                <button className="btn btn-sm btn-danger" onClick={detenerSeguimiento}>
                  Detener seguimiento
                </button>
              ) : (
                <button className="btn btn-sm btn-primary" onClick={iniciarSeguimiento}>
                  Iniciar seguimiento
                </button>
              )}

              <button className="btn btn-sm btn-outline" onClick={() => setModalMapa(true)}>
                Ver mapa completo
              </button>
            </div>
          }
        >
          <div className="responsive-map responsive-map--compact">
            <MapaProfesional
              posicion={posicionProfesional}
              precision={ubicacion?.precision}
              confiable={ubicacionConfiable}
            />
          </div>

          <div className="prof-location-status">
            <span>{siguiendo ? 'Seguimiento activo' : 'Seguimiento detenido'}</span>
            <span>Estado backend: {estadoActual}</span>

            {ubicacion && (
              <span style={{ color: ubicacionConfiable ? '#1f7a3a' : '#c97706' }}>
                {ubicacionConfiable ? 'Ubicacion confiable' : 'Ubicacion aproximada'} - Precision: {Math.round(ubicacion.precision)} m
              </span>
            )}

            <span>
              {proximaVisita
                ? `Proxima visita: ${proximaVisita.razonSocialEmpresa} - ${proximaVisita.tipoRevision ?? 'Sin tipo'} - ${formatearHora(proximaVisita.fechaProgramada)}`
                : 'Sin proximas visitas asignadas'}
            </span>

            {mensajeUbicacion && (
              <span style={{ color: mensajeUbicacion.includes('No se pudo') ? '#c0392b' : '#1f7a3a' }}>
                {mensajeUbicacion}
              </span>
            )}
          </div>
        </Panel>
      </div>

      <Panel titulo="Mis clientes asignados">
        <table className="app-table">
          <thead>
            <tr>
              {['Cliente', 'Rubro', 'Ultima visita', 'Estado'].map((h) => (
                <th key={h}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {(resumenClientes?.clientes ?? []).length === 0 ? (
              <tr>
                <td colSpan={4} style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                  No tienes clientes asignados.
                </td>
              </tr>
            ) : (
              resumenClientes!.clientes.map((c) => (
                <tr key={c.idCliente}>
                  <td>{c.razonSocial}</td>
                  <td>{c.rubro}</td>
                  <td>{fmtUltimaVisita(c.ultimaVisita)}</td>
                  <td>
                    <Badge variante={badgePorEstadoCliente[c.estado]}>{c.estado}</Badge>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </Panel>

      {/* <SeguimientoPreventivoPanel
        titulo="Mis actividades preventivas"
        compacto
        editable
      /> */}

      {modalMapa && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,.45)',
            zIndex: 3000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 18,
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) setModalMapa(false);
          }}
        >
          <div
            style={{
              width: 'min(1040px, 96vw)',
              background: 'white',
              borderRadius: 12,
              overflow: 'hidden',
              boxShadow: '0 12px 40px rgba(0,0,0,.22)',
              position: 'relative',
              zIndex: 3001,
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '14px 18px',
                borderBottom: '1px solid #e8edf3',
                background: '#f8fafc',
              }}
            >
              <strong style={{ color: '#18395a', fontSize: 15 }}>Mapa completo de ubicacion</strong>
              <button
                onClick={() => setModalMapa(false)}
                style={{ background: 'none', border: 'none', fontSize: 22, cursor: 'pointer', color: '#8994a3', lineHeight: 1 }}
              >
                x
              </button>
            </div>

            <div className="map-modal-body">
              <MapaProfesional
                posicion={posicionProfesional}
                precision={ubicacion?.precision}
                confiable={ubicacionConfiable}
                grande
              />
            </div>

            <div className="prof-map-modal-footer">
              <span>
                {ubicacion
                  ? `${ubicacionConfiable ? 'Lectura confiable' : 'Lectura aproximada'} - ${ubicacion.fecha.toLocaleString('es-CL')} - Precision: ${Math.round(ubicacion.precision)} m`
                  : 'Aun no hay lectura de ubicacion.'}
              </span>

              <button className="btn btn-outline" onClick={() => setModalMapa(false)}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

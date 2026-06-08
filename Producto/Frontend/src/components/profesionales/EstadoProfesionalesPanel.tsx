import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import Panel from '../ui/Panel';
import Badge from '../ui/Badge';
import KpiCard from '../ui/KpiCard';
import { useAuth } from '../../context/AuthContext';
import type {
  EstadoProfesional,
  ProfesionalResponse,
  UbicacionProfesionalResponse,
  VarianteBadge,
} from '../../types';
import { actualizarEstadoProfesional, listarProfesionales } from '../../api/profesionales';
import { listarUbicacionesActivas, registrarMiUbicacion } from '../../api/ubicaciones';
import { obtenerUbicacionActual } from '../../api/geolocalizacion';

const labelEstado: Record<EstadoProfesional, string> = {
  DISPONIBLE: 'Disponible',
  EN_VISITA: 'En visita',
  EN_CAPACITACION: 'En capacitación',
};

const badgePorEstado: Record<EstadoProfesional, VarianteBadge> = {
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

export default function EstadoProfesionalesPanel() {
  const { rol } = useAuth();

  const [profesionales, setProfesionales] = useState<ProfesionalResponse[]>([]);
  const [ubicaciones, setUbicaciones] = useState<UbicacionProfesionalResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardandoId, setGuardandoId] = useState<number | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [siguiendo, setSiguiendo] = useState(false);

  const watchIdRef = useRef<number | null>(null);

  const cargarProfesionales = useCallback(async () => {
    const data = await listarProfesionales(0, 100);
    setProfesionales(data.content);
  }, []);

  const cargarUbicaciones = useCallback(async () => {
    const data = await listarUbicacionesActivas();
    setUbicaciones(data);
  }, []);

  const cargarTodo = useCallback(async () => {
    setCargando(true);
    try {
      await Promise.all([cargarProfesionales(), cargarUbicaciones()]);
    } finally {
      setCargando(false);
    }
  }, [cargarProfesionales, cargarUbicaciones]);

  useEffect(() => {
    cargarTodo();

    const intervalId = window.setInterval(cargarTodo, 10000);

    return () => {
      window.clearInterval(intervalId);

      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
      }
    };
  }, [cargarTodo]);

  const centro = useMemo<[number, number]>(() => {
    if (ubicaciones.length === 0) return [-33.4489, -70.6693];
    return [Number(ubicaciones[0].latitud), Number(ubicaciones[0].longitud)];
  }, [ubicaciones]);

  async function cambiarEstado(id: number, estado: EstadoProfesional) {
    setGuardandoId(id);
    setMensaje(null);

    try {
      await actualizarEstadoProfesional(id, { estado });
      await cargarTodo();
      setMensaje('Estado actualizado correctamente.');
    } catch {
      setMensaje('No se pudo actualizar el estado.');
    } finally {
      setGuardandoId(null);
    }
  }

  async function actualizarMiUbicacion() {
    setMensaje(null);

    try {
      const ubicacion = await obtenerUbicacionActual();
      await registrarMiUbicacion(ubicacion);
      await cargarTodo();
      setMensaje('Ubicación actualizada correctamente.');
    } catch {
      setMensaje('No se pudo obtener la ubicación. Revisa los permisos del navegador.');
    }
  }

  function iniciarSeguimiento() {
    setMensaje(null);

    if (!navigator.geolocation) {
      setMensaje('El navegador no soporta geolocalización.');
      return;
    }

    if (watchIdRef.current !== null) return;

    watchIdRef.current = navigator.geolocation.watchPosition(
      async (position) => {
        await registrarMiUbicacion({
          latitud: position.coords.latitude,
          longitud: position.coords.longitude,
        });

        await cargarUbicaciones();
      },
      () => {
        setMensaje('No se pudo seguir la ubicación. Revisa los permisos del navegador.');
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 10000,
      }
    );

    setSiguiendo(true);
    setMensaje('Seguimiento de ubicación activado.');
  }

  function detenerSeguimiento() {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }

    setSiguiendo(false);
    setMensaje('Seguimiento detenido.');
  }

  return (
    <>
      <div className="page-subtitle">
        Monitoreo operativo con actualización automática cada 10 segundos
      </div>

      <div className="kpi-row">
        <KpiCard label="Disponibles" value={profesionales.filter((p) => p.estado === 'DISPONIBLE').length} variante="ok" />
        <KpiCard label="En visita" value={profesionales.filter((p) => p.estado === 'EN_VISITA').length} />
        <KpiCard label="En capacitación" value={profesionales.filter((p) => p.estado === 'EN_CAPACITACION').length} variante="warn" />
        <KpiCard label="Con ubicación" value={ubicaciones.length} />
      </div>

      {mensaje && (
        <div className="alert-item alert-item--info" style={{ marginBottom: 12 }}>
          {mensaje}
        </div>
      )}

      <div className="grid-2">
        <Panel
          titulo="Mapa en tiempo real"
          accion={
            rol === 'PROFESIONAL' ? (
              <div className="btn-group">
                <button className="btn btn-sm btn-primary" onClick={actualizarMiUbicacion}>
                  Actualizar mi ubicación
                </button>

                {siguiendo ? (
                  <button className="btn btn-sm btn-danger" onClick={detenerSeguimiento}>
                    Detener seguimiento
                  </button>
                ) : (
                  <button className="btn btn-sm btn-outline" onClick={iniciarSeguimiento}>
                    Seguir mi ubicación
                  </button>
                )}
              </div>
            ) : (
              <button className="btn btn-sm btn-outline" onClick={cargarTodo}>
                Refrescar
              </button>
            )
          }
        >
          <div className="mapa-profesionales">
            <MapContainer center={centro} zoom={11} style={{ height: '100%', width: '100%' }}>
              <TileLayer
                attribution="&copy; OpenStreetMap"
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {ubicaciones.map((u) => (
                <Marker
                  key={u.idProfesional}
                  position={[Number(u.latitud), Number(u.longitud)]}
                  icon={crearIcono(u.estado)}
                >
                  <Popup>
                    <strong>{u.nombreProfesional}</strong>
                    <br />
                    {u.email}
                    <br />
                    {labelEstado[u.estado]}
                    <br />
                    Última actualización: {new Date(u.fechaRegistro).toLocaleString('es-CL')}
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        </Panel>

        <Panel titulo="Estado operativo">
          {cargando ? (
            <div className="placeholder">Cargando profesionales...</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Profesional</th>
                  <th>Estado</th>
                  <th>Actualizar</th>
                </tr>
              </thead>
              <tbody>
                {profesionales.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <strong>{p.nombreCompleto}</strong>
                      <div style={{ fontSize: 11, color: '#9ca3af' }}>{p.email}</div>
                    </td>
                    <td>
                      <Badge variante={badgePorEstado[p.estado]}>
                        {labelEstado[p.estado]}
                      </Badge>
                    </td>
                    <td>
                      <select
                        className="estado-select"
                        value={p.estado}
                        disabled={guardandoId === p.id}
                        onChange={(e) => cambiarEstado(p.id, e.target.value as EstadoProfesional)}
                      >
                        <option value="DISPONIBLE">Disponible</option>
                        <option value="EN_VISITA">En visita</option>
                        <option value="EN_CAPACITACION">En capacitación</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      </div>
    </>
  );
}
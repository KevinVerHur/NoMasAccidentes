import { useCallback, useEffect, useMemo, useState } from 'react';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useNavigate } from 'react-router-dom';

import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import Modal from '../components/ui/Modal';
import { listarUbicacionesActivas } from '../api/ubicaciones';
import { obtenerDashboardAdmin } from '../api/dashboard';
import type {
  DashboardAdminResponse,
  EstadoVisita,
  VarianteBadge,
  VarianteAlerta,
  VarianteBarra,
  EstadoProfesional,
  UbicacionProfesionalResponse,
} from '../types';


const CENTRO_FALLBACK: [number, number] = [-33.4489, -70.6693];

const badgePorEstadoVisita: Record<EstadoVisita, VarianteBadge> = {
  PROGRAMADA: 'yellow',
  EN_CURSO: 'blue',
  REALIZADA: 'green',
  CANCELADA: 'red',
};

const labelEstadoVisita: Record<EstadoVisita, string> = {
  PROGRAMADA: 'Pendiente',
  EN_CURSO: 'En curso',
  REALIZADA: 'Realizada',
  CANCELADA: 'No realizada',
};

const iconoAlerta: Record<VarianteAlerta, string> = {
  peligro: '🔴',
  warn: '🟡',
  info: '🔵',
  ok: '🟢',
};

const claseAlerta: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn: 'alert-item alert-item--warn',
  info: 'alert-item alert-item--info',
  ok: 'alert-item alert-item--ok',
};

const colorBarra: Record<VarianteBarra, string> = {
  default: 'bg-azul',
  warn: 'bg-warn',
  ok: 'bg-ok',
  peligro: 'bg-peligro',
};

function varianteTasa(tasa: number | null): VarianteBarra {
  if (tasa === null) return 'default';
  if (tasa >= 5) return 'peligro';
  if (tasa >= 3) return 'warn';
  if (tasa === 0) return 'ok';
  return 'default';
}

function badgePorEstadoPago(estado: string): VarianteBadge {
  if (estado === 'Al día') return 'green';
  if (estado === 'Atrasado') return 'yellow';
  return 'red'; // Moroso / Suspendido
}

const fmtCLP = (v: number | null) => (v === null ? '—' : `$${v.toLocaleString('es-CL')}`);

const fmtFecha = (iso: string | null) =>
  iso
    ? new Date(`${iso}T00:00:00`).toLocaleDateString('es-CL', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    })
    : '—';

const mesActual = new Date().toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });

const labelEstado: Record<EstadoProfesional, string> = {
  DISPONIBLE: 'Disponible',
  EN_VISITA: 'En visita',
  EN_CAPACITACION: 'En capacitación',
};

const colorPorEstado: Record<EstadoProfesional, string> = {
  DISPONIBLE: '#27ae60',
  EN_VISITA: '#2563eb',
  EN_CAPACITACION: '#e07b00',
};

function minutosDesde(fechaRegistro: string) {
  return Math.floor((Date.now() - new Date(fechaRegistro).getTime()) / 60000);
}

function estaDesactualizada(ubicacion: UbicacionProfesionalResponse) {
  return minutosDesde(ubicacion.fechaRegistro) >= 10;
}

function textoFrescura(ubicacion: UbicacionProfesionalResponse) {
  const minutos = minutosDesde(ubicacion.fechaRegistro);

  if (minutos < 1) return 'Actualizada hace menos de 1 minuto';
  if (minutos === 1) return 'Actualizada hace 1 minuto';
  return `Actualizada hace ${minutos} minutos`;
}

function crearIcono(estado: EstadoProfesional, desactualizada: boolean) {
  const color = desactualizada ? '#6b7280' : colorPorEstado[estado];

  return L.divIcon({
    className: 'estado-marker',
    html: `<span style="background:${color}"></span>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  });
}

function AjustarMapa({
  ubicaciones,
  grande,
}: {
  ubicaciones: UbicacionProfesionalResponse[];
  grande: boolean;
}) {
  const map = useMap();

  useEffect(() => {
    const id = window.setTimeout(() => {
      map.invalidateSize();

      if (ubicaciones.length === 0) {
        map.setView(CENTRO_FALLBACK, grande ? 11 : 10);
        return;
      }

      const puntos = ubicaciones.map((u) => [
        Number(u.latitud),
        Number(u.longitud),
      ]) as [number, number][];

      if (puntos.length === 1) {
        map.setView(puntos[0], grande ? 15 : 13, { animate: true });
        return;
      }

      map.fitBounds(puntos, {
        padding: grande ? [60, 60] : [28, 28],
        maxZoom: grande ? 15 : 13,
        animate: true,
      });
    }, 150);

    return () => window.clearTimeout(id);
  }, [map, ubicaciones, grande]);

  return null;
}

function MapaAdmin({
  ubicaciones,
  grande = false,
}: {
  ubicaciones: UbicacionProfesionalResponse[];
  grande?: boolean;
}) {
  const centro = useMemo<[number, number]>(() => {
    if (ubicaciones.length === 0) return CENTRO_FALLBACK;
    return [Number(ubicaciones[0].latitud), Number(ubicaciones[0].longitud)];
  }, [ubicaciones]);

  return (
    <div style={{ height: '100%', width: '100%', position: 'relative', zIndex: 0 }}>
      <MapContainer
        center={centro}
        zoom={ubicaciones.length > 0 ? 13 : 10}
        style={{ height: '100%', width: '100%', zIndex: 0 }}
        scrollWheelZoom={grande}
      >
        <AjustarMapa ubicaciones={ubicaciones} grande={grande} />

        <TileLayer
          attribution="&copy; OpenStreetMap"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {ubicaciones.map((u) => {
          const desactualizada = estaDesactualizada(u);

          return (
            <Marker
              key={u.idProfesional}
              position={[Number(u.latitud), Number(u.longitud)]}
              icon={crearIcono(u.estado, desactualizada)}
            >
              <Popup>
                <strong>{u.nombreProfesional}</strong>
                <br />
                {u.email}
                <br />
                Estado: {labelEstado[u.estado]}
                <br />
                {textoFrescura(u)}
                {desactualizada && (
                  <>
                    <br />
                    Ubicación desactualizada
                  </>
                )}
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>

      {ubicaciones.length === 0 && (
        <div
          style={{
            position: 'absolute',
            left: 16,
            bottom: 16,
            zIndex: 500,
            background: 'rgba(255,255,255,.94)',
            border: '1px solid #d1d5db',
            borderRadius: 8,
            padding: '8px 10px',
            fontSize: 12,
            color: '#4b5563',
            boxShadow: '0 4px 14px rgba(0,0,0,.12)',
          }}
        >
          Sin ubicaciones activas reportadas.
        </div>
      )}
    </div>
  );
}

export default function DashboardAdmin() {
  const [modalMapa, setModalMapa] = useState(false);
  const [ubicaciones, setUbicaciones] = useState<UbicacionProfesionalResponse[]>([]);
  const [errorMapa, setErrorMapa] = useState<string | null>(null);
  const [ultimaActualizacionMapa, setUltimaActualizacionMapa] = useState<Date | null>(null);
  const [datos, setDatos] = useState<DashboardAdminResponse | null>(null);
  const [errorDatos, setErrorDatos] = useState<string | null>(null);
  const navigate = useNavigate();


  useEffect(() => {
    obtenerDashboardAdmin()
      .then(setDatos)
      .catch(() => setErrorDatos('No se pudieron cargar los datos del panel.'));
  }, []);

  const cargarMapa = useCallback(async () => {
    // setCargandoMapa(true);
    setErrorMapa(null);

    try {
      const data = await listarUbicacionesActivas();
      setUbicaciones(data);
      setUltimaActualizacionMapa(new Date());
    } catch {
      setErrorMapa('No se pudieron cargar las ubicaciones activas.');
    } finally {
      // setCargandoMapa(false);
    }
  }, []);

  useEffect(() => {
    cargarMapa();

    const id = window.setInterval(cargarMapa, 5000);

    return () => window.clearInterval(id);
  }, [cargarMapa]);

  const profesionalesActualizados = ubicaciones.filter((u) => !estaDesactualizada(u)).length;
  const profesionalesDesactualizados = ubicaciones.filter(estaDesactualizada).length;

  const kpis = datos?.kpis;
  const visitas = datos?.visitasRecientes ?? [];
  const alertas = datos?.alertas ?? [];
  const accidentabilidad = datos?.accidentabilidad ?? [];
  const pagos = datos?.controlPagos ?? [];

  // Magnitud de la barra de accidentabilidad: usa la tasa si hay nº de trabajadores; si no, los accidentes.
  const magnitud = (a: { tasa: number | null; accidentes: number }) => a.tasa ?? a.accidentes;
  const maxMagnitud = Math.max(1, ...accidentabilidad.map(magnitud));

  return (
    <>
      <div className="page-title">Dashboard General</div>
      <div className="page-subtitle" style={{ textTransform: 'capitalize' }}>
        Resumen operativo — {mesActual}
      </div>

      {errorDatos && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          <span>🔴</span>
          <div>{errorDatos}</div>
        </div>
      )}

      <div className="kpi-row">
        <KpiCard label="Clientes activos" value={kpis?.clientesActivos ?? 0} sub="Total vigentes" variante="ok" />
        <KpiCard label="Visitas pendientes" value={kpis?.visitasPendientesSemana ?? 0} sub="Semana en curso" variante="warn" />
        <KpiCard label="Clientes morosos" value={kpis?.clientesMorosos ?? 0} sub="Morosos / suspendidos" variante="peligro" />
        <KpiCard label="Capacitaciones este mes" value={kpis?.capacitacionesMes ?? 0} sub="Mes en curso" />
      </div>

      <div className="grid-2">
        <Panel
          titulo="📅 Visitas recientes"
          accion={
            <button className="btn btn-sm btn-outline" onClick={() => navigate('/visitas')}>
              Ver visitas
            </button>
          }
        >

          <table className="app-table">
            <thead>
              <tr>
                {['Cliente', 'Profesional', 'Fecha', 'Estado'].map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visitas.length === 0 ? (
                <tr>
                  <td colSpan={4} style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                    Sin visitas registradas.
                  </td>
                </tr>
              ) : (
                visitas.map((v, i) => (
                  <tr key={i}>
                    <td>{v.cliente}</td>
                    <td>{v.profesional}</td>
                    <td>{fmtFecha(v.fecha)}</td>
                    <td>
                      <Badge variante={badgePorEstadoVisita[v.estado]}>{labelEstadoVisita[v.estado]}</Badge>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </Panel>

        <Panel titulo="🔔 Alertas del sistema">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12 }}>
            {alertas.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                Sin alertas activas. 🎉
              </div>
            ) : (
              alertas.map((a, i) => (
                <div key={i} className={claseAlerta[a.severidad]}>
                  <span>{iconoAlerta[a.severidad]}</span>
                  <div>
                    <b>{a.titulo}</b> {a.detalle}
                  </div>
                </div>
              ))
            )}
          </div>
        </Panel>
      </div>

      <div className="grid-2">
        <Panel
          titulo="📍 Profesionales en terreno"
          accion={
            <div className="btn-group">
              <button className="btn btn-sm btn-outline" onClick={() => navigate('/profesionales')}>
                Profesionales
              </button>
              <button className="btn btn-sm btn-primary" onClick={() => setModalMapa(true)}>
                Ver mapa
              </button>
            </div>
          }
        >
          <div style={{ height: 181, width: '100%', position: 'relative', zIndex: 0 }}>
            <MapaAdmin ubicaciones={ubicaciones} />
          </div>

          <div
            style={{
              display: 'flex',
              gap: 14,
              padding: '10px 16px',
              borderTop: '1px solid #f3f4f6',
              fontSize: 11,
              color: '#4b5563',
              flexWrap: 'wrap',
            }}
          >
            <span>🟢 Actualizados: {profesionalesActualizados}</span>
            <span>⚪ Desactualizados: {profesionalesDesactualizados}</span>
            <span>Total visibles: {ubicaciones.length}</span>
            {ultimaActualizacionMapa && (
              <span>Última lectura: {ultimaActualizacionMapa.toLocaleTimeString('es-CL')}</span>
            )}
            {errorMapa && <span style={{ color: '#c0392b' }}>{errorMapa}</span>}
          </div>
        </Panel>

        <Panel
          titulo="📈 Accidentabilidad por cliente"
          accion={
            <button className="btn btn-sm btn-outline" onClick={() => navigate('/reportes')}>
              Ver reportes
            </button>
          }
        >
          <div style={{ padding: '14px 16px' }}>
            {accidentabilidad.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#9ca3af', padding: 16, fontSize: 12 }}>
                Sin datos de accidentabilidad.
              </div>
            ) : (
              accidentabilidad.map((a) => (
                <div
                  key={a.idCliente}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    marginBottom: 10,
                    fontSize: 11,
                  }}
                >
                  <span style={{ width: 110, textAlign: 'right', color: '#6b7280', flexShrink: 0 }}>
                    {a.cliente}
                  </span>
                  <div style={{ flex: 1, background: '#e5e7eb', borderRadius: 6, height: 14, overflow: 'hidden' }}>
                    <div
                      className={`h-full rounded ${colorBarra[varianteTasa(a.tasa)]}`}
                      style={{ width: `${(magnitud(a) / maxMagnitud) * 100}%` }}
                    />
                  </div>
                  <span style={{ width: 48, fontWeight: 'bold', color: '#374151', textAlign: 'right' }}>
                    {a.tasa !== null ? `${a.tasa.toFixed(1)}%` : `${a.accidentes} acc.`}
                  </span>
                </div>
              ))
            )}
          </div>
        </Panel>
      </div>

      <Panel
        titulo="💰 Control de pagos y morosidades"
        accion={
          <button className="btn btn-sm btn-outline" onClick={() => navigate('/pagos')}>
            Ver pagos
          </button>
        }
      >
        <table className="app-table">
          <thead>
            <tr>
              {['Cliente', 'Plan mensual', 'Último pago', 'Meses adeudados', 'Estado'].map((h) => (
                <th key={h}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {pagos.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                  Sin clientes con planes de pago.
                </td>
              </tr>
            ) : (
              pagos.map((p) => (
                <tr key={p.idCliente}>
                  <td>{p.cliente}</td>
                  <td>{fmtCLP(p.planMensual)}</td>
                  <td>{fmtFecha(p.ultimoPago)}</td>
                  <td>{p.mesesAdeudados}</td>
                  <td>
                    <Badge variante={badgePorEstadoPago(p.estado)}>{p.estado}</Badge>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </Panel>

      <Modal
        abierto={modalMapa}
        titulo="Mapa en tiempo real"
        ancho="lg"
        onCerrar={() => setModalMapa(false)}
        footer={
          <div
            style={{
              width: '100%',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: 12,
              fontSize: 12,
              color: '#4b5563',
            }}
          >
            <span>
              {ultimaActualizacionMapa
                ? `Actualización automática cada 5 segundos · Última lectura: ${ultimaActualizacionMapa.toLocaleTimeString('es-CL')}`
                : 'Esperando ubicaciones activas.'}
            </span>

            <button className="btn btn-outline" onClick={() => setModalMapa(false)}>
              Cerrar
            </button>
          </div>
        }
      >
        <div style={{ height: 520, position: 'relative', zIndex: 0 }}>
          <MapaAdmin ubicaciones={ubicaciones} grande />
        </div>

        <div
          style={{
            display: 'flex',
            gap: 12,
            marginTop: 12,
            fontSize: 12,
            color: '#4b5563',
            flexWrap: 'wrap',
          }}
        >
          <span>🟢 Disponible</span>
          <span>🔵 En visita</span>
          <span>🟠 En capacitación</span>
          <span>⚪ Ubicación desactualizada</span>
        </div>
      </Modal>
    </>
  );
}
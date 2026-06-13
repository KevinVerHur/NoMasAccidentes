import { useCallback, useEffect, useMemo, useState } from 'react';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import Modal from '../components/ui/Modal';
import { listarUbicacionesActivas } from '../api/ubicaciones';
import type {
  VisitaResumen,
  AlertaDashboard,
  PagoResumen,
  AccidentabilidadResumen,
  VarianteBadge,
  VarianteAlerta,
  EstadoProfesional,
  UbicacionProfesionalResponse,
} from '../types';

const CENTRO_FALLBACK: [number, number] = [-33.4489, -70.6693];

const visitas: VisitaResumen[] = [
  { cliente: 'Constructora LM', profesional: 'E. Pérez', fecha: '18 Abr', estado: 'Realizada' },
  { cliente: 'Transporte Sur', profesional: 'N. Lavín', fecha: '19 Abr', estado: 'Realizada' },
  { cliente: 'Minera Andes', profesional: 'K. Vergara', fecha: '21 Abr', estado: 'Pendiente' },
  { cliente: 'Agrícola Del Valle', profesional: 'E. Pérez', fecha: '22 Abr', estado: 'Pendiente' },
  { cliente: 'Fábrica MetalPro', profesional: 'N. Lavín', fecha: '15 Abr', estado: 'No realizada' },
];

const alertas: AlertaDashboard[] = [
  { tipo: 'peligro', icono: '🔴', destacado: 'Transporte Sur', texto: ' lleva 2 meses sin pago. Servicio en riesgo de suspensión.' },
  { tipo: 'peligro', icono: '🔴', destacado: 'Fábrica MetalPro', texto: ' incumplió visita planificada del 15 Abr.' },
  { tipo: 'warn', icono: '🟡', destacado: 'Minera Andes', texto: ' usó 9/10 asesorías incluidas en el plan.' },
  { tipo: 'warn', icono: '🟡', destacado: 'Capacitación "Manejo manual de cargas"', texto: ' sin confirmar asistentes.' },
  { tipo: 'info', icono: '🔵', destacado: 'Reporte mensual de Agrícola Del Valle', texto: ' listo para enviar.' },
];

const accidentabilidad: AccidentabilidadResumen[] = [
  { cliente: 'Minera Andes', porcentaje: 82, tasa: '3.8%', variante: 'peligro' },
  { cliente: 'Agrícola Valle', porcentaje: 65, tasa: '3.5%', variante: 'warn' },
  { cliente: 'Transporte Sur', porcentaje: 55, tasa: '3.3%', variante: 'warn' },
  { cliente: 'Constructora LM', porcentaje: 45, tasa: '3.1%', variante: 'default' },
  { cliente: 'MetalPro', porcentaje: 28, tasa: '1.9%', variante: 'ok' },
];

const pagos: PagoResumen[] = [
  { cliente: 'Constructora LM', planMensual: '$350.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Minera Andes', planMensual: '$480.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Agrícola Del Valle', planMensual: '$290.000', ultimoPago: '15 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
  { cliente: 'Transporte Sur', planMensual: '$320.000', ultimoPago: '01 Feb 2026', mesesAdeudados: 2, estado: 'Moroso' },
  { cliente: 'Fábrica MetalPro', planMensual: '$260.000', ultimoPago: '20 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
];

const badgePorEstadoVisita: Record<VisitaResumen['estado'], VarianteBadge> = {
  Realizada: 'green',
  Pendiente: 'yellow',
  'No realizada': 'red',
};

const badgePorEstadoPago: Record<PagoResumen['estado'], VarianteBadge> = {
  'Al día': 'green',
  Atrasado: 'yellow',
  Moroso: 'red',
};

const claseAlerta: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn: 'alert-item alert-item--warn',
  info: 'alert-item alert-item--info',
  ok: 'alert-item alert-item--ok',
};

const colorBarra: Record<AccidentabilidadResumen['variante'], string> = {
  default: 'bg-azul',
  warn: 'bg-warn',
  ok: 'bg-ok',
  peligro: 'bg-peligro',
};

const accionPago: Record<PagoResumen['estado'], { label: string; clase: string }> = {
  'Al día': { label: 'Ver detalle', clase: 'btn btn-sm btn-outline' },
  Atrasado: { label: 'Notificar', clase: 'btn btn-sm btn-warn' },
  Moroso: { label: 'Suspender', clase: 'btn btn-sm btn-danger' },
};

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
  const [cargandoMapa, setCargandoMapa] = useState(false);
  const [errorMapa, setErrorMapa] = useState<string | null>(null);
  const [ultimaActualizacionMapa, setUltimaActualizacionMapa] = useState<Date | null>(null);

  const cargarMapa = useCallback(async () => {
    setCargandoMapa(true);
    setErrorMapa(null);

    try {
      const data = await listarUbicacionesActivas();
      setUbicaciones(data);
      setUltimaActualizacionMapa(new Date());
    } catch {
      setErrorMapa('No se pudieron cargar las ubicaciones activas.');
    } finally {
      setCargandoMapa(false);
    }
  }, []);

  useEffect(() => {
    cargarMapa();

    const id = window.setInterval(cargarMapa, 5000);

    return () => window.clearInterval(id);
  }, [cargarMapa]);

  const profesionalesActualizados = ubicaciones.filter((u) => !estaDesactualizada(u)).length;
  const profesionalesDesactualizados = ubicaciones.filter(estaDesactualizada).length;

  return (
    <>
      <div className="page-title">Dashboard General</div>
      <div className="page-subtitle">Resumen operativo — Abril 2026</div>

      <div className="kpi-row">
        <KpiCard label="Clientes activos" value={24} sub="+2 este mes" variante="ok" />
        <KpiCard label="Visitas pendientes" value={7} sub="Semana en curso" variante="warn" />
        <KpiCard label="Clientes morosos" value={3} sub="Servicio suspendido: 1" variante="peligro" />
        <KpiCard label="Capacitaciones este mes" value={5} sub="Próxima: 25 Abr" />
      </div>

      <div className="grid-2">
        <Panel
          titulo="📅 Visitas recientes"
          accion={<button className="btn btn-sm btn-primary">+ Nueva visita</button>}
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
              {visitas.map((v, i) => (
                <tr key={i}>
                  <td>{v.cliente}</td>
                  <td>{v.profesional}</td>
                  <td>{v.fecha}</td>
                  <td>
                    <Badge variante={badgePorEstadoVisita[v.estado]}>{v.estado}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Panel>

        <Panel
          titulo="🔔 Alertas del sistema"
          accion={<button className="btn btn-sm btn-outline">Ver todas</button>}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12 }}>
            {alertas.map((a, i) => (
              <div key={i} className={claseAlerta[a.tipo]}>
                <span>{a.icono}</span>
                <div>
                  <b>{a.destacado}</b>
                  {a.texto}
                </div>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      <div className="grid-2">
        <Panel
          titulo="📍 Profesionales en terreno"
          accion={
            <div className="btn-group">
              <button className="btn btn-sm btn-outline" onClick={cargarMapa} disabled={cargandoMapa}>
                {cargandoMapa ? 'Actualizando...' : 'Refrescar'}
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
          accion={<button className="btn btn-sm btn-outline">Ver reportes</button>}
        >
          <div style={{ padding: '14px 16px' }}>
            {accidentabilidad.map((a, i) => (
              <div
                key={i}
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
                  <div className={`h-full rounded ${colorBarra[a.variante]}`} style={{ width: `${a.porcentaje}%` }} />
                </div>
                <span style={{ width: 36, fontWeight: 'bold', color: '#374151' }}>{a.tasa}</span>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      <Panel
        titulo="💰 Control de pagos y morosidades"
        accion={<button className="btn btn-sm btn-primary">Registrar pago</button>}
      >
        <table className="app-table">
          <thead>
            <tr>
              {['Cliente', 'Plan mensual', 'Último pago', 'Meses adeudados', 'Estado', 'Acción'].map((h) => (
                <th key={h}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {pagos.map((p, i) => (
              <tr key={i}>
                <td>{p.cliente}</td>
                <td>{p.planMensual}</td>
                <td>{p.ultimoPago}</td>
                <td>{p.mesesAdeudados}</td>
                <td>
                  <Badge variante={badgePorEstadoPago[p.estado]}>{p.estado}</Badge>
                </td>
                <td>
                  <button className={accionPago[p.estado].clase}>
                    {accionPago[p.estado].label}
                  </button>
                </td>
              </tr>
            ))}
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
import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import type {
  VisitaResumen,
  AlertaDashboard,
  PagoResumen,
  AccidentabilidadResumen,
  VarianteBadge,
  VarianteAlerta,
} from '../types';

const visitas: VisitaResumen[] = [
  { cliente: 'Constructora LM',    profesional: 'E. Pérez',   fecha: '18 Abr', estado: 'Realizada' },
  { cliente: 'Transporte Sur',     profesional: 'N. Lavín',   fecha: '19 Abr', estado: 'Realizada' },
  { cliente: 'Minera Andes',       profesional: 'K. Vergara', fecha: '21 Abr', estado: 'Pendiente' },
  { cliente: 'Agrícola Del Valle', profesional: 'E. Pérez',   fecha: '22 Abr', estado: 'Pendiente' },
  { cliente: 'Fábrica MetalPro',   profesional: 'N. Lavín',   fecha: '15 Abr', estado: 'No realizada' },
];

const alertas: AlertaDashboard[] = [
  { tipo: 'peligro', icono: '🔴', destacado: 'Transporte Sur',                         texto: ' lleva 2 meses sin pago. Servicio en riesgo de suspensión.' },
  { tipo: 'peligro', icono: '🔴', destacado: 'Fábrica MetalPro',                       texto: ' incumplió visita planificada del 15 Abr.' },
  { tipo: 'warn',    icono: '🟡', destacado: 'Minera Andes',                           texto: ' usó 9/10 asesorías incluidas en el plan.' },
  { tipo: 'warn',    icono: '🟡', destacado: 'Capacitación "Manejo manual de cargas"', texto: ' sin confirmar asistentes.' },
  { tipo: 'info',    icono: '🔵', destacado: 'Reporte mensual de Agrícola Del Valle',  texto: ' listo para enviar.' },
];

const accidentabilidad: AccidentabilidadResumen[] = [
  { cliente: 'Minera Andes',    porcentaje: 82, tasa: '3.8%', variante: 'peligro' },
  { cliente: 'Agrícola Valle',  porcentaje: 65, tasa: '3.5%', variante: 'warn' },
  { cliente: 'Transporte Sur',  porcentaje: 55, tasa: '3.3%', variante: 'warn' },
  { cliente: 'Constructora LM', porcentaje: 45, tasa: '3.1%', variante: 'default' },
  { cliente: 'MetalPro',        porcentaje: 28, tasa: '1.9%', variante: 'ok' },
];

const pagos: PagoResumen[] = [
  { cliente: 'Constructora LM',    planMensual: '$350.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Minera Andes',       planMensual: '$480.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Agrícola Del Valle', planMensual: '$290.000', ultimoPago: '15 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
  { cliente: 'Transporte Sur',     planMensual: '$320.000', ultimoPago: '01 Feb 2026', mesesAdeudados: 2, estado: 'Moroso' },
  { cliente: 'Fábrica MetalPro',   planMensual: '$260.000', ultimoPago: '20 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
];

const badgePorEstadoVisita: Record<VisitaResumen['estado'], VarianteBadge> = {
  'Realizada':    'green',
  'Pendiente':    'yellow',
  'No realizada': 'red',
};

const badgePorEstadoPago: Record<PagoResumen['estado'], VarianteBadge> = {
  'Al día':  'green',
  'Atrasado':'yellow',
  'Moroso':  'red',
};

const claseAlerta: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn:    'alert-item alert-item--warn',
  info:    'alert-item alert-item--info',
  ok:      'alert-item alert-item--ok',
};

const colorBarra: Record<AccidentabilidadResumen['variante'], string> = {
  default: 'bg-azul',
  warn:    'bg-warn',
  ok:      'bg-ok',
  peligro: 'bg-peligro',
};

const accionPago: Record<PagoResumen['estado'], { label: string; clase: string }> = {
  'Al día':  { label: 'Ver detalle', clase: 'btn btn-sm btn-outline' },
  'Atrasado':{ label: 'Notificar',   clase: 'btn btn-sm btn-warn' },
  'Moroso':  { label: 'Suspender',   clase: 'btn btn-sm btn-danger' },
};

export default function Dashboard() {
  return (
    <>
      <div className="page-title">Dashboard General</div>
      <div className="page-subtitle">Resumen operativo — Abril 2026</div>

      {/* KPI Row */}
      <div className="kpi-row">
        <KpiCard label="Clientes activos"        value={24} sub="+2 este mes"            variante="ok" />
        <KpiCard label="Visitas pendientes"       value={7}  sub="Semana en curso"        variante="warn" />
        <KpiCard label="Clientes morosos"         value={3}  sub="Servicio suspendido: 1" variante="peligro" />
        <KpiCard label="Capacitaciones este mes"  value={5}  sub="Próxima: 25 Abr" />
      </div>

      {/* Fila 1: Visitas + Alertas */}
      <div className="grid-2">
        <Panel
          titulo="📅 Visitas recientes"
          accion={<button className="btn btn-sm btn-primary">+ Nueva visita</button>}
        >
          <table className="app-table">
            <thead>
              <tr>
                {['Cliente', 'Profesional', 'Fecha', 'Estado'].map(h => <th key={h}>{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {visitas.map((v, i) => (
                <tr key={i}>
                  <td>{v.cliente}</td>
                  <td>{v.profesional}</td>
                  <td>{v.fecha}</td>
                  <td><Badge variante={badgePorEstadoVisita[v.estado]}>{v.estado}</Badge></td>
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
                <div><b>{a.destacado}</b>{a.texto}</div>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      {/* Fila 2: Mapa + Accidentabilidad */}
      <div className="grid-2">
        <Panel
          titulo="📍 Profesionales en terreno"
          accion={<button className="btn btn-sm btn-outline">Ver mapa</button>}
        >
          <div className="relative h-[180px] bg-[#d9e8f5] flex flex-col items-center justify-center gap-1.5 overflow-hidden">
            <svg className="absolute inset-0 w-full h-full" viewBox="0 0 400 180" preserveAspectRatio="none">
              <line x1="0" y1="60"  x2="400" y2="60"  stroke="#a0bcd8" strokeWidth="1" />
              <line x1="0" y1="120" x2="400" y2="120" stroke="#a0bcd8" strokeWidth="1" />
              <line x1="100" y1="0" x2="100" y2="180" stroke="#a0bcd8" strokeWidth="1" />
              <line x1="200" y1="0" x2="200" y2="180" stroke="#a0bcd8" strokeWidth="1" />
              <line x1="300" y1="0" x2="300" y2="180" stroke="#a0bcd8" strokeWidth="1" />
            </svg>
            <div className="absolute w-3 h-3 rounded-full border-2 border-white shadow-md"
                 style={{ background: '#c0392b', top: 40, left: 80 }} />
            <div className="absolute w-3 h-3 rounded-full border-2 border-white shadow-md"
                 style={{ background: '#27ae60', top: 90, left: 200 }} />
            <div className="absolute w-3 h-3 rounded-full border-2 border-white shadow-md"
                 style={{ background: '#e07b00', top: 60, left: 310 }} />
            <span className="relative z-10 text-azul text-[13px] font-bold">🗺️ Mapa en tiempo real</span>
            <span className="relative z-10 text-[11px] text-[#5a8cb5]">3 profesionales en terreno hoy</span>
          </div>
          <div style={{ display: 'flex', gap: 14, padding: '10px 16px', borderTop: '1px solid #f3f4f6', fontSize: 11, color: '#4b5563' }}>
            <span>🔴 E. Pérez — en visita</span>
            <span>🟢 N. Lavín — disponible</span>
            <span>🟠 K. Vergara — en capacitación</span>
          </div>
        </Panel>

        <Panel
          titulo="📈 Accidentabilidad por cliente"
          accion={<button className="btn btn-sm btn-outline">Ver reportes</button>}
        >
          <div style={{ padding: '14px 16px' }}>
            {accidentabilidad.map((a, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10, fontSize: 11 }}>
                <span style={{ width: 110, textAlign: 'right', color: '#6b7280', flexShrink: 0 }}>{a.cliente}</span>
                <div style={{ flex: 1, background: '#e5e7eb', borderRadius: 6, height: 14, overflow: 'hidden' }}>
                  <div className={`h-full rounded ${colorBarra[a.variante]}`} style={{ width: `${a.porcentaje}%` }} />
                </div>
                <span style={{ width: 36, fontWeight: 'bold', color: '#374151' }}>{a.tasa}</span>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      {/* Fila 3: Pagos (full width) */}
      <Panel
        titulo="💰 Control de pagos y morosidades"
        accion={<button className="btn btn-sm btn-primary">Registrar pago</button>}
      >
        <table className="app-table">
          <thead>
            <tr>
              {['Cliente', 'Plan mensual', 'Último pago', 'Meses adeudados', 'Estado', 'Acción'].map(h => (
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
                <td><Badge variante={badgePorEstadoPago[p.estado]}>{p.estado}</Badge></td>
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
    </>
  );
}

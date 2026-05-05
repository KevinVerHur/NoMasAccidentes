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
  { cliente: 'Constructora LM',   profesional: 'E. Pérez',   fecha: '18 Abr', estado: 'Realizada' },
  { cliente: 'Transporte Sur',    profesional: 'N. Lavín',   fecha: '19 Abr', estado: 'Realizada' },
  { cliente: 'Minera Andes',      profesional: 'K. Vergara', fecha: '21 Abr', estado: 'Pendiente' },
  { cliente: 'Agrícola Del Valle',profesional: 'E. Pérez',   fecha: '22 Abr', estado: 'Pendiente' },
  { cliente: 'Fábrica MetalPro',  profesional: 'N. Lavín',   fecha: '15 Abr', estado: 'No realizada' },
];

const alertas: AlertaDashboard[] = [
  { tipo: 'peligro', icono: '🔴', destacado: 'Transporte Sur',                          texto: ' lleva 2 meses sin pago. Servicio en riesgo de suspensión.' },
  { tipo: 'peligro', icono: '🔴', destacado: 'Fábrica MetalPro',                        texto: ' incumplió visita planificada del 15 Abr.' },
  { tipo: 'warn',    icono: '🟡', destacado: 'Minera Andes',                            texto: ' usó 9/10 asesorías incluidas en el plan.' },
  { tipo: 'warn',    icono: '🟡', destacado: 'Capacitación "Manejo manual de cargas"',  texto: ' sin confirmar asistentes.' },
  { tipo: 'info',    icono: '🔵', destacado: 'Reporte mensual de Agrícola Del Valle',   texto: ' listo para enviar.' },
];

const accidentabilidad: AccidentabilidadResumen[] = [
  { cliente: 'Minera Andes',    porcentaje: 82, tasa: '3.8%', variante: 'peligro' },
  { cliente: 'Agrícola Valle',  porcentaje: 65, tasa: '3.5%', variante: 'warn' },
  { cliente: 'Transporte Sur',  porcentaje: 55, tasa: '3.3%', variante: 'warn' },
  { cliente: 'Constructora LM', porcentaje: 45, tasa: '3.1%', variante: 'default' },
  { cliente: 'MetalPro',        porcentaje: 28, tasa: '1.9%', variante: 'ok' },
];

const pagos: PagoResumen[] = [
  { cliente: 'Constructora LM',   planMensual: '$350.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Minera Andes',      planMensual: '$480.000', ultimoPago: '01 Abr 2026', mesesAdeudados: 0, estado: 'Al día' },
  { cliente: 'Agrícola Del Valle',planMensual: '$290.000', ultimoPago: '15 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
  { cliente: 'Transporte Sur',    planMensual: '$320.000', ultimoPago: '01 Feb 2026', mesesAdeudados: 2, estado: 'Moroso' },
  { cliente: 'Fábrica MetalPro',  planMensual: '$260.000', ultimoPago: '20 Mar 2026', mesesAdeudados: 1, estado: 'Atrasado' },
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

const colorAlerta: Record<VarianteAlerta, string> = {
  peligro: 'bg-red-50 text-red-800',
  warn:    'bg-yellow-50 text-yellow-800',
  info:    'bg-blue-50 text-blue-800',
  ok:      'bg-green-50 text-green-800',
};

const colorBarra: Record<AccidentabilidadResumen['variante'], string> = {
  default: 'bg-azul',
  warn:    'bg-warn',
  ok:      'bg-ok',
  peligro: 'bg-peligro',
};

const accionPago: Record<PagoResumen['estado'], string> = {
  'Al día':  'Ver detalle',
  'Atrasado':'Notificar',
  'Moroso':  'Suspender',
};

export default function Dashboard() {
  return (
    <>
      <div className="text-[18px] font-bold text-azul mb-1">Dashboard General</div>
      <div className="text-[12px] text-gray-400 mb-5">Resumen operativo — Abril 2026</div>

      {/* KPI Row */}
      <div className="flex gap-3.5 mb-5 flex-wrap">
        <KpiCard label="Clientes activos"        value={24} sub="+2 este mes"             variante="ok" />
        <KpiCard label="Visitas pendientes"       value={7}  sub="Semana en curso"         variante="warn" />
        <KpiCard label="Clientes morosos"         value={3}  sub="Servicio suspendido: 1"  variante="peligro" />
        <KpiCard label="Capacitaciones este mes"  value={5}  sub="Próxima: 25 Abr" />
      </div>

      {/* Fila 1: Visitas + Alertas */}
      <div className="grid grid-cols-2 gap-4 mb-0">
        <Panel
          titulo="📅 Visitas recientes"
          accion={
            <button className="text-[11px] px-2.5 py-1 rounded bg-azul text-white border-none cursor-pointer">
              + Nueva visita
            </button>
          }
        >
          <table className="w-full border-collapse text-[12px]">
            <thead>
              <tr>
                {['Cliente', 'Profesional', 'Fecha', 'Estado'].map(h => (
                  <th key={h} className="bg-gray-50 text-gray-500 font-semibold px-3 py-2 text-left border-b border-gray-100">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visitas.map((v, i) => (
                <tr key={i} className="hover:bg-slate-50">
                  <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{v.cliente}</td>
                  <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{v.profesional}</td>
                  <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{v.fecha}</td>
                  <td className="px-3 py-2 border-b border-gray-50">
                    <Badge variante={badgePorEstadoVisita[v.estado]}>{v.estado}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Panel>

        <Panel
          titulo="🔔 Alertas del sistema"
          accion={
            <button className="text-[11px] px-2.5 py-1 rounded bg-white text-azul border border-azul cursor-pointer">
              Ver todas
            </button>
          }
        >
          <div className="flex flex-col gap-2 p-3">
            {alertas.map((a, i) => (
              <div key={i} className={`flex items-start gap-2.5 px-2.5 py-2 rounded-md text-[12px] ${colorAlerta[a.tipo]}`}>
                <span className="shrink-0">{a.icono}</span>
                <div><b>{a.destacado}</b>{a.texto}</div>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      {/* Fila 2: Mapa + Accidentabilidad */}
      <div className="grid grid-cols-2 gap-4 mb-0">
        <Panel
          titulo="📍 Profesionales en terreno"
          accion={
            <button className="text-[11px] px-2.5 py-1 rounded bg-white text-azul border border-azul cursor-pointer">
              Ver mapa
            </button>
          }
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
          <div className="flex gap-3.5 px-4 py-2.5 text-[11px] border-t border-gray-100 text-gray-600">
            <span>🔴 E. Pérez — en visita</span>
            <span>🟢 N. Lavín — disponible</span>
            <span>🟠 K. Vergara — en capacitación</span>
          </div>
        </Panel>

        <Panel
          titulo="📈 Accidentabilidad por cliente"
          accion={
            <button className="text-[11px] px-2.5 py-1 rounded bg-white text-azul border border-azul cursor-pointer">
              Ver reportes
            </button>
          }
        >
          <div className="px-4 py-3.5">
            {accidentabilidad.map((a, i) => (
              <div key={i} className="flex items-center gap-2 mb-2.5 text-[11px]">
                <span className="w-[110px] text-right text-gray-500 shrink-0">{a.cliente}</span>
                <div className="flex-1 bg-gray-200 rounded h-3.5 overflow-hidden">
                  <div
                    className={`h-full rounded ${colorBarra[a.variante]}`}
                    style={{ width: `${a.porcentaje}%` }}
                  />
                </div>
                <span className="w-10 font-bold text-gray-700">{a.tasa}</span>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      {/* Fila 3: Pagos (full width) */}
      <Panel
        titulo="💰 Control de pagos y morosidades"
        accion={
          <button className="text-[11px] px-2.5 py-1 rounded bg-azul text-white border-none cursor-pointer">
            Registrar pago
          </button>
        }
      >
        <table className="w-full border-collapse text-[12px]">
          <thead>
            <tr>
              {['Cliente', 'Plan mensual', 'Último pago', 'Meses adeudados', 'Estado', 'Acción'].map(h => (
                <th key={h} className="bg-gray-50 text-gray-500 font-semibold px-3 py-2 text-left border-b border-gray-100">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {pagos.map((p, i) => (
              <tr key={i} className="hover:bg-slate-50">
                <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{p.cliente}</td>
                <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{p.planMensual}</td>
                <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{p.ultimoPago}</td>
                <td className="px-3 py-2 border-b border-gray-50 text-gray-600">{p.mesesAdeudados}</td>
                <td className="px-3 py-2 border-b border-gray-50">
                  <Badge variante={badgePorEstadoPago[p.estado]}>{p.estado}</Badge>
                </td>
                <td className="px-3 py-2 border-b border-gray-50">
                  <span
                    className={`cursor-pointer text-[12px] hover:underline ${
                      p.estado === 'Moroso' ? 'text-peligro font-bold' : 'text-blue-600'
                    }`}
                  >
                    {accionPago[p.estado]}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Panel>
    </>
  );
}

import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import type {
  MiVisita,
  MiClienteAsignado,
  VarianteBadge,
} from '../types';

const visitas: MiVisita[] = [
  { cliente: 'Minera Andes',       fecha: '21 Abr', hora: '09:00', direccion: 'Ruta 5 Norte Km 42',     estado: 'Programada' },
  { cliente: 'Constructora LM',    fecha: '21 Abr', hora: '14:30', direccion: 'Av. Industrial 1200',    estado: 'Programada' },
  { cliente: 'Agrícola Del Valle', fecha: '22 Abr', hora: '10:00', direccion: 'Parcela 7, San Felipe',  estado: 'Pendiente' },
  { cliente: 'Transporte Sur',     fecha: '19 Abr', hora: '11:00', direccion: 'Terminal de Carga B',    estado: 'Realizada' },
];

const clientes: MiClienteAsignado[] = [
  { razonSocial: 'Minera Andes',       rubro: 'Minería',      ultimaVisita: '08 Abr', estado: 'ACTIVO' },
  { razonSocial: 'Constructora LM',    rubro: 'Construcción', ultimaVisita: '12 Abr', estado: 'ACTIVO' },
  { razonSocial: 'Agrícola Del Valle', rubro: 'Agrícola',     ultimaVisita: '15 Mar', estado: 'MOROSO' },
  { razonSocial: 'Transporte Sur',     rubro: 'Transporte',   ultimaVisita: '19 Abr', estado: 'SUSPENDIDO' },
];

const badgePorEstadoVisita: Record<MiVisita['estado'], VarianteBadge> = {
  'Realizada':  'green',
  'Programada': 'blue',
  'Pendiente':  'yellow',
};

const badgePorEstadoCliente: Record<MiClienteAsignado['estado'], VarianteBadge> = {
  ACTIVO:     'green',
  MOROSO:     'yellow',
  SUSPENDIDO: 'red',
};

export default function DashboardProfesional() {
  const { email } = useAuth();

  return (
    <>
      <div className="page-title">Mi Panel</div>
      <div className="page-subtitle">{email} — Prevencionista de riesgos · Abril 2026</div>

      {/* KPI Row */}
      <div className="kpi-row">
        <KpiCard label="Visitas hoy"            value={2} sub="Próxima: 09:00"        variante="warn" />
        <KpiCard label="Visitas pendientes"     value={1} sub="Esta semana" />
        <KpiCard label="Mis clientes asignados" value={4} sub="1 moroso, 1 suspendido" />
        <KpiCard label="Mi estado"              value="Disponible" sub="Sin visita en curso" variante="ok" />
      </div>

      {/* Fila 1: Mis visitas + Estado en terreno */}
      <div className="grid-2">
        <Panel
          titulo="📅 Mis próximas visitas"
          accion={<button className="btn btn-sm btn-primary">+ Registrar visita</button>}
        >
          <table className="app-table">
            <thead>
              <tr>
                {['Cliente', 'Fecha', 'Hora', 'Dirección', 'Estado'].map(h => <th key={h}>{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {visitas.map((v, i) => (
                <tr key={i}>
                  <td>{v.cliente}</td>
                  <td>{v.fecha}</td>
                  <td>{v.hora}</td>
                  <td>{v.direccion}</td>
                  <td><Badge variante={badgePorEstadoVisita[v.estado]}>{v.estado}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </Panel>

        <Panel
          titulo="📍 Mi ubicación en terreno"
          accion={<button className="btn btn-sm btn-outline">Actualizar estado</button>}
        >
          <div className="relative h-[180px] bg-[#d9e8f5] flex flex-col items-center justify-center gap-1.5 overflow-hidden">
            <svg className="absolute inset-0 w-full h-full" viewBox="0 0 400 180" preserveAspectRatio="none">
              <line x1="0" y1="60"  x2="400" y2="60"  stroke="#a0bcd8" strokeWidth="1" />
              <line x1="0" y1="120" x2="400" y2="120" stroke="#a0bcd8" strokeWidth="1" />
              <line x1="200" y1="0" x2="200" y2="180" stroke="#a0bcd8" strokeWidth="1" />
            </svg>
            <div className="absolute w-3 h-3 rounded-full border-2 border-white shadow-md"
                 style={{ background: '#27ae60', top: 80, left: 190 }} />
            <span className="relative z-10 text-azul text-[13px] font-bold">🗺️ Próxima visita: Minera Andes</span>
            <span className="relative z-10 text-[11px] text-[#5a8cb5]">Ruta 5 Norte Km 42 · 09:00</span>
          </div>
          <div style={{ display: 'flex', gap: 14, padding: '10px 16px', borderTop: '1px solid #f3f4f6', fontSize: 11, color: '#4b5563' }}>
            <span>🟢 Estado actual: Disponible</span>
          </div>
        </Panel>
      </div>

      {/* Fila 2: Mis clientes (full width) */}
      <Panel
        titulo="👥 Mis clientes asignados"
        accion={<button className="btn btn-sm btn-outline">Ver todos</button>}
      >
        <table className="app-table">
          <thead>
            <tr>
              {['Cliente', 'Rubro', 'Última visita', 'Estado', 'Acción'].map(h => <th key={h}>{h}</th>)}
            </tr>
          </thead>
          <tbody>
            {clientes.map((c, i) => (
              <tr key={i}>
                <td>{c.razonSocial}</td>
                <td>{c.rubro}</td>
                <td>{c.ultimaVisita}</td>
                <td><Badge variante={badgePorEstadoCliente[c.estado]}>{c.estado}</Badge></td>
                <td><button className="btn btn-sm btn-outline">Ver ficha</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </Panel>
    </>
  );
}

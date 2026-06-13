import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import type {
  ProximaActividad,
  AccionImportante,
  VarianteBadge,
  VarianteAlerta,
} from '../types';

const acciones: AccionImportante[] = [
  { tipo: 'warn', titulo: 'Confirmación pendiente', texto: 'Capacitación "Manejo manual de cargas" programada para el 05/06/2026.' },
  { tipo: 'info', titulo: 'Reporte mensual disponible', texto: 'Mayo 2026 ya puede revisarse desde el módulo de reportes.' },
  { tipo: 'ok',   titulo: 'Pago registrado',           texto: 'Cuota de mayo pagada correctamente. El servicio se mantiene activo.' },
];

const actividades: ProximaActividad[] = [
  { fecha: '24/05/2026', actividad: 'Visita preventiva mensual',           profesional: 'Eduardo Pérez', estado: 'Programada' },
  { fecha: '05/06/2026', actividad: 'Capacitación: Manejo manual de cargas', profesional: 'Natalia Lavín', estado: 'Por confirmar' },
  { fecha: '12/06/2026', actividad: 'Revisión de matriz de riesgos',       profesional: 'Eduardo Pérez', estado: 'Pendiente' },
];

const resumen = [
  { label: 'Accidentes registrados en el mes', valor: 0 },
  { label: 'Días perdidos',                    valor: 0 },
  { label: 'Hallazgos abiertos',               valor: 3 },
  { label: 'Mejoras implementadas',            valor: 8 },
];

const claseAccion: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn:    'alert-item alert-item--warn',
  info:    'alert-item alert-item--info',
  ok:      'alert-item alert-item--ok',
};

const badgePorEstado: Record<ProximaActividad['estado'], VarianteBadge> = {
  'Programada':   'blue',
  'Por confirmar':'yellow',
  'Pendiente':    'gray',
};

export default function DashboardCliente() {
  const { email } = useAuth();

  return (
    <>
      <div className="page-title">
        Portal de Cliente <Badge variante="green">Servicio activo</Badge>
      </div>
      <div className="page-subtitle">
        {email} — Seguimiento de visitas, capacitaciones, reportes y pagos · Mayo 2026
      </div>

      {/* KPIs */}
      <div className="kpi-row">
        <KpiCard label="Visitas del mes"          value="2/2"    sub="Cumplimiento contractual mensual"  variante="ok"   progreso={100} />
        <KpiCard label="Capacitaciones pendientes" value={1}      sub="Requiere confirmar asistentes"     variante="warn" progreso={55} />
        <KpiCard label="Asesorías usadas"          value="7/10"   sub="3 disponibles dentro del plan"     variante="default" progreso={70} />
        <KpiCard label="Estado de pago"            value="Al día" sub="Próximo vencimiento: 30/05/2026"   variante="ok" />
      </div>

      {/* Fila 1: Acciones importantes + Accesos rápidos */}
      <div className="grid-2">
        <Panel titulo="🔔 Acciones importantes">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12 }}>
            {acciones.map((a, i) => (
              <div key={i} className={claseAccion[a.tipo]}>
                <div><b>{a.titulo}:</b> {a.texto}</div>
              </div>
            ))}
          </div>
        </Panel>

        <Panel titulo="⚡ Accesos rápidos">
          <div className="grid-2" style={{ padding: 14, gap: 10 }}>
            <button className="btn btn-primary">Solicitar asesoría</button>
            <button className="btn btn-primary">Reportar accidente</button>
            <button className="btn btn-outline">Subir documento</button>
            <button className="btn btn-outline">Actualizar contacto</button>
          </div>
        </Panel>
      </div>

      {/* Fila 2: Próximas actividades + Resumen preventivo */}
      <div className="grid-2">
        <Panel
          titulo="📅 Próximas actividades"
          accion={<button className="btn btn-sm btn-outline">Ver calendario</button>}
        >
          <table className="app-table">
            <thead>
              <tr>
                {['Fecha', 'Actividad', 'Profesional', 'Estado'].map(h => <th key={h}>{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {actividades.map((a, i) => (
                <tr key={i}>
                  <td>{a.fecha}</td>
                  <td>{a.actividad}</td>
                  <td>{a.profesional}</td>
                  <td><Badge variante={badgePorEstado[a.estado]}>{a.estado}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </Panel>

        <Panel
          titulo="📊 Resumen preventivo"
          accion={<button className="btn btn-sm btn-outline">Ver indicadores</button>}
        >
          <div style={{ padding: '14px 16px' }}>
            {resumen.map((r, i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '7px 0', fontSize: 12, color: '#5e6a78' }}>
                <span>{r.label}</span>
                <b style={{ color: '#243041' }}>{r.valor}</b>
              </div>
            ))}
            <div className="placeholder" style={{ minHeight: 110, marginTop: 12 }}>
              Tendencia de accidentabilidad<br />Feb 3,3% · Mar 3,1% · Abr 2,8% · May 2,5%
            </div>
          </div>
        </Panel>
      </div>
    </>
  );
}

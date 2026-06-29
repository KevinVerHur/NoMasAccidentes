import { useEffect, useState } from 'react';
import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import { obtenerDashboardCliente } from '../api/dashboard';
import type {
  DashboardClienteResponse,
  VarianteBadge,
  VarianteAlerta,
  VarianteKpi,
} from '../types';

const claseAccion: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn: 'alert-item alert-item--warn',
  info: 'alert-item alert-item--info',
  ok: 'alert-item alert-item--ok',
};

function badgeActividad(estado: string): VarianteBadge {
  if (estado === 'Programada') return 'blue';
  if (estado === 'Por confirmar') return 'yellow';
  return 'gray';
}

function varianteEstadoPago(estado: string): VarianteKpi {
  if (estado === 'Al día') return 'ok';
  if (estado === 'Suspendido') return 'peligro';
  return 'warn';
}

function servicioBadge(estado: string): { variante: VarianteBadge; texto: string } {
  if (estado === 'Suspendido') return { variante: 'red', texto: 'Servicio suspendido' };
  if (estado === 'Moroso' || estado === 'Atrasado') return { variante: 'yellow', texto: 'Pago pendiente' };
  return { variante: 'green', texto: 'Servicio activo' };
}

const fmtFecha = (iso: string | null) =>
  iso
    ? new Date(`${iso}T00:00:00`).toLocaleDateString('es-CL', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      })
    : '—';

const mesActual = new Date().toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });

export default function DashboardCliente() {
  const { email } = useAuth();
  const [datos, setDatos] = useState<DashboardClienteResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    obtenerDashboardCliente()
      .then(setDatos)
      .catch(() => setError('No se pudieron cargar los datos de tu panel.'));
  }, []);

  const kpis = datos?.kpis;
  const acciones = datos?.accionesImportantes ?? [];
  const actividades = datos?.proximasActividades ?? [];
  const resumen = datos?.resumen;
  const servicio = servicioBadge(kpis?.estadoPago ?? 'Al día');

  const resumenFilas = resumen
    ? [
        { label: 'Accidentes registrados en el mes', valor: resumen.accidentesMes },
        { label: 'Días perdidos en el mes', valor: resumen.diasPerdidosMes },
        { label: 'Accidentes acumulados del año', valor: resumen.accidentesAnio },
        { label: 'Capacitaciones realizadas en el año', valor: resumen.capacitacionesRealizadasAnio },
      ]
    : [];

  return (
    <>
      <div className="page-title">
        Portal de Cliente <Badge variante={servicio.variante}>{servicio.texto}</Badge>
      </div>
      <div className="page-subtitle" style={{ textTransform: 'capitalize' }}>
        {email} — Seguimiento de visitas, capacitaciones, reportes y pagos · {mesActual}
      </div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          <div>{error}</div>
        </div>
      )}

      {/* KPIs */}
      <div className="kpi-row">
        <KpiCard
          label="Visitas del mes"
          value={`${kpis?.visitasRealizadasMes ?? 0}/${kpis?.visitasProgramadasMes ?? 0}`}
          sub="Realizadas / programadas"
          variante={
            kpis && kpis.visitasProgramadasMes > 0 && kpis.visitasRealizadasMes >= kpis.visitasProgramadasMes
              ? 'ok'
              : 'warn'
          }
          progreso={
            kpis && kpis.visitasProgramadasMes > 0
              ? Math.min(100, (kpis.visitasRealizadasMes / kpis.visitasProgramadasMes) * 100)
              : 0
          }
        />
        <KpiCard
          label="Capacitaciones pendientes"
          value={kpis?.capacitacionesPendientes ?? 0}
          sub="Programadas por realizar"
          variante={(kpis?.capacitacionesPendientes ?? 0) > 0 ? 'warn' : 'ok'}
        />
        <KpiCard
          label="Asesorías usadas"
          value={`${kpis?.asesoriasUsadas ?? 0}/${kpis?.asesoriasLimite ?? 10}`}
          sub={`${Math.max(0, (kpis?.asesoriasLimite ?? 10) - (kpis?.asesoriasUsadas ?? 0))} disponibles dentro del plan`}
          variante="default"
          progreso={
            kpis && kpis.asesoriasLimite > 0
              ? Math.min(100, (kpis.asesoriasUsadas / kpis.asesoriasLimite) * 100)
              : 0
          }
        />
        <KpiCard
          label="Estado de pago"
          value={kpis?.estadoPago ?? '—'}
          sub={
            kpis?.proximoVencimiento
              ? `Próximo vencimiento: ${fmtFecha(kpis.proximoVencimiento)}`
              : 'Sin cuotas pendientes'
          }
          variante={varianteEstadoPago(kpis?.estadoPago ?? 'Al día')}
        />
      </div>

      {/* Fila 1: Acciones importantes + Accesos rápidos */}
      <div className="grid-2">
        <Panel titulo="🔔 Acciones importantes">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12 }}>
            {acciones.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                Sin acciones pendientes. 🎉
              </div>
            ) : (
              acciones.map((a, i) => (
                <div key={i} className={claseAccion[a.severidad]}>
                  <div>
                    <b>{a.titulo}:</b> {a.detalle}
                  </div>
                </div>
              ))
            )}
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
        <Panel titulo="📅 Próximas actividades">
          <table className="app-table">
            <thead>
              <tr>
                {['Fecha', 'Actividad', 'Profesional', 'Estado'].map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {actividades.length === 0 ? (
                <tr>
                  <td colSpan={4} style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                    Sin actividades próximas.
                  </td>
                </tr>
              ) : (
                actividades.map((a, i) => (
                  <tr key={i}>
                    <td>{fmtFecha(a.fecha)}</td>
                    <td>{a.actividad}</td>
                    <td>{a.profesional}</td>
                    <td>
                      <Badge variante={badgeActividad(a.estado)}>{a.estado}</Badge>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </Panel>

        <Panel titulo="📊 Resumen preventivo">
          <div style={{ padding: '14px 16px' }}>
            {resumenFilas.map((r, i) => (
              <div
                key={i}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  padding: '7px 0',
                  fontSize: 12,
                  color: '#5e6a78',
                }}
              >
                <span>{r.label}</span>
                <b style={{ color: '#243041' }}>{r.valor}</b>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </>
  );
}

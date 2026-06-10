import { useEffect, useState } from 'react';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type { PagoResponse, EstadoPago, VarianteBadge } from '../types';
import { misPagos } from '../api/pagos';

const badgePorEstadoPago: Record<EstadoPago, VarianteBadge> = {
  PENDIENTE: 'yellow',
  PAGADO:    'green',
  ATRASADO:  'red',
};

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

export default function MisPagos() {
  const [pagos, setPagos]       = useState<PagoResponse[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    misPagos().then(setPagos).catch(() => {}).finally(() => setCargando(false));
  }, []);

  const pagadas    = pagos.filter(p => p.estadoPago === 'PAGADO').length;
  const pendientes = pagos.filter(p => p.estadoPago === 'PENDIENTE').length;
  const atrasadas  = pagos.filter(p => p.estadoPago === 'ATRASADO').length;
  const adeudado   = pagos.filter(p => p.estadoPago !== 'PAGADO').reduce((s, p) => s + p.monto, 0);

  return (
    <>
      <div className="page-title">Mis pagos</div>
      <div className="page-subtitle">Estado de tus cuotas y pagos</div>

      <div className="kpi-row">
        <KpiCard label="Pagadas"    value={pagadas} variante="ok" />
        <KpiCard label="Pendientes" value={pendientes} variante="warn" />
        <KpiCard label="Atrasadas"  value={atrasadas} variante="peligro" />
        <KpiCard label="Adeudado"   value={clp(adeudado)} />
      </div>

      <Panel titulo="💳 Historial de cuotas">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : pagos.length === 0 ? (
          <div className="placeholder">No tienes cuotas registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cuota</th><th>Monto</th><th>Vencimiento</th><th>Fecha pago</th><th>Medio</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {pagos.map(p => (
                <tr key={p.id}>
                  <td>#{p.numeroCuota}</td>
                  <td>{clp(p.monto)}</td>
                  <td>{fmtFecha(p.fechaVencimiento)}</td>
                  <td>{fmtFecha(p.fechaPago)}</td>
                  <td>{p.medioPago ?? '—'}</td>
                  <td><Badge variante={badgePorEstadoPago[p.estadoPago]}>{p.estadoPago}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>
    </>
  );
}

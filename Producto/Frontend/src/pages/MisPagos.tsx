import { useEffect, useState, useCallback } from 'react';
import type { CSSProperties } from 'react';
import { useSearchParams } from 'react-router-dom';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type { PagoResponse, EstadoPago, VarianteBadge } from '../types';
import {
  misPagos, iniciarWebpay, abrirVentanaWebpay, redirigirAWebpay, descargarMiBoleta,
} from '../api/pagos';

const badgePorEstadoPago: Record<EstadoPago, VarianteBadge> = {
  PENDIENTE: 'yellow',
  PAGADO:    'green',
  ATRASADO:  'red',
};

type Estado = 'exito' | 'fallo' | 'cancelado';
const bannerPorEstado: Record<Estado, { texto: string; estilo: CSSProperties }> = {
  exito:     { texto: '✓ Pago realizado con éxito. Tu cuota quedó pagada.',
               estilo: { background: '#e6f4ea', color: '#1e8e3e', border: '1px solid #b7e1c4' } },
  fallo:     { texto: '✕ El pago no pudo completarse. La cuota sigue pendiente.',
               estilo: { background: '#fce8e6', color: '#c5221f', border: '1px solid #f3b4b0' } },
  cancelado: { texto: 'Cancelaste el pago. La cuota sigue pendiente.',
               estilo: { background: '#fef7e0', color: '#a56300', border: '1px solid #f5d98b' } },
};

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

export default function MisPagos() {
  const [pagos, setPagos]       = useState<PagoResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [procesando, setProcesando] = useState<number | null>(null);
  const [bannerEstado, setBannerEstado] = useState<Estado | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();

  const cargar = useCallback(() => {
    setCargando(true);
    misPagos().then(setPagos).catch(() => {}).finally(() => setCargando(false));
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  // Resultado del retorno de Webpay. Llega en la ventana emergente (?estado=...).
  // Si esta pestaña es el popup (tiene opener), avisa a la original y se cierra;
  // si no, muestra el resultado aquí mismo.
  const estadoParam = searchParams.get('estado') as Estado | null;
  useEffect(() => {
    if (!estadoParam) return;
    if (window.opener && !window.opener.closed) {
      window.opener.postMessage({ tipo: 'webpay-resultado', estado: estadoParam }, window.location.origin);
      window.close();
      return;
    }
    setBannerEstado(estadoParam);
    cargar();
    setSearchParams({}, { replace: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estadoParam]);

  // Ventana original: recibe el resultado del popup y refresca.
  useEffect(() => {
    const onMensaje = (e: MessageEvent) => {
      if (e.origin !== window.location.origin) return;
      if (e.data?.tipo === 'webpay-resultado') {
        setBannerEstado(e.data.estado as Estado);
        cargar();
      }
    };
    window.addEventListener('message', onMensaje);
    return () => window.removeEventListener('message', onMensaje);
  }, [cargar]);

  // Respaldo: si el popup no pudo avisar (opener perdido), al volver el foco a
  // esta pestaña se refresca el historial igual.
  useEffect(() => {
    const onFoco = () => cargar();
    window.addEventListener('focus', onFoco);
    return () => window.removeEventListener('focus', onFoco);
  }, [cargar]);

  async function onPagar(idPago: number) {
    // La ventana se abre de forma síncrona dentro del click para que el navegador
    // no la bloquee como popup (abrirla tras el await sí se bloquearía).
    const ventana = abrirVentanaWebpay();
    if (ventana) {
      ventana.document.write('<p style="font-family:sans-serif;padding:24px">Redirigiendo a Webpay…</p>');
    }
    setProcesando(idPago);
    try {
      const datos = await iniciarWebpay(idPago);
      redirigirAWebpay(datos);
    } catch {
      ventana?.close();
      alert('No se pudo iniciar el pago. Intenta nuevamente.');
    } finally {
      setProcesando(null);
    }
  }

  const banner = bannerEstado ? bannerPorEstado[bannerEstado] : null;

  const pagadas    = pagos.filter(p => p.estadoPago === 'PAGADO').length;
  const pendientes = pagos.filter(p => p.estadoPago === 'PENDIENTE').length;
  const atrasadas  = pagos.filter(p => p.estadoPago === 'ATRASADO').length;
  const adeudado   = pagos.filter(p => p.estadoPago !== 'PAGADO').reduce((s, p) => s + p.monto, 0);

  return (
    <>
      <div className="page-title">Mis pagos</div>
      <div className="page-subtitle">Estado de tus cuotas y pagos</div>

      {banner && (
        <div style={{ ...banner.estilo, marginBottom: 16, padding: '10px 14px', borderRadius: 8, fontSize: 14 }}>
          {banner.texto}
        </div>
      )}

      <div className="kpi-row">
        <KpiCard label="Pagadas"    value={pagadas} variante="ok" />
        <KpiCard label="Pendientes" value={pendientes} variante="warn" />
        <KpiCard label="Atrasadas"  value={atrasadas} variante="peligro" />
        <KpiCard label="Adeudado"   value={clp(adeudado)} />
      </div>

      <Panel titulo="Historial de cuotas">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : pagos.length === 0 ? (
          <div className="placeholder">No tienes cuotas registradas.</div>
        ) : (
          <>
            <div className="client-payments-mobile">
              {pagos.map(p => (
                <article className="client-payment-card" key={p.id}>
                  <div className="client-payment-card__header">
                    <div>
                      <span className="client-payment-card__eyebrow">Cuota</span>
                      <strong>#{p.numeroCuota}</strong>
                    </div>
                    <Badge variante={badgePorEstadoPago[p.estadoPago]}>{p.estadoPago}</Badge>
                  </div>

                  <div className="client-payment-card__amount">{clp(p.monto)}</div>

                  <div className="client-payment-card__grid">
                    <span>Vencimiento</span>
                    <strong>{fmtFecha(p.fechaVencimiento)}</strong>

                    <span>Fecha pago</span>
                    <strong>{fmtFecha(p.fechaPago)}</strong>

                    <span>Medio</span>
                    <strong>{p.medioPago ?? '-'}</strong>
                  </div>

                  {p.estadoPago === 'PAGADO' ? (
                    <button className="btn btn-outline client-payment-card__button" onClick={() => descargarMiBoleta(p.id)}>
                      Comprobante
                    </button>
                  ) : (
                    <button
                      className="btn btn-primary client-payment-card__button"
                      disabled={procesando === p.id}
                      onClick={() => onPagar(p.id)}
                    >
                      {procesando === p.id ? 'Redirigiendo...' : 'Pagar'}
                    </button>
                  )}
                </article>
              ))}
            </div>

            <table className="app-table client-payments-table">
            <thead>
              <tr>
                <th>Cuota</th><th>Monto</th><th>Vencimiento</th><th>Fecha pago</th><th>Medio</th><th>Estado</th><th>Acción</th>
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
                  <td>
                    {p.estadoPago === 'PAGADO' ? (
                      <button className="btn btn-sm btn-outline" onClick={() => descargarMiBoleta(p.id)}>
                        📄 Comprobante
                      </button>
                    ) : (
                      <button
                        className="btn btn-sm btn-primary"
                        disabled={procesando === p.id}
                        onClick={() => onPagar(p.id)}
                      >
                        {procesando === p.id ? 'Redirigiendo…' : 'Pagar'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
            </table>
          </>
        )}
      </Panel>
    </>
  );
}

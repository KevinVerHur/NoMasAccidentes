import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type {
  EmpresaResponse,
  MensualidadResponse,
  PlanPagoResponse,
  PagoResponse,
  RegistrarPagoRequest,
  CobroExtraResponse,
  EstadoPago,
  VarianteBadge,
} from '../types';
import { listarClientes } from '../api/clientes';
import {
  listarMensualidades,
  listarPlanesPorCliente,
  historialPagos,
  registrarPago,
  evaluarMorosidad,
  suspenderMorosos,
  listarCobrosExtra,
} from '../api/pagos';

const badgePorEstadoPago: Record<EstadoPago, VarianteBadge> = {
  PENDIENTE: 'yellow',
  PAGADO: 'green',
  ATRASADO: 'red',
};

const MEDIOS_PAGO = ['Transferencia', 'Efectivo', 'Tarjeta', 'Webpay'];

const clp = (n: number) =>
  n.toLocaleString('es-CL', {
    style: 'currency',
    currency: 'CLP',
    maximumFractionDigits: 0,
  });

const fmtFecha = (iso: string | null) =>
  iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

const mensajeError = (e: unknown, fallback: string) =>
  (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data?.mensaje ??
  (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data?.message ??
  fallback;

export default function Pagos() {
  const [clientes, setClientes] = useState<EmpresaResponse[]>([]);
  const [mensualidades, setMensualidades] = useState<MensualidadResponse[]>([]);
  const [idEmpresa, setIdEmpresa] = useState<number | null>(null);
  const [planes, setPlanes] = useState<PlanPagoResponse[]>([]);
  const [pagos, setPagos] = useState<PagoResponse[]>([]);
  const [cobrosPorPago, setCobrosPorPago] = useState<Record<number, CobroExtraResponse[]>>({});
  const [cargandoCliente, setCargandoCliente] = useState(false);
  const [modalPago, setModalPago] = useState<PagoResponse | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const formPago = useForm<RegistrarPagoRequest>();

  const planBasico =
    mensualidades.find(m => m.nombrePlan === 'PLAN_BASICO') ?? mensualidades[0];

  useEffect(() => {
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
    listarMensualidades().then(setMensualidades).catch(() => {});
  }, []);

  const cargarCliente = useCallback(async (id: number) => {
    setCargandoCliente(true);
    try {
      const [pls, pgs] = await Promise.all([
        listarPlanesPorCliente(id),
        historialPagos(id),
      ]);

      setPlanes(pls);
      setPagos(pgs);

      const entradas = await Promise.all(
        pgs.map(async pago => {
          const cobros = await listarCobrosExtra(pago.id).catch(() => []);
          return [pago.id, cobros] as const;
        })
      );

      const mapa: Record<number, CobroExtraResponse[]> = {};
      entradas.forEach(([idPago, cobros]) => {
        mapa[idPago] = cobros;
      });
      setCobrosPorPago(mapa);
    } finally {
      setCargandoCliente(false);
    }
  }, []);

  useEffect(() => {
    if (idEmpresa != null) cargarCliente(idEmpresa);
  }, [idEmpresa, cargarCliente]);

  const extrasPago = (idPago: number) => cobrosPorPago[idPago] ?? [];

  const totalExtras = (idPago: number) =>
    extrasPago(idPago).reduce((total, cobro) => total + cobro.monto, 0);

  const totalPago = (pago: PagoResponse) =>
    pago.monto + totalExtras(pago.id);

  const esCuotaExigible = (pago: PagoResponse) => {
    if (pago.estadoPago === 'ATRASADO') return true;
    if (pago.estadoPago !== 'PENDIENTE') return false;

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    const vencimiento = new Date(pago.fechaVencimiento);
    vencimiento.setHours(0, 0, 0, 0);

    return vencimiento <= hoy;
  };

  const pagadas = pagos.filter(p => p.estadoPago === 'PAGADO').length;
  const pendientes = pagos.filter(p => p.estadoPago === 'PENDIENTE').length;
  const atrasadas = pagos.filter(p => p.estadoPago === 'ATRASADO').length;

  const totalAdeudado = pagos
    .filter(esCuotaExigible)
    .reduce((total, pago) => total + totalPago(pago), 0);

  async function onRegistrarPago(data: RegistrarPagoRequest) {
    if (!modalPago || idEmpresa == null) return;

    setError(null);
    setGuardando(true);

    try {
      await registrarPago(modalPago.id, data);
      setModalPago(null);
      formPago.reset();
      await cargarCliente(idEmpresa);
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al registrar el pago.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onEvaluarMorosidad() {
    setAviso(null);
    const r = await evaluarMorosidad();
    setAviso(`Morosidad evaluada: ${r.cuotasMarcadas} cuota(s) marcada(s) como atrasada(s).`);
    if (idEmpresa != null) await cargarCliente(idEmpresa);
  }

  async function onSuspenderMorosos() {
    setAviso(null);
    const r = await suspenderMorosos();
    setAviso(`${r.empresasSuspendidas} cliente(s) suspendido(s) por morosidad.`);
  }

  return (
    <>
      <div className="page-title">Pagos</div>
      <div className="page-subtitle">Plan básico, suscripción mensual y control de morosidad</div>

      <div className="kpi-row">
        <KpiCard label="Pagadas" value={pagadas} variante="ok" />
        <KpiCard label="Pendientes" value={pendientes} variante="warn" />
        <KpiCard label="Atrasadas" value={atrasadas} variante="peligro" />
        <KpiCard label="Adeudado" value={clp(totalAdeudado)} />
      </div>

      {aviso && (
        <div className="alert-item alert-item--info" style={{ marginBottom: 12 }}>
          {aviso}
        </div>
      )}

      <Panel
        titulo="Acciones de cobranza"
        accion={
          <div className="btn-group">
            <button className="btn btn-sm btn-warn" onClick={onEvaluarMorosidad}>
              Evaluar morosidad
            </button>
            <button className="btn btn-sm btn-danger" onClick={onSuspenderMorosos}>
              Suspender morosos
            </button>
          </div>
        }
      >
        <div style={{ fontSize: 13, color: '#6b7280' }}>
          “Evaluar morosidad” marca como atrasadas las cuotas vencidas e impagas.
          “Suspender morosos” suspende a quienes acumulan 2 o más cuotas atrasadas.
        </div>
      </Panel>

      <div className="grid-2">
        <Panel titulo="Tarifas del plan básico">
          {!planBasico ? (
            <div className="placeholder">Cargando tarifas...</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Concepto</th>
                  <th>Incluido</th>
                  <th>Precio adicional</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Mensualidad plan básico</td>
                  <td>Servicio mensual</td>
                  <td>{clp(planBasico.montoBase)}</td>
                </tr>
                <tr>
                  <td>Visitas preventivas</td>
                  <td>{planBasico.visitasIncluidas ?? 0} al mes</td>
                  <td>{clp(planBasico.costoVisitaExtra ?? 0)} por visita extra</td>
                </tr>
                <tr>
                  <td>Asesorías</td>
                  <td>{planBasico.asesoriasIncluidas ?? 0} al mes</td>
                  <td>{clp(planBasico.costoAsesoriaExtra ?? 0)} por asesoría extra</td>
                </tr>
                <tr>
                  <td>Capacitaciones</td>
                  <td>
                    {planBasico.capacitacionesIncluidas == null
                      ? 'Sin límite'
                      : `${planBasico.capacitacionesIncluidas} al mes`}
                  </td>
                  <td>
                    {planBasico.costoCapacitacionExtra
                      ? `${clp(planBasico.costoCapacitacionExtra)} por capacitación extra`
                      : 'No aplica'}
                  </td>
                </tr>
                <tr>
                  <td>Llamados</td>
                  <td>Incluidos entre 09:00 y 18:00</td>
                  <td>
                    {clp(planBasico.costoLlamadoFueraHorario ?? 0)} antes de 09:00 o después de 18:00
                  </td>
                </tr>
              </tbody>
            </table>
          )}
        </Panel>

        <Panel titulo="Cliente">
          <select
            className="auth-input"
            value={idEmpresa ?? ''}
            onChange={e => setIdEmpresa(e.target.value ? Number(e.target.value) : null)}
            style={{ marginBottom: 12 }}
          >
            <option value="">Seleccionar cliente...</option>
            {clientes.map(cliente => (
              <option key={cliente.id} value={cliente.id}>
                {cliente.razonSocial}
              </option>
            ))}
          </select>

          {idEmpresa == null ? (
            <div className="placeholder">Selecciona un cliente para ver su suscripción y pagos.</div>
          ) : planes.length === 0 ? (
            <div className="placeholder">El cliente no tiene suscripción activa registrada.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Plan</th>
                  <th>Inicio</th>
                  <th>Estado</th>
                  <th>Periodicidad</th>
                </tr>
              </thead>
              <tbody>
                {planes.map(plan => (
                  <tr key={plan.id}>
                    <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{plan.nombrePlan}</td>
                    <td>{fmtFecha(plan.fechaInicio)}</td>
                    <td>
                      <Badge variante={plan.activo ? 'green' : 'gray'}>
                        {plan.activo ? 'Activo' : 'Terminado'}
                      </Badge>
                    </td>
                    <td>{plan.periodicidad}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      </div>

      {idEmpresa != null && (
        <Panel titulo="Historial de cuotas">
          {cargandoCliente ? (
            <div className="placeholder">Cargando...</div>
          ) : pagos.length === 0 ? (
            <div className="placeholder">Sin cuotas registradas para este cliente.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Cuota</th>
                  <th>Base</th>
                  <th>Extras</th>
                  <th>Total</th>
                  <th>Vencimiento</th>
                  <th>Fecha pago</th>
                  <th>Medio</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {pagos.map(pago => (
                  <tr key={pago.id}>
                    <td>#{pago.numeroCuota}</td>
                    <td>{clp(pago.monto)}</td>
                    <td>
                      {totalExtras(pago.id) > 0 ? (
                        <div>
                          <strong>{clp(totalExtras(pago.id))}</strong>
                          <div style={{ fontSize: 12, color: '#6b7280' }}>
                            {extrasPago(pago.id).map(c => c.descripcion ?? c.tipoCobro).join(', ')}
                          </div>
                        </div>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td>{clp(totalPago(pago))}</td>
                    <td>{fmtFecha(pago.fechaVencimiento)}</td>
                    <td>{fmtFecha(pago.fechaPago)}</td>
                    <td>{pago.medioPago ?? '—'}</td>
                    <td>
                      <Badge variante={badgePorEstadoPago[pago.estadoPago]}>
                        {pago.estadoPago}
                      </Badge>
                    </td>
                    <td>
                      {pago.estadoPago !== 'PAGADO' && (
                        <button
                          className="btn btn-sm btn-success"
                          onClick={() => {
                            formPago.reset();
                            setError(null);
                            setModalPago(pago);
                          }}
                        >
                          Registrar pago
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      <Modal
        abierto={!!modalPago}
        titulo="Registrar pago de cuota"
        ancho="sm"
        onCerrar={() => setModalPago(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPago(null)}>
              Cancelar
            </button>
            <button
              className="btn btn-success"
              form="form-pago"
              type="submit"
              disabled={guardando}
            >
              {guardando ? 'Registrando...' : 'Confirmar pago'}
            </button>
          </>
        }
      >
        <form id="form-pago" onSubmit={formPago.handleSubmit(onRegistrarPago)} noValidate>
          <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 12 }}>
            Cuota #{modalPago?.numeroCuota} · {modalPago && clp(totalPago(modalPago))} · vence{' '}
            {fmtFecha(modalPago?.fechaVencimiento ?? null)}
          </div>

          <label className="auth-label">Medio de pago</label>
          <select className="auth-input" {...formPago.register('medioPago')}>
            <option value="">Seleccionar...</option>
            {MEDIOS_PAGO.map(medio => (
              <option key={medio} value={medio}>
                {medio}
              </option>
            ))}
          </select>

          {error && (
            <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>
              {error}
            </div>
          )}
        </form>
      </Modal>
    </>
  );
}
import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type {
  ClienteResponse, MensualidadResponse, CrearMensualidadRequest,
  PlanPagoResponse, CrearPlanPagoRequest, PagoResponse, RegistrarPagoRequest,
  EstadoPago, VarianteBadge,
} from '../types';
import { listarClientes } from '../api/clientes';
import {
  listarMensualidades, crearMensualidad, listarPlanesPorCliente, crearPlanPago,
  historialPagos, registrarPago, evaluarMorosidad, suspenderMorosos,
} from '../api/pagos';

const badgePorEstadoPago: Record<EstadoPago, VarianteBadge> = {
  PENDIENTE: 'yellow',
  PAGADO:    'green',
  ATRASADO:  'red',
};

const MEDIOS_PAGO = ['Transferencia', 'Efectivo', 'Tarjeta', 'Webpay'];
const PERIODICIDADES = ['MENSUAL', 'TRIMESTRAL', 'ANUAL'] as const;

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';
const mensajeError = (e: unknown, fallback: string) =>
  (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;

export default function Pagos() {
  const [clientes, setClientes]       = useState<ClienteResponse[]>([]);
  const [mensualidades, setMensualidades] = useState<MensualidadResponse[]>([]);
  const [idCliente, setIdCliente]     = useState<number | null>(null);
  const [planes, setPlanes]           = useState<PlanPagoResponse[]>([]);
  const [pagos, setPagos]             = useState<PagoResponse[]>([]);
  const [cargandoCliente, setCargandoCliente] = useState(false);
  const [modalPlan, setModalPlan]     = useState(false);
  const [modalMensualidad, setModalMensualidad] = useState(false);
  const [modalPago, setModalPago]     = useState<PagoResponse | null>(null);
  const [guardando, setGuardando]     = useState(false);
  const [error, setError]             = useState<string | null>(null);
  const [aviso, setAviso]             = useState<string | null>(null);

  const formPlan        = useForm<CrearPlanPagoRequest>();
  const formMensualidad = useForm<CrearMensualidadRequest>();
  const formPago        = useForm<RegistrarPagoRequest>();

  useEffect(() => {
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
    listarMensualidades().then(setMensualidades).catch(() => {});
  }, []);

  const cargarCliente = useCallback(async (id: number) => {
    setCargandoCliente(true);
    try {
      const [pls, pgs] = await Promise.all([listarPlanesPorCliente(id), historialPagos(id)]);
      setPlanes(pls);
      setPagos(pgs);
    } finally {
      setCargandoCliente(false);
    }
  }, []);

  useEffect(() => { if (idCliente != null) cargarCliente(idCliente); }, [idCliente, cargarCliente]);

  const pagadas   = pagos.filter(p => p.estadoPago === 'PAGADO').length;
  const pendientes = pagos.filter(p => p.estadoPago === 'PENDIENTE').length;
  const atrasadas = pagos.filter(p => p.estadoPago === 'ATRASADO').length;
  const totalAdeudado = pagos.filter(p => p.estadoPago !== 'PAGADO').reduce((s, p) => s + p.monto, 0);

  async function onCrearPlan(data: CrearPlanPagoRequest) {
    if (idCliente == null) return;
    setError(null);
    setGuardando(true);
    try {
      await crearPlanPago({ ...data, idCliente });
      setModalPlan(false);
      formPlan.reset();
      await cargarCliente(idCliente);
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al asignar el plan.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onCrearMensualidad(data: CrearMensualidadRequest) {
    setError(null);
    setGuardando(true);
    try {
      await crearMensualidad(data);
      setModalMensualidad(false);
      formMensualidad.reset();
      setMensualidades(await listarMensualidades());
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al crear el plan.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onRegistrarPago(data: RegistrarPagoRequest) {
    if (!modalPago || idCliente == null) return;
    setError(null);
    setGuardando(true);
    try {
      await registrarPago(modalPago.id, data);
      setModalPago(null);
      formPago.reset();
      await cargarCliente(idCliente);
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
    if (idCliente != null) await cargarCliente(idCliente);
  }

  async function onSuspenderMorosos() {
    setAviso(null);
    const r = await suspenderMorosos();
    setAviso(`${r.clientesSuspendidos} cliente(s) suspendido(s) por morosidad.`);
  }

  return (
    <>
      <div className="page-title">Pagos</div>
      <div className="page-subtitle">Planes, cuotas y control de morosidad</div>

      <div className="kpi-row">
        <KpiCard label="Pagadas"     value={pagadas} variante="ok" />
        <KpiCard label="Pendientes"  value={pendientes} variante="warn" />
        <KpiCard label="Atrasadas"   value={atrasadas} variante="peligro" />
        <KpiCard label="Adeudado"    value={clp(totalAdeudado)} />
      </div>

      {aviso && <div className="alert-item alert-item--info" style={{ marginBottom: 12 }}>{aviso}</div>}

      <Panel
        titulo="⚙️ Acciones de cobranza"
        accion={
          <div className="btn-group">
            <button className="btn btn-sm btn-warn" onClick={onEvaluarMorosidad}>Evaluar morosidad</button>
            <button className="btn btn-sm btn-danger" onClick={onSuspenderMorosos}>Suspender morosos</button>
          </div>
        }
      >
        <div style={{ fontSize: 13, color: '#6b7280' }}>
          "Evaluar morosidad" marca como atrasadas las cuotas vencidas e impagas y deja morosos a los clientes afectados (RF11).
          "Suspender morosos" suspende a quienes acumulan 2 o más cuotas atrasadas (RF12).
        </div>
      </Panel>

      <div className="grid-2">
        {/* Catálogo de planes */}
        <Panel
          titulo="📦 Catálogo de planes"
          accion={<button className="btn btn-sm btn-primary" onClick={() => { formMensualidad.reset(); setError(null); setModalMensualidad(true); }}>+ Nuevo plan</button>}
        >
          {mensualidades.length === 0 ? (
            <div className="placeholder">No hay planes en el catálogo.</div>
          ) : (
            <table className="app-table">
              <thead><tr><th>Plan</th><th>Monto base</th><th>Visitas</th></tr></thead>
              <tbody>
                {mensualidades.map(m => (
                  <tr key={m.id}>
                    <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{m.nombrePlan}</td>
                    <td>{clp(m.montoBase)}</td>
                    <td>{m.visitasIncluidas ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>

        {/* Selector de cliente + planes asignados */}
        <Panel
          titulo="🏢 Cliente"
          accion={idCliente != null
            ? <button className="btn btn-sm btn-primary" onClick={() => { formPlan.reset(); setError(null); setModalPlan(true); }}>+ Asignar plan</button>
            : undefined}
        >
          <select className="auth-input" value={idCliente ?? ''} onChange={e => setIdCliente(e.target.value ? Number(e.target.value) : null)} style={{ marginBottom: 12 }}>
            <option value="">Seleccionar cliente...</option>
            {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
          </select>
          {idCliente == null ? (
            <div className="placeholder">Selecciona un cliente para ver sus planes y pagos.</div>
          ) : planes.length === 0 ? (
            <div className="placeholder">El cliente no tiene planes asignados.</div>
          ) : (
            <table className="app-table">
              <thead><tr><th>Plan</th><th>Inicio</th><th>Cuotas</th><th>Periodicidad</th></tr></thead>
              <tbody>
                {planes.map(p => (
                  <tr key={p.id}>
                    <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{p.nombrePlan}</td>
                    <td>{fmtFecha(p.fechaInicio)}</td>
                    <td>{p.cuotasTotales ?? '—'}</td>
                    <td>{p.periodicidad}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      </div>

      {/* Historial de cuotas */}
      {idCliente != null && (
        <Panel titulo="💳 Historial de cuotas">
          {cargandoCliente ? (
            <div className="placeholder">Cargando...</div>
          ) : pagos.length === 0 ? (
            <div className="placeholder">Sin cuotas registradas para este cliente.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Cuota</th><th>Monto</th><th>Vencimiento</th><th>Fecha pago</th><th>Medio</th><th>Estado</th><th>Acciones</th>
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
                      {p.estadoPago !== 'PAGADO' && (
                        <button className="btn btn-sm btn-success" onClick={() => { formPago.reset(); setError(null); setModalPago(p); }}>Registrar pago</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      {/* Modal nuevo plan (catálogo) */}
      <Modal
        abierto={modalMensualidad}
        titulo="Nuevo plan (catálogo)"
        onCerrar={() => setModalMensualidad(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalMensualidad(false)}>Cancelar</button>
            <button className="btn btn-primary" form="form-mensualidad" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Guardar plan'}</button>
          </>
        }
      >
        <form id="form-mensualidad" onSubmit={formMensualidad.handleSubmit(onCrearMensualidad)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Nombre del plan *</label>
              <input className={`auth-input ${formMensualidad.formState.errors.nombrePlan ? 'auth-input--error' : ''}`}
                placeholder="BASICO, PRO, PREMIUM..."
                {...formMensualidad.register('nombrePlan', { required: 'Obligatorio' })} />
              {formMensualidad.formState.errors.nombrePlan && <span className="auth-field-error">{formMensualidad.formState.errors.nombrePlan.message}</span>}
            </div>
            <div>
              <label className="auth-label">Monto base *</label>
              <input type="number" step="1" className={`auth-input ${formMensualidad.formState.errors.montoBase ? 'auth-input--error' : ''}`}
                {...formMensualidad.register('montoBase', { required: 'Obligatorio', valueAsNumber: true, min: { value: 1, message: 'Debe ser mayor a 0' } })} />
              {formMensualidad.formState.errors.montoBase && <span className="auth-field-error">{formMensualidad.formState.errors.montoBase.message}</span>}
            </div>
            <div>
              <label className="auth-label">Visitas incluidas</label>
              <input type="number" className="auth-input" {...formMensualidad.register('visitasIncluidas', { valueAsNumber: true })} />
            </div>
            <div>
              <label className="auth-label">Costo visita extra</label>
              <input type="number" className="auth-input" {...formMensualidad.register('costoVisitaExtra', { valueAsNumber: true })} />
            </div>
            <div>
              <label className="auth-label">Costo asesoría extra</label>
              <input type="number" className="auth-input" {...formMensualidad.register('costoAsesoriaExtra', { valueAsNumber: true })} />
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal asignar plan al cliente */}
      <Modal
        abierto={modalPlan}
        titulo="Asignar plan al cliente"
        onCerrar={() => setModalPlan(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPlan(false)}>Cancelar</button>
            <button className="btn btn-primary" form="form-plan" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Asignar y generar cuotas'}</button>
          </>
        }
      >
        <form id="form-plan" onSubmit={formPlan.handleSubmit(onCrearPlan)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Plan *</label>
              <select className={`auth-input ${formPlan.formState.errors.idMensualidad ? 'auth-input--error' : ''}`}
                {...formPlan.register('idMensualidad', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {mensualidades.map(m => <option key={m.id} value={m.id}>{m.nombrePlan} — {clp(m.montoBase)}</option>)}
              </select>
              {formPlan.formState.errors.idMensualidad && <span className="auth-field-error">{formPlan.formState.errors.idMensualidad.message}</span>}
            </div>
            <div>
              <label className="auth-label">Fecha de inicio *</label>
              <input type="date" className={`auth-input ${formPlan.formState.errors.fechaInicio ? 'auth-input--error' : ''}`}
                {...formPlan.register('fechaInicio', { required: 'Obligatorio' })} />
              {formPlan.formState.errors.fechaInicio && <span className="auth-field-error">{formPlan.formState.errors.fechaInicio.message}</span>}
            </div>
            <div>
              <label className="auth-label">N° de cuotas *</label>
              <input type="number" className={`auth-input ${formPlan.formState.errors.cuotasTotales ? 'auth-input--error' : ''}`}
                {...formPlan.register('cuotasTotales', { required: 'Obligatorio', valueAsNumber: true, min: { value: 1, message: 'Mínimo 1' } })} />
              {formPlan.formState.errors.cuotasTotales && <span className="auth-field-error">{formPlan.formState.errors.cuotasTotales.message}</span>}
            </div>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Periodicidad</label>
              <select className="auth-input" {...formPlan.register('periodicidad')}>
                {PERIODICIDADES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal registrar pago */}
      <Modal
        abierto={!!modalPago}
        titulo="Registrar pago de cuota"
        ancho="sm"
        onCerrar={() => setModalPago(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPago(null)}>Cancelar</button>
            <button className="btn btn-success" form="form-pago" type="submit" disabled={guardando}>{guardando ? 'Registrando...' : 'Confirmar pago'}</button>
          </>
        }
      >
        <form id="form-pago" onSubmit={formPago.handleSubmit(onRegistrarPago)} noValidate>
          <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 12 }}>
            Cuota #{modalPago?.numeroCuota} · {modalPago && clp(modalPago.monto)} · vence {fmtFecha(modalPago?.fechaVencimiento ?? null)}
          </div>
          <label className="auth-label">Medio de pago</label>
          <select className="auth-input" {...formPago.register('medioPago')}>
            <option value="">Seleccionar...</option>
            {MEDIOS_PAGO.map(m => <option key={m} value={m}>{m}</option>)}
          </select>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>
    </>
  );
}

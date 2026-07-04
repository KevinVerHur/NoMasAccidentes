import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type {
  MorosidadDetalle,
  MorosidadItem,
  MorosidadNotificacionRequest,
  MorosidadPagoRequest,
  RiesgoMorosidad,
  VarianteBadge,
} from '../types';
import {
  listarMorosidades,
  notificarDeudaMorosidad,
  obtenerDetalleMorosidad,
  registrarPagoMorosidad,
  suspenderClienteMorosidad,
} from '../api/morosidad';

type ModalPagoState = {
  morosidad: MorosidadItem;
  idPago: number;
  monto: number;
};

const MEDIOS_PAGO = ['Transferencia', 'Efectivo', 'Tarjeta', 'Webpay'];

const badgeRiesgo: Record<RiesgoMorosidad, VarianteBadge> = {
  CRITICO: 'red',
  ALERTA: 'yellow',
  OBSERVACION: 'blue',
};

const textoRiesgo: Record<RiesgoMorosidad, string> = {
  CRITICO: 'Crítico',
  ALERTA: 'Alerta',
  OBSERVACION: 'Observación',
};

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fechaHoy = () => new Date().toISOString().slice(0, 10);
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : 'Sin registro';
const mensajeError = (e: unknown, fallback: string) =>
  (e as { response?: { data?: { mensaje?: string } }; message?: string })?.response?.data?.mensaje ??
  (e as { message?: string })?.message ??
  fallback;

export default function Morosidad() {
  const [morosidades, setMorosidades] = useState<MorosidadItem[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [filtro, setFiltro] = useState('');
  const [riesgo, setRiesgo] = useState<RiesgoMorosidad | ''>('');
  const [notificacionesSesion, setNotificacionesSesion] = useState(0);
  const [modalPago, setModalPago] = useState<ModalPagoState | null>(null);
  const [modalSuspension, setModalSuspension] = useState<MorosidadItem | null>(null);
  const [modalNotificacion, setModalNotificacion] = useState<MorosidadItem | null>(null);
  const [detalle, setDetalle] = useState<MorosidadDetalle | null>(null);

  const formPago = useForm<MorosidadPagoRequest>();
  const formNotificacion = useForm<MorosidadNotificacionRequest>();

  async function cargarMorosidades() {
    setCargando(true);
    setError(null);
    try {
      setMorosidades(await listarMorosidades());
    } catch (e: unknown) {
      setError(mensajeError(e, 'No fue posible cargar la información de morosidades.'));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargarMorosidades();
  }, []);

  const resumen = useMemo(() => ({
    clientesMora: morosidades.filter((item) => item.mesesDeuda > 0 || item.estadoCliente === 'MOROSO').length,
    montoTotalAdeudado: morosidades.reduce((total, item) => total + item.montoAdeudado, 0),
    serviciosSuspendidos: morosidades.filter((item) => item.suspendido).length,
    notificacionesEnviadas: notificacionesSesion,
  }), [morosidades, notificacionesSesion]);

  const morosidadesFiltradas = useMemo(() => {
    const texto = filtro.trim().toLowerCase();

    return morosidades.filter((item) => {
      const coincideTexto = !texto || item.cliente.toLowerCase().includes(texto) || item.email.toLowerCase().includes(texto);
      const coincideRiesgo = !riesgo || item.riesgo === riesgo;
      return coincideTexto && coincideRiesgo;
    });
  }, [morosidades, filtro, riesgo]);

  function abrirPago(item: MorosidadItem) {
    const cuota = item.cuotasAdeudadas[0];
    if (!cuota) {
      setError('El cliente no tiene cuotas pendientes disponibles para registrar pago.');
      return;
    }

    setError(null);
    setModalPago({ morosidad: item, idPago: cuota.id, monto: cuota.monto });
    formPago.reset({
      idPago: cuota.id,
      monto: cuota.monto,
      fecha: fechaHoy(),
      metodo: '',
    });
  }

  async function abrirDetalle(item: MorosidadItem) {
    setError(null);
    try {
      setDetalle(await obtenerDetalleMorosidad(item.idCliente));
    } catch (e: unknown) {
      setError(mensajeError(e, 'No fue posible cargar el detalle de morosidad.'));
    }
  }

  function abrirNotificacion(item: MorosidadItem) {
    setError(null);
    setModalNotificacion(item);
    formNotificacion.reset({
      idCliente: item.idCliente,
      destinatario: item.email,
      asunto: `Regularización de deuda - ${item.cliente}`,
      mensaje: `Estimado cliente, registramos una deuda pendiente por ${clp(item.montoAdeudado)}. Solicitamos regularizar el pago para mantener el servicio activo.`,
    });
  }

  async function onRegistrarPago(data: MorosidadPagoRequest) {
    if (!modalPago) return;
    setGuardando(true);
    setError(null);
    try {
      await registrarPagoMorosidad({ ...data, idPago: modalPago.idPago, monto: modalPago.monto });
      setModalPago(null);
      formPago.reset();
      setAviso('Pago registrado correctamente. La información fue actualizada.');
      await cargarMorosidades();
    } catch (e: unknown) {
      setError(mensajeError(e, 'No fue posible registrar el pago.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onSuspender() {
    if (!modalSuspension) return;
    setGuardando(true);
    setError(null);
    try {
      await suspenderClienteMorosidad(modalSuspension.idCliente);
      setModalSuspension(null);
      setAviso('Servicio suspendido correctamente.');
      await cargarMorosidades();
    } catch (e: unknown) {
      setError(mensajeError(e, 'No fue posible suspender el servicio.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onEnviarNotificacion(data: MorosidadNotificacionRequest) {
    setGuardando(true);
    setError(null);
    try {
      await notificarDeudaMorosidad(data);
    } catch {
      setNotificacionesSesion((total) => total + 1);
      setModalNotificacion(null);
      formNotificacion.reset();
      setAviso('Notificación preparada en frontend. Pendiente integración con backend para envío y persistencia.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Morosidades</div>
      <div className="page-subtitle">Seguimiento de clientes atrasados y gestión de cobranza</div>

      <div className="kpi-row">
        <KpiCard label="Clientes mora" value={resumen.clientesMora} variante="warn" />
        <KpiCard label="Monto total adeudado" value={clp(resumen.montoTotalAdeudado)} variante="peligro" />
        <KpiCard label="Servicios suspendidos" value={resumen.serviciosSuspendidos} variante="default" />
        <KpiCard label="Notificaciones enviadas" value={resumen.notificacionesEnviadas} variante="ok" sub="Sesión actual" />
      </div>

      {aviso && <div className="alert-item alert-item--ok" style={{ marginBottom: 12 }}>{aviso}</div>}
      {error && <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>{error}</div>}

      <Panel
        titulo="Clientes con deuda"
        accion={<button className="btn btn-sm btn-outline" onClick={cargarMorosidades} disabled={cargando}>Actualizar</button>}
      >
        <div className="searchbar">
          <input
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            placeholder="Filtrar clientes morosos..."
          />
          <select value={riesgo} onChange={(e) => setRiesgo(e.target.value as RiesgoMorosidad | '')}>
            <option value="">Todos los riesgos</option>
            <option value="CRITICO">Crítico</option>
            <option value="ALERTA">Alerta</option>
            <option value="OBSERVACION">Observación</option>
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando morosidades...</div>
        ) : morosidadesFiltradas.length === 0 ? (
          <div className="placeholder">No hay clientes morosos con los filtros seleccionados.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Meses deuda</th>
                <th>Monto</th>
                <th>Riesgo</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {morosidadesFiltradas.map((item) => (
                <tr key={item.idCliente}>
                  <td>
                    <div style={{ fontWeight: 700, color: '#1a3a5c' }}>{item.cliente}</div>
                    <div style={{ fontSize: 11, color: '#9ca3af' }}>{item.email}</div>
                  </td>
                  <td>{item.mesesDeuda}</td>
                  <td>{clp(item.montoAdeudado)}</td>
                  <td><Badge variante={badgeRiesgo[item.riesgo]}>{textoRiesgo[item.riesgo]}</Badge></td>
                  <td>
                    <div className="btn-group">
                      <button className="btn btn-sm btn-success" onClick={() => abrirPago(item)} disabled={item.cuotasAdeudadas.length === 0}>Registrar pago</button>
                      <button className="btn btn-sm btn-danger" onClick={() => setModalSuspension(item)} disabled={item.suspendido}>Suspender</button>
                      <button className="btn btn-sm btn-warn" onClick={() => abrirNotificacion(item)}>Notificar</button>
                      <button className="btn btn-sm btn-outline" onClick={() => abrirDetalle(item)}>Ver detalle</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      <Panel titulo="Gestión rápida de cobranza">
        <div className="responsive-action-row" style={{ padding: 16 }}>
          <div style={{ fontSize: 12, color: '#6b7280' }}>
            Filtra clientes morosos, registra pagos completos de cuotas existentes y suspende servicios usando los endpoints actuales del sistema.
          </div>
          <div className="btn-group">
            <button className="btn btn-sm btn-outline" onClick={() => setRiesgo('CRITICO')}>Ver críticos</button>
            <button className="btn btn-sm btn-outline" onClick={() => { setFiltro(''); setRiesgo(''); }}>Limpiar filtros</button>
          </div>
        </div>
      </Panel>

      <Modal
        abierto={!!modalPago}
        titulo="Registrar Pago desde Morosidad"
        ancho="sm"
        onCerrar={() => setModalPago(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalPago(null)}>Cancelar</button>
            <button className="btn btn-success" form="form-pago-morosidad" type="submit" disabled={guardando}>{guardando ? 'Registrando...' : 'Registrar'}</button>
          </>
        }
      >
        <form id="form-pago-morosidad" onSubmit={formPago.handleSubmit(onRegistrarPago)} noValidate>
          <label className="auth-label">Cliente</label>
          <input className="auth-input" value={modalPago?.morosidad.cliente ?? ''} readOnly />

          <label className="auth-label" style={{ marginTop: 10 }}>Monto</label>
          <input
            type="number"
            className={`auth-input ${formPago.formState.errors.monto ? 'auth-input--error' : ''}`}
            readOnly
            {...formPago.register('monto', { required: 'Obligatorio', valueAsNumber: true })}
          />
          <div style={{ fontSize: 11, color: '#9ca3af', marginTop: 4 }}>
            El backend actual registra pagos completos por cuota; pagos parciales quedan pendientes de backend.
          </div>

          <label className="auth-label" style={{ marginTop: 10 }}>Fecha</label>
          <input
            type="date"
            className={`auth-input ${formPago.formState.errors.fecha ? 'auth-input--error' : ''}`}
            {...formPago.register('fecha', { required: 'Obligatorio' })}
          />

          <label className="auth-label" style={{ marginTop: 10 }}>Método</label>
          <select
            className={`auth-input ${formPago.formState.errors.metodo ? 'auth-input--error' : ''}`}
            {...formPago.register('metodo', { required: 'Obligatorio' })}
          >
            <option value="">Seleccionar...</option>
            {MEDIOS_PAGO.map((medio) => <option key={medio} value={medio}>{medio}</option>)}
          </select>
          {formPago.formState.errors.metodo && <span className="auth-field-error">{formPago.formState.errors.metodo.message}</span>}
        </form>
      </Modal>

      <Modal
        abierto={!!modalSuspension}
        titulo="Confirmar Suspensión por mora"
        ancho="sm"
        onCerrar={() => setModalSuspension(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalSuspension(null)}>Cancelar</button>
            <button className="btn btn-danger" onClick={onSuspender} disabled={guardando}>{guardando ? 'Suspendiendo...' : 'Suspender'}</button>
          </>
        }
      >
        <div className="danger-box">
          Se suspenderá el servicio por deuda crítica. Requiere confirmación del administrador.
        </div>
        <div style={{ fontSize: 13, color: '#4b5563' }}>
          Cliente: <strong>{modalSuspension?.cliente}</strong>
        </div>
      </Modal>

      <Modal
        abierto={!!detalle}
        titulo="Detalle de Morosidad"
        ancho="lg"
        onCerrar={() => setDetalle(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setDetalle(null)}>Cerrar</button>
            <button
              className="btn btn-success"
              onClick={() => {
                if (detalle) {
                  setDetalle(null);
                  abrirPago(detalle);
                }
              }}
              disabled={!detalle || detalle.cuotasAdeudadas.length === 0}
            >
              Registrar pago
            </button>
          </>
        }
      >
        {detalle && (
          <div className="grid-2">
            <div>
              <label className="auth-label">Cliente</label>
              <div className="doc-preview">{detalle.cliente}</div>
            </div>
            <div>
              <label className="auth-label">Deuda</label>
              <div className="doc-preview">{clp(detalle.montoAdeudado)} · {detalle.mesesDeuda} cuota(s)</div>
            </div>
            <div>
              <label className="auth-label">Último pago</label>
              <div className="doc-preview">{fmtFecha(detalle.ultimoPago)}</div>
            </div>
            <div>
              <label className="auth-label">Historial de notificaciones</label>
              <div className="placeholder" style={{ minHeight: 92 }}>
                Historial de notificaciones pendiente de integración con backend.
              </div>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        abierto={!!modalNotificacion}
        titulo="Notificar deuda"
        onCerrar={() => setModalNotificacion(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNotificacion(null)}>Cancelar</button>
            <button className="btn btn-warn" form="form-notificacion-morosidad" type="submit" disabled={guardando}>{guardando ? 'Preparando...' : 'Enviar notificación'}</button>
          </>
        }
      >
        <form id="form-notificacion-morosidad" onSubmit={formNotificacion.handleSubmit(onEnviarNotificacion)} noValidate>
          <label className="auth-label">Destinatario</label>
          <input
            className={`auth-input ${formNotificacion.formState.errors.destinatario ? 'auth-input--error' : ''}`}
            {...formNotificacion.register('destinatario', { required: 'Obligatorio' })}
          />

          <label className="auth-label" style={{ marginTop: 10 }}>Asunto</label>
          <input
            className={`auth-input ${formNotificacion.formState.errors.asunto ? 'auth-input--error' : ''}`}
            {...formNotificacion.register('asunto', { required: 'Obligatorio' })}
          />

          <label className="auth-label" style={{ marginTop: 10 }}>Mensaje</label>
          <textarea
            className={`auth-input ${formNotificacion.formState.errors.mensaje ? 'auth-input--error' : ''}`}
            rows={5}
            {...formNotificacion.register('mensaje', { required: 'Obligatorio' })}
          />
          <div style={{ fontSize: 11, color: '#9ca3af', marginTop: 8 }}>
            El envío y registro real de notificaciones queda pendiente del endpoint backend.
          </div>
        </form>
      </Modal>
    </>
  );
}

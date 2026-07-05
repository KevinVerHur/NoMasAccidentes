import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';
import type {
  AsesoriaResponse, EstadoAsesoria, TipoAsesoria, VarianteBadge,
  AccidenteResponse, CrearAccidenteRequest, GravedadAccidente,
  FiscalizacionResponse, CrearFiscalizacionRequest, EntidadFiscalizadora, ResultadoFiscalizacion,
  MultaResponse, CrearMultaRequest, EstadoMulta,
  PropuestaMejoraResponse, CrearPropuestaMejoraRequest, EstadoPropuesta,
  InformeAsesoriaResponse,
} from '../types';
import { obtenerAsesoria, atenderAsesoria, cerrarAsesoria, cancelarAsesoria } from '../api/asesorias';
import { listarAccidentes, crearAccidente } from '../api/accidentes';
import { listarFiscalizaciones, crearFiscalizacion } from '../api/fiscalizaciones';
import { listarMultas, crearMulta, actualizarEstadoMulta } from '../api/multas';
import { listarPropuestas, crearPropuesta, actualizarEstadoPropuesta } from '../api/propuestas';
import { generarInformeAsesoria, obtenerInformeAsesoria, descargarInformePdf } from '../api/informes';

const badgeEstadoAsesoria: Record<EstadoAsesoria, VarianteBadge> = {
  SOLICITADA: 'blue', EN_PROCESO: 'yellow', CERRADA: 'green', CANCELADA: 'gray',
};
const labelEstadoAsesoria: Record<EstadoAsesoria, string> = {
  SOLICITADA: 'Solicitada', EN_PROCESO: 'En proceso', CERRADA: 'Cerrada', CANCELADA: 'Cancelada',
};
const labelTipo: Record<TipoAsesoria, string> = { ACCIDENTE: 'Accidente', FISCALIZACION: 'Fiscalización' };

const labelGravedad: Record<GravedadAccidente, string> = { LEVE: 'Leve', GRAVE: 'Grave', FATAL: 'Fatal' };
const badgeGravedad: Record<GravedadAccidente, VarianteBadge> = { LEVE: 'yellow', GRAVE: 'red', FATAL: 'red' };

const labelEntidad: Record<EntidadFiscalizadora, string> = {
  DIRECCION_TRABAJO: 'Dirección del Trabajo', SUSESO: 'SUSESO', SEREMI_SALUD: 'SEREMI de Salud',
  MUTUALIDAD: 'Mutualidad', OTRO: 'Otro',
};
const labelResultado: Record<ResultadoFiscalizacion, string> = {
  CONFORME: 'Conforme', CON_OBSERVACIONES: 'Con observaciones', NO_CONFORME: 'No conforme',
};
const badgeResultado: Record<ResultadoFiscalizacion, VarianteBadge> = {
  CONFORME: 'green', CON_OBSERVACIONES: 'yellow', NO_CONFORME: 'red',
};

const ESTADOS_MULTA: EstadoMulta[] = ['PENDIENTE', 'PAGADA', 'APELADA', 'ANULADA'];
const labelEstadoMulta: Record<EstadoMulta, string> = {
  PENDIENTE: 'Pendiente', PAGADA: 'Pagada', APELADA: 'Apelada', ANULADA: 'Anulada',
};
const badgeEstadoMulta: Record<EstadoMulta, VarianteBadge> = {
  PENDIENTE: 'yellow', PAGADA: 'green', APELADA: 'blue', ANULADA: 'gray',
};

const ESTADOS_PROPUESTA: EstadoPropuesta[] = ['PENDIENTE', 'EN_PROCESO', 'VERIFICADA', 'DESCARTADA'];
const labelEstadoPropuesta: Record<EstadoPropuesta, string> = {
  PENDIENTE: 'Pendiente', EN_PROCESO: 'En proceso', VERIFICADA: 'Verificada', DESCARTADA: 'Descartada',
};
const badgeEstadoPropuesta: Record<EstadoPropuesta, VarianteBadge> = {
  PENDIENTE: 'yellow', EN_PROCESO: 'blue', VERIFICADA: 'green', DESCARTADA: 'gray',
};

function fmtFecha(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('es-CL', { day: '2-digit', month: '2-digit', year: 'numeric' });
}
function fmtMonto(monto: number): string {
  return monto.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
}
function mensajeError(e: unknown, fallback: string): string {
  return (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;
}

export default function AsesoriaDetalle() {
  const { id } = useParams();
  const idAsesoria = Number(id);
  const navigate = useNavigate();
  const { rol } = useAuth();
  const esAdmin = rol === 'ADMIN';

  const [asesoria, setAsesoria]   = useState<AsesoriaResponse | null>(null);
  const [cargando, setCargando]   = useState(true);
  const [accionando, setAccionando] = useState(false);
  const [error, setError]         = useState<string | null>(null);

  const [accidentes, setAccidentes] = useState<AccidenteResponse[]>([]);
  const [fiscalizaciones, setFiscalizaciones] = useState<FiscalizacionResponse[]>([]);
  const [fiscSel, setFiscSel] = useState<FiscalizacionResponse | null>(null);
  const [multas, setMultas] = useState<MultaResponse[]>([]);

  const [informe, setInforme] = useState<InformeAsesoriaResponse | null>(null);
  const [propuestas, setPropuestas] = useState<PropuestaMejoraResponse[]>([]);
  const [generandoInforme, setGenerandoInforme] = useState(false);

  const [modalAccidente, setModalAccidente] = useState(false);
  const [modalFisc, setModalFisc] = useState(false);
  const [modalMulta, setModalMulta] = useState(false);
  const [modalPropuesta, setModalPropuesta] = useState(false);
  const [guardando, setGuardando] = useState(false);

  const formAccidente = useForm<CrearAccidenteRequest>();
  const formFisc = useForm<CrearFiscalizacionRequest>();
  const formMulta = useForm<CrearMultaRequest>();
  const formPropuesta = useForm<CrearPropuestaMejoraRequest>();

  const cargarAsesoria = useCallback(async () => {
    setCargando(true);
    try {
      const a = await obtenerAsesoria(idAsesoria);
      setAsesoria(a);
    } catch (e) {
      setError(mensajeError(e, 'No se pudo cargar la asesoría.'));
    } finally {
      setCargando(false);
    }
  }, [idAsesoria]);

  const cargarSatelites = useCallback(async (tipo: TipoAsesoria) => {
    if (tipo === 'ACCIDENTE') {
      listarAccidentes(idAsesoria).then(setAccidentes).catch(() => {});
    } else {
      listarFiscalizaciones(idAsesoria).then(setFiscalizaciones).catch(() => {});
    }
  }, [idAsesoria]);

  const cargarInforme = useCallback(async () => {
    try {
      const inf = await obtenerInformeAsesoria(idAsesoria);
      setInforme(inf);
      const props = await listarPropuestas(inf.id);
      setPropuestas(props);
    } catch {
      setInforme(null);
      setPropuestas([]);
    }
  }, [idAsesoria]);

  useEffect(() => { cargarAsesoria(); }, [cargarAsesoria]);
  useEffect(() => { if (asesoria) { cargarSatelites(asesoria.tipo); cargarInforme(); } }, [asesoria, cargarSatelites, cargarInforme]);

  useEffect(() => {
    if (!fiscSel) { setMultas([]); return; }
    listarMultas(fiscSel.id).then(setMultas).catch(() => setMultas([]));
  }, [fiscSel]);

  async function accionAsesoria(fn: (id: number) => Promise<AsesoriaResponse>, fallback: string) {
    setError(null);
    setAccionando(true);
    try {
      const actualizada = await fn(idAsesoria);
      setAsesoria(actualizada);
    } catch (e) {
      setError(mensajeError(e, fallback));
    } finally {
      setAccionando(false);
    }
  }

  async function onCrearAccidente(data: CrearAccidenteRequest) {
    setError(null); setGuardando(true);
    try {
      await crearAccidente({ ...data, idAsesoria });
      setModalAccidente(false); formAccidente.reset();
      await cargarSatelites('ACCIDENTE');
    } catch (e) {
      setError(mensajeError(e, 'Error al registrar el accidente.'));
    } finally { setGuardando(false); }
  }

  async function onCrearFisc(data: CrearFiscalizacionRequest) {
    setError(null); setGuardando(true);
    try {
      await crearFiscalizacion({ ...data, idAsesoria });
      setModalFisc(false); formFisc.reset();
      await cargarSatelites('FISCALIZACION');
    } catch (e) {
      setError(mensajeError(e, 'Error al registrar la fiscalización.'));
    } finally { setGuardando(false); }
  }

  async function onCrearMulta(data: CrearMultaRequest) {
    if (!fiscSel) return;
    setError(null); setGuardando(true);
    try {
      await crearMulta({ ...data, idFiscalizacion: fiscSel.id });
      setModalMulta(false); formMulta.reset();
      setMultas(await listarMultas(fiscSel.id));
    } catch (e) {
      setError(mensajeError(e, 'Error al registrar la multa.'));
    } finally { setGuardando(false); }
  }

  async function onCambiarEstadoMulta(m: MultaResponse, estado: EstadoMulta) {
    if (estado === m.estadoPago || !fiscSel) return;
    setError(null);
    try {
      await actualizarEstadoMulta(m.id, estado);
      setMultas(await listarMultas(fiscSel.id));
    } catch (e) {
      setError(mensajeError(e, 'Error al actualizar la multa.'));
    }
  }

  async function onGenerarInforme() {
    setError(null); setGenerandoInforme(true);
    try {
      const inf = await generarInformeAsesoria(idAsesoria);
      setInforme(inf);
      setPropuestas(await listarPropuestas(inf.id));
    } catch (e) {
      setError(mensajeError(e, 'Error al generar el informe.'));
    } finally { setGenerandoInforme(false); }
  }

  async function onDescargarInforme() {
    if (!informe) return;
    setError(null);
    try {
      await descargarInformePdf(informe.id);
    } catch (e) {
      setError(mensajeError(e, 'Error al descargar el informe.'));
    }
  }

  async function onCrearPropuesta(data: CrearPropuestaMejoraRequest) {
    if (!informe) return;
    setError(null); setGuardando(true);
    try {
      await crearPropuesta({ ...data, idInforme: informe.id });
      setModalPropuesta(false); formPropuesta.reset();
      setPropuestas(await listarPropuestas(informe.id));
    } catch (e) {
      setError(mensajeError(e, 'Error al registrar la propuesta.'));
    } finally { setGuardando(false); }
  }

  async function onCambiarEstadoPropuesta(p: PropuestaMejoraResponse, estado: EstadoPropuesta) {
    if (estado === p.estado || !informe) return;
    setError(null);
    try {
      await actualizarEstadoPropuesta(p.id, estado);
      setPropuestas(await listarPropuestas(informe.id));
    } catch (e) {
      setError(mensajeError(e, 'Error al actualizar la propuesta.'));
    }
  }

  if (cargando) return <div className="placeholder">Cargando asesoría...</div>;
  if (!asesoria) return (
    <>
      <div className="page-title">Asesoría</div>
      <div className="auth-alert auth-alert--error">{error ?? 'Asesoría no encontrada.'}</div>
      <button className="btn btn-outline" style={{ marginTop: 12 }} onClick={() => navigate('/asesorias')}>← Volver</button>
    </>
  );

  const puedeGenerarInforme = asesoria.estado === 'EN_PROCESO' || asesoria.estado === 'CERRADA';

  return (
    <>
      <button className="btn btn-sm btn-outline" style={{ marginBottom: 10 }} onClick={() => navigate('/asesorias')}>← Volver a asesorías</button>
      <div className="page-title">Asesoría #{asesoria.id} — {asesoria.razonSocialEmpresa}</div>
      <div className="page-subtitle">{labelTipo[asesoria.tipo]} · {asesoria.nombreProfesional}</div>

      {error && <div className="auth-alert auth-alert--error" style={{ marginBottom: 12 }}>{error}</div>}

      <Panel
        titulo="Datos de la asesoría"
        accion={
          <div className="btn-group">
            {asesoria.estado === 'SOLICITADA' && (
              <button className="btn btn-sm btn-primary" disabled={accionando} onClick={() => accionAsesoria(atenderAsesoria, 'Error al iniciar la atención.')}>
                {accionando ? 'Procesando...' : 'Registrar atención'}
              </button>
            )}
            {asesoria.estado === 'EN_PROCESO' && (
              <button className="btn btn-sm btn-success" disabled={accionando} onClick={() => accionAsesoria(cerrarAsesoria, 'Error al cerrar la asesoría.')}>
                {accionando ? 'Procesando...' : 'Cerrar asesoría'}
              </button>
            )}
            {esAdmin && asesoria.estado !== 'CERRADA' && asesoria.estado !== 'CANCELADA' && (
              <button className="btn btn-sm btn-warn" disabled={accionando} onClick={() => accionAsesoria(cancelarAsesoria, 'Error al cancelar.')}>Cancelar</button>
            )}
          </div>
        }
      >
        <div style={{ padding: 16, fontSize: 13, lineHeight: 1.8 }}>
          <div><strong>Estado:</strong> <Badge variante={badgeEstadoAsesoria[asesoria.estado]}>{labelEstadoAsesoria[asesoria.estado]}</Badge>
            {asesoria.esAsesoriaExtra && <span style={{ color: '#f0a500', fontWeight: 600, marginLeft: 8 }}>Asesoría extra</span>}
          </div>
          <div><strong>Solicitada:</strong> {fmtFecha(asesoria.fechaSolicitud)} · <strong>Atención:</strong> {fmtFecha(asesoria.fechaAtencion)}</div>
          <div style={{ marginTop: 6 }}><strong>Motivo:</strong></div>
          <div style={{ whiteSpace: 'pre-wrap' }}>{asesoria.motivo}</div>
        </div>
      </Panel>

      {/* Accidentes */}
      {asesoria.tipo === 'ACCIDENTE' && (
        <Panel
          titulo="Accidentes laborales"
          accion={<button className="btn btn-sm btn-primary" onClick={() => { formAccidente.reset(); setError(null); setModalAccidente(true); }}>+ Registrar accidente</button>}
        >
          {accidentes.length === 0 ? (
            <div className="placeholder">Sin accidentes registrados.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr><th>Fecha</th><th>Gravedad</th><th>Trabajador</th><th>Días perdidos</th><th>SUSESO</th><th>Descripción</th></tr>
              </thead>
              <tbody>
                {accidentes.map(ac => (
                  <tr key={ac.id}>
                    <td>{fmtFecha(ac.fechaOcurrencia)}</td>
                    <td><Badge variante={badgeGravedad[ac.gravedad]}>{labelGravedad[ac.gravedad]}</Badge></td>
                    <td>{ac.trabajadorAfectado ?? '—'}</td>
                    <td>{ac.diasPerdidos ?? '—'}</td>
                    <td>{ac.fueReportadoSusseso ? 'Sí' : 'No'}</td>
                    <td style={{ maxWidth: 260, whiteSpace: 'normal' }}>{ac.descripcion ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      {/* Fiscalizaciones */}
      {asesoria.tipo === 'FISCALIZACION' && (
        <Panel
          titulo="Fiscalizaciones"
          accion={<button className="btn btn-sm btn-primary" onClick={() => { formFisc.reset(); setError(null); setModalFisc(true); }}>+ Registrar fiscalización</button>}
        >
          {fiscalizaciones.length === 0 ? (
            <div className="placeholder">Sin fiscalizaciones registradas.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr><th>Fecha</th><th>Entidad</th><th>Motivo</th><th>Resultado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {fiscalizaciones.map(f => (
                  <tr key={f.id} style={fiscSel?.id === f.id ? { background: '#eff6ff' } : undefined}>
                    <td>{fmtFecha(f.fecha)}</td>
                    <td>{labelEntidad[f.entidadFiscalizadora]}</td>
                    <td style={{ maxWidth: 220, whiteSpace: 'normal' }}>{f.motivo ?? '—'}</td>
                    <td>{f.resultado ? <Badge variante={badgeResultado[f.resultado]}>{labelResultado[f.resultado]}</Badge> : '—'}</td>
                    <td>
                      <button className="btn btn-sm btn-outline" onClick={() => setFiscSel(fiscSel?.id === f.id ? null : f)}>
                        {fiscSel?.id === f.id ? 'Ocultar multas' : 'Ver multas'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      {/* Multas de la fiscalización seleccionada */}
      {asesoria.tipo === 'FISCALIZACION' && fiscSel && (
        <Panel
          titulo={`⚖ Multas — fiscalización del ${fmtFecha(fiscSel.fecha)}`}
          accion={esAdmin ? <button className="btn btn-sm btn-primary" onClick={() => { formMulta.reset(); setError(null); setModalMulta(true); }}>+ Registrar multa</button> : undefined}
        >
          {multas.length === 0 ? (
            <div className="placeholder">Sin multas registradas para esta fiscalización.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr><th>Emisión</th><th>Monto</th><th>Normativa</th><th>Motivo</th><th>Estado de pago</th></tr>
              </thead>
              <tbody>
                {multas.map(m => (
                  <tr key={m.id}>
                    <td>{fmtFecha(m.fechaEmision)}</td>
                    <td>{fmtMonto(m.monto)}</td>
                    <td>{m.normativaInfringida ?? '—'}</td>
                    <td style={{ maxWidth: 220, whiteSpace: 'normal' }}>{m.motivo ?? '—'}</td>
                    <td>
                      {esAdmin ? (
                        <select className="auth-input" style={{ padding: '4px 8px', fontSize: 12 }} value={m.estadoPago}
                          onChange={e => onCambiarEstadoMulta(m, e.target.value as EstadoMulta)}>
                          {ESTADOS_MULTA.map(es => <option key={es} value={es}>{labelEstadoMulta[es]}</option>)}
                        </select>
                      ) : (
                        <Badge variante={badgeEstadoMulta[m.estadoPago]}>{labelEstadoMulta[m.estadoPago]}</Badge>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      {/* Informe + propuestas de mejora */}
      <Panel
        titulo="Informe de la asesoría"
        accion={
          informe
            ? <button className="btn btn-sm btn-outline" onClick={onDescargarInforme}>📄 Descargar PDF</button>
            : <button className="btn btn-sm btn-primary" disabled={!puedeGenerarInforme || generandoInforme} onClick={onGenerarInforme}>
                {generandoInforme ? 'Generando...' : 'Generar informe'}
              </button>
        }
      >
        {!informe ? (
          <div className="placeholder">
            {puedeGenerarInforme
              ? 'Aún no se ha generado el informe de esta asesoría.'
              : 'El informe podrá generarse cuando la asesoría esté en proceso o cerrada.'}
          </div>
        ) : (
          <div style={{ padding: 16, fontSize: 13 }}>
            <div style={{ marginBottom: 12 }}>
              Emitido el <strong>{fmtFecha(informe.fechaEmision)}</strong>{informe.hallazgos ? ` · ${informe.hallazgos}` : ''}
            </div>
            <div className="responsive-inline-fields" style={{ justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ fontWeight: 700, color: '#18395a' }}>Propuestas de mejora (RF25)</span>
              <button className="btn btn-sm btn-primary" onClick={() => { formPropuesta.reset(); setError(null); setModalPropuesta(true); }}>+ Agregar propuesta</button>
            </div>
            {propuestas.length === 0 ? (
              <div className="placeholder">Sin propuestas de mejora registradas.</div>
            ) : (
              <table className="app-table">
                <thead>
                  <tr><th>Descripción</th><th>Responsable</th><th>Límite</th><th>Verificación</th><th>Estado</th></tr>
                </thead>
                <tbody>
                  {propuestas.map(p => (
                    <tr key={p.id}>
                      <td style={{ maxWidth: 280, whiteSpace: 'normal' }}>{p.descripcion}</td>
                      <td>{p.responsable ?? '—'}</td>
                      <td>{fmtFecha(p.fechaLimite)}</td>
                      <td>{fmtFecha(p.fechaVerificacion)}</td>
                      <td>
                        <select className="auth-input" style={{ padding: '4px 8px', fontSize: 12 }} value={p.estado}
                          onChange={e => onCambiarEstadoPropuesta(p, e.target.value as EstadoPropuesta)}>
                          {ESTADOS_PROPUESTA.map(es => <option key={es} value={es}>{labelEstadoPropuesta[es]}</option>)}
                        </select>
                        <div style={{ marginTop: 4 }}>
                          <Badge variante={badgeEstadoPropuesta[p.estado]}>{labelEstadoPropuesta[p.estado]}</Badge>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </Panel>

      {/* Modal accidente */}
      <Modal abierto={modalAccidente} titulo="Registrar accidente laboral" onCerrar={() => setModalAccidente(false)}
        footer={<>
          <button className="btn btn-outline" onClick={() => setModalAccidente(false)}>Cancelar</button>
          <button className="btn btn-primary" form="form-accidente" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Registrar'}</button>
        </>}>
        <form id="form-accidente" onSubmit={formAccidente.handleSubmit(onCrearAccidente)} noValidate>
          <div className="responsive-form-grid">
            <div>
              <label className="auth-label">Fecha de ocurrencia *</label>
              <input type="date" className={`auth-input ${formAccidente.formState.errors.fechaOcurrencia ? 'auth-input--error' : ''}`}
                {...formAccidente.register('fechaOcurrencia', { required: 'Obligatorio' })} />
              {formAccidente.formState.errors.fechaOcurrencia && <span className="auth-field-error">{formAccidente.formState.errors.fechaOcurrencia.message}</span>}
            </div>
            <div>
              <label className="auth-label">Gravedad *</label>
              <select className={`auth-input ${formAccidente.formState.errors.gravedad ? 'auth-input--error' : ''}`}
                {...formAccidente.register('gravedad', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                <option value="LEVE">Leve</option>
                <option value="GRAVE">Grave</option>
                <option value="FATAL">Fatal</option>
              </select>
              {formAccidente.formState.errors.gravedad && <span className="auth-field-error">{formAccidente.formState.errors.gravedad.message}</span>}
            </div>
            <div>
              <label className="auth-label">Trabajador afectado</label>
              <input className="auth-input" {...formAccidente.register('trabajadorAfectado', { maxLength: { value: 150, message: 'Máx. 150' } })} />
            </div>
            <div>
              <label className="auth-label">Días perdidos</label>
              <input type="number" min={0} className="auth-input" {...formAccidente.register('diasPerdidos', { valueAsNumber: true, min: { value: 0, message: 'No negativo' } })} />
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Descripción</label>
              <textarea rows={3} className="auth-input" {...formAccidente.register('descripcion', { maxLength: { value: 2000, message: 'Máx. 2000' } })} />
            </div>
            <div className="responsive-span-2 responsive-check-row">
              <input type="checkbox" id="reportadoSusseso" {...formAccidente.register('fueReportadoSusseso')} />
              <label htmlFor="reportadoSusseso" style={{ fontSize: 13 }}>Reportado a SUSESO</label>
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal fiscalización */}
      <Modal abierto={modalFisc} titulo="Registrar fiscalización" onCerrar={() => setModalFisc(false)}
        footer={<>
          <button className="btn btn-outline" onClick={() => setModalFisc(false)}>Cancelar</button>
          <button className="btn btn-primary" form="form-fisc" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Registrar'}</button>
        </>}>
        <form id="form-fisc" onSubmit={formFisc.handleSubmit(onCrearFisc)} noValidate>
          <div className="responsive-form-grid">
            <div>
              <label className="auth-label">Fecha *</label>
              <input type="date" className={`auth-input ${formFisc.formState.errors.fecha ? 'auth-input--error' : ''}`}
                {...formFisc.register('fecha', { required: 'Obligatorio' })} />
              {formFisc.formState.errors.fecha && <span className="auth-field-error">{formFisc.formState.errors.fecha.message}</span>}
            </div>
            <div>
              <label className="auth-label">Entidad fiscalizadora *</label>
              <select className={`auth-input ${formFisc.formState.errors.entidadFiscalizadora ? 'auth-input--error' : ''}`}
                {...formFisc.register('entidadFiscalizadora', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                <option value="DIRECCION_TRABAJO">Dirección del Trabajo</option>
                <option value="SUSESO">SUSESO</option>
                <option value="SEREMI_SALUD">SEREMI de Salud</option>
                <option value="MUTUALIDAD">Mutualidad</option>
                <option value="OTRO">Otro</option>
              </select>
              {formFisc.formState.errors.entidadFiscalizadora && <span className="auth-field-error">{formFisc.formState.errors.entidadFiscalizadora.message}</span>}
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Resultado</label>
              <select className="auth-input" {...formFisc.register('resultado')}>
                <option value="">Sin resultado (en curso)</option>
                <option value="CONFORME">Conforme</option>
                <option value="CON_OBSERVACIONES">Con observaciones</option>
                <option value="NO_CONFORME">No conforme</option>
              </select>
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Motivo</label>
              <textarea rows={2} className="auth-input" {...formFisc.register('motivo', { maxLength: { value: 500, message: 'Máx. 500' } })} />
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Observaciones</label>
              <textarea rows={3} className="auth-input" {...formFisc.register('observaciones', { maxLength: { value: 2000, message: 'Máx. 2000' } })} />
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal multa */}
      <Modal abierto={modalMulta} titulo="Registrar multa" onCerrar={() => setModalMulta(false)}
        footer={<>
          <button className="btn btn-outline" onClick={() => setModalMulta(false)}>Cancelar</button>
          <button className="btn btn-primary" form="form-multa" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Registrar'}</button>
        </>}>
        <form id="form-multa" onSubmit={formMulta.handleSubmit(onCrearMulta)} noValidate>
          <div className="responsive-form-grid">
            <div>
              <label className="auth-label">Fecha de emisión *</label>
              <input type="date" className={`auth-input ${formMulta.formState.errors.fechaEmision ? 'auth-input--error' : ''}`}
                {...formMulta.register('fechaEmision', { required: 'Obligatorio' })} />
              {formMulta.formState.errors.fechaEmision && <span className="auth-field-error">{formMulta.formState.errors.fechaEmision.message}</span>}
            </div>
            <div>
              <label className="auth-label">Monto (CLP) *</label>
              <input type="number" min={1} step="1" className={`auth-input ${formMulta.formState.errors.monto ? 'auth-input--error' : ''}`}
                {...formMulta.register('monto', { required: 'Obligatorio', valueAsNumber: true, min: { value: 1, message: 'Mayor a cero' } })} />
              {formMulta.formState.errors.monto && <span className="auth-field-error">{formMulta.formState.errors.monto.message}</span>}
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Normativa infringida</label>
              <input className="auth-input" {...formMulta.register('normativaInfringida', { maxLength: { value: 150, message: 'Máx. 150' } })} />
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Motivo</label>
              <textarea rows={3} className="auth-input" {...formMulta.register('motivo', { maxLength: { value: 1000, message: 'Máx. 1000' } })} />
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal propuesta */}
      <Modal abierto={modalPropuesta} titulo="Registrar propuesta de mejora" onCerrar={() => setModalPropuesta(false)}
        footer={<>
          <button className="btn btn-outline" onClick={() => setModalPropuesta(false)}>Cancelar</button>
          <button className="btn btn-primary" form="form-propuesta" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Registrar'}</button>
        </>}>
        <form id="form-propuesta" onSubmit={formPropuesta.handleSubmit(onCrearPropuesta)} noValidate>
          <div className="responsive-form-grid">
            <div className="responsive-span-2">
              <label className="auth-label">Descripción *</label>
              <textarea rows={3} className={`auth-input ${formPropuesta.formState.errors.descripcion ? 'auth-input--error' : ''}`}
                {...formPropuesta.register('descripcion', { required: 'Obligatorio', maxLength: { value: 1000, message: 'Máx. 1000' } })} />
              {formPropuesta.formState.errors.descripcion && <span className="auth-field-error">{formPropuesta.formState.errors.descripcion.message}</span>}
            </div>
            <div>
              <label className="auth-label">Fecha límite</label>
              <input type="date" className="auth-input" {...formPropuesta.register('fechaLimite')} />
            </div>
            <div>
              <label className="auth-label">Responsable</label>
              <input className="auth-input" {...formPropuesta.register('responsable', { maxLength: { value: 120, message: 'Máx. 120' } })} />
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>
    </>
  );
}

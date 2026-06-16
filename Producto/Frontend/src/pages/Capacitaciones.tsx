import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';
import type {
  CapacitacionResponse,
  CrearCapacitacionRequest,
  AsistenciaResponse,
  EstadoCapacitacion,
  VarianteBadge,
  ClienteResponse,
  ProfesionalResponse,
  AsistenteRequest,
  AsistenteResponse,
} from '../types';
import {
  listarCapacitaciones,
  crearCapacitacion,
  cancelarCapacitacion,
  confirmarAsistencia,
  inscribirAsistentes,
} from '../api/capacitaciones';
import { listarClientes, miCliente } from '../api/clientes';
import { listarProfesionales } from '../api/profesionales';
import { crearAsistente, editarAsistente, eliminarAsistente, listarAsistentesPorCliente } from '../api/asistentes';

// ── Helpers ────────────────────────────────────────────────────────────────────

const MESES = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];

const badgePorEstado: Record<EstadoCapacitacion, VarianteBadge> = {
  PROGRAMADA: 'gray',
  EN_CURSO:   'yellow',
  REALIZADA:  'green',
  CANCELADA:  'red',
};

const labelEstado: Record<EstadoCapacitacion, string> = {
  PROGRAMADA: 'Sin confirmar',
  EN_CURSO:   'En curso',
  REALIZADA:  'Realizada',
  CANCELADA:  'Cancelada',
};

function fmtFecha(iso: string | null): string {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  return `${parseInt(d)} ${MESES[parseInt(m)-1]} ${y}`;
}

function mensajeError(e: unknown, fallback: string): string {
  return (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;
}

function fechaMinima(): string {
  const d = new Date();
  d.setDate(d.getDate() + 15);
  return d.toISOString().split('T')[0];
}

// ══════════════════════════════════════════════════════════════════════════════
//  VISTA ADMIN — gestión completa
// ══════════════════════════════════════════════════════════════════════════════

function VistaAdmin() {
  const [capacitaciones, setCapacitaciones] = useState<CapacitacionResponse[]>([]);
  const [clientes,       setClientes]       = useState<ClienteResponse[]>([]);
  const [profesionales,  setProfesionales]  = useState<ProfesionalResponse[]>([]);
  const [cargando,       setCargando]       = useState(true);
  const [busqueda,       setBusqueda]       = useState('');

  const [modalNueva,        setModalNueva]        = useState(false);
  const [modalDetalle,      setModalDetalle]      = useState<CapacitacionResponse | null>(null);
  const [modalAsistentes,   setModalAsistentes]   = useState<CapacitacionResponse | null>(null);
  const [modalCancelar,     setModalCancelar]     = useState<CapacitacionResponse | null>(null);
  const [modalCertificados, setModalCertificados] = useState<CapacitacionResponse | null>(null);
  const [modalActa,         setModalActa]         = useState<CapacitacionResponse | null>(null);
  const [guardando,         setGuardando]         = useState(false);
  const [error,             setError]             = useState<string | null>(null);
  const [confirmandoId,     setConfirmandoId]     = useState<number | null>(null);

  const formNueva = useForm<CrearCapacitacionRequest>();

  const cargar = useCallback(async () => {
    setCargando(true);
    try { const d = await listarCapacitaciones(0, 200); setCapacitaciones(d.content); }
    finally { setCargando(false); }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);
  useEffect(() => {
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
    listarProfesionales(0, 200).then(d => setProfesionales(d.content)).catch(() => {});
  }, []);

  const filtradas = capacitaciones.filter(c => {
    const t = busqueda.toLowerCase();
    return !t || c.curso.toLowerCase().includes(t) || c.cliente.toLowerCase().includes(t) || c.relator.toLowerCase().includes(t);
  });

  const programadas   = capacitaciones.filter(c => c.estado === 'PROGRAMADA').length;
  const realizadas    = capacitaciones.filter(c => c.estado === 'REALIZADA').length;

  async function onCrear(data: CrearCapacitacionRequest) {
    setError(null); setGuardando(true);
    try { await crearCapacitacion(data); setModalNueva(false); formNueva.reset(); await cargar(); }
    catch (e) { setError(mensajeError(e, 'Error al programar la capacitación.')); }
    finally { setGuardando(false); }
  }

  async function onCancelar() {
    if (!modalCancelar) return;
    setGuardando(true);
    try { await cancelarCapacitacion(modalCancelar.id); setModalCancelar(null); await cargar(); }
    catch (e) { setError(mensajeError(e, 'Error al cancelar.')); }
    finally { setGuardando(false); }
  }

  async function onConfirmar(idCap: number, idAsistente: number) {
    setConfirmandoId(idAsistente);
    try {
      await confirmarAsistencia(idCap, idAsistente);
      const fresca = (await listarCapacitaciones(0, 200)).content.find(c => c.id === idCap);
      if (fresca) setModalAsistentes(fresca);
      await cargar();
    } catch (e) { alert(mensajeError(e, 'No se pudo confirmar.')); }
    finally { setConfirmandoId(null); }
  }

  return (
    <>
      <div className="page-title">Capacitaciones</div>
      <div className="page-subtitle">Gestión de cursos y talleres — {capacitaciones.length} registros</div>

      <div className="kpi-row">
        <KpiCard label="Sin cap. 90d" value={5}              variante="peligro" />
        <KpiCard label="Programadas"  value={programadas}    variante="warn" />
        <KpiCard label="Realizadas"   value={realizadas}     variante="ok" />
        <KpiCard label="Asistencia"   value="87%"            sub="promedio" />
      </div>

      <Panel
        titulo="🎓 Cursos y asistencia"
        accion={
          <button className="btn btn-sm btn-primary" onClick={() => { formNueva.reset(); setError(null); setModalNueva(true); }}>
            + Nueva capacitación
          </button>
        }
      >
        <div className="searchbar">
          <input placeholder="Buscar por curso, cliente o relator..." value={busqueda} onChange={e => setBusqueda(e.target.value)} />
        </div>

        {cargando ? <div className="placeholder">Cargando capacitaciones...</div>
        : filtradas.length === 0 ? <div className="placeholder">No hay capacitaciones registradas.</div>
        : filtradas.map((c, i) => {
          const confirmados  = c.asistencias.filter(a => a.confirmado).length;
          const esProgramada = c.estado === 'PROGRAMADA';
          const esRealizada  = c.estado === 'REALIZADA';
          return (
            <div key={c.id} style={{ padding: '16px 20px', borderBottom: i < filtradas.length - 1 ? '1px solid #f3f4f6' : 'none' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 16 }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <span style={{ fontSize: 14, fontWeight: 700, color: '#1a3a5c' }}>{c.curso}</span>
                    <Badge variante={badgePorEstado[c.estado]}>{labelEstado[c.estado]}</Badge>
                    {c.esCapacitacionExtra && (
                      <span style={{ fontSize: 10, fontWeight: 700, color: '#e07b00', background: '#fffbeb', borderRadius: 4, padding: '2px 6px', border: '1px solid #fde68a' }}>⚡ Extra</span>
                    )}
                  </div>
                  <div style={{ fontSize: 12, color: '#6b7280' }}>
                    {fmtFecha(c.fechaProgramada)} · {c.horaProgramada} · {c.cliente} · {c.relator} · {confirmados}/{c.cupos} confirmados
                  </div>
                  {esProgramada && confirmados === 0 && (
                    <div style={{ marginTop: 6, fontSize: 11, color: '#92400e', background: '#fffbeb', borderRadius: 6, padding: '4px 10px', display: 'inline-block' }}>
                      ⚠️ Sin confirmar asistentes
                    </div>
                  )}
                </div>
                <div className="btn-group" style={{ flexShrink: 0, marginTop: 2 }}>
                  <button className="btn btn-sm btn-outline" onClick={() => setModalDetalle(c)}>Ver detalles</button>
                  {esProgramada && <button className="btn btn-sm btn-primary" onClick={() => setModalAsistentes(c)}>Confirmar asistentes</button>}
                  {esRealizada && <>
                    <button className="btn btn-sm btn-outline" onClick={() => setModalActa(c)}>Ver acta</button>
                    <button className="btn btn-sm btn-success" onClick={() => setModalCertificados(c)}>Descargar certificados</button>
                  </>}
                  {(esProgramada || c.estado === 'EN_CURSO') && (
                    <button className="btn btn-sm btn-danger" onClick={() => { setError(null); setModalCancelar(c); }}>Cancelar</button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </Panel>

      {/* Modal Nueva */}
      <Modal abierto={modalNueva} titulo="Nueva Capacitación" onCerrar={() => setModalNueva(false)}
        footer={<><button className="btn btn-outline" onClick={() => setModalNueva(false)}>Cancelar</button><button className="btn btn-primary" form="form-cap-nueva" type="submit" disabled={guardando}>{guardando ? 'Guardando...' : 'Guardar'}</button></>}>
        <form id="form-cap-nueva" onSubmit={formNueva.handleSubmit(onCrear)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div>
              <label className="auth-label">Cliente *</label>
              <select className={`auth-input ${formNueva.formState.errors.idCliente ? 'auth-input--error' : ''}`} {...formNueva.register('idCliente', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>{clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
              </select>
              {formNueva.formState.errors.idCliente && <span className="auth-field-error">{formNueva.formState.errors.idCliente.message}</span>}
            </div>
            <div>
              <label className="auth-label">Curso *</label>
              <input className={`auth-input ${formNueva.formState.errors.curso ? 'auth-input--error' : ''}`} placeholder="Manejo manual de cargas" {...formNueva.register('curso', { required: 'Obligatorio' })} />
              {formNueva.formState.errors.curso && <span className="auth-field-error">{formNueva.formState.errors.curso.message}</span>}
            </div>
            <div>
              <label className="auth-label">Relator *</label>
              <select className={`auth-input ${formNueva.formState.errors.idRelator ? 'auth-input--error' : ''}`} {...formNueva.register('idRelator', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>{profesionales.map(p => <option key={p.id} value={p.id}>{p.nombreCompleto}</option>)}
              </select>
              {formNueva.formState.errors.idRelator && <span className="auth-field-error">{formNueva.formState.errors.idRelator.message}</span>}
            </div>
            <div>
              <label className="auth-label">Fecha *</label>
              <input type="date" min={fechaMinima()} className={`auth-input ${formNueva.formState.errors.fechaProgramada ? 'auth-input--error' : ''}`} {...formNueva.register('fechaProgramada', { required: 'Obligatorio' })} />
              {formNueva.formState.errors.fechaProgramada && <span className="auth-field-error">{formNueva.formState.errors.fechaProgramada.message}</span>}
              <span style={{ fontSize: 11, color: '#9ca3af', display: 'block', marginTop: 2 }}>Mínimo 15 días de anticipación</span>
            </div>
            <div>
              <label className="auth-label">Hora *</label>
              <input type="time" className={`auth-input ${formNueva.formState.errors.horaProgramada ? 'auth-input--error' : ''}`} {...formNueva.register('horaProgramada', { required: 'Obligatorio' })} />
              {formNueva.formState.errors.horaProgramada && <span className="auth-field-error">{formNueva.formState.errors.horaProgramada.message}</span>}
            </div>
            <div>
              <label className="auth-label">Cupos</label>
              <input type="number" min={1} className="auth-input" placeholder="20" {...formNueva.register('cupos', { valueAsNumber: true })} />
            </div>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Objetivo</label>
              <textarea className="auth-input" rows={3} placeholder="Capacitar sobre manipulación segura de cargas." {...formNueva.register('objetivo')} />
            </div>
            <div style={{ gridColumn: 'span 2', display: 'flex', alignItems: 'center', gap: 8 }}>
              <input type="checkbox" id="esCapacitacionExtra" {...formNueva.register('esCapacitacionExtra')} />
              <label htmlFor="esCapacitacionExtra" style={{ fontSize: 13 }}>Capacitación adicional (genera costo extra)</label>
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal Detalle */}
      <Modal abierto={!!modalDetalle} titulo="Ver Detalles Curso" onCerrar={() => setModalDetalle(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalDetalle(null)}>Cerrar</button>{modalDetalle?.estado === 'PROGRAMADA' && <button className="btn btn-primary" onClick={() => { setModalAsistentes(modalDetalle); setModalDetalle(null); }}>Confirmar asistentes</button>}</>}>
        {modalDetalle && (
          <div style={{ fontSize: 13, lineHeight: 1.9 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2px 24px' }}>
              <p><strong>Curso:</strong> {modalDetalle.curso}</p>
              <p><strong>Estado:</strong> <Badge variante={badgePorEstado[modalDetalle.estado]}>{labelEstado[modalDetalle.estado]}</Badge></p>
              <p><strong>Cliente:</strong> {modalDetalle.cliente}</p>
              <p><strong>Relator:</strong> {modalDetalle.relator}</p>
              <p><strong>Fecha:</strong> {fmtFecha(modalDetalle.fechaProgramada)}</p>
              <p><strong>Hora:</strong> {modalDetalle.horaProgramada}</p>
              <p><strong>Confirmados:</strong> {modalDetalle.asistencias.filter(a => a.confirmado).length}/{modalDetalle.cupos}</p>
              <p><strong>Cupos disp.:</strong> {modalDetalle.cuposDisponibles}</p>
            </div>
            {modalDetalle.objetivo && (
              <div style={{ marginTop: 10, background: '#f8fafc', borderLeft: '3px solid #cbd5e1', padding: '8px 12px', borderRadius: 6, fontSize: 12, color: '#4b5563' }}>
                <strong>Objetivo:</strong> {modalDetalle.objetivo}
              </div>
            )}
            {modalDetalle.esCapacitacionExtra && <div className="alert-item alert-item--warn" style={{ marginTop: 10 }}>⚡ Capacitación adicional — genera costo extra al plan.</div>}
          </div>
        )}
      </Modal>

      {/* Modal Confirmar Asistentes */}
      <Modal abierto={!!modalAsistentes} titulo="Confirmar Asistentes" ancho="lg" onCerrar={() => setModalAsistentes(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalAsistentes(null)}>Cancelar</button><button className="btn btn-primary" onClick={() => setModalAsistentes(null)}>Guardar</button></>}>
        {modalAsistentes && (
          <>
            <div style={{ fontSize: 12, color: '#6b7280', marginBottom: 14 }}><strong>{modalAsistentes.curso}</strong> · {fmtFecha(modalAsistentes.fechaProgramada)} · {modalAsistentes.horaProgramada}</div>
            {modalAsistentes.asistencias.length === 0 ? (
              <div className="alert-item alert-item--warn">⚠️ No hay asistentes inscritos aún.</div>
            ) : (
              <>
                <table className="app-table">
                  <thead><tr><th>Trabajador</th><th>RUT</th><th>Cargo</th><th>Estado</th><th>Acción</th></tr></thead>
                  <tbody>
                    {modalAsistentes.asistencias.map((a: AsistenciaResponse) => (
                      <tr key={a.idAsistencia}>
                        <td style={{ fontWeight: 600 }}>{a.nombreAsistente}</td>
                        <td>{a.rutAsistente}</td>
                        <td>{a.cargoAsistente ?? '—'}</td>
                        <td><Badge variante={a.confirmado ? 'green' : 'gray'}>{a.confirmado ? 'Confirmado' : 'Pendiente'}</Badge></td>
                        <td>{!a.confirmado && <button className="btn btn-sm btn-primary" disabled={confirmandoId === a.idAsistente} onClick={() => onConfirmar(modalAsistentes.id, a.idAsistente)}>{confirmandoId === a.idAsistente ? 'Confirmando...' : 'Confirmar'}</button>}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {modalAsistentes.asistencias.every(a => !a.confirmado) && (
                  <div className="alert-item alert-item--warn" style={{ marginTop: 12 }}>⚠️ No se puede cerrar la capacitación sin asistencias confirmadas.</div>
                )}
              </>
            )}
          </>
        )}
      </Modal>

      {/* Modal Acta */}
      <Modal abierto={!!modalActa} titulo="Ver Acta" ancho="sm" onCerrar={() => setModalActa(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalActa(null)}>Cerrar</button><button className="btn btn-primary" onClick={() => alert('Descargando acta...')}>Descargar acta</button></>}>
        {modalActa && (
          <div style={{ fontSize: 13, lineHeight: 1.9 }}>
            <p style={{ fontWeight: 700, fontSize: 14, marginBottom: 10 }}>Acta — {modalActa.curso}</p>
            <p><strong>Fecha:</strong> {fmtFecha(modalActa.fechaRealizacion ?? modalActa.fechaProgramada)}</p>
            <p><strong>Cliente:</strong> {modalActa.cliente}</p>
            <p><strong>Relator:</strong> {modalActa.relator}</p>
            <p><strong>Asistencia:</strong> {modalActa.asistencias.filter(a => a.asistio).length}/{modalActa.asistencias.length} presentes</p>
            <div style={{ marginTop: 10, background: '#f0fdf4', borderLeft: '4px solid #27ae60', padding: '8px 12px', borderRadius: 6, fontSize: 12 }}>Resultado: capacitación realizada correctamente.</div>
          </div>
        )}
      </Modal>

      {/* Modal Certificados */}
      <Modal abierto={!!modalCertificados} titulo="Descargar Certificados" ancho="sm" onCerrar={() => setModalCertificados(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalCertificados(null)}>Cerrar</button>{modalCertificados?.asistencias.some(a => a.confirmado) && <button className="btn btn-success" onClick={() => alert('Generando certificados...')}>Generar</button>}</>}>
        {modalCertificados && (
          modalCertificados.asistencias.filter(a => a.confirmado).length === 0 ? (
            <div className="alert-item alert-item--warn">⚠️ Sin asistentes confirmados. La descarga está bloqueada.</div>
          ) : (
            <>
              <div className="alert-item alert-item--ok" style={{ marginBottom: 12 }}>✅ {modalCertificados.asistencias.filter(a => a.confirmado).length} asistentes listos para certificar.</div>
              <table className="app-table">
                <thead><tr><th>Asistente</th><th>RUT</th><th></th></tr></thead>
                <tbody>{modalCertificados.asistencias.filter(a => a.confirmado).map(a => (
                  <tr key={a.idAsistencia}>
                    <td style={{ fontWeight: 600 }}>{a.nombreAsistente}</td>
                    <td>{a.rutAsistente}</td>
                    <td><button className="btn btn-sm btn-outline" onClick={() => alert(`Descargando certificado de ${a.nombreAsistente}`)}>🏅 Descargar</button></td>
                  </tr>
                ))}</tbody>
              </table>
            </>
          )
        )}
      </Modal>

      {/* Modal Cancelar */}
      <Modal abierto={!!modalCancelar} titulo="Cancelar capacitación" ancho="sm" onCerrar={() => setModalCancelar(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalCancelar(null)}>Cancelar</button><button className="btn btn-danger" onClick={onCancelar} disabled={guardando}>{guardando ? 'Cancelando...' : 'Confirmar'}</button></>}>
        <div style={{ background: '#fef2f2', borderLeft: '4px solid #c0392b', padding: 12, borderRadius: 8, fontSize: 13, lineHeight: 1.5 }}>
          ¿Cancelar <strong>"{modalCancelar?.curso}"</strong> del {fmtFecha(modalCancelar?.fechaProgramada ?? null)} a las {modalCancelar?.horaProgramada}?
        </div>
        {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
      </Modal>
    </>
  );
}

// ══════════════════════════════════════════════════════════════════════════════
//  VISTA PROFESIONAL — capacitaciones por dictar + registrar asistencia
//  Wireframe: PDF profesional p.3
// ══════════════════════════════════════════════════════════════════════════════

function VistaProfesional() {
  const [capacitaciones, setCapacitaciones] = useState<CapacitacionResponse[]>([]);
  const [cargando,       setCargando]       = useState(true);
  const [modalDetalle,   setModalDetalle]   = useState<CapacitacionResponse | null>(null);
  const [modalAsistencia,setModalAsistencia]= useState<CapacitacionResponse | null>(null);
  const [modalActa,      setModalActa]      = useState<CapacitacionResponse | null>(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try { const d = await listarCapacitaciones(0, 200); setCapacitaciones(d.content); }
    finally { setCargando(false); }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const proximas  = capacitaciones.filter(c => c.estado === 'PROGRAMADA' || c.estado === 'EN_CURSO');
  const historial = capacitaciones.filter(c => c.estado === 'REALIZADA');
  const porDictar = proximas.length;
  const dictadas  = historial.length;
  const confirmados = capacitaciones.reduce((acc, c) => acc + c.asistencias.filter(a => a.confirmado).length, 0);
  const totalAsistentes = capacitaciones.reduce((acc, c) => acc + c.asistencias.length, 0);
  const extras    = capacitaciones.filter(c => c.esCapacitacionExtra).length;

  return (
    <>
      <div className="page-title">Capacitaciones</div>
      <div className="page-subtitle">Tus capacitaciones asignadas como relator</div>

      {/* KPIs — wireframe profesional p.3 */}
      <div className="kpi-row">
        <KpiCard label="Por dictar"             value={porDictar}                               variante="warn" />
        <KpiCard label="Dictadas (mes)"         value={dictadas}                                variante="ok" />
        <KpiCard label="Asistentes confirmados" value={`${confirmados}/${totalAsistentes}`} />
        <KpiCard label="Adicionales"            value={extras} />
      </div>

      {/* Capacitaciones programadas */}
      <Panel titulo="📅 Capacitaciones programadas">
        {cargando ? <div className="placeholder">Cargando...</div>
        : proximas.length === 0 ? <div className="placeholder">No tienes capacitaciones programadas próximamente.</div>
        : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Tema</th>
                <th>Tipo</th>
                <th>Asistentes</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {proximas.map(c => {
                const confirmados = c.asistencias.filter(a => a.confirmado).length;
                return (
                  <tr key={c.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{fmtFecha(c.fechaProgramada)}</div>
                      <div style={{ fontSize: 11, color: '#9ca3af' }}>{c.horaProgramada}</div>
                    </td>
                    <td>{c.cliente}</td>
                    <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{c.curso}</td>
                    <td>
                      <Badge variante={c.esCapacitacionExtra ? 'yellow' : 'blue'}>
                        {c.esCapacitacionExtra ? 'Extra' : 'Plan'}
                      </Badge>
                    </td>
                    <td>
                      <span style={{ fontSize: 12, color: confirmados === 0 ? '#c0392b' : '#4b5563' }}>
                        {confirmados}/{c.cupos} confirmados
                      </span>
                    </td>
                    <td>
                      <div className="btn-group">
                        <button className="btn btn-sm btn-outline" onClick={() => setModalDetalle(c)}>Detalle</button>
                        <button className="btn btn-sm btn-primary" onClick={() => setModalAsistencia(c)}>Registrar asistencia</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </Panel>

      {/* Historial */}
      <Panel titulo="📚 Historial de capacitaciones">
        {cargando ? <div className="placeholder">Cargando...</div>
        : historial.length === 0 ? <div className="placeholder">Sin capacitaciones realizadas aún.</div>
        : (
          <table className="app-table">
            <thead>
              <tr><th>Fecha</th><th>Cliente</th><th>Tema</th><th>Asistentes</th><th>Acción</th></tr>
            </thead>
            <tbody>
              {historial.map(c => (
                <tr key={c.id}>
                  <td>{fmtFecha(c.fechaRealizacion ?? c.fechaProgramada)}</td>
                  <td>{c.cliente}</td>
                  <td style={{ fontWeight: 600 }}>{c.curso}</td>
                  <td>{c.asistencias.filter(a => a.asistio).length}/{c.asistencias.length}</td>
                  <td><button className="btn btn-sm btn-outline" onClick={() => setModalActa(c)}>Ver acta</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      {/* Modal Detalle capacitación — wireframe profesional p.3 */}
      <Modal abierto={!!modalDetalle} titulo="Detalle de capacitación" ancho="sm" onCerrar={() => setModalDetalle(null)}
        footer={<button className="btn btn-outline" onClick={() => setModalDetalle(null)}>Cerrar</button>}>
        {modalDetalle && (
          <div style={{ fontSize: 13, lineHeight: 1.9 }}>
            <p><strong>Cliente:</strong> {modalDetalle.cliente}</p>
            <p><strong>Tema:</strong> {modalDetalle.curso}</p>
            <p><strong>Fecha:</strong> {fmtFecha(modalDetalle.fechaProgramada)} · {modalDetalle.horaProgramada}</p>
            <p><strong>Asistentes confirmados:</strong> {modalDetalle.asistencias.filter(a => a.confirmado).length}/{modalDetalle.cupos} — {modalDetalle.asistencias.filter(a => a.confirmado).length === 0 ? 'pendiente confirmación del cliente.' : 'confirmados.'}</p>
            {modalDetalle.objetivo && (
              <div style={{ marginTop: 10, background: '#f8fafc', borderLeft: '3px solid #cbd5e1', padding: '8px 12px', borderRadius: 6, fontSize: 12 }}>
                <strong>Objetivo:</strong> {modalDetalle.objetivo}
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Modal Registrar asistencia — wireframe profesional p.3 */}
      <Modal abierto={!!modalAsistencia} titulo="Registrar asistencia" ancho="lg" onCerrar={() => setModalAsistencia(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalAsistencia(null)}>Cancelar</button><button className="btn btn-success" onClick={() => { alert('Asistencia registrada correctamente.'); setModalAsistencia(null); }}>Cerrar asistencia</button></>}>
        {modalAsistencia && (
          <>
            <div style={{ fontSize: 12, color: '#6b7280', marginBottom: 14 }}>
              <strong>{modalAsistencia.curso}</strong> · {modalAsistencia.cliente} · {fmtFecha(modalAsistencia.fechaProgramada)}
            </div>
            {modalAsistencia.asistencias.length === 0 ? (
              <div className="alert-item alert-item--warn">⚠️ No hay asistentes inscritos en esta capacitación.</div>
            ) : (
              <table className="app-table">
                <thead><tr><th>Trabajador</th><th>Cargo</th><th>Presente</th></tr></thead>
                <tbody>
                  {modalAsistencia.asistencias.map((a: AsistenciaResponse) => (
                    <tr key={a.idAsistencia}>
                      <td style={{ fontWeight: 600 }}>{a.nombreAsistente}</td>
                      <td>{a.cargoAsistente ?? '—'}</td>
                      <td>
                        <Badge variante={a.asistio ? 'green' : 'gray'}>{a.asistio ? 'Presente' : 'Ausente'}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            {/* Firma capacitador */}
            <div style={{ marginTop: 16, padding: '12px 16px', background: '#f8fafc', borderRadius: 8, border: '1px dashed #cbd5e1', textAlign: 'center', cursor: 'pointer', fontSize: 12, color: '#6b7280' }}
              onClick={() => alert('Firma digital registrada.')}>
              ✍️ Haz clic para firmar digitalmente como capacitador
            </div>
            <div style={{ marginTop: 12 }}>
              <label className="auth-label">Observaciones</label>
              <textarea className="auth-input" rows={3} placeholder="Capacitación dictada según contenido planificado." />
            </div>
          </>
        )}
      </Modal>

      {/* Modal Acta */}
      <Modal abierto={!!modalActa} titulo="Acta de capacitación" ancho="sm" onCerrar={() => setModalActa(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalActa(null)}>Cerrar</button><button className="btn btn-primary" onClick={() => alert('Descargando acta...')}>Descargar acta</button></>}>
        {modalActa && (
          <div style={{ fontSize: 13, lineHeight: 1.9 }}>
            <p><strong>Cliente:</strong> {modalActa.cliente}</p>
            <p><strong>Fecha:</strong> {fmtFecha(modalActa.fechaRealizacion ?? modalActa.fechaProgramada)}</p>
            <p><strong>Asistencia:</strong> {modalActa.asistencias.filter(a => a.asistio).length}/{modalActa.asistencias.length}</p>
            <div style={{ marginTop: 10, background: '#f0fdf4', borderLeft: '4px solid #27ae60', padding: '8px 12px', borderRadius: 6, fontSize: 12 }}>
              Resultado: Capacitación dictada correctamente.
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}

// ══════════════════════════════════════════════════════════════════════════════
//  VISTA CLIENTE — confirmar asistentes, ver actas, descargar certificados
//  Wireframe: PDF clientes p.3
// ══════════════════════════════════════════════════════════════════════════════

const FORM_VACIO: Omit<AsistenteRequest, 'idCliente'> = {
  rut: '', nombre: '', apellidos: '', cargo: '', area: '', email: '',
};

function generarRutTecnico(): string {
  return `TMP${Date.now().toString().slice(-9)}`;
}

function separarNombreCompleto(nombreCompleto: string) {
  const partes = nombreCompleto.trim().split(/\s+/);

  return {
    nombre: partes[0] ?? '',
    apellidos: partes.slice(1).join(' ') || '-',
  };
}


function VistaCliente() {
  const { email } = useAuth();
  
  // ── Estado general ──────────────────────────────────────────────────────────
  const [idCliente,      setIdCliente]      = useState<number | null>(null);
  const [capacitaciones, setCapacitaciones] = useState<CapacitacionResponse[]>([]);
  const [asistentes,     setAsistentes]     = useState<AsistenteResponse[]>([]);
  const [cargando,       setCargando]       = useState(true);
  const [tab,            setTab]            = useState<'capacitaciones' | 'trabajadores'>('capacitaciones');

  // ── Modales capacitaciones ──────────────────────────────────────────────────
  const [modalDetalle,   setModalDetalle]   = useState<CapacitacionResponse | null>(null);
  const [modalConfirmar, setModalConfirmar] = useState<CapacitacionResponse | null>(null);
  const [modalActa,      setModalActa]      = useState<CapacitacionResponse | null>(null);
  const [modalSolicitar, setModalSolicitar] = useState(false);
  const [modalInscribir, setModalInscribir] = useState<CapacitacionResponse | null>(null);
  const [firmado,        setFirmado]        = useState(false);
  const [observacion,    setObservacion]    = useState('');
  const [confirmandoId,  setConfirmandoId]  = useState<number | null>(null);
  const [inscribiendo,   setInscribiendo]   = useState(false);
  const [seleccionados,  setSeleccionados]  = useState<number[]>([]);
  const [errorModal,     setErrorModal]     = useState<string | null>(null);

  // ── Modales asistentes ──────────────────────────────────────────────────────
  const [modalAsistente,   setModalAsistente]   = useState(false);
  const [asistenteEditar,  setAsistenteEditar]  = useState<AsistenteResponse | null>(null);
  const [modalEliminar,    setModalEliminar]     = useState<AsistenteResponse | null>(null);
  const [formAsistente,    setFormAsistente]     = useState(FORM_VACIO);
  const [guardandoAsis,    setGuardandoAsis]     = useState(false);
  const [errorAsis,        setErrorAsis]         = useState<string | null>(null);
  const [modalAgregarRapido, setModalAgregarRapido] = useState(false);
  const [nombreRapido,     setNombreRapido]      = useState('');
  const [guardandoRapido,  setGuardandoRapido]   = useState(false);
  const [errorRapido,      setErrorRapido]       = useState<string | null>(null);

  // ── Carga inicial ───────────────────────────────────────────────────────────
  const cargarCapacitaciones = useCallback(async () => {
    const d = await listarCapacitaciones(0, 200);
    setCapacitaciones(d.content);
  }, []);

  const cargarAsistentes = useCallback(async (id: number) => {
    const data = await listarAsistentesPorCliente(id);
    setAsistentes(data);
  }, []);

  useEffect(() => {
    async function init() {
      setCargando(true);
      try {
        const cliente = await miCliente();
        setIdCliente(cliente.id);
        await Promise.all([cargarCapacitaciones(), cargarAsistentes(cliente.id)]);
      } catch { /* error silencioso */ }
      finally { setCargando(false); }
    }
    init();
  }, [email, cargarCapacitaciones, cargarAsistentes]);

  // ── KPIs ────────────────────────────────────────────────────────────────────
  const programadas  = capacitaciones.filter(c => c.estado === 'PROGRAMADA');
  const historial    = capacitaciones.filter(c => c.estado === 'REALIZADA');
  const porConfirmar = programadas.filter(c => c.asistencias.filter(a => a.confirmado).length === 0).length;
  const certificados = historial.reduce((acc, c) => acc + c.asistencias.filter(a => a.confirmado).length, 0);

  // ── Confirmar asistencia ────────────────────────────────────────────────────
  async function onConfirmar(idCap: number, idAsistente: number) {
    if (!firmado) { alert('Antes de confirmar, firma digitalmente.'); return; }
    setConfirmandoId(idAsistente);
    try {
      await confirmarAsistencia(idCap, idAsistente, {
        observaciones: observacion || undefined,
        firmaDigital: `Firmado por ${email}`,
      });
      await cargarCapacitaciones();
      const fresca = (await listarCapacitaciones(0, 200)).content.find(c => c.id === idCap);
      if (fresca) setModalConfirmar(fresca);
    } catch (e) { alert(mensajeError(e, 'No se pudo confirmar.')); }
    finally { setConfirmandoId(null); }
  }



  async function onAgregarRapido(){
    if (!modalConfirmar || !idCliente) return;

    const nombreCompleto = nombreRapido.trim();

    if (!nombreCompleto) {
      setErrorRapido('Ingresa el nombre y apellido del asistente.');
      return;
    }

    const partesNombre = separarNombreCompleto(nombreCompleto);

    setGuardandoRapido(true);
    setErrorRapido(null);

    try{
      const nuevo = await crearAsistente({
        idCliente, 
        rut: generarRutTecnico(),
        nombre: partesNombre.nombre,
        apellidos: partesNombre.apellidos,
      });

      await inscribirAsistentes(modalConfirmar.id, {
        idsAsistentes: [nuevo.id],
      });

      await cargarAsistentes(idCliente);

      const actualizadas = (await listarCapacitaciones(0, 200)).content;
      setCapacitaciones(actualizadas);

      const capacitacionActualizada = actualizadas.find(c => c.id === modalConfirmar.id);
      if (capacitacionActualizada) {
        setModalConfirmar(capacitacionActualizada);
      }

      setModalAgregarRapido(false);
    } catch (e) {
      setErrorRapido(mensajeError(e, 'No se pudo agregar el asistente.'));
    } finally {
      setGuardandoRapido(false);
    }
  }

  // ── Inscribir asistentes ────────────────────────────────────────────────────
  async function onInscribir() {
    if (!modalInscribir || seleccionados.length === 0) return;
    setInscribiendo(true); setErrorModal(null);
    try {
      await inscribirAsistentes(modalInscribir.id, { idsAsistentes: seleccionados });
      await cargarCapacitaciones();
      setModalInscribir(null);
      setSeleccionados([]);
    } catch (e) { setErrorModal(mensajeError(e, 'Error al inscribir asistentes.')); }
    finally { setInscribiendo(false); }
  }

  function toggleSeleccionado(id: number) {
    setSeleccionados(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  }

  // ── CRUD asistentes ─────────────────────────────────────────────────────────
  function abrirCrear() {
    setAsistenteEditar(null);
    setFormAsistente(FORM_VACIO);
    setErrorAsis(null);
    setModalAsistente(true);
  }

  function abrirEditar(a: AsistenteResponse) {
    setAsistenteEditar(a);
    setFormAsistente({ rut: a.rut, nombre: a.nombre, apellidos: a.apellidos, cargo: a.cargo ?? '', area: a.area ?? '', email: a.email ?? '' });
    setErrorAsis(null);
    setModalAsistente(true);
  }



  async function onGuardarAsistente() {
    if (!idCliente) return;
    if (!formAsistente.rut || !formAsistente.nombre || !formAsistente.apellidos) {
      setErrorAsis('RUT, nombre y apellidos son obligatorios.'); return;
    }
    setGuardandoAsis(true); setErrorAsis(null);
    try {
      const payload: AsistenteRequest = { idCliente, ...formAsistente };
      if (asistenteEditar) {
        await editarAsistente(asistenteEditar.id, payload);
      } else {
        await crearAsistente(payload);
      }
      await cargarAsistentes(idCliente);
      setModalAsistente(false);
    } catch (e) { setErrorAsis(mensajeError(e, 'Error al guardar el trabajador.')); }
    finally { setGuardandoAsis(false); }
  }

  async function onEliminarAsistente() {
    if (!modalEliminar || !idCliente) return;
    try {
      await eliminarAsistente(modalEliminar.id);
      await cargarAsistentes(idCliente);
      setModalEliminar(null);
    } catch (e) { alert(mensajeError(e, 'No se pudo eliminar el trabajador.')); }
  }

  // ── Render ──────────────────────────────────────────────────────────────────
  if (cargando) return <div className="placeholder" style={{ marginTop: 40 }}>Cargando tu información...</div>;

  return (
    <>
      <div className="page-title">Capacitaciones</div>
      <div className="page-subtitle">Revisa capacitaciones, confirma asistentes y gestiona tus trabajadores</div>

      <div className="kpi-row">
        <KpiCard label="Por confirmar"    value={porConfirmar}    variante="warn" sub={porConfirmar > 0 ? `Cap. del ${fmtFecha(programadas[0]?.fechaProgramada)}` : undefined} />
        <KpiCard label="Realizadas"       value={historial.length} variante="ok" sub="Durante 2026" />
        <KpiCard label="Asistencia prom." value="89%"              sub="Últimas capacitaciones" />
        <KpiCard label="Certificados"     value={certificados}     sub="Emitidos a trabajadores" />
      </div>

     
      {/* ── TAB CAPACITACIONES ── */}
      {tab === 'capacitaciones' && (
        <>
          <Panel
            titulo="🎓 Capacitaciones programadas"
            accion={<button className="btn btn-sm btn-outline" onClick={() => setModalSolicitar(true)}>Solicitar capacitación extra</button>}
          >
            {programadas.length === 0
              ? <div className="placeholder">No tienes capacitaciones programadas próximamente.</div>
              : (
                <table className="app-table">
                  <thead><tr><th>Fecha</th><th>Tema</th><th>Lugar</th><th>Capacitador</th><th>Estado</th><th>Acción</th></tr></thead>
                  <tbody>
                    {programadas.map(c => {
                      const confirmados = c.asistencias.filter(a => a.confirmado).length;
                      const yaInscritos = c.asistencias.map(a => a.rutAsistente);
                      const hayNuevos   = asistentes.some(a => !yaInscritos.includes(a.rut));
                      return (
                        <tr key={c.id}>
                          <td><div style={{ fontWeight: 600 }}>{fmtFecha(c.fechaProgramada)}</div><div style={{ fontSize: 11, color: '#9ca3af' }}>{c.horaProgramada}</div></td>
                          <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{c.curso}</td>
                          <td>Por confirmar</td>
                          <td>{c.relator}</td>
                          <td>
                            <Badge variante={confirmados === 0 ? 'yellow' : 'blue'}>
                              {confirmados === 0 ? 'Requiere confirmar' : `${confirmados}/${c.cupos} confirmados`}
                            </Badge>
                          </td>
                          <td>
                            <div className="btn-group">
                              <button className="btn btn-sm btn-outline" onClick={() => setModalDetalle(c)}>Detalle</button>
                              {hayNuevos && (
                                <button className="btn btn-sm btn-outline" onClick={() => { setSeleccionados([]); setErrorModal(null); setModalInscribir(c); }}>+ Inscribir</button>
                              )}
                              <button className="btn btn-sm btn-warn" onClick={() => { setFirmado(false); setObservacion(''); setModalConfirmar(c); }}>Confirmar</button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
          </Panel>

          <Panel titulo="📚 Historial de capacitaciones">
            {historial.length === 0
              ? <div className="placeholder">Sin capacitaciones realizadas aún.</div>
              : (
                <table className="app-table">
                  <thead><tr><th>Fecha</th><th>Tema</th><th>Asistencia</th><th>Documento</th><th>Acción</th></tr></thead>
                  <tbody>
                    {historial.map(c => {
                      const confirmadosHist = c.asistencias.filter(a => a.confirmado).length;
                      return (
                        <tr key={c.id}>
                          <td>{fmtFecha(c.fechaRealizacion ?? c.fechaProgramada)}</td>
                          <td style={{ fontWeight: 600 }}>{c.curso}</td>
                          <td><Badge variante="green">{c.asistencias.filter(a => a.asistio).length}/{c.asistencias.length} asistentes</Badge></td>
                          <td>{confirmadosHist > 0 ? 'Acta + certificados' : 'Acta'}</td>
                          <td>
                            <div className="btn-group">
                              <button className="btn btn-sm btn-outline" onClick={() => setModalActa(c)}>Ver acta</button>
                              {confirmadosHist > 0 && <button className="btn btn-sm btn-success" onClick={() => alert('Descargando certificados...')}>Certificados</button>}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
          </Panel>
        </>
      )}

      {/* ── TAB TRABAJADORES ── */}
      {tab === 'trabajadores' && (
        <Panel
          titulo="👷 Mis trabajadores"
          accion={<button className="btn btn-sm btn-primary" onClick={abrirCrear}>+ Agregar trabajador</button>}
        >
          {asistentes.length === 0
            ? <div className="placeholder">No tienes trabajadores registrados aún.<br />Agrega uno para poder inscribirlos en capacitaciones.</div>
            : (
              <table className="app-table">
                <thead><tr><th>Nombre</th><th>RUT</th><th>Cargo</th><th>Área</th><th>Email</th><th>Acción</th></tr></thead>
                <tbody>
                  {asistentes.map(a => (
                    <tr key={a.id}>
                      <td style={{ fontWeight: 600 }}>{a.nombreCompleto}</td>
                      <td>{a.rut}</td>
                      <td>{a.cargo ?? '—'}</td>
                      <td>{a.area ?? '—'}</td>
                      <td>{a.email ?? '—'}</td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-sm btn-outline" onClick={() => abrirEditar(a)}>Editar</button>
                          <button className="btn btn-sm btn-danger"  onClick={() => setModalEliminar(a)}>Eliminar</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
        </Panel>
      )}

      {/* ── Modal Detalle ── */}
      <Modal abierto={!!modalDetalle} titulo="Detalle de capacitación" ancho="sm" onCerrar={() => setModalDetalle(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalDetalle(null)}>Cerrar</button>{modalDetalle?.estado === 'PROGRAMADA' && <button className="btn btn-warn" onClick={() => { setFirmado(false); setObservacion(''); setModalConfirmar(modalDetalle); setModalDetalle(null); }}>Confirmar asistentes</button>}</>}>
        {modalDetalle && (
          <div className="doc-preview">
            <p><strong>Tema:</strong> {modalDetalle.curso}</p>
            <p><strong>Fecha:</strong> {fmtFecha(modalDetalle.fechaProgramada)} · {modalDetalle.horaProgramada}</p>
            <p><strong>Capacitador:</strong> {modalDetalle.relator}</p>
            {modalDetalle.objetivo && <p><strong>Objetivo:</strong> {modalDetalle.objetivo}</p>}
            <p><strong>Requisitos:</strong> sala habilitada, listado de asistentes y disponibilidad en turno.</p>
          </div>
        )}
      </Modal>

      {/* ── Modal Inscribir asistentes ── */}
      <Modal abierto={!!modalInscribir} titulo={`Inscribir trabajadores — ${modalInscribir?.curso ?? ''}`} ancho="lg" onCerrar={() => setModalInscribir(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalInscribir(null)}>Cancelar</button><button className="btn btn-primary" onClick={onInscribir} disabled={inscribiendo || seleccionados.length === 0}>{inscribiendo ? 'Inscribiendo...' : `Inscribir ${seleccionados.length > 0 ? `(${seleccionados.length})` : ''}`}</button></>}>
        {modalInscribir && (
          <>
            <div className="info-box" style={{ marginBottom: 12 }}>
              Selecciona los trabajadores que inscribirás en esta capacitación. Solo se muestran los que aún no están inscritos.
            </div>
            {(() => {
              const yaInscritos = new Set(modalInscribir.asistencias.map(a => a.rutAsistente));
              const disponibles = asistentes.filter(a => !yaInscritos.has(a.rut));
              return disponibles.length === 0
                ? <div className="placeholder">Todos tus trabajadores ya están inscritos en esta capacitación.</div>
                : (
                  <div className="check-list">
                    {disponibles.map(a => (
                      <div className="check-row" key={a.id}>
                        <div className="left">
                          <input type="checkbox" checked={seleccionados.includes(a.id)} onChange={() => toggleSeleccionado(a.id)} />
                          <div>
                            <b>{a.nombreCompleto}</b><br />
                            <span style={{ fontSize: 11, color: '#7a8795' }}>{a.rut}{a.cargo ? ` · ${a.cargo}` : ''}</span>
                          </div>
                        </div>
                        <Badge variante="gray">Disponible</Badge>
                      </div>
                    ))}
                  </div>
                );
            })()}
            {errorModal && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{errorModal}</div>}
          </>
        )}
      </Modal>

      {/* ── Modal Confirmar asistentes ── */}
      <Modal
        abierto={!!modalConfirmar}
        titulo={`Confirmar asistentes — ${modalConfirmar?.curso ?? ''}`}
        ancho="lg"
        onCerrar={() => setModalConfirmar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalConfirmar(null)}>
              Cancelar
            </button>
            <button className="btn btn-success" onClick={() => setModalConfirmar(null)}>
              Confirmar asistencia
            </button>
          </>
        }
      >
        {modalConfirmar && (
          <>
            <div className="info-box">
              Confirma la asistencia de cada trabajador. Firma digitalmente antes de confirmar.
            </div>

            <div className="form-grid" style={{ marginBottom: 14 }}>
              <div className="form-group">
                <label className="auth-label">Fecha</label>
                <input
                  className="auth-input"
                  value={`${fmtFecha(modalConfirmar.fechaProgramada)} · ${modalConfirmar.horaProgramada}`}
                  readOnly
                />
              </div>

              <div className="form-group">
                <label className="auth-label">Lugar</label>
                <input className="auth-input" value="Por confirmar" readOnly />
              </div>
            </div>

            <div className="form-group span2" style={{ marginBottom: 14 }}>
              <label className="auth-label">Agregar trabajador</label>

              <div style={{ display: 'flex', gap: 8 }}>
                <input
                  className="auth-input"
                  placeholder="Ej: Maria Soto Rojas"
                  value={nombreRapido}
                  onChange={e => setNombreRapido(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      onAgregarRapido();
                    }
                  }}
                />

                <button
                  className="btn btn-primary"
                  onClick={onAgregarRapido}
                  disabled={guardandoRapido || !nombreRapido.trim()}
                >
                  {guardandoRapido ? 'Agregando...' : 'Agregar'}
                </button>
              </div>

              {errorRapido && (
                <div className="auth-alert auth-alert--error" style={{ marginTop: 8 }}>
                  {errorRapido}
                </div>
              )}
            </div>
            {modalConfirmar.asistencias.length === 0 ? (
              <div className="alert-item alert-item--warn">
                No hay trabajadores inscritos. Usa el botón Agregar para sumar asistentes.
              </div>
            ) : (
              <div className="check-list">
                {modalConfirmar.asistencias.map((a: AsistenciaResponse) => (
                  <div className="check-row" key={a.idAsistencia}>
                    <div className="left">
                      <input type="checkbox" checked={a.confirmado} readOnly />
                      <div>
                        <b>{a.nombreAsistente}</b>
                        {a.cargoAsistente && (
                          <>
                            <br />
                            <span style={{ fontSize: 11, color: '#7a8795' }}>
                              {a.cargoAsistente}
                            </span>
                          </>
                        )}
                      </div>
                    </div>

                    {a.confirmado ? (
                      <Badge variante="green">Confirmado</Badge>
                    ) : (
                      <button
                        className="btn btn-sm btn-primary"
                        disabled={confirmandoId === a.idAsistente}
                        onClick={() => onConfirmar(modalConfirmar.id, a.idAsistente)}
                      >
                        {confirmandoId === a.idAsistente ? 'Confirmando...' : 'Confirmar'}
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}

            <div className="form-grid" style={{ marginTop: 14 }}>
              <div className="form-group span2">
                <label className="auth-label">Firma digital del contacto</label>
                <div className={`signature ${firmado ? 'signed' : ''}`} onClick={() => setFirmado(true)}>
                  {firmado ? `Firma digital registrada por ${email}` : 'Haz clic aquí para firmar digitalmente'}
                </div>
              </div>

              <div className="form-group span2">
                <label className="auth-label">Observación</label>
                <textarea
                  className="auth-input"
                  rows={2}
                  placeholder="Observaciones opcionales..."
                  value={observacion}
                  onChange={e => setObservacion(e.target.value)}
                />
              </div>
            </div>
          </>
        )}
      </Modal>

      {/* ── Modal Agregar asistente rápido ── */}
      <Modal
        abierto={modalAgregarRapido}
        titulo="Agregar asistente"
        ancho="sm"
        onCerrar={() => setModalAgregarRapido(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setNombreRapido('')}>
              Cancelar
            </button>
            <button className="btn btn-primary" onClick={onAgregarRapido} disabled={guardandoRapido}>
              {guardandoRapido ? 'Agregando...' : 'Agregar'}
            </button>
          </>
        }
      >
        <div className="form-group">
          <label className="auth-label">Nombre y apellido *</label>
          <input
            className="auth-input"
            placeholder="Ej: Maria Soto Rojas"
            value={nombreRapido}
            onChange={e => setNombreRapido(e.target.value)}
          />
        </div>

        {errorRapido && (
          <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>
            {errorRapido}
          </div>
        )}
      </Modal>

      {/* ── Modal Acta ── */}
      <Modal abierto={!!modalActa} titulo="Acta de capacitación" ancho="lg" onCerrar={() => setModalActa(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalActa(null)}>Cerrar</button><button className="btn btn-success" onClick={() => alert('Descargando acta...')}>Descargar acta</button></>}>
        {modalActa && (
          <div className="doc-preview">
            <h3 style={{ color: '#1a3a5c', marginBottom: 8 }}>Acta — {modalActa.curso}</h3>
            <p><strong>Fecha:</strong> {fmtFecha(modalActa.fechaRealizacion ?? modalActa.fechaProgramada)}</p>
            <p><strong>Asistencia:</strong> {modalActa.asistencias.filter(a => a.asistio).length}/{modalActa.asistencias.length} trabajadores</p>
            <p><strong>Resultado:</strong> Capacitación realizada correctamente. Certificados emitidos para asistentes confirmados.</p>
            {modalActa.asistencias.some(a => !a.asistio) && (
              <p><strong>Observaciones:</strong> {modalActa.asistencias.filter(a => !a.asistio).length} trabajador(es) ausente(s).</p>
            )}
          </div>
        )}
      </Modal>

      {/* ── Modal Solicitar extra ── */}
      <Modal abierto={modalSolicitar} titulo="Solicitar capacitación extra" onCerrar={() => setModalSolicitar(false)}
        footer={<><button className="btn btn-outline" onClick={() => setModalSolicitar(false)}>Cancelar</button><button className="btn btn-primary" onClick={() => { alert('Solicitud enviada correctamente.'); setModalSolicitar(false); }}>Enviar solicitud</button></>}>
        <div className="warning-box">Esta solicitud puede generar cobro adicional si supera lo incluido en el plan.</div>
        <div className="form-grid">
          <div className="form-group"><label className="auth-label">Tema</label><input className="auth-input" placeholder="Ej: Trabajo en altura" /></div>
          <div className="form-group"><label className="auth-label">Fecha tentativa</label><input type="date" className="auth-input" /></div>
          <div className="form-group"><label className="auth-label">Cantidad estimada</label><input type="number" className="auth-input" placeholder="15" /></div>
          <div className="form-group"><label className="auth-label">Modalidad</label><select className="auth-input"><option>Presencial</option><option>Online</option><option>Híbrido</option></select></div>
          <div className="form-group span2"><label className="auth-label">Motivo</label><textarea className="auth-input" rows={2} placeholder="Justificación de la solicitud..." /></div>
        </div>
      </Modal>

      {/* ── Modal Agregar/Editar trabajador ── */}
      <Modal abierto={modalAsistente} titulo={asistenteEditar ? 'Editar trabajador' : 'Agregar trabajador'} onCerrar={() => setModalAsistente(false)}
        footer={<><button className="btn btn-outline" onClick={() => setModalAsistente(false)}>Cancelar</button><button className="btn btn-primary" onClick={onGuardarAsistente} disabled={guardandoAsis}>{guardandoAsis ? 'Guardando...' : 'Guardar'}</button></>}>
        <div className="form-grid">
          <div className="form-group">
            <label className="auth-label">RUT *</label>
            <input className="auth-input" placeholder="12345678-9" value={formAsistente.rut} onChange={e => setFormAsistente(f => ({ ...f, rut: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="auth-label">Nombre *</label>
            <input className="auth-input" placeholder="María" value={formAsistente.nombre} onChange={e => setFormAsistente(f => ({ ...f, nombre: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="auth-label">Apellidos *</label>
            <input className="auth-input" placeholder="Soto Rojas" value={formAsistente.apellidos} onChange={e => setFormAsistente(f => ({ ...f, apellidos: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="auth-label">Cargo</label>
            <input className="auth-input" placeholder="Operaria de bodega" value={formAsistente.cargo ?? ''} onChange={e => setFormAsistente(f => ({ ...f, cargo: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="auth-label">Área</label>
            <input className="auth-input" placeholder="Bodega" value={formAsistente.area ?? ''} onChange={e => setFormAsistente(f => ({ ...f, area: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="auth-label">Email</label>
            <input type="email" className="auth-input" placeholder="trabajador@empresa.cl" value={formAsistente.email ?? ''} onChange={e => setFormAsistente(f => ({ ...f, email: e.target.value }))} />
          </div>
        </div>
        {errorAsis && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{errorAsis}</div>}
      </Modal>

      {/* ── Modal Eliminar trabajador ── */}
      <Modal abierto={!!modalEliminar} titulo="Eliminar trabajador" ancho="sm" onCerrar={() => setModalEliminar(null)}
        footer={<><button className="btn btn-outline" onClick={() => setModalEliminar(null)}>Cancelar</button><button className="btn btn-danger" onClick={onEliminarAsistente}>Eliminar</button></>}>
        <div className="danger-box">
          ¿Eliminar a <strong>{modalEliminar?.nombreCompleto}</strong> ({modalEliminar?.rut})? Esta acción no se puede deshacer.
        </div>
      </Modal>
    </>
  );
}

// ══════════════════════════════════════════════════════════════════════════════
//  COMPONENTE RAÍZ — enrutamiento por rol
// ══════════════════════════════════════════════════════════════════════════════

export default function Capacitaciones() {
  const { rol } = useAuth();
  console.log('rol actual:', rol); // ← agrega estoQ

  if (rol === 'PROFESIONAL' || rol === 'CAPACITADOR') return <VistaProfesional />;
  if (rol === 'CLIENTE') return <VistaCliente />;
  return <VistaAdmin />;
}

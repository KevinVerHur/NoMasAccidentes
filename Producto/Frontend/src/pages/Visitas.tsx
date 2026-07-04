import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';
import type {
  VisitaResponse, PlanificarVisitaRequest, RegistrarVisitaRequest,
  EstadoVisita, VarianteBadge, EmpresaResponse, ProfesionalResponse,
  ItemChequeoResponse, EstadoCumplimiento, ResultadoChequeoRequest,
} from '../types';
import {
  listarVisitas, planificarVisita, iniciarVisita, registrarVisita, cancelarVisita, eliminarVisita,
  obtenerListaChequeoEmpresa,
} from '../api/visitas';
import { listarClientes } from '../api/clientes';
import { listarProfesionales } from '../api/profesionales';
import { generarInforme, descargarInformePdf } from '../api/informes';

const badgePorEstado: Record<EstadoVisita, VarianteBadge> = {
  PROGRAMADA: 'blue',
  EN_CURSO:   'yellow',
  REALIZADA:  'green',
  CANCELADA:  'gray',
};

const labelEstado: Record<EstadoVisita, string> = {
  PROGRAMADA: 'Programada',
  EN_CURSO:   'En curso',
  REALIZADA:  'Realizada',
  CANCELADA:  'Cancelada',
};

const TIPOS_REVISION = ['MENSUAL', 'SEGUIMIENTO', 'EXTRAORDINARIA'];

function fmtFecha(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('es-CL', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function mensajeError(e: unknown, fallback: string): string {
  return (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;
}

export default function Visitas() {
  const { rol } = useAuth();
  const esAdmin = rol === 'ADMIN';

  const [visitas, setVisitas]           = useState<VisitaResponse[]>([]);
  const [clientes, setClientes]         = useState<EmpresaResponse[]>([]);
  const [profesionales, setProfesionales] = useState<ProfesionalResponse[]>([]);
  const [cargando, setCargando]         = useState(true);
  const [busqueda, setBusqueda]         = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [modalNueva, setModalNueva]     = useState(false);
  const [modalRegistrar, setModalRegistrar] = useState<VisitaResponse | null>(null);
  const [itemsChequeo, setItemsChequeo] = useState<ItemChequeoResponse[]>([]);
  const [marcas, setMarcas] = useState<Record<number, { estado: EstadoCumplimiento; observacion: string }>>({});
  const [cargandoChequeo, setCargandoChequeo] = useState(false);
  const [modalCancelar, setModalCancelar]   = useState<VisitaResponse | null>(null);
  const [modalEliminar, setModalEliminar]   = useState<VisitaResponse | null>(null);
  const [guardando, setGuardando]       = useState(false);
  const [generandoInforme, setGenerandoInforme] = useState<number | null>(null);
  const [error, setError]               = useState<string | null>(null);

  const formNueva    = useForm<PlanificarVisitaRequest>();
  const formRegistro = useForm<RegistrarVisitaRequest>();

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const data = await listarVisitas(0, 200);
      setVisitas(data.content);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  // Catálogos solo para el formulario de planificación (ADMIN).
  useEffect(() => {
    if (!esAdmin) return;
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
    listarProfesionales(0, 200).then(d => setProfesionales(d.content)).catch(() => {});
  }, [esAdmin]);

  const filtradas = visitas.filter(v => {
    const texto = busqueda.toLowerCase();
    const coincide = !texto
      || v.razonSocialEmpresa.toLowerCase().includes(texto)
      || v.nombreProfesional.toLowerCase().includes(texto);
    const estado = !filtroEstado || v.estado === filtroEstado;
    return coincide && estado;
  });

  const programadas = visitas.filter(v => v.estado === 'PROGRAMADA').length;
  const enCurso     = visitas.filter(v => v.estado === 'EN_CURSO').length;
  const realizadas  = visitas.filter(v => v.estado === 'REALIZADA').length;

  async function onPlanificar(data: PlanificarVisitaRequest) {
    setError(null);
    setGuardando(true);
    try {
      await planificarVisita(data);
      setModalNueva(false);
      formNueva.reset();
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al planificar la visita.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onIniciar(id: number) {
    setError(null);
    try {
      await iniciarVisita(id);
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al iniciar la visita.'));
    }
  }

  function abrirRegistrar(v: VisitaResponse) {
    formRegistro.reset({ observaciones: v.observaciones ?? '', latitud: undefined, longitud: undefined });
    setError(null);
    setItemsChequeo([]);
    setMarcas({});
    setCargandoChequeo(true);
    setModalRegistrar(v);
    // Cargar la lista de chequeo del cliente para marcarla en terreno (RF19).
    obtenerListaChequeoEmpresa(v.idEmpresa)
      .then((lista) => {
        setItemsChequeo(lista.items);
        // Por defecto todo "Cumple"; el profesional marca las excepciones.
        const inicial: Record<number, { estado: EstadoCumplimiento; observacion: string }> = {};
        lista.items.forEach((it) => { inicial[it.id] = { estado: 'CUMPLE', observacion: '' }; });
        setMarcas(inicial);
      })
      .catch(() => setItemsChequeo([]))
      .finally(() => setCargandoChequeo(false));
    // Intentar capturar la geolocalización del dispositivo en terreno (RF05/RF14).
    navigator.geolocation?.getCurrentPosition(
      pos => {
        formRegistro.setValue('latitud', Number(pos.coords.latitude.toFixed(6)));
        formRegistro.setValue('longitud', Number(pos.coords.longitude.toFixed(6)));
      },
      () => {},
    );
  }

  function setMarca(idItem: number, campo: 'estado' | 'observacion', valor: string) {
    setMarcas((prev) => ({
      ...prev,
      [idItem]: {
        estado: campo === 'estado' ? (valor as EstadoCumplimiento) : (prev[idItem]?.estado ?? 'CUMPLE'),
        observacion: campo === 'observacion' ? valor : (prev[idItem]?.observacion ?? ''),
      },
    }));
  }

  async function onRegistrar(data: RegistrarVisitaRequest) {
    if (!modalRegistrar) return;
    setError(null);
    setGuardando(true);
    const resultados: ResultadoChequeoRequest[] = itemsChequeo.map((it) => ({
      idItem: it.id,
      estado: marcas[it.id]?.estado ?? 'CUMPLE',
      observacion: marcas[it.id]?.observacion?.trim() || undefined,
    }));
    try {
      await registrarVisita(modalRegistrar.id, { ...data, resultados });
      setModalRegistrar(null);
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al registrar la visita.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onInforme(v: VisitaResponse) {
    setError(null);
    setGenerandoInforme(v.id);
    try {
      const informe = await generarInforme(v.id);
      await descargarInformePdf(informe.id);
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al generar el informe.'));
    } finally {
      setGenerandoInforme(null);
    }
  }

  async function onCancelar() {
    if (!modalCancelar) return;
    setGuardando(true);
    try {
      await cancelarVisita(modalCancelar.id);
      setModalCancelar(null);
      await cargar();
    } finally {
      setGuardando(false);
    }
  }

  async function onEliminar() {
    if (!modalEliminar) return;
    setGuardando(true);
    try {
      await eliminarVisita(modalEliminar.id);
      setModalEliminar(null);
      await cargar();
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Visitas</div>
      <div className="page-subtitle">
        {esAdmin ? 'Planificación y seguimiento de visitas en terreno' : 'Tus visitas asignadas en terreno'} — {visitas.length} registros
      </div>

      <div className="kpi-row">
        <KpiCard label="Programadas" value={programadas} variante="warn" />
        <KpiCard label="En curso"    value={enCurso} />
        <KpiCard label="Realizadas"  value={realizadas} variante="ok" />
        <KpiCard label="Total"       value={visitas.length} />
      </div>

      <Panel
        titulo="📅 Listado de visitas"
        accion={esAdmin
          ? <button className="btn btn-sm btn-primary" onClick={() => { formNueva.reset(); setError(null); setModalNueva(true); }}>+ Planificar visita</button>
          : undefined}
      >
        <div className="searchbar">
          <input
            placeholder="Buscar por cliente o profesional..."
            value={busqueda}
            onChange={e => setBusqueda(e.target.value)}
          />
          <select value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="PROGRAMADA">Programada</option>
            <option value="EN_CURSO">En curso</option>
            <option value="REALIZADA">Realizada</option>
            <option value="CANCELADA">Cancelada</option>
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando visitas...</div>
        ) : filtradas.length === 0 ? (
          <div className="placeholder">No se encontraron visitas con los filtros aplicados.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Profesional</th>
                <th>Fecha programada</th>
                <th>Tipo</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map(v => (
                <tr key={v.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{v.razonSocialEmpresa}</div>
                    {v.esVisitaExtra && <div style={{ fontSize: 11, color: '#f0a500', fontWeight: 600 }}>Visita extra</div>}
                  </td>
                  <td>{v.nombreProfesional}</td>
                  <td>{fmtFecha(v.fechaProgramada)}</td>
                  <td><span style={{ fontSize: 11, fontWeight: 600 }}>{v.tipoRevision ?? '—'}</span></td>
                  <td><Badge variante={badgePorEstado[v.estado]}>{labelEstado[v.estado]}</Badge></td>
                  <td>
                    <div className="btn-group">
                      {v.estado === 'PROGRAMADA' && (
                        <button className="btn btn-sm btn-primary" onClick={() => onIniciar(v.id)}>Iniciar</button>
                      )}
                      {(v.estado === 'PROGRAMADA' || v.estado === 'EN_CURSO') && (
                        <button className="btn btn-sm btn-success" onClick={() => abrirRegistrar(v)}>Registrar</button>
                      )}
                      {v.estado === 'REALIZADA' && (
                        <button className="btn btn-sm btn-outline" disabled={generandoInforme === v.id} onClick={() => onInforme(v)}>
                          {generandoInforme === v.id ? 'Generando...' : '📄 Informe'}
                        </button>
                      )}
                      {esAdmin && v.estado !== 'REALIZADA' && v.estado !== 'CANCELADA' && (
                        <button className="btn btn-sm btn-warn" onClick={() => setModalCancelar(v)}>Cancelar</button>
                      )}
                      {esAdmin && (
                        <button className="btn btn-sm btn-danger" onClick={() => setModalEliminar(v)}>Eliminar</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      {/* Modal planificar visita (ADMIN) */}
      <Modal
        abierto={modalNueva}
        titulo="Planificar visita"
        onCerrar={() => setModalNueva(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNueva(false)}>Cancelar</button>
            <button className="btn btn-primary" form="form-visita-nueva" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Planificar'}
            </button>
          </>
        }
      >
        <form id="form-visita-nueva" onSubmit={formNueva.handleSubmit(onPlanificar)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Cliente *</label>
              <select className={`auth-input ${formNueva.formState.errors.idEmpresa ? 'auth-input--error' : ''}`}
                {...formNueva.register('idEmpresa', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
              </select>
              {formNueva.formState.errors.idEmpresa && <span className="auth-field-error">{formNueva.formState.errors.idEmpresa.message}</span>}
            </div>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Profesional *</label>
              <select className={`auth-input ${formNueva.formState.errors.idProfesional ? 'auth-input--error' : ''}`}
                {...formNueva.register('idProfesional', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {profesionales.map(p => <option key={p.id} value={p.id}>{p.nombreCompleto}</option>)}
              </select>
              {formNueva.formState.errors.idProfesional && <span className="auth-field-error">{formNueva.formState.errors.idProfesional.message}</span>}
            </div>
            <div>
              <label className="auth-label">Fecha programada *</label>
              <input type="date" className={`auth-input ${formNueva.formState.errors.fechaProgramada ? 'auth-input--error' : ''}`}
                {...formNueva.register('fechaProgramada', { required: 'Obligatorio' })}
              />
              {formNueva.formState.errors.fechaProgramada && <span className="auth-field-error">{formNueva.formState.errors.fechaProgramada.message}</span>}
            </div>
            <div>
              <label className="auth-label">Tipo de revisión</label>
              <select className="auth-input" {...formNueva.register('tipoRevision')}>
                <option value="">Seleccionar...</option>
                {TIPOS_REVISION.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div style={{ gridColumn: 'span 2', display: 'flex', alignItems: 'center', gap: 8 }}>
              <input type="checkbox" id="esVisitaExtra" {...formNueva.register('esVisitaExtra')} />
              <label htmlFor="esVisitaExtra" style={{ fontSize: 13 }}>Visita adicional (costo extra)</label>
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal registrar visita en terreno */}
      <Modal
        abierto={!!modalRegistrar}
        titulo="Registrar visita en terreno"
        onCerrar={() => setModalRegistrar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalRegistrar(null)}>Cancelar</button>
            <button className="btn btn-success" form="form-visita-registro" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Marcar como realizada'}
            </button>
          </>
        }
      >
        <form id="form-visita-registro" onSubmit={formRegistro.handleSubmit(onRegistrar)} noValidate>
          <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 12 }}>
            <strong>{modalRegistrar?.razonSocialEmpresa}</strong> · {fmtFecha(modalRegistrar?.fechaProgramada ?? null)}
          </div>

          <label className="auth-label">Lista de chequeo</label>
          {cargandoChequeo ? (
            <div style={{ fontSize: 12, color: '#9ca3af', marginBottom: 12 }}>Cargando lista de chequeo…</div>
          ) : itemsChequeo.length === 0 ? (
            <div style={{ fontSize: 12, color: '#9ca3af', marginBottom: 12 }}>
              Esta empresa no tiene ítems en su lista de chequeo. Puedes registrar la visita igual.
            </div>
          ) : (
            <div style={{ maxHeight: 260, overflowY: 'auto', border: '1px solid #e5e7eb', borderRadius: 8, padding: 8, marginBottom: 12 }}>
              {itemsChequeo.map((it) => {
                const marca = marcas[it.id]?.estado ?? 'CUMPLE';
                return (
                  <div key={it.id} style={{ padding: '8px 4px', borderBottom: '1px solid #f1f1f1' }}>
                    <div style={{ fontSize: 13, fontWeight: 500 }}>{it.descripcion}</div>
                    {it.normaLegal && (
                      <div style={{ fontSize: 11, color: '#9ca3af' }}>{it.normaLegal}</div>
                    )}
                    <div style={{ display: 'flex', gap: 8, marginTop: 6, alignItems: 'center' }}>
                      <select
                        className="auth-input"
                        style={{ maxWidth: 150, padding: '4px 8px' }}
                        value={marca}
                        onChange={(e) => setMarca(it.id, 'estado', e.target.value)}
                      >
                        <option value="CUMPLE">Cumple</option>
                        <option value="NO_CUMPLE">No cumple</option>
                        <option value="NO_APLICA">No aplica</option>
                      </select>
                      {marca === 'NO_CUMPLE' && (
                        <input
                          className="auth-input"
                          style={{ flex: 1, padding: '4px 8px' }}
                          placeholder="Observación / propuesta de mejora"
                          value={marcas[it.id]?.observacion ?? ''}
                          onChange={(e) => setMarca(it.id, 'observacion', e.target.value)}
                        />
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          <div>
            <label className="auth-label">Observaciones</label>
            <textarea className="auth-input" rows={4}
              placeholder="Hallazgos, condiciones observadas, acuerdos..."
              {...formRegistro.register('observaciones', { maxLength: { value: 2000, message: 'Máx. 2000 caracteres' } })}
            />
            {formRegistro.formState.errors.observaciones && <span className="auth-field-error">{formRegistro.formState.errors.observaciones.message}</span>}
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginTop: 12 }}>
            <div>
              <label className="auth-label">Latitud</label>
              <input className="auth-input" step="any" type="number" {...formRegistro.register('latitud', { valueAsNumber: true })} />
            </div>
            <div>
              <label className="auth-label">Longitud</label>
              <input className="auth-input" step="any" type="number" {...formRegistro.register('longitud', { valueAsNumber: true })} />
            </div>
          </div>
          <div style={{ fontSize: 11, color: '#9ca3af', marginTop: 6 }}>
            La ubicación se completa automáticamente si el dispositivo lo permite.
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal confirmar cancelación */}
      <Modal
        abierto={!!modalCancelar}
        titulo="Cancelar visita"
        ancho="sm"
        onCerrar={() => setModalCancelar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalCancelar(null)}>Volver</button>
            <button className="btn btn-danger" onClick={onCancelar} disabled={guardando}>
              {guardando ? 'Cancelando...' : 'Confirmar cancelación'}
            </button>
          </>
        }
      >
        <div style={{ background: '#fef2f2', borderLeft: '4px solid #c0392b', padding: 12, borderRadius: 8, fontSize: 13, lineHeight: 1.5 }}>
          ¿Cancelar la visita a <strong>{modalCancelar?.razonSocialEmpresa}</strong> del {fmtFecha(modalCancelar?.fechaProgramada ?? null)}?
        </div>
      </Modal>

      {/* Modal confirmar eliminación */}
      <Modal
        abierto={!!modalEliminar}
        titulo="Eliminar visita"
        ancho="sm"
        onCerrar={() => setModalEliminar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEliminar(null)}>Cancelar</button>
            <button className="btn btn-danger" onClick={onEliminar} disabled={guardando}>
              {guardando ? 'Eliminando...' : 'Confirmar eliminación'}
            </button>
          </>
        }
      >
        <div style={{ background: '#fef2f2', borderLeft: '4px solid #c0392b', padding: 12, borderRadius: 8, fontSize: 13, lineHeight: 1.5 }}>
          ¿Eliminar la visita a <strong>{modalEliminar?.razonSocialEmpresa}</strong>? Se mantiene el registro histórico (soft delete).
        </div>
      </Modal>
    </>
  );
}

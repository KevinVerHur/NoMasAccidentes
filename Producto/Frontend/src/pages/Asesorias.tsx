import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';
import type {
  AsesoriaResponse, CrearAsesoriaRequest,
  EstadoAsesoria, TipoAsesoria, VarianteBadge, EmpresaResponse, ProfesionalResponse,
} from '../types';
import {
  listarAsesorias, misAsesorias, crearAsesoria, atenderAsesoria, cerrarAsesoria, cancelarAsesoria,
} from '../api/asesorias';
import { listarClientes } from '../api/clientes';
import { listarProfesionales } from '../api/profesionales';

const badgePorEstado: Record<EstadoAsesoria, VarianteBadge> = {
  SOLICITADA: 'blue',
  EN_PROCESO: 'yellow',
  CERRADA:    'green',
  CANCELADA:  'gray',
};

const labelEstado: Record<EstadoAsesoria, string> = {
  SOLICITADA: 'Solicitada',
  EN_PROCESO: 'En proceso',
  CERRADA:    'Cerrada',
  CANCELADA:  'Cancelada',
};

const labelTipo: Record<TipoAsesoria, string> = {
  ACCIDENTE:     'Accidente',
  FISCALIZACION: 'Fiscalización',
};

function fmtFecha(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('es-CL', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function mensajeError(e: unknown, fallback: string): string {
  return (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;
}

export default function Asesorias() {
  const { rol } = useAuth();
  const navigate = useNavigate();
  const esAdmin = rol === 'ADMIN';

  const [asesorias, setAsesorias]       = useState<AsesoriaResponse[]>([]);
  const [clientes, setClientes]         = useState<EmpresaResponse[]>([]);
  const [profesionales, setProfesionales] = useState<ProfesionalResponse[]>([]);
  const [cargando, setCargando]         = useState(true);
  const [busqueda, setBusqueda]         = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [modalNueva, setModalNueva]     = useState(false);
  const [modalCancelar, setModalCancelar] = useState<AsesoriaResponse | null>(null);
  const [guardando, setGuardando]       = useState(false);
  const [accionando, setAccionando]     = useState<number | null>(null);
  const [error, setError]               = useState<string | null>(null);

  const formNueva = useForm<CrearAsesoriaRequest>();

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      // El ADMIN ve todas las asesorías; el profesional solo las asignadas a él.
      const data = esAdmin ? (await listarAsesorias(0, 200)).content : await misAsesorias();
      setAsesorias(data);
    } finally {
      setCargando(false);
    }
  }, [esAdmin]);

  useEffect(() => { cargar(); }, [cargar]);

  // Catálogos solo para el formulario de alta (ADMIN).
  useEffect(() => {
    if (!esAdmin) return;
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
    listarProfesionales(0, 200).then(d => setProfesionales(d.content)).catch(() => {});
  }, [esAdmin]);

  const filtradas = asesorias.filter(a => {
    const texto = busqueda.toLowerCase();
    const coincide = !texto
      || a.razonSocialEmpresa.toLowerCase().includes(texto)
      || a.nombreProfesional.toLowerCase().includes(texto)
      || a.motivo.toLowerCase().includes(texto);
    const estado = !filtroEstado || a.estado === filtroEstado;
    return coincide && estado;
  });

  const solicitadas = asesorias.filter(a => a.estado === 'SOLICITADA').length;
  const enProceso   = asesorias.filter(a => a.estado === 'EN_PROCESO').length;
  const cerradas    = asesorias.filter(a => a.estado === 'CERRADA').length;

  async function onCrear(data: CrearAsesoriaRequest) {
    setError(null);
    setGuardando(true);
    try {
      await crearAsesoria(data);
      setModalNueva(false);
      formNueva.reset();
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al registrar la asesoría.'));
    } finally {
      setGuardando(false);
    }
  }

  async function onAtender(id: number) {
    setError(null);
    setAccionando(id);
    try {
      await atenderAsesoria(id);
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al iniciar la atención.'));
    } finally {
      setAccionando(null);
    }
  }

  async function onCerrar(id: number) {
    setError(null);
    setAccionando(id);
    try {
      await cerrarAsesoria(id);
      await cargar();
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al cerrar la asesoría.'));
    } finally {
      setAccionando(null);
    }
  }

  async function onCancelar() {
    if (!modalCancelar) return;
    setGuardando(true);
    try {
      await cancelarAsesoria(modalCancelar.id);
      setModalCancelar(null);
      await cargar();
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Asesorías</div>
      <div className="page-subtitle">
        {esAdmin ? 'Registro y seguimiento de asesorías por accidentes o fiscalizaciones' : 'Asesorías asignadas para atención'} — {asesorias.length} registros
      </div>

      <div className="kpi-row">
        <KpiCard label="Solicitadas" value={solicitadas} variante="warn" />
        <KpiCard label="En proceso"  value={enProceso} />
        <KpiCard label="Cerradas"    value={cerradas} variante="ok" />
        <KpiCard label="Total"       value={asesorias.length} />
      </div>

      <Panel
        titulo="Listado de asesorías"
        accion={esAdmin
          ? <button className="btn btn-sm btn-primary" onClick={() => { formNueva.reset(); setError(null); setModalNueva(true); }}>+ Registrar asesoría</button>
          : undefined}
      >
        <div className="searchbar">
          <input
            placeholder="Buscar por cliente, profesional o motivo..."
            value={busqueda}
            onChange={e => setBusqueda(e.target.value)}
          />
          <select value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="SOLICITADA">Solicitada</option>
            <option value="EN_PROCESO">En proceso</option>
            <option value="CERRADA">Cerrada</option>
            <option value="CANCELADA">Cancelada</option>
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando asesorías...</div>
        ) : filtradas.length === 0 ? (
          <div className="placeholder">No se encontraron asesorías con los filtros aplicados.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Profesional</th>
                <th>Tipo</th>
                <th>Motivo</th>
                <th>Solicitada</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map(a => (
                <tr key={a.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{a.razonSocialEmpresa}</div>
                    {a.esAsesoriaExtra && <div style={{ fontSize: 11, color: '#f0a500', fontWeight: 600 }}>Asesoría extra</div>}
                  </td>
                  <td>{a.nombreProfesional}</td>
                  <td><span style={{ fontSize: 11, fontWeight: 600 }}>{labelTipo[a.tipo]}</span></td>
                  <td style={{ maxWidth: 280, whiteSpace: 'normal' }}>{a.motivo}</td>
                  <td>{fmtFecha(a.fechaSolicitud)}</td>
                  <td><Badge variante={badgePorEstado[a.estado]}>{labelEstado[a.estado]}</Badge></td>
                  <td>
                    <div className="btn-group">
                      {a.estado === 'SOLICITADA' && (
                        <button className="btn btn-sm btn-primary" disabled={accionando === a.id} onClick={() => onAtender(a.id)}>
                          {accionando === a.id ? 'Procesando...' : 'Registrar atención'}
                        </button>
                      )}
                      {a.estado === 'EN_PROCESO' && (
                        <button className="btn btn-sm btn-success" disabled={accionando === a.id} onClick={() => onCerrar(a.id)}>
                          {accionando === a.id ? 'Procesando...' : 'Cerrar'}
                        </button>
                      )}
                      <button className="btn btn-sm btn-outline" onClick={() => navigate(`/asesorias/${a.id}`)}>Ver detalle</button>
                      {esAdmin && a.estado !== 'CERRADA' && a.estado !== 'CANCELADA' && (
                        <button className="btn btn-sm btn-warn" onClick={() => setModalCancelar(a)}>Cancelar</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
      </Panel>

      {/* Modal registrar asesoría (ADMIN) */}
      <Modal
        abierto={modalNueva}
        titulo="Registrar asesoría"
        onCerrar={() => setModalNueva(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNueva(false)}>Cancelar</button>
            <button className="btn btn-primary" form="form-asesoria-nueva" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Registrar'}
            </button>
          </>
        }
      >
        <form id="form-asesoria-nueva" onSubmit={formNueva.handleSubmit(onCrear)} noValidate>
          <div className="responsive-form-grid">
            <div className="responsive-span-2">
              <label className="auth-label">Cliente *</label>
              <select className={`auth-input ${formNueva.formState.errors.idEmpresa ? 'auth-input--error' : ''}`}
                {...formNueva.register('idEmpresa', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
              </select>
              {formNueva.formState.errors.idEmpresa && <span className="auth-field-error">{formNueva.formState.errors.idEmpresa.message}</span>}
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Profesional *</label>
              <select className={`auth-input ${formNueva.formState.errors.idProfesional ? 'auth-input--error' : ''}`}
                {...formNueva.register('idProfesional', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {profesionales.map(p => <option key={p.id} value={p.id}>{p.nombreCompleto}</option>)}
              </select>
              {formNueva.formState.errors.idProfesional && <span className="auth-field-error">{formNueva.formState.errors.idProfesional.message}</span>}
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Tipo *</label>
              <select className={`auth-input ${formNueva.formState.errors.tipo ? 'auth-input--error' : ''}`}
                {...formNueva.register('tipo', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                <option value="ACCIDENTE">Accidente</option>
                <option value="FISCALIZACION">Fiscalización</option>
              </select>
              {formNueva.formState.errors.tipo && <span className="auth-field-error">{formNueva.formState.errors.tipo.message}</span>}
            </div>
            <div className="responsive-span-2">
              <label className="auth-label">Motivo *</label>
              <textarea className={`auth-input ${formNueva.formState.errors.motivo ? 'auth-input--error' : ''}`} rows={4}
                placeholder="Descripción del accidente o fiscalización que origina la asesoría..."
                {...formNueva.register('motivo', {
                  required: 'Obligatorio',
                  maxLength: { value: 500, message: 'Máx. 500 caracteres' },
                })}
              />
              {formNueva.formState.errors.motivo && <span className="auth-field-error">{formNueva.formState.errors.motivo.message}</span>}
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal confirmar cancelación */}
      <Modal
        abierto={!!modalCancelar}
        titulo="Cancelar asesoría"
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
          ¿Cancelar la asesoría de <strong>{modalCancelar?.razonSocialEmpresa}</strong>?
        </div>
      </Modal>
    </>
  );
}

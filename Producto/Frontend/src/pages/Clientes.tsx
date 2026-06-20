import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type { ClienteResponse, CrearClienteRequest, ActualizarClienteRequest, EstadoCliente, VarianteBadge } from '../types';
import { listarClientes, crearCliente, actualizarCliente, suspenderCliente, reactivarCliente, eliminarCliente } from '../api/clientes';

const badgePorEstado: Record<EstadoCliente, VarianteBadge> = {
  ACTIVO:     'green',
  MOROSO:     'red',
  SUSPENDIDO: 'gray',
};

const labelEstado: Record<EstadoCliente, string> = {
  ACTIVO:     'Activo',
  MOROSO:     'Moroso',
  SUSPENDIDO: 'Suspendido',
};

const RUBROS = ['Construcción', 'Minería', 'Transporte', 'Manufactura', 'Agricultura', 'Servicios', 'Otro'];
const PLANES = ['BASICO', 'PRO', 'PREMIUM'];

export default function Clientes() {
  const [clientes, setClientes]         = useState<ClienteResponse[]>([]);
  const [cargando, setCargando]         = useState(true);
  const [busqueda, setBusqueda]         = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [modalNuevo, setModalNuevo]     = useState(false);
  const [modalEditar, setModalEditar]   = useState<ClienteResponse | null>(null);
  const [modalSuspender, setModalSuspender] = useState<ClienteResponse | null>(null);
  const [modalEliminar, setModalEliminar]   = useState<ClienteResponse | null>(null);
  const [reactivandoId, setReactivandoId] = useState<number | null>(null);
  const [guardando, setGuardando]       = useState(false);
  const [error, setError]               = useState<string | null>(null);

  const formNuevo  = useForm<CrearClienteRequest>();
  const formEditar = useForm<ActualizarClienteRequest>();

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const data = await listarClientes(0, 100);
      setClientes(data.content);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const filtrados = clientes.filter(c => {
    const texto = busqueda.toLowerCase();
    const coincide = !texto || c.razonSocial.toLowerCase().includes(texto) || c.rut.toLowerCase().includes(texto) || c.rubro.toLowerCase().includes(texto);
    const estado = !filtroEstado || c.estado === filtroEstado;
    return coincide && estado;
  });

  const activos     = clientes.filter(c => c.estado === 'ACTIVO').length;
  const suspendidos = clientes.filter(c => c.estado === 'SUSPENDIDO').length;
  const morosos     = clientes.filter(c => c.estado === 'MOROSO').length;

  async function onCrear(data: CrearClienteRequest) {
    setError(null);
    setGuardando(true);
    try {
      await crearCliente(data);
      setModalNuevo(false);
      formNuevo.reset();
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje;
      setError(msg ?? 'Error al crear cliente. Ya existe un cliente con ese correo.');
    } finally {
      setGuardando(false);
    }
  }

  function abrirEditar(c: ClienteResponse) {
    formEditar.reset({
      razonSocial:    c.razonSocial,
      rut:            c.rut,
      nombreContacto: c.nombreContacto,
      email:          c.email,
      telefono:       c.telefono ?? '',
      rubro:          c.rubro,
      plan:           c.plan,
      idProfesional:  c.idProfesional ?? undefined,
      estado:         c.estado,
    });
    setError(null);
    setModalEditar(c);
  }

  async function onEditar(data: ActualizarClienteRequest) {
    if (!modalEditar) return;
    setError(null);
    setGuardando(true);
    try {
      await actualizarCliente(modalEditar.id, data);
      setModalEditar(null);
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje;
      setError(msg ?? 'Error al actualizar el cliente.');
    } finally {
      setGuardando(false);
    }
  }

  async function onSuspender() {
    if (!modalSuspender) return;
    setGuardando(true);
    try {
      await suspenderCliente(modalSuspender.id);
      setModalSuspender(null);
      await cargar();
    } finally {
      setGuardando(false);
    }
  }

  async function onReactivar(id: number) {
    setError(null);
    setReactivandoId(id);
    try {
      await reactivarCliente(id);
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje;
      setError(msg ?? 'Error de integridad: El usuario asociado (ID 3) no puede ser cargado o no existe.');
    } finally {
      setReactivandoId(null);
    }
  }

  async function onEliminar() {
    if (!modalEliminar) return;
    setError(null);
    setGuardando(true);
    try {
      await eliminarCliente(modalEliminar.id);
      setModalEliminar(null);
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje; // Mantener el mensaje de error del backend si existe
      setError(msg ?? 'Error al eliminar el cliente. Verifique que no tenga registros asociados.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Clientes</div>
      <div className="page-subtitle">Gestión de empresas — {clientes.length} registros activos</div>

      <div className="kpi-row">
        <KpiCard label="Activos"     value={activos}     variante="ok" />
        <KpiCard label="Suspendidos" value={suspendidos} variante="warn" />
        <KpiCard label="Morosos"     value={morosos}     variante="peligro" />
        <KpiCard label="Total"       value={clientes.length} />
      </div>

      <Panel
        titulo="👥 Listado de clientes"
        accion={<button className="btn btn-sm btn-primary" onClick={() => { formNuevo.reset(); setError(null); setModalNuevo(true); }}>+ Nuevo cliente</button>}
      >
        <div className="searchbar">
          <input
            placeholder="Buscar por nombre, RUT o rubro..."
            value={busqueda}
            onChange={e => setBusqueda(e.target.value)}
          />
          <select value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="ACTIVO">Activo</option>
            <option value="MOROSO">Moroso</option>
            <option value="SUSPENDIDO">Suspendido</option>
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando clientes...</div>
        ) : filtrados.length === 0 ? (
          <div className="placeholder">No se encontraron clientes con los filtros aplicados.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>RUT</th>
                <th>Rubro</th>
                <th>Plan</th>
                <th>Profesional asignado</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filtrados.map(c => (
                <tr key={c.id}>
                  <td>
                    <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{c.razonSocial}</div>
                    <div style={{ fontSize: 11, color: '#9ca3af' }}>{c.nombreContacto} · {c.email}</div>
                  </td>
                  <td>{c.rut}</td>
                  <td>{c.rubro}</td>
                  <td><span style={{ fontSize: 11, fontWeight: 600 }}>{c.plan}</span></td>
                  <td>{c.nombreProfesional ?? <span style={{ color: '#9ca3af' }}>Sin asignar</span>}</td>
                  <td><Badge variante={badgePorEstado[c.estado]}>{labelEstado[c.estado]}</Badge></td>
                  <td>
                    <div className="btn-group">
                      <button className="btn btn-sm btn-outline" onClick={() => abrirEditar(c)}>Editar</button>
                      {c.estado === 'SUSPENDIDO' ? (
                        <button className="btn btn-sm btn-success" disabled={reactivandoId === c.id} onClick={() => onReactivar(c.id)}>
                          {reactivandoId === c.id ? 'Cargando...' : 'Reactivar'}
                        </button>
                      ) : (
                        <button className="btn btn-sm btn-warn" onClick={() => setModalSuspender(c)}>Suspender</button>
                      )}
                      <button className="btn btn-sm btn-danger" onClick={() => setModalEliminar(c)}>Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      {/* Modal nuevo cliente */}
      <Modal
        abierto={modalNuevo}
        titulo="Nuevo Cliente"
        onCerrar={() => setModalNuevo(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNuevo(false)}>Cancelar</button>
            <button className="btn btn-primary" form="form-cliente-nuevo" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cliente'}
            </button>
          </>
        }
      >
        <form id="form-cliente-nuevo" onSubmit={formNuevo.handleSubmit(onCrear)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Razón social *</label>
              <input className={`auth-input ${formNuevo.formState.errors.razonSocial ? 'auth-input--error' : ''}`}
                placeholder="Empresa SpA"
                {...formNuevo.register('razonSocial', { required: 'Obligatorio', maxLength: { value: 200, message: 'Máx. 200 caracteres' } })}
              />
              {formNuevo.formState.errors.razonSocial && <span className="auth-field-error">{formNuevo.formState.errors.razonSocial.message}</span>}
            </div>
            <div>
              <label className="auth-label">RUT *</label>
              <input className={`auth-input ${formNuevo.formState.errors.rut ? 'auth-input--error' : ''}`}
                placeholder="76123456-7"
                {...formNuevo.register('rut', { required: 'Obligatorio', pattern: { value: /^\d{7,8}-[\dkK]$/, message: 'RUT inválido (ej: 76123456-7)' } })}
              />
              {formNuevo.formState.errors.rut && <span className="auth-field-error">{formNuevo.formState.errors.rut.message}</span>}
            </div>
            <div>
              <label className="auth-label">Contacto *</label>
              <input className={`auth-input ${formNuevo.formState.errors.nombreContacto ? 'auth-input--error' : ''}`}
                placeholder="Nombre del contacto"
                {...formNuevo.register('nombreContacto', { required: 'Obligatorio' })}
              />
              {formNuevo.formState.errors.nombreContacto && <span className="auth-field-error">{formNuevo.formState.errors.nombreContacto.message}</span>}
            </div>
            <div>
              <label className="auth-label">Email *</label>
              <input type="email" className={`auth-input ${formNuevo.formState.errors.email ? 'auth-input--error' : ''}`}
                placeholder="contacto@empresa.cl"
                {...formNuevo.register('email', { required: 'Obligatorio', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' } })}
              />
              {formNuevo.formState.errors.email && <span className="auth-field-error">{formNuevo.formState.errors.email.message}</span>}
            </div>
            <div>
              <label className="auth-label">Teléfono</label>
              <input className="auth-input" placeholder="+56 9 1234 5678" {...formNuevo.register('telefono')} />
            </div>
            <div>
              <label className="auth-label">Rubro *</label>
              <select className={`auth-input ${formNuevo.formState.errors.rubro ? 'auth-input--error' : ''}`}
                {...formNuevo.register('rubro', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                {RUBROS.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              {formNuevo.formState.errors.rubro && <span className="auth-field-error">{formNuevo.formState.errors.rubro.message}</span>}
            </div>
            <div>
              <label className="auth-label">Plan *</label>
              <select className={`auth-input ${formNuevo.formState.errors.plan ? 'auth-input--error' : ''}`}
                {...formNuevo.register('plan', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                {PLANES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
              {formNuevo.formState.errors.plan && <span className="auth-field-error">{formNuevo.formState.errors.plan.message}</span>}
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal editar cliente */}
      <Modal
        abierto={!!modalEditar}
        titulo="Editar Cliente"
        onCerrar={() => setModalEditar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEditar(null)}>Cancelar</button>
            <button className="btn btn-primary" form="form-cliente-editar" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </>
        }
      >
        <form id="form-cliente-editar" onSubmit={formEditar.handleSubmit(onEditar)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Razón social *</label>
              <input className={`auth-input ${formEditar.formState.errors.razonSocial ? 'auth-input--error' : ''}`}
                {...formEditar.register('razonSocial', { required: 'Obligatorio', maxLength: { value: 200, message: 'Máx. 200 caracteres' } })}
              />
              {formEditar.formState.errors.razonSocial && <span className="auth-field-error">{formEditar.formState.errors.razonSocial.message}</span>}
            </div>
            <div>
              <label className="auth-label">RUT *</label>
              <input className={`auth-input ${formEditar.formState.errors.rut ? 'auth-input--error' : ''}`}
                {...formEditar.register('rut', { required: 'Obligatorio', pattern: { value: /^\d{7,8}-[\dkK]$/, message: 'RUT inválido (ej: 76123456-7)' } })}
              />
              {formEditar.formState.errors.rut && <span className="auth-field-error">{formEditar.formState.errors.rut.message}</span>}
            </div>
            <div>
              <label className="auth-label">Contacto *</label>
              <input className={`auth-input ${formEditar.formState.errors.nombreContacto ? 'auth-input--error' : ''}`}
                {...formEditar.register('nombreContacto', { required: 'Obligatorio' })}
              />
              {formEditar.formState.errors.nombreContacto && <span className="auth-field-error">{formEditar.formState.errors.nombreContacto.message}</span>}
            </div>
            <div>
              <label className="auth-label">Email *</label>
              <input type="email" className={`auth-input ${formEditar.formState.errors.email ? 'auth-input--error' : ''}`}
                {...formEditar.register('email', { required: 'Obligatorio', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' } })}
              />
              {formEditar.formState.errors.email && <span className="auth-field-error">{formEditar.formState.errors.email.message}</span>}
            </div>
            <div>
              <label className="auth-label">Teléfono</label>
              <input className="auth-input" {...formEditar.register('telefono')} />
            </div>
            <div>
              <label className="auth-label">Rubro *</label>
              <select className={`auth-input ${formEditar.formState.errors.rubro ? 'auth-input--error' : ''}`}
                {...formEditar.register('rubro', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                {RUBROS.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              {formEditar.formState.errors.rubro && <span className="auth-field-error">{formEditar.formState.errors.rubro.message}</span>}
            </div>
            <div>
              <label className="auth-label">Plan *</label>
              <select className={`auth-input ${formEditar.formState.errors.plan ? 'auth-input--error' : ''}`}
                {...formEditar.register('plan', { required: 'Obligatorio' })}>
                <option value="">Seleccionar...</option>
                {PLANES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
              {formEditar.formState.errors.plan && <span className="auth-field-error">{formEditar.formState.errors.plan.message}</span>}
            </div>
            <div>
              <label className="auth-label">Estado *</label>
              <select className="auth-input" {...formEditar.register('estado', { required: 'Obligatorio' })}>
                <option value="ACTIVO">Activo</option>
                <option value="MOROSO">Moroso</option>
                <option value="SUSPENDIDO">Suspendido</option>
              </select>
            </div>
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal confirmar suspensión */}
      <Modal
        abierto={!!modalSuspender}
        titulo="Suspender cliente"
        ancho="sm"
        onCerrar={() => setModalSuspender(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalSuspender(null)}>Cancelar</button>
            <button className="btn btn-danger" onClick={onSuspender} disabled={guardando}>
              {guardando ? 'Suspendiendo...' : 'Confirmar suspensión'}
            </button>
          </>
        }
      >
        <div style={{ background: '#fef2f2', borderLeft: '4px solid #c0392b', padding: 12, borderRadius: 8, fontSize: 13, lineHeight: 1.5 }}>
          ¿Suspender el servicio de <strong>{modalSuspender?.razonSocial}</strong>?
          No podrá recibir nuevas visitas ni asesorías hasta ser reactivado.
        </div>
      </Modal>

      {/* Modal confirmar eliminación */}
      <Modal
        abierto={!!modalEliminar}
        titulo="Eliminar cliente"
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
          ¿Eliminar a <strong>{modalEliminar?.razonSocial}</strong>? Esta acción es reversible solo por el administrador de base de datos.
        </div>
      </Modal>
    </>
  );
}

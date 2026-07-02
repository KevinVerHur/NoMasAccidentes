import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type { EmpresaResponse, CrearEmpresaRequest, ActualizarEmpresaRequest, EstadoEmpresa, RubroResponse, RepresentanteResponse, CrearRepresentanteRequest, VarianteBadge } from '../types';
import { listarClientes, crearCliente, actualizarCliente, suspenderCliente, reactivarCliente, eliminarCliente } from '../api/clientes';
import { listarRubros } from '../api/rubros';
import { listarRepresentantes, crearRepresentante, eliminarRepresentante } from '../api/representantes';

const badgePorEstado: Record<EstadoEmpresa, VarianteBadge> = {
  ACTIVO:     'green',
  MOROSO:     'red',
  SUSPENDIDO: 'gray',
};

const labelEstado: Record<EstadoEmpresa, string> = {
  ACTIVO:     'Activo',
  MOROSO:     'Moroso',
  SUSPENDIDO: 'Suspendido',
};

const PLANES = ['BASICO', 'PRO', 'PREMIUM'];

export default function Clientes() {
  const [clientes, setClientes]         = useState<EmpresaResponse[]>([]);
  const [rubros, setRubros]             = useState<RubroResponse[]>([]);
  const [cargando, setCargando]         = useState(true);
  const [busqueda, setBusqueda]         = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [modalNuevo, setModalNuevo]     = useState(false);
  const [modalEditar, setModalEditar]   = useState<EmpresaResponse | null>(null);
  const [modalSuspender, setModalSuspender] = useState<EmpresaResponse | null>(null);
  const [modalEliminar, setModalEliminar]   = useState<EmpresaResponse | null>(null);
  const [reactivandoId, setReactivandoId] = useState<number | null>(null);
  const [guardando, setGuardando]       = useState(false);
  const [error, setError]               = useState<string | null>(null);

  // Drill-down a los contactos/representantes de una empresa.
  const [empresaSel, setEmpresaSel]           = useState<EmpresaResponse | null>(null);
  const [representantes, setRepresentantes]   = useState<RepresentanteResponse[]>([]);
  const [cargandoContactos, setCargandoContactos] = useState(false);
  const [modalNuevoContacto, setModalNuevoContacto] = useState(false);

  const formNuevo  = useForm<CrearEmpresaRequest>();
  const formEditar = useForm<ActualizarEmpresaRequest>();
  const formRep    = useForm<CrearRepresentanteRequest>({ defaultValues: { conAcceso: true } });

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
  useEffect(() => { listarRubros().then(setRubros).catch(() => {}); }, []);

  const filtrados = clientes.filter(c => {
    const texto = busqueda.toLowerCase();
    const coincide = !texto || c.razonSocial.toLowerCase().includes(texto) || c.rut.toLowerCase().includes(texto) || c.nombreRubro.toLowerCase().includes(texto);
    const estado = !filtroEstado || c.estado === filtroEstado;
    return coincide && estado;
  });

  const activos     = clientes.filter(c => c.estado === 'ACTIVO').length;
  const suspendidos = clientes.filter(c => c.estado === 'SUSPENDIDO').length;
  const morosos     = clientes.filter(c => c.estado === 'MOROSO').length;

  async function onCrear(data: CrearEmpresaRequest) {
    setError(null);
    setGuardando(true);
    try {
      await crearCliente(data);
      setModalNuevo(false);
      formNuevo.reset();
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Error al crear el cliente. Verifique que el RUT y el correo no estén duplicados.');
    } finally {
      setGuardando(false);
    }
  }

  function abrirEditar(c: EmpresaResponse) {
    formEditar.reset({
      razonSocial:    c.razonSocial,
      rut:            c.rut,
      direccion:      c.direccion ?? '',
      comuna:         c.comuna ?? '',
      idRubro:        c.idRubro,
      plan:           c.plan,
      cantidadTrabajadores: c.cantidadTrabajadores ?? undefined,
      idProfesional:  c.idProfesional ?? undefined,
      estado:         c.estado,
    });
    setError(null);
    setModalEditar(c);
  }

  async function onEditar(data: ActualizarEmpresaRequest) {
    if (!modalEditar) return;
    setError(null);
    setGuardando(true);
    try {
      await actualizarCliente(modalEditar.id, data);
      setModalEditar(null);
      await cargar();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
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
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Error al reactivar el cliente.');
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
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Error al eliminar el cliente. Verifique que no tenga registros asociados.');
    } finally {
      setGuardando(false);
    }
  }

  // ---- Contactos / representantes ----
  const cargarContactos = useCallback(async (idEmpresa: number) => {
    setCargandoContactos(true);
    try {
      setRepresentantes(await listarRepresentantes(idEmpresa));
    } finally {
      setCargandoContactos(false);
    }
  }, []);

  async function verContactos(c: EmpresaResponse) {
    setError(null);
    setEmpresaSel(c);
    await cargarContactos(c.id);
  }

  function volverALista() {
    setEmpresaSel(null);
    setRepresentantes([]);
  }

  async function onCrearRepresentante(data: CrearRepresentanteRequest) {
    if (!empresaSel) return;
    setError(null);
    setGuardando(true);
    try {
      await crearRepresentante(empresaSel.id, data);
      setModalNuevoContacto(false);
      formRep.reset({ conAcceso: true });
      await cargarContactos(empresaSel.id);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Error al agregar el representante. Verifique que el correo no esté en uso.');
    } finally {
      setGuardando(false);
    }
  }

  async function onEliminarRepresentante(id: number) {
    if (!empresaSel) return;
    try {
      await eliminarRepresentante(empresaSel.id, id);
      await cargarContactos(empresaSel.id);
    } catch { /* noop */ }
  }

  // ============================ VISTA DE CONTACTOS ============================
  if (empresaSel) {
    return (
      <>
        <button className="btn btn-sm btn-outline" style={{ marginBottom: 10 }} onClick={volverALista}>← Volver a clientes</button>
        <div className="page-title">{empresaSel.razonSocial}</div>
        <div className="page-subtitle">
          {empresaSel.rut} · {empresaSel.nombreRubro} · Plan {empresaSel.plan} · <Badge variante={badgePorEstado[empresaSel.estado]}>{labelEstado[empresaSel.estado]}</Badge>
        </div>

        <Panel
          titulo="👤 Contactos / Representantes"
          accion={<button className="btn btn-sm btn-primary" onClick={() => { formRep.reset({ conAcceso: true }); setError(null); setModalNuevoContacto(true); }}>+ Nuevo contacto</button>}
        >
          {cargandoContactos ? (
            <div className="placeholder">Cargando contactos...</div>
          ) : representantes.length === 0 ? (
            <div className="placeholder">Esta empresa no tiene contactos registrados. Agrega el primero con “+ Nuevo contacto”.</div>
          ) : (
            <table className="app-table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Cargo</th>
                  <th>Email</th>
                  <th>Teléfono</th>
                  <th>Acceso al portal</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {representantes.map(r => (
                  <tr key={r.id}>
                    <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{r.nombre}</td>
                    <td>{r.cargo ?? '—'}</td>
                    <td>{r.email}</td>
                    <td>{r.telefono ?? '—'}</td>
                    <td><Badge variante={r.tieneAcceso ? 'green' : 'gray'}>{r.tieneAcceso ? 'Con acceso' : 'Sin acceso'}</Badge></td>
                    <td>
                      <button className="btn btn-sm btn-danger" onClick={() => onEliminarRepresentante(r.id)}>Quitar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>

        {/* Modal agregar contacto */}
        <Modal
          abierto={modalNuevoContacto}
          titulo={`Nuevo contacto — ${empresaSel.razonSocial}`}
          onCerrar={() => setModalNuevoContacto(false)}
          footer={
            <>
              <button className="btn btn-outline" onClick={() => setModalNuevoContacto(false)}>Cancelar</button>
              <button className="btn btn-primary" form="form-representante" type="submit" disabled={guardando}>
                {guardando ? 'Agregando...' : 'Agregar contacto'}
              </button>
            </>
          }
        >
          <form id="form-representante" onSubmit={formRep.handleSubmit(onCrearRepresentante)} noValidate>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <div>
                <label className="auth-label">Nombre *</label>
                <input className={`auth-input ${formRep.formState.errors.nombre ? 'auth-input--error' : ''}`}
                  {...formRep.register('nombre', { required: 'Obligatorio' })} />
                {formRep.formState.errors.nombre && <span className="auth-field-error">{formRep.formState.errors.nombre.message}</span>}
              </div>
              <div>
                <label className="auth-label">Cargo</label>
                <input className="auth-input" placeholder="Encargado de Prevención" {...formRep.register('cargo')} />
              </div>
              <div>
                <label className="auth-label">Email *</label>
                <input type="email" className={`auth-input ${formRep.formState.errors.email ? 'auth-input--error' : ''}`}
                  {...formRep.register('email', { required: 'Obligatorio', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' } })} />
                {formRep.formState.errors.email && <span className="auth-field-error">{formRep.formState.errors.email.message}</span>}
              </div>
              <div>
                <label className="auth-label">Teléfono</label>
                <input className="auth-input" {...formRep.register('telefono')} />
              </div>
              <div style={{ gridColumn: 'span 2', display: 'flex', alignItems: 'center', gap: 8 }}>
                <input type="checkbox" id="conAcceso" {...formRep.register('conAcceso')} />
                <label htmlFor="conAcceso" style={{ fontSize: 13 }}>Dar acceso al portal (envía invitación por correo)</label>
              </div>
            </div>
            {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
          </form>
        </Modal>
      </>
    );
  }

  // ============================ VISTA DE LISTADO ============================
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
                <tr key={c.id} onClick={() => verContactos(c)} style={{ cursor: 'pointer' }}>
                  <td>
                    <div style={{ fontWeight: 600, color: '#18395a' }}>{c.razonSocial}</div>
                    {c.comuna && <div style={{ fontSize: 11, color: '#9ca3af' }}>{c.comuna}</div>}
                  </td>
                  <td>{c.rut}</td>
                  <td>{c.nombreRubro}</td>
                  <td><span style={{ fontSize: 11, fontWeight: 600 }}>{c.plan}</span></td>
                  <td>{c.nombreProfesional ?? <span style={{ color: '#9ca3af' }}>Sin asignar</span>}</td>
                  <td><Badge variante={badgePorEstado[c.estado]}>{labelEstado[c.estado]}</Badge></td>
                  <td>
                    <div className="btn-group">
                      <button className="btn btn-sm btn-outline" onClick={e => { e.stopPropagation(); abrirEditar(c); }}>Editar</button>
                      {c.estado === 'SUSPENDIDO' ? (
                        <button className="btn btn-sm btn-success" disabled={reactivandoId === c.id} onClick={e => { e.stopPropagation(); onReactivar(c.id); }}>
                          {reactivandoId === c.id ? 'Cargando...' : 'Reactivar'}
                        </button>
                      ) : (
                        <button className="btn btn-sm btn-warn" onClick={e => { e.stopPropagation(); setModalSuspender(c); }}>Suspender</button>
                      )}
                      <button className="btn btn-sm btn-danger" onClick={e => { e.stopPropagation(); setModalEliminar(c); }}>Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <div className="help" style={{ marginTop: 8, paddingLeft: 12, fontSize: 11, color: '#9ca3af' }}>
          Haz clic en una empresa para ver y gestionar sus contactos.
        </div>
      </Panel>

      {/* Modal nuevo cliente (empresa + primer representante) */}
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
          <div style={{ fontSize: 12, fontWeight: 700, color: '#18395a', marginBottom: 8 }}>Empresa</div>
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
              <label className="auth-label">Rubro *</label>
              <select className={`auth-input ${formNuevo.formState.errors.idRubro ? 'auth-input--error' : ''}`}
                {...formNuevo.register('idRubro', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {rubros.map(r => <option key={r.id} value={r.id}>{r.nombre}</option>)}
              </select>
              {formNuevo.formState.errors.idRubro && <span className="auth-field-error">{formNuevo.formState.errors.idRubro.message}</span>}
            </div>
            <div>
              <label className="auth-label">Dirección</label>
              <input className="auth-input" placeholder="Av. Siempre Viva 742" {...formNuevo.register('direccion')} />
            </div>
            <div>
              <label className="auth-label">Comuna</label>
              <input className="auth-input" placeholder="Santiago" {...formNuevo.register('comuna')} />
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
            <div>
              <label className="auth-label">N° de trabajadores</label>
              <input type="number" min="0" className="auth-input" placeholder="Ej: 50"
                {...formNuevo.register('cantidadTrabajadores', { valueAsNumber: true, min: { value: 0, message: 'No puede ser negativo' } })} />
              {formNuevo.formState.errors.cantidadTrabajadores && <span className="auth-field-error">{formNuevo.formState.errors.cantidadTrabajadores.message}</span>}
            </div>
          </div>

          <div style={{ fontSize: 12, fontWeight: 700, color: '#18395a', margin: '16px 0 8px' }}>Representante (contacto)</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div>
              <label className="auth-label">Nombre *</label>
              <input className={`auth-input ${formNuevo.formState.errors.nombreContacto ? 'auth-input--error' : ''}`}
                placeholder="Nombre del contacto"
                {...formNuevo.register('nombreContacto', { required: 'Obligatorio' })}
              />
              {formNuevo.formState.errors.nombreContacto && <span className="auth-field-error">{formNuevo.formState.errors.nombreContacto.message}</span>}
            </div>
            <div>
              <label className="auth-label">Cargo</label>
              <input className="auth-input" placeholder="Encargado de Prevención" {...formNuevo.register('cargoContacto')} />
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
          </div>
          <div className="help" style={{ marginTop: 8, fontSize: 11, color: '#9ca3af' }}>
            Se le enviará una invitación por correo para que active su acceso al portal.
          </div>
          {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
        </form>
      </Modal>

      {/* Modal editar cliente (datos de la empresa) */}
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
              <label className="auth-label">Rubro *</label>
              <select className={`auth-input ${formEditar.formState.errors.idRubro ? 'auth-input--error' : ''}`}
                {...formEditar.register('idRubro', { required: 'Obligatorio', valueAsNumber: true })}>
                <option value="">Seleccionar...</option>
                {rubros.map(r => <option key={r.id} value={r.id}>{r.nombre}</option>)}
              </select>
              {formEditar.formState.errors.idRubro && <span className="auth-field-error">{formEditar.formState.errors.idRubro.message}</span>}
            </div>
            <div>
              <label className="auth-label">Dirección</label>
              <input className="auth-input" {...formEditar.register('direccion')} />
            </div>
            <div>
              <label className="auth-label">Comuna</label>
              <input className="auth-input" {...formEditar.register('comuna')} />
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
              <label className="auth-label">N° de trabajadores</label>
              <input type="number" min="0" className="auth-input"
                {...formEditar.register('cantidadTrabajadores', { valueAsNumber: true, min: { value: 0, message: 'No puede ser negativo' } })} />
              {formEditar.formState.errors.cantidadTrabajadores && <span className="auth-field-error">{formEditar.formState.errors.cantidadTrabajadores.message}</span>}
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

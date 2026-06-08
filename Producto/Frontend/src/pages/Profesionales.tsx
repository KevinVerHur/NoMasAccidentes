import { useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import type { ProfesionalResponse, CrearProfesionalRequest, ActualizarProfesionalRequest, EstadoProfesional, VarianteBadge } from '../types';
import { listarProfesionales, crearProfesional, actualizarProfesional, eliminarProfesional } from '../api/profesionales';
import EstadoProfesionalesPanel from '../components/profesionales/EstadoProfesionalesPanel';

const MAX_CLIENTES_CARGA = 12;

function porcentajeCarga(cantidad: number) {
    return Math.min(Math.round((cantidad / MAX_CLIENTES_CARGA) * 100), 100);
}

function varianteCarga(pct: number): VarianteBadge {
    if (pct >= 90) return 'red';
    if (pct >= 70) return 'yellow';
    return 'green';
}

const badgePorEstado: Record<EstadoProfesional, VarianteBadge> = {
    DISPONIBLE:      'green',
    EN_VISITA:       'blue',
    EN_CAPACITACION: 'yellow',
};

const labelEstado: Record<EstadoProfesional, string> = {
    DISPONIBLE:      'Disponible',
    EN_VISITA:       'En visita',
    EN_CAPACITACION: 'Capacitando',
};

export default function Profesionales() {
    const [profesionales, setProfesionales] = useState<ProfesionalResponse[]>([]);
    const [cargando, setCargando]           = useState(true);
    const [busqueda, setBusqueda]           = useState('');
    const [modalNuevo, setModalNuevo]       = useState(false);
    const [modalEditar, setModalEditar]     = useState<ProfesionalResponse | null>(null);
    const [modalPerfil, setModalPerfil]     = useState<ProfesionalResponse | null>(null);
    const [modalEliminar, setModalEliminar] = useState<ProfesionalResponse | null>(null);
    const [guardando, setGuardando]         = useState(false);
    const [error, setError]                 = useState<string | null>(null);

    const formNuevo  = useForm<CrearProfesionalRequest>();
    const formEditar = useForm<ActualizarProfesionalRequest>();

    const cargar = useCallback(async () => {
        setCargando(true);
        try {
            const data = await listarProfesionales(0, 100);
            setProfesionales(data.content);
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => { cargar(); }, [cargar]);

    const filtrados = profesionales.filter(p => {
        const texto = busqueda.toLowerCase();
        return !texto || p.nombreCompleto.toLowerCase().includes(texto) || p.rut.toLowerCase().includes(texto) || (p.especialidad ?? '').toLowerCase().includes(texto);
    });

    const cargaAlta     = profesionales.filter(p => porcentajeCarga(p.cantidadClientes) >= 80).length;
    const enVisita      = profesionales.filter(p => p.estado === 'EN_VISITA').length;
    const totalClientes = profesionales.reduce((acc, p) => acc + p.cantidadClientes, 0);

    async function onCrear(data: CrearProfesionalRequest) {
        setError(null);
        setGuardando(true);
        try {
            await crearProfesional(data);
            setModalNuevo(false);
            formNuevo.reset();
            await cargar();
        } catch (e: unknown) {
            const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje;
            setError(msg ?? 'Error al agregar el profesional.');
        } finally {
            setGuardando(false);
        }
    }

    function abrirEditar(p: ProfesionalResponse) {
        formEditar.reset({
            rut:         p.rut,
            telefono:    p.telefono ?? '',
            especialidad: p.especialidad ?? '',
        });
        setError(null);
        setModalEditar(p);
    }

    async function onEditar(data: ActualizarProfesionalRequest) {
        if (!modalEditar) return;
        setError(null);
        setGuardando(true);
        try {
            await actualizarProfesional(modalEditar.id, data);
            setModalEditar(null);
            await cargar();
        } catch (e: unknown) {
            const msg = (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje;
            setError(msg ?? 'Error al actualizar el profesional.');
        } finally {
            setGuardando(false);
        }
    }

    async function onEliminar() {
        if (!modalEliminar) return;
        setGuardando(true);
        try {
            await eliminarProfesional(modalEliminar.id);
            setModalEliminar(null);
            await cargar();
        } finally {
            setGuardando(false);
        }
    }

    return (
        <>
            <div className="page-title">Profesionales</div>
            <div className="page-subtitle">Control del equipo y carga laboral</div>

            <div className="kpi-row">
                <KpiCard label="Activos"            value={profesionales.length} variante="ok" />
                <KpiCard label="Carga alta (≥80%)"  value={cargaAlta}            variante="warn" />
                <KpiCard label="En terreno"         value={enVisita} />
                <KpiCard label="Clientes asignados" value={totalClientes} />
            </div>

            <EstadoProfesionalesPanel />

            <Panel
                titulo="🧑‍💼 Listado de profesionales"
                accion={<button className="btn btn-sm btn-primary" onClick={() => { formNuevo.reset(); setError(null); setModalNuevo(true); }}>+ Agregar profesional</button>}
            >
                <div className="searchbar">
                    <input
                        placeholder="Buscar por nombre, RUT o especialidad..."
                        value={busqueda}
                        onChange={e => setBusqueda(e.target.value)}
                    />
                </div>

                {cargando ? (
                    <div className="placeholder">Cargando profesionales...</div>
                ) : filtrados.length === 0 ? (
                    <div className="placeholder">No hay profesionales registrados aún.</div>
                ) : (
                    <table className="app-table">
                        <thead>
                        <tr>
                            <th>Profesional</th>
                            <th>RUT</th>
                            <th>Especialidad</th>
                            <th>Estado</th>
                            <th>Clientes</th>
                            <th>Carga</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filtrados.map(p => {
                            const pct = porcentajeCarga(p.cantidadClientes);
                            return (
                                <tr key={p.id}>
                                    <td>
                                        <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{p.nombreCompleto}</div>
                                        <div style={{ fontSize: 11, color: '#9ca3af' }}>{p.email}</div>
                                    </td>
                                    <td>{p.rut}</td>
                                    <td>{p.especialidad ?? <span style={{ color: '#9ca3af' }}>—</span>}</td>
                                    <td><Badge variante={badgePorEstado[p.estado]}>{labelEstado[p.estado]}</Badge></td>
                                    <td style={{ textAlign: 'center' }}>{p.cantidadClientes}</td>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                            <div style={{ flex: 1, background: '#e5e7eb', borderRadius: 6, height: 8, minWidth: 60 }}>
                                                <div style={{ width: `${pct}%`, height: '100%', borderRadius: 6, background: pct >= 90 ? '#c0392b' : pct >= 70 ? '#e07b00' : '#27ae60', transition: 'width .3s' }} />
                                            </div>
                                            <Badge variante={varianteCarga(pct)}>{pct}%</Badge>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="btn-group">
                                            <button className="btn btn-sm btn-outline" onClick={() => setModalPerfil(p)}>Ver perfil</button>
                                            <button className="btn btn-sm btn-outline" onClick={() => abrirEditar(p)}>Editar</button>
                                            <button className="btn btn-sm btn-danger" onClick={() => setModalEliminar(p)}>Eliminar</button>
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                )}
            </Panel>

            {/* Modal nuevo profesional */}
            <Modal
                abierto={modalNuevo}
                titulo="Agregar Profesional"
                onCerrar={() => setModalNuevo(false)}
                footer={
                    <>
                        <button className="btn btn-outline" onClick={() => setModalNuevo(false)}>Cancelar</button>
                        <button className="btn btn-primary" form="form-profesional-nuevo" type="submit" disabled={guardando}>
                            {guardando ? 'Guardando...' : 'Agregar profesional'}
                        </button>
                    </>
                }
            >
                <form id="form-profesional-nuevo" onSubmit={formNuevo.handleSubmit(onCrear)} noValidate>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                        <div>
                            <label className="auth-label">Nombre *</label>
                            <input className={`auth-input ${formNuevo.formState.errors.nombre ? 'auth-input--error' : ''}`}
                                   placeholder="Juan"
                                   {...formNuevo.register('nombre', { required: 'Obligatorio' })}
                            />
                            {formNuevo.formState.errors.nombre && <span className="auth-field-error">{formNuevo.formState.errors.nombre.message}</span>}
                        </div>
                        <div>
                            <label className="auth-label">Apellido *</label>
                            <input className={`auth-input ${formNuevo.formState.errors.apellido ? 'auth-input--error' : ''}`}
                                   placeholder="Pérez"
                                   {...formNuevo.register('apellido', { required: 'Obligatorio' })}
                            />
                            {formNuevo.formState.errors.apellido && <span className="auth-field-error">{formNuevo.formState.errors.apellido.message}</span>}
                        </div>
                        <div style={{ gridColumn: 'span 2' }}>
                            <label className="auth-label">Email *</label>
                            <input type="email" className={`auth-input ${formNuevo.formState.errors.email ? 'auth-input--error' : ''}`}
                                   placeholder="juan.perez@empresa.cl"
                                   {...formNuevo.register('email', { required: 'Obligatorio', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' } })}
                            />
                            {formNuevo.formState.errors.email && <span className="auth-field-error">{formNuevo.formState.errors.email.message}</span>}
                        </div>
                        <div style={{ gridColumn: 'span 2' }}>
                            <label className="auth-label">Contraseña temporal *</label>
                            <input type="password" className={`auth-input ${formNuevo.formState.errors.password ? 'auth-input--error' : ''}`}
                                   placeholder="Mínimo 8 caracteres"
                                   {...formNuevo.register('password', { required: 'Obligatorio', minLength: { value: 8, message: 'Mínimo 8 caracteres' } })}
                            />
                            {formNuevo.formState.errors.password && <span className="auth-field-error">{formNuevo.formState.errors.password.message}</span>}
                            <span style={{ fontSize: 11, color: '#9ca3af', marginTop: 4, display: 'block' }}>El profesional podrá cambiarla al iniciar sesión.</span>
                        </div>
                        <div>
                            <label className="auth-label">RUT *</label>
                            <input className={`auth-input ${formNuevo.formState.errors.rut ? 'auth-input--error' : ''}`}
                                   placeholder="12345678-9"
                                   {...formNuevo.register('rut', { required: 'Obligatorio', pattern: { value: /^\d{7,8}-[\dkK]$/, message: 'RUT inválido (ej: 12345678-9)' } })}
                            />
                            {formNuevo.formState.errors.rut && <span className="auth-field-error">{formNuevo.formState.errors.rut.message}</span>}
                        </div>
                        <div>
                            <label className="auth-label">Teléfono</label>
                            <input className="auth-input" placeholder="+56 9 1234 5678" {...formNuevo.register('telefono')} />
                        </div>
                        <div style={{ gridColumn: 'span 2' }}>
                            <label className="auth-label">Especialidad</label>
                            <input className="auth-input" placeholder="Prevención de riesgos, Higiene ocupacional..." {...formNuevo.register('especialidad')} />
                        </div>
                    </div>
                    {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
                </form>
            </Modal>

            {/* Modal editar profesional */}
            <Modal
                abierto={!!modalEditar}
                titulo={`Editar — ${modalEditar?.nombreCompleto ?? ''}`}
                ancho="sm"
                onCerrar={() => setModalEditar(null)}
                footer={
                    <>
                        <button className="btn btn-outline" onClick={() => setModalEditar(null)}>Cancelar</button>
                        <button className="btn btn-primary" form="form-profesional-editar" type="submit" disabled={guardando}>
                            {guardando ? 'Guardando...' : 'Guardar cambios'}
                        </button>
                    </>
                }
            >
                <form id="form-profesional-editar" onSubmit={formEditar.handleSubmit(onEditar)} noValidate>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        <div>
                            <label className="auth-label">RUT *</label>
                            <input className={`auth-input ${formEditar.formState.errors.rut ? 'auth-input--error' : ''}`}
                                   {...formEditar.register('rut', { required: 'Obligatorio', pattern: { value: /^\d{7,8}-[\dkK]$/, message: 'RUT inválido (ej: 12345678-9)' } })}
                            />
                            {formEditar.formState.errors.rut && <span className="auth-field-error">{formEditar.formState.errors.rut.message}</span>}
                        </div>
                        <div>
                            <label className="auth-label">Especialidad</label>
                            <input className="auth-input" {...formEditar.register('especialidad')} />
                        </div>
                        <div>
                            <label className="auth-label">Teléfono</label>
                            <input className="auth-input" {...formEditar.register('telefono')} />
                        </div>
                    </div>
                    {error && <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>{error}</div>}
                </form>
            </Modal>

            {/* Modal perfil profesional */}
            <Modal
                abierto={!!modalPerfil}
                titulo="Perfil del Profesional"
                onCerrar={() => setModalPerfil(null)}
                footer={<button className="btn btn-outline" onClick={() => setModalPerfil(null)}>Cerrar</button>}
            >
                {modalPerfil && (
                    <div style={{ lineHeight: 1.8, fontSize: 13 }}>
                        <p><strong>Nombre:</strong> {modalPerfil.nombreCompleto}</p>
                        <p><strong>Email:</strong> {modalPerfil.email}</p>
                        <p><strong>RUT:</strong> {modalPerfil.rut}</p>
                        <p><strong>Especialidad:</strong> {modalPerfil.especialidad ?? '—'}</p>
                        <p><strong>Teléfono:</strong> {modalPerfil.telefono ?? '—'}</p>
                        <p><strong>Estado:</strong> {labelEstado[modalPerfil.estado]}</p>
                        <p><strong>Clientes asignados:</strong> {modalPerfil.cantidadClientes}</p>
                        <p><strong>Carga:</strong> {porcentajeCarga(modalPerfil.cantidadClientes)}%</p>
                    </div>
                )}
            </Modal>

            {/* Modal confirmar eliminación */}
            <Modal
                abierto={!!modalEliminar}
                titulo="Eliminar profesional"
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
                    ¿Eliminar a <strong>{modalEliminar?.nombreCompleto}</strong>? Esta acción es reversible solo por el administrador de base de datos.
                </div>
            </Modal>
        </>
    );
}

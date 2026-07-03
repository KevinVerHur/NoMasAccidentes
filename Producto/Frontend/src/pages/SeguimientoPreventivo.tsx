import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import { listarClientes } from '../api/clientes';
import {
  cambiarEstadoActividadPreventiva,
  crearActividadPreventiva,
  eliminarActividadPreventiva,
  listarActividadesPreventivas,
} from '../api/ActividadesPreventivas';
import type {
  ActividadPreventivaResponse,
  EmpresaResponse,
  CrearActividadPreventivaRequest,
  EstadoActividadPreventiva,
  VarianteBadge,
} from '../types';

const badgePorEstado: Record<EstadoActividadPreventiva, VarianteBadge> = {
  PENDIENTE: 'gray',
  EN_CURSO: 'yellow',
  CUMPLIDA: 'green',
  VENCIDA: 'red',
};

const labelEstado: Record<EstadoActividadPreventiva, string> = {
  PENDIENTE: 'Pendiente',
  EN_CURSO: 'En curso',
  CUMPLIDA: 'Cumplida',
  VENCIDA: 'Vencida',
};

function fmtFecha(iso: string | null) {
  return iso ? new Date(iso).toLocaleDateString('es-CL') : '-';
}

function mensajeError(e: unknown, fallback: string) {
  const data = (e as { response?: { data?: { message?: string; errores?: string[] } } })?.response?.data;
  return data?.message ?? (data?.errores?.length ? data.errores.join(' · ') : null) ?? fallback;
}

export default function SeguimientoPreventivo() {
  const [actividades, setActividades] = useState<ActividadPreventivaResponse[]>([]);
  const [clientes, setClientes] = useState<EmpresaResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [modalNueva, setModalNueva] = useState(false);
  const [modalEliminar, setModalEliminar] = useState<ActividadPreventivaResponse | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const formNueva = useForm<CrearActividadPreventivaRequest>();

  const cargar = useCallback(async () => {
    setCargando(true);

    try {
      const estado = filtroEstado ? filtroEstado as EstadoActividadPreventiva : undefined;
      const data = await listarActividadesPreventivas(0, 200, undefined, estado);
      setActividades(data.content);
    } finally {
      setCargando(false);
    }
  }, [filtroEstado]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    listarClientes(0, 200)
      .then((data) => setClientes(data.content))
      .catch(() => setClientes([]));
  }, []);

  const filtradas = actividades.filter((a) => {
    const texto = busqueda.toLowerCase();

    return !texto
      || a.razonSocialEmpresa.toLowerCase().includes(texto)
      || a.titulo.toLowerCase().includes(texto)
      || (a.normativa ?? '').toLowerCase().includes(texto)
      || (a.responsable ?? '').toLowerCase().includes(texto);
  });

  const pendientes = actividades.filter((a) => a.estado === 'PENDIENTE').length;
  const enCurso = actividades.filter((a) => a.estado === 'EN_CURSO').length;
  const cumplidas = actividades.filter((a) => a.estado === 'CUMPLIDA').length;
  const vencidas = actividades.filter((a) => a.estado === 'VENCIDA' || a.vencida).length;

  function abrirNueva() {
    formNueva.reset({
      idEmpresa: undefined as unknown as number,
      titulo: '',
      descripcion: '',
      normativa: '',
      responsable: '',
      fechaPlanificada: new Date().toISOString().slice(0, 10),
      fechaCompromiso: '',
      observaciones: '',
    });

    setError(null);
    setModalNueva(true);
  }

  async function onCrear(data: CrearActividadPreventivaRequest) {
    setGuardando(true);
    setError(null);

    try {
      await crearActividadPreventiva(data);
      setModalNueva(false);
      await cargar();
    } catch (e) {
      setError(mensajeError(e, 'No se pudo crear la actividad preventiva.'));
    } finally {
      setGuardando(false);
    }
  }

  async function cambiarEstado(id: number, estado: EstadoActividadPreventiva) {
    setError(null);

    try {
      await cambiarEstadoActividadPreventiva(id, { estado });
      await cargar();
    } catch (e) {
      setError(mensajeError(e, 'No se pudo actualizar el estado.'));
    }
  }

  async function onEliminar() {
    if (!modalEliminar) return;

    setGuardando(true);

    try {
      await eliminarActividadPreventiva(modalEliminar.id);
      setModalEliminar(null);
      await cargar();
    } catch (e) {
      setError(mensajeError(e, 'No se pudo eliminar la actividad preventiva.'));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Seguimiento preventivo</div>
      <div className="page-subtitle">
        Seguimiento de actividades preventivas planificadas por cliente
      </div>

      <div className="kpi-row">
        <KpiCard label="Pendientes" value={pendientes} />
        <KpiCard label="En curso" value={enCurso} variante="warn" />
        <KpiCard label="Cumplidas" value={cumplidas} variante="ok" />
        <KpiCard label="Vencidas" value={vencidas} variante="peligro" />
      </div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          {error}
        </div>
      )}

      <Panel
        titulo="Seguimiento preventivo"
        accion={
          <button className="btn btn-sm btn-primary" onClick={abrirNueva}>
            + Nueva actividad preventiva
          </button>
        }
      >
        <div className="searchbar">
          <input
            placeholder="Buscar por cliente, actividad o responsable..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />

          <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="EN_CURSO">En curso</option>
            <option value="CUMPLIDA">Cumplida</option>
            <option value="VENCIDA">Vencida</option>
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando actividades preventivas...</div>
        ) : filtradas.length === 0 ? (
          <div className="placeholder">No hay actividades preventivas registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Actividad preventiva</th>
                <th>Normativa</th>
                <th>Responsable</th>
                <th>Fecha limite</th>
                <th>Estado</th>
                <th>Observaciones</th>
                <th>Accion</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map((a) => (
                <tr key={a.id}>
                  <td>{a.razonSocialEmpresa}</td>
                  <td>
                    <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{a.titulo}</div>
                    <div style={{ fontSize: 11, color: '#6b7280' }}>{a.descripcion ?? '-'}</div>
                    {a.reportadoPorCliente && a.estado !== 'CUMPLIDA' && (
                      <div style={{ fontSize: 11, color: '#1a7f37', fontWeight: 600, marginTop: 2 }}>
                        ✓ Cliente reportó cumplimiento{a.comentarioCliente ? `: ${a.comentarioCliente}` : ''}
                      </div>
                    )}
                  </td>
                  <td>{a.normativa ?? '-'}</td>
                  <td>{a.responsable ?? '-'}</td>
                  <td>{fmtFecha(a.fechaCompromiso)}</td>
                  <td>
                    <Badge variante={badgePorEstado[a.estado]}>
                      {labelEstado[a.estado]}
                    </Badge>
                  </td>
                  <td>{a.observaciones ?? '-'}</td>
                  <td>
                    <div className="btn-group">
                      {a.estado === 'PENDIENTE' && (
                        <button className="btn btn-sm btn-primary" onClick={() => cambiarEstado(a.id, 'EN_CURSO')}>
                          Iniciar
                        </button>
                      )}

                      {a.estado !== 'CUMPLIDA' && (
                        <button className="btn btn-sm btn-success" onClick={() => cambiarEstado(a.id, 'CUMPLIDA')}>
                          Cumplir
                        </button>
                      )}

                      <button className="btn btn-sm btn-danger" onClick={() => setModalEliminar(a)}>
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      <Modal
        abierto={modalNueva}
        titulo="Nueva actividad preventiva"
        onCerrar={() => setModalNueva(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNueva(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-actividad-nueva" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar actividad'}
            </button>
          </>
        }
      >
        <form id="form-actividad-nueva" onSubmit={formNueva.handleSubmit(onCrear)} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Cliente *</label>
              <select
                className="auth-input"
                {...formNueva.register('idEmpresa', {
                  required: 'Obligatorio',
                  valueAsNumber: true,
                })}
              >
                <option value="">Seleccionar...</option>
                {clientes.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.razonSocial}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Actividad preventiva *</label>
              <input
                className="auth-input"
                placeholder="Ej: Subir evidencia de señalética"
                {...formNueva.register('titulo', { required: 'Obligatorio' })}
              />
            </div>

            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Descripcion</label>
              <textarea
                className="auth-input"
                rows={3}
                placeholder="Detalle de la actividad, evidencia esperada o instrucción..."
                {...formNueva.register('descripcion')}
              />
            </div>

            <div>
              <label className="auth-label">Normativa</label>
              <input
                className="auth-input"
                placeholder="Ley 16.744, DS N°40..."
                {...formNueva.register('normativa')}
              />
            </div>

            <div>
              <label className="auth-label">Responsable</label>
              <input
                className="auth-input"
                placeholder="Cliente, Profesional, Consultora..."
                {...formNueva.register('responsable')}
              />
            </div>

            <div>
              <label className="auth-label">Fecha planificada *</label>
              <input
                type="date"
                className="auth-input"
                {...formNueva.register('fechaPlanificada', { required: 'Obligatorio' })}
              />
            </div>

            <div>
              <label className="auth-label">Fecha limite *</label>
              <input
                type="date"
                className="auth-input"
                {...formNueva.register('fechaCompromiso', { required: 'Obligatorio' })}
              />
            </div>

            <div style={{ gridColumn: 'span 2' }}>
              <label className="auth-label">Observaciones</label>
              <textarea
                className="auth-input"
                rows={3}
                placeholder="Notas internas para seguimiento..."
                {...formNueva.register('observaciones')}
              />
            </div>
          </div>

          {error && (
            <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>
              {error}
            </div>
          )}
        </form>
      </Modal>

      <Modal
        abierto={!!modalEliminar}
        titulo="Eliminar actividad preventiva"
        ancho="sm"
        onCerrar={() => setModalEliminar(null)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalEliminar(null)}>
              Cancelar
            </button>
            <button className="btn btn-danger" onClick={onEliminar} disabled={guardando}>
              {guardando ? 'Eliminando...' : 'Confirmar eliminacion'}
            </button>
          </>
        }
      >
        <div className="alert-item alert-item--peligro">
          Se eliminara la actividad <strong>{modalEliminar?.titulo}</strong>.
        </div>
      </Modal>
    </>
  );
}
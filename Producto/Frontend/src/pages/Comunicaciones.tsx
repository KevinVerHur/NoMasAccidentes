import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import Badge from '../components/ui/Badge';
import KpiCard from '../components/ui/KpiCard';
import Modal from '../components/ui/Modal';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import { listarClientes } from '../api/clientes';
import { crearConsulta, listarConsultas, listarConsultasPorCliente } from '../api/consultas';
import type {
  ClienteResponse,
  ConsultaResponse,
  CrearConsultaRequest,
  VarianteBadge,
} from '../types';

function fmtFecha(iso: string) {
  return new Date(iso).toLocaleString('es-CL');
}

function mensajeError(e: unknown, fallback: string) {
  return (e as { response?: { data?: { mensaje?: string; message?: string } } })?.response?.data?.mensaje
    ?? (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    ?? fallback;
}

const badgeHorario = (consulta: ConsultaResponse): { variante: VarianteBadge; texto: string } => {
  if (consulta.fueraHorario) return { variante: 'yellow', texto: 'Fuera de horario' };
  return { variante: 'green', texto: 'Horario habil' };
};

export default function Comunicaciones() {
  const { rol } = useAuth();
  const [consultas, setConsultas] = useState<ConsultaResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [filtroCliente, setFiltroCliente] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [modalNueva, setModalNueva] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const formNueva = useForm<CrearConsultaRequest>();

  const cargarConsultas = useCallback(async () => {
    setCargando(true);

    try {
      if (filtroCliente) {
        const data = await listarConsultasPorCliente(Number(filtroCliente));
        setConsultas(data);
      } else if (rol === 'PROFESIONAL') {
        setConsultas([]);
      } else {
        const data = await listarConsultas(0, 200);
        setConsultas(data.content);
      }
    } finally {
      setCargando(false);
    }
  }, [filtroCliente, rol]);

  useEffect(() => {
    cargarConsultas();
  }, [cargarConsultas]);

  useEffect(() => {
    listarClientes(0, 200)
      .then((data) => setClientes(data.content))
      .catch(() => setClientes([]));
  }, []);

  const filtradas = consultas.filter((consulta) => {
    const texto = busqueda.toLowerCase();
    return !texto
      || consulta.cliente.toLowerCase().includes(texto)
      || consulta.motivo.toLowerCase().includes(texto)
      || (consulta.detalle ?? '').toLowerCase().includes(texto);
  });

  const fueraHorario = consultas.filter((consulta) => consulta.fueraHorario).length;
  const conCosto = consultas.filter((consulta) => consulta.costoAdicional).length;
  const dentroHorario = consultas.length - fueraHorario;

  function abrirNueva() {
    formNueva.reset({
      idCliente: filtroCliente ? Number(filtroCliente) : undefined as unknown as number,
      motivo: '',
      detalle: '',
    });

    setError(null);
    setModalNueva(true);
  }

  async function onCrear(data: CrearConsultaRequest) {
    setGuardando(true);
    setError(null);

    try {
      await crearConsulta(data);
      setModalNueva(false);
      await cargarConsultas();
    } catch (e) {
      setError(mensajeError(e, 'No se pudo registrar la consulta.'));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <div className="page-title">Comunicaciones</div>
      <div className="page-subtitle">
        Historial de consultas del centro de llamados
      </div>

      <div className="kpi-row">
        <KpiCard label="Consultas" value={consultas.length} />
        <KpiCard label="En horario" value={dentroHorario} variante="ok" />
        <KpiCard label="Fuera de horario" value={fueraHorario} variante="warn" />
        <KpiCard label="Con costo adicional" value={conCosto} variante="peligro" />
      </div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          {error}
        </div>
      )}

      <Panel
        titulo="Registro de consultas"
        accion={
          rol === 'ADMIN' ? (
            <button className="btn btn-sm btn-primary" onClick={abrirNueva}>
              + Nueva consulta
            </button>
          ) : undefined
        }
      >
        <div className="searchbar">
          <input
            placeholder="Buscar por cliente, motivo o detalle..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />

          <select value={filtroCliente} onChange={(e) => setFiltroCliente(e.target.value)}>
            <option value="">Todos los clientes</option>
            {clientes.map((cliente) => (
              <option key={cliente.id} value={cliente.id}>
                {cliente.razonSocial}
              </option>
            ))}
          </select>
        </div>

        {cargando ? (
          <div className="placeholder">Cargando consultas...</div>
        ) : filtradas.length === 0 ? (
          <div className="placeholder">
            {rol === 'PROFESIONAL' && !filtroCliente
              ? 'Selecciona un cliente para ver su historial de consultas.'
              : 'No hay consultas registradas.'}
          </div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Fecha</th>
                <th>Motivo</th>
                <th>Detalle</th>
                <th>Horario</th>
                <th>Costo</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map((consulta) => {
                const horario = badgeHorario(consulta);
                return (
                  <tr key={consulta.id}>
                    <td>{consulta.cliente}</td>
                    <td>{fmtFecha(consulta.fechaHora)}</td>
                    <td>{consulta.motivo}</td>
                    <td>{consulta.detalle ?? '-'}</td>
                    <td>
                      <Badge variante={horario.variante}>{horario.texto}</Badge>
                    </td>
                    <td>
                      <Badge variante={consulta.costoAdicional ? 'red' : 'gray'}>
                        {consulta.costoAdicional ? 'Adicional' : 'Sin costo'}
                      </Badge>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </Panel>

      <Modal
        abierto={modalNueva}
        titulo="Nueva consulta"
        onCerrar={() => setModalNueva(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalNueva(false)}>
              Cancelar
            </button>
            <button className="btn btn-primary" form="form-consulta-nueva" type="submit" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Registrar consulta'}
            </button>
          </>
        }
      >
        <form id="form-consulta-nueva" onSubmit={formNueva.handleSubmit(onCrear)} noValidate>
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <label className="auth-label">Cliente *</label>
              <select
                className="auth-input"
                {...formNueva.register('idCliente', {
                  required: 'Obligatorio',
                  valueAsNumber: true,
                })}
              >
                <option value="">Seleccionar...</option>
                {clientes.map((cliente) => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.razonSocial}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="auth-label">Motivo *</label>
              <input
                className="auth-input"
                placeholder="Ej: Consulta normativa"
                {...formNueva.register('motivo', { required: 'Obligatorio' })}
              />
            </div>

            <div>
              <label className="auth-label">Detalle</label>
              <textarea
                className="auth-input"
                rows={4}
                placeholder="Detalle de la consulta recibida..."
                {...formNueva.register('detalle')}
              />
            </div>
          </div>

          <div className="alert-item alert-item--info" style={{ marginTop: 12 }}>
            El backend marca automaticamente si la consulta fue registrada fuera de horario y si genera costo adicional.
          </div>

          {error && (
            <div className="auth-alert auth-alert--error" style={{ marginTop: 12 }}>
              {error}
            </div>
          )}
        </form>
      </Modal>
    </>
  );
}

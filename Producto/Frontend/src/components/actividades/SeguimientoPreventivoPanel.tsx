import { useCallback, useEffect, useMemo, useState } from 'react';
import Panel from '../ui/Panel';
import Badge from '../ui/Badge';
import Modal from '../ui/Modal';
import {
  cambiarEstadoActividadPreventiva,
  listarActividadesPreventivas,
  misActividadesPreventivas,
  reportarCumplimiento,
} from '../../api/ActividadesPreventivas';
import type {
  ActividadPreventivaResponse,
  EstadoActividadPreventiva,
  VarianteBadge,
} from '../../types';

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

const fmtFecha = (iso: string | null) =>
  iso ? new Date(iso).toLocaleDateString('es-CL') : '-';

interface Props {
  titulo?: string;
  idCliente?: number;
  modoCliente?: boolean;
  compacto?: boolean;
  editable?: boolean;
}

export default function SeguimientoPreventivoPanel({
  titulo = 'Seguimiento preventivo',
  idCliente,
  modoCliente = false,
  compacto = false,
  editable = false,
}: Props) {
  const [actividades, setActividades] = useState<ActividadPreventivaResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [estado, setEstado] = useState('');
  const [reportando, setReportando] = useState<ActividadPreventivaResponse | null>(null);
  const [comentarioReporte, setComentarioReporte] = useState('');
  const [guardandoReporte, setGuardandoReporte] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);

    try {
      if (modoCliente) {
        const data = await misActividadesPreventivas();
        setActividades(data);
      } else {
        const data = await listarActividadesPreventivas(
          0,
          100,
          idCliente,
          estado ? estado as EstadoActividadPreventiva : undefined,
        );
        setActividades(data.content);
      }
    } finally {
      setCargando(false);
    }
  }, [idCliente, modoCliente, estado]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const filtradas = useMemo(() => {
    const texto = busqueda.toLowerCase();

    return actividades.filter((a) =>
      !texto ||
      a.razonSocialEmpresa.toLowerCase().includes(texto) ||
      a.titulo.toLowerCase().includes(texto) ||
      (a.responsable ?? '').toLowerCase().includes(texto)
    );
  }, [actividades, busqueda]);

  async function cambiarEstado(id: number, nuevoEstado: EstadoActividadPreventiva) {
    await cambiarEstadoActividadPreventiva(id, { estado: nuevoEstado });
    await cargar();
  }

  async function confirmarReporte() {
    if (!reportando) return;
    setGuardandoReporte(true);
    try {
      await reportarCumplimiento(reportando.id, comentarioReporte.trim());
      setReportando(null);
      setComentarioReporte('');
      await cargar();
    } finally {
      setGuardandoReporte(false);
    }
  }

  return (
    <>
    <Panel titulo={titulo}>
      {!compacto && (
        <div className="searchbar">
          <input
            placeholder="Buscar por cliente, actividad o responsable..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />

          <select value={estado} onChange={(e) => setEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="EN_CURSO">En curso</option>
            <option value="CUMPLIDA">Cumplida</option>
            <option value="VENCIDA">Vencida</option>
          </select>
        </div>
      )}

      {cargando ? (
        <div className="placeholder">Cargando actividades preventivas...</div>
      ) : filtradas.length === 0 ? (
        <div className="placeholder">No hay actividades preventivas registradas.</div>
      ) : (
        <table className="app-table">
          <thead>
            <tr>
              {!idCliente && !modoCliente && <th>Cliente</th>}
              <th>Actividad preventiva</th>
              <th>Responsable</th>
              <th>Fecha limite</th>
              <th>Estado</th>
              {!compacto && <th>Observaciones</th>}
              {modoCliente && <th>Cumplimiento</th>}
              {editable && <th>Accion</th>}
            </tr>
          </thead>
          <tbody>
            {filtradas.map((a) => (
              <tr key={a.id}>
                {!idCliente && !modoCliente && <td>{a.razonSocialEmpresa}</td>}
                <td>
                  <div style={{ fontWeight: 600, color: '#1a3a5c' }}>{a.titulo}</div>
                  <div style={{ fontSize: 11, color: '#6b7280' }}>{a.descripcion ?? '-'}</div>
                </td>
                <td>{a.responsable ?? '-'}</td>
                <td>{fmtFecha(a.fechaCompromiso)}</td>
                <td>
                  <Badge variante={badgePorEstado[a.estado]}>
                    {labelEstado[a.estado]}
                  </Badge>
                </td>
                {!compacto && <td>{a.observaciones ?? '-'}</td>}
                {modoCliente && (
                  <td>
                    {a.reportadoPorCliente ? (
                      <span className="text-[12px] text-ok font-bold">Reportado ✓</span>
                    ) : a.estado !== 'CUMPLIDA' ? (
                      <button className="btn btn-sm btn-primary" onClick={() => { setReportando(a); setComentarioReporte(''); }}>
                        Reportar cumplimiento
                      </button>
                    ) : (
                      <span className="text-gray-400">—</span>
                    )}
                  </td>
                )}
                {editable && (
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
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Panel>

    <Modal
      abierto={!!reportando}
      titulo="Reportar cumplimiento"
      ancho="sm"
      onCerrar={() => setReportando(null)}
      footer={
        <>
          <button className="btn btn-outline" onClick={() => setReportando(null)} disabled={guardandoReporte}>
            Cancelar
          </button>
          <button className="btn btn-primary" onClick={confirmarReporte} disabled={guardandoReporte}>
            {guardandoReporte ? 'Enviando…' : 'Reportar'}
          </button>
        </>
      }
    >
      <p className="text-[13px] text-gray-600" style={{ marginBottom: 8 }}>
        Le avisarás a la consultora que cumpliste tu parte de <strong>{reportando?.titulo}</strong>.
        Un profesional lo verificará y la marcará como cumplida.
      </p>
      <textarea
        className="auth-input"
        rows={3}
        maxLength={500}
        placeholder="Comentario o detalle de la evidencia (opcional)"
        value={comentarioReporte}
        onChange={(e) => setComentarioReporte(e.target.value)}
      />
    </Modal>
    </>
  );
}
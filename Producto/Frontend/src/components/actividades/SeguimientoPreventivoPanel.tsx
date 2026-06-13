import { useCallback, useEffect, useMemo, useState } from 'react';
import Panel from '../ui/Panel';
import Badge from '../ui/Badge';
import {
  cambiarEstadoActividadPreventiva,
  listarActividadesPreventivas,
  misActividadesPreventivas,
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
      a.razonSocialCliente.toLowerCase().includes(texto) ||
      a.titulo.toLowerCase().includes(texto) ||
      (a.responsable ?? '').toLowerCase().includes(texto)
    );
  }, [actividades, busqueda]);

  async function cambiarEstado(id: number, nuevoEstado: EstadoActividadPreventiva) {
    await cambiarEstadoActividadPreventiva(id, { estado: nuevoEstado });
    await cargar();
  }

  return (
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
              {editable && <th>Accion</th>}
            </tr>
          </thead>
          <tbody>
            {filtradas.map((a) => (
              <tr key={a.id}>
                {!idCliente && !modoCliente && <td>{a.razonSocialCliente}</td>}
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
  );
}
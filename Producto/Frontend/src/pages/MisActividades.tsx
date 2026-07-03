import { useEffect, useState } from 'react';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type {
  VisitaResponse,
  AsesoriaResponse,
  CapacitacionResponse,
  EstadoVisita,
  EstadoAsesoria,
  EstadoCapacitacion,
  VarianteBadge,
} from '../types';
import { misVisitas } from '../api/visitas';
import { misAsesoriasCliente } from '../api/asesorias';
import { misCapacitaciones } from '../api/capacitaciones';

const badgeVisita: Record<EstadoVisita, VarianteBadge> = {
  PROGRAMADA: 'blue',
  EN_CURSO: 'yellow',
  REALIZADA: 'green',
  CANCELADA: 'gray',
};
const badgeAsesoria: Record<EstadoAsesoria, VarianteBadge> = {
  SOLICITADA: 'yellow',
  EN_PROCESO: 'blue',
  CERRADA: 'green',
  CANCELADA: 'gray',
};
const badgeCapacitacion: Record<EstadoCapacitacion, VarianteBadge> = {
  PROGRAMADA: 'blue',
  EN_CURSO: 'yellow',
  REALIZADA: 'green',
  CANCELADA: 'gray',
};

const fmtFecha = (iso: string | null) =>
  iso ? new Date(iso).toLocaleDateString('es-CL') : '—';
const hora = (h: string | null) => (h ? h.slice(0, 5) : '—');

export default function MisActividades() {
  const [visitas, setVisitas] = useState<VisitaResponse[]>([]);
  const [asesorias, setAsesorias] = useState<AsesoriaResponse[]>([]);
  const [capacitaciones, setCapacitaciones] = useState<CapacitacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    Promise.allSettled([misVisitas(), misAsesoriasCliente(), misCapacitaciones()])
      .then(([v, a, c]) => {
        if (v.status === 'fulfilled') setVisitas(v.value);
        if (a.status === 'fulfilled') setAsesorias(a.value);
        if (c.status === 'fulfilled') setCapacitaciones(c.value);
      })
      .finally(() => setCargando(false));
  }, []);

  return (
    <>
      <div className="page-title">Mis actividades</div>
      <div className="page-subtitle">
        Historial de tus visitas, asesorías y capacitaciones (solo lectura)
      </div>

      <Panel titulo="📅 Visitas">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : visitas.length === 0 ? (
          <div className="placeholder">No tienes visitas registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Fecha</th><th>Profesional</th><th>Tipo de revisión</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {visitas.map((v) => (
                <tr key={v.id}>
                  <td>{fmtFecha(v.fechaProgramada)}</td>
                  <td>{v.nombreProfesional}</td>
                  <td>{v.tipoRevision ?? '—'}</td>
                  <td><Badge variante={badgeVisita[v.estado]}>{v.estado}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      <Panel titulo="📋 Asesorías">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : asesorias.length === 0 ? (
          <div className="placeholder">No tienes asesorías registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Fecha</th><th>Tipo</th><th>Motivo</th><th>Profesional</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {asesorias.map((a) => (
                <tr key={a.id}>
                  <td>{fmtFecha(a.fechaSolicitud)}</td>
                  <td>{a.tipo}</td>
                  <td>{a.motivo}</td>
                  <td>{a.nombreProfesional}</td>
                  <td>
                    <Badge variante={badgeAsesoria[a.estado]}>{a.estado}</Badge>
                    {a.esAsesoriaExtra && (
                      <span className="ml-1 text-[11px] text-warn font-bold">EXTRA</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      <Panel titulo="🎓 Capacitaciones">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : capacitaciones.length === 0 ? (
          <div className="placeholder">No tienes capacitaciones registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Curso</th><th>Fecha</th><th>Hora</th><th>Lugar</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {capacitaciones.map((c) => (
                <tr key={c.id}>
                  <td>{c.curso}</td>
                  <td>{fmtFecha(c.fechaProgramada)}</td>
                  <td>{hora(c.horaProgramada)}</td>
                  <td>{c.lugar}</td>
                  <td>
                    <Badge variante={badgeCapacitacion[c.estado]}>{c.estado}</Badge>
                    {c.esCapacitacionExtra && (
                      <span className="ml-1 text-[11px] text-warn font-bold">EXTRA</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>
    </>
  );
}

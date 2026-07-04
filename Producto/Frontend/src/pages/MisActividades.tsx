import { useEffect, useState } from 'react';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type {
  VisitaResponse,
  AsesoriaResponse,
  InformeResponse,
  InformeAsesoriaResponse,
  EstadoVisita,
  EstadoAsesoria,
  VarianteBadge,
} from '../types';
import { misVisitas } from '../api/visitas';
import { misAsesoriasCliente } from '../api/asesorias';
import {
  listarMisInformes,
  listarMisInformesAsesoria,
  descargarMiInformePdf,
} from '../api/informes';

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

const fmtFecha = (iso: string | null) =>
  iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

async function descargarInforme(idInforme: number) {
  try {
    await descargarMiInformePdf(idInforme);
  } catch {
    alert('No se pudo descargar el informe.');
  }
}

export default function MisActividades() {
  const [visitas, setVisitas] = useState<VisitaResponse[]>([]);
  const [asesorias, setAsesorias] = useState<AsesoriaResponse[]>([]);
  const [informePorVisita, setInformePorVisita] = useState<Record<number, InformeResponse>>({});
  const [informePorAsesoria, setInformePorAsesoria] = useState<Record<number, InformeAsesoriaResponse>>({});
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    Promise.allSettled([
      misVisitas(),
      misAsesoriasCliente(),
      listarMisInformes(),
      listarMisInformesAsesoria(),
    ])
      .then(([v, a, iv, ia]) => {
        if (v.status === 'fulfilled') setVisitas(v.value);
        if (a.status === 'fulfilled') setAsesorias(a.value);
        if (iv.status === 'fulfilled') {
          setInformePorVisita(Object.fromEntries(iv.value.map((i) => [i.idVisita, i])));
        }
        if (ia.status === 'fulfilled') {
          setInformePorAsesoria(Object.fromEntries(ia.value.map((i) => [i.idAsesoria, i])));
        }
      })
      .finally(() => setCargando(false));
  }, []);

  const botonInforme = (informe: InformeResponse | InformeAsesoriaResponse | undefined) =>
    informe && informe.tieneArchivo ? (
      <button className="btn btn-sm btn-outline" onClick={() => descargarInforme(informe.id)}>
        📄 Descargar
      </button>
    ) : (
      <span className="text-gray-400">—</span>
    );

  return (
    <>
      <div className="page-title">Mis actividades</div>
      <div className="page-subtitle">
        Historial de tus visitas y asesorías (solo lectura)
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
                <th>Fecha</th><th>Profesional</th><th>Tipo de revisión</th><th>Estado</th><th>Informe</th>
              </tr>
            </thead>
            <tbody>
              {visitas.map((v) => (
                <tr key={v.id}>
                  <td>{fmtFecha(v.fechaProgramada)}</td>
                  <td>{v.nombreProfesional}</td>
                  <td>{v.tipoRevision ?? '—'}</td>
                  <td><Badge variante={badgeVisita[v.estado]}>{v.estado}</Badge></td>
                  <td>{botonInforme(informePorVisita[v.id])}</td>
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
                <th>Fecha</th><th>Tipo</th><th>Motivo</th><th>Profesional</th><th>Estado</th><th>Informe</th>
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
                  <td>{botonInforme(informePorAsesoria[a.id])}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>
    </>
  );
}

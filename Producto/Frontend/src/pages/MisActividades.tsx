import { useEffect, useState } from 'react';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type { VisitaResponse, InformeResponse, EstadoVisita, VarianteBadge } from '../types';
import { misVisitas } from '../api/visitas';
import { listarMisInformes, descargarMiInformePdf } from '../api/informes';

const badgePorEstado: Record<EstadoVisita, VarianteBadge> = {
  PROGRAMADA: 'blue',
  EN_CURSO:   'yellow',
  REALIZADA:  'green',
  CANCELADA:  'gray',
};

const labelEstado: Record<EstadoVisita, string> = {
  PROGRAMADA: 'Programada',
  EN_CURSO:   'En curso',
  REALIZADA:  'Realizada',
  CANCELADA:  'Cancelada',
};

const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

export default function MisActividades() {
  const [visitas, setVisitas]   = useState<VisitaResponse[]>([]);
  const [informePorVisita, setInformePorVisita] = useState<Map<number, InformeResponse>>(new Map());
  const [cargando, setCargando] = useState(true);
  const [descargando, setDescargando] = useState<number | null>(null);

  useEffect(() => {
    Promise.all([misVisitas(), listarMisInformes()])
      .then(([vs, informes]) => {
        setVisitas(vs);
        setInformePorVisita(new Map(informes.map(i => [i.idVisita, i])));
      })
      .catch(() => {})
      .finally(() => setCargando(false));
  }, []);

  const realizadas  = visitas.filter(v => v.estado === 'REALIZADA').length;
  const programadas = visitas.filter(v => v.estado === 'PROGRAMADA').length;

  async function onDescargar(idInforme: number) {
    setDescargando(idInforme);
    try {
      await descargarMiInformePdf(idInforme);
    } finally {
      setDescargando(null);
    }
  }

  return (
    <>
      <div className="page-title">Mis actividades</div>
      <div className="page-subtitle">Visitas realizadas a tu empresa y sus informes</div>

      <div className="kpi-row">
        <KpiCard label="Realizadas"  value={realizadas} variante="ok" />
        <KpiCard label="Programadas" value={programadas} variante="warn" />
        <KpiCard label="Total"       value={visitas.length} />
      </div>

      <Panel titulo="📅 Visitas">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : visitas.length === 0 ? (
          <div className="placeholder">Aún no tienes visitas registradas.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr><th>Fecha</th><th>Profesional</th><th>Tipo</th><th>Estado</th><th>Informe</th></tr>
            </thead>
            <tbody>
              {visitas.map(v => {
                const informe = informePorVisita.get(v.id);
                return (
                  <tr key={v.id}>
                    <td>{fmtFecha(v.fechaProgramada)}</td>
                    <td>{v.nombreProfesional}</td>
                    <td>{v.tipoRevision ?? '—'}</td>
                    <td><Badge variante={badgePorEstado[v.estado]}>{labelEstado[v.estado]}</Badge></td>
                    <td>
                      {informe && informe.tieneArchivo ? (
                        <button className="btn btn-sm btn-outline" disabled={descargando === informe.id} onClick={() => onDescargar(informe.id)}>
                          {descargando === informe.id ? 'Descargando...' : '📄 Descargar'}
                        </button>
                      ) : v.estado === 'REALIZADA' ? (
                        <span style={{ fontSize: 12, color: '#9ca3af' }}>Sin informe aún</span>
                      ) : (
                        <span style={{ fontSize: 12, color: '#9ca3af' }}>—</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </Panel>
    </>
  );
}

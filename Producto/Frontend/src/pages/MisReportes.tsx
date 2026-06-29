import { useEffect, useState } from 'react';
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend,
} from 'recharts';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import type { ReporteMensualResponse, AccidentabilidadMensualResponse } from '../types';
import { misReportes, descargarMiReportePdf, miAccidentabilidad } from '../api/reportes';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
const MESES_CORTO = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

export default function MisReportes() {
  const anioActual = new Date().getFullYear();
  const [reportes, setReportes] = useState<ReporteMensualResponse[]>([]);
  const [accidentabilidad, setAccidentabilidad] = useState<AccidentabilidadMensualResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [descargando, setDescargando] = useState<number | null>(null);

  useEffect(() => {
    Promise.all([misReportes(), miAccidentabilidad(anioActual)])
      .then(([reps, acc]) => { setReportes(reps); setAccidentabilidad(acc); })
      .catch(() => {})
      .finally(() => setCargando(false));
  }, [anioActual]);

  async function onDescargar(id: number) {
    setDescargando(id);
    try {
      await descargarMiReportePdf(id);
    } finally {
      setDescargando(null);
    }
  }

  const ultimo = reportes[0];
  const dataGrafico = accidentabilidad.map(a => ({
    mes: MESES_CORTO[a.mes - 1],
    accidentes: a.totalAccidentes,
    diasPerdidos: a.diasPerdidos,
  }));

  return (
    <>
      <div className="page-title">Mis reportes</div>
      <div className="page-subtitle">Reportes mensuales de gestión de tu empresa</div>

      {ultimo && (
        <div className="kpi-row">
          <KpiCard label="Último periodo" value={`${MESES[ultimo.mes - 1]} ${ultimo.anio}`} />
          <KpiCard label="Visitas" value={ultimo.totalVisitas} variante="ok" />
          <KpiCard label="Capacitaciones" value={ultimo.totalCapacitaciones} variante="ok" />
          <KpiCard label="Accidentes" value={ultimo.totalAccidentes} variante={ultimo.totalAccidentes > 0 ? 'peligro' : 'ok'} />
        </div>
      )}

      <Panel titulo="📈 Tendencia de accidentabilidad del año">
        {dataGrafico.every(d => d.accidentes === 0 && d.diasPerdidos === 0) ? (
          <div className="placeholder">Sin accidentes registrados este año.</div>
        ) : (
          <div style={{ width: '100%', height: 280, padding: 12 }}>
            <ResponsiveContainer>
              <LineChart data={dataGrafico}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
                <XAxis dataKey="mes" fontSize={12} />
                <YAxis allowDecimals={false} fontSize={12} />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="accidentes" name="Accidentes" stroke="#e23d3d" strokeWidth={2} />
                <Line type="monotone" dataKey="diasPerdidos" name="Días perdidos" stroke="#f0a500" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </Panel>

      <Panel titulo="📄 Historial de reportes">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : reportes.length === 0 ? (
          <div className="placeholder">Aún no tienes reportes disponibles.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Periodo</th><th>Emitido</th><th>Visitas</th><th>Capacitaciones</th>
                <th>Asesorías</th><th>Accidentes</th><th>Multas</th><th>Costos extra</th><th>PDF</th>
              </tr>
            </thead>
            <tbody>
              {reportes.map(r => (
                <tr key={r.id}>
                  <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{MESES[r.mes - 1]} {r.anio}</td>
                  <td>{fmtFecha(r.fechaEmision)}</td>
                  <td>{r.totalVisitas}</td>
                  <td>{r.totalCapacitaciones}</td>
                  <td>{r.totalAsesorias}</td>
                  <td>{r.totalAccidentes}</td>
                  <td>{r.totalMultas}</td>
                  <td>{clp(r.costosExtra)}</td>
                  <td>
                    {r.tieneArchivo && (
                      <button className="btn btn-sm btn-outline" disabled={descargando === r.id} onClick={() => onDescargar(r.id)}>
                        {descargando === r.id ? 'Descargando...' : 'Descargar'}
                      </button>
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

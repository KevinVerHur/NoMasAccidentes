import { useCallback, useEffect, useState } from 'react';
import {
  ResponsiveContainer, LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, Tooltip, CartesianGrid, Legend,
} from 'recharts';
import KpiCard from '../components/ui/KpiCard';
import Panel from '../components/ui/Panel';
import Modal from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';
import type {
  EmpresaResponse, ReporteMensualResponse,
  AccidentabilidadMensualResponse, RendimientoProfesionalResponse,
} from '../types';
import { listarClientes } from '../api/clientes';
import {
  generarReporte, listarReportesPorCliente, descargarReportePdf,
  ejecutarCierreMensual, accidentabilidad, rendimientoProfesional,
} from '../api/reportes';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
const MESES_CORTO = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
const COLORES_PIE = ['#1a5fb4', '#2a9d8f', '#e9c46a', '#f0a500', '#e23d3d', '#8e44ad'];

const clp = (n: number) => n.toLocaleString('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });
const fmtFecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-CL') : '—';
const mensajeError = (e: unknown, fallback: string) =>
  (e as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje ?? fallback;

export default function ReportesGestion() {
  const { rol } = useAuth();
  const esAdmin = rol === 'ADMIN';
  const hoy = new Date();
  const anios = [hoy.getFullYear(), hoy.getFullYear() - 1, hoy.getFullYear() - 2];

  const [clientes, setClientes] = useState<EmpresaResponse[]>([]);
  const [idEmpresa, setIdEmpresa] = useState<number | null>(null);
  const [mes, setMes] = useState(hoy.getMonth() + 1);
  const [anio, setAnio] = useState(hoy.getFullYear());

  const [historial, setHistorial] = useState<ReporteMensualResponse[]>([]);
  const [reporteGenerado, setReporteGenerado] = useState<ReporteMensualResponse | null>(null);
  const [accData, setAccData] = useState<AccidentabilidadMensualResponse[]>([]);
  const [rendData, setRendData] = useState<RendimientoProfesionalResponse[]>([]);

  const [generando, setGenerando] = useState(false);
  const [descargando, setDescargando] = useState<number | null>(null);
  const [modalCierre, setModalCierre] = useState(false);
  const [cerrando, setCerrando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listarClientes(0, 200).then(d => setClientes(d.content)).catch(() => {});
  }, []);

  const cargarCliente = useCallback(async (id: number, año: number) => {
    const [reps, acc] = await Promise.all([
      listarReportesPorCliente(id),
      accidentabilidad(id, año),
    ]);
    setHistorial(reps);
    setAccData(acc);
  }, []);

  useEffect(() => { if (idEmpresa != null) cargarCliente(idEmpresa, anio).catch(() => {}); }, [idEmpresa, anio, cargarCliente]);
  useEffect(() => { rendimientoProfesional(mes, anio).then(setRendData).catch(() => {}); }, [mes, anio]);

  async function onGenerar() {
    if (idEmpresa == null) return;
    setError(null);
    setAviso(null);
    setGenerando(true);
    try {
      const reporte = await generarReporte(idEmpresa, mes, anio);
      setReporteGenerado(reporte);
      setAviso(`Reporte de ${MESES[mes - 1]} ${anio} generado.`);
      await cargarCliente(idEmpresa, anio);
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al generar el reporte.'));
    } finally {
      setGenerando(false);
    }
  }

  async function onDescargar(id: number) {
    setDescargando(id);
    try {
      await descargarReportePdf(id);
    } finally {
      setDescargando(null);
    }
  }

  async function onCierreMensual() {
    setError(null);
    setAviso(null);
    setCerrando(true);
    try {
      const r = await ejecutarCierreMensual(mes, anio);
      setAviso(`Cierre mensual ejecutado: ${r.reportesGenerados} reporte(s) generados y enviados por correo.`);
      setModalCierre(false);
      if (idEmpresa != null) await cargarCliente(idEmpresa, anio);
    } catch (e: unknown) {
      setError(mensajeError(e, 'Error al ejecutar el cierre mensual.'));
    } finally {
      setCerrando(false);
    }
  }

  const kpis = reporteGenerado ?? historial[0];
  const dataAcc = accData.map(a => ({ mes: MESES_CORTO[a.mes - 1], accidentes: a.totalAccidentes, tasa: a.tasa }));
  const composicion = reporteGenerado ? [
    { name: 'Visitas', value: reporteGenerado.totalVisitas },
    { name: 'Capacitaciones', value: reporteGenerado.totalCapacitaciones },
    { name: 'Asesorías', value: reporteGenerado.totalAsesorias },
    { name: 'Llamados', value: reporteGenerado.totalLlamados },
    { name: 'Accidentes', value: reporteGenerado.totalAccidentes },
    { name: 'Multas', value: reporteGenerado.totalMultas },
  ].filter(d => d.value > 0) : [];

  return (
    <>
      <div className="page-title">Reportes e indicadores</div>
      <div className="page-subtitle">Reportes mensuales de gestión por cliente e indicadores</div>

      {aviso && <div className="alert-item alert-item--ok" style={{ marginBottom: 12 }}>{aviso}</div>}
      {error && <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>{error}</div>}

      <Panel
        titulo="Generar reporte mensual"
        accion={esAdmin
          ? <button className="btn btn-sm btn-warn" onClick={() => { setError(null); setModalCierre(true); }}>Cierre mensual (todos)</button>
          : undefined}
      >
        <div className="btn-group" style={{ flexWrap: 'wrap', alignItems: 'flex-end', gap: 12, paddingTop: 12, paddingBottom: 12, paddingLeft: 16, paddingRight: 16 }}>
          <div style={{ flex: 1, minWidth: 200 }}>
            <label className="auth-label">Cliente</label>
            <select className="auth-input" value={idEmpresa ?? ''} onChange={e => { setReporteGenerado(null); setIdEmpresa(e.target.value ? Number(e.target.value) : null); }}>
              <option value="">Seleccionar cliente...</option>
              {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
            </select>
          </div>
          <div>
            <label className="auth-label">Mes</label>
            <select className="auth-input" value={mes} onChange={e => setMes(Number(e.target.value))}>
              {MESES.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
            </select>
          </div>
          <div>
            <label className="auth-label">Año</label>
            <select className="auth-input" value={anio} onChange={e => setAnio(Number(e.target.value))}>
              {anios.map(a => <option key={a} value={a}>{a}</option>)}
            </select>
          </div>
          <button className="btn btn-primary" disabled={idEmpresa == null || generando} onClick={onGenerar}>
            {generando ? 'Generando...' : 'Generar reporte'}
          </button>
        </div>
      </Panel>

      {kpis && (
        <div className="kpi-row">
          <KpiCard label="Visitas" value={kpis.totalVisitas} variante="ok" />
          <KpiCard label="Capacitaciones" value={kpis.totalCapacitaciones} variante="ok" />
          <KpiCard label="Asesorías" value={kpis.totalAsesorias} />
          <KpiCard label="Llamados" value={kpis.totalLlamados} />
          <KpiCard label="Accidentes" value={kpis.totalAccidentes} variante={kpis.totalAccidentes > 0 ? 'peligro' : 'ok'} />
          <KpiCard label="Costos extra" value={clp(kpis.costosExtra)} />
        </div>
      )}

      <div className="grid-2">
        <Panel titulo={`Accidentabilidad ${idEmpresa != null ? anio : '(elige un cliente)'}`}>
          {idEmpresa == null ? (
            <div className="placeholder">Selecciona un cliente para ver su accidentabilidad.</div>
          ) : (
            <div className="responsive-chart">
              <ResponsiveContainer>
                <LineChart data={dataAcc}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
                  <XAxis dataKey="mes" fontSize={12} />
                  <YAxis allowDecimals={false} fontSize={12} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="accidentes" name="Accidentes" stroke="#e23d3d" strokeWidth={2} />
                  <Line type="monotone" dataKey="tasa" name="Tasa (%)" stroke="#1a5fb4" strokeWidth={2} connectNulls />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </Panel>

        <Panel titulo="Composición del periodo">
          {composicion.length === 0 ? (
            <div className="placeholder">Genera un reporte para ver su composición.</div>
          ) : (
            <div className="responsive-chart">
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={composicion} dataKey="value" nameKey="name" outerRadius={90} label>
                    {composicion.map((_, i) => <Cell key={i} fill={COLORES_PIE[i % COLORES_PIE.length]} />)}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </Panel>
      </div>

      <Panel titulo={`Rendimiento de profesionales — ${MESES[mes - 1]} ${anio}`}>
        {rendData.length === 0 ? (
          <div className="placeholder">Sin profesionales o sin datos para el periodo.</div>
        ) : (
          <div className="responsive-chart responsive-chart--tall">
            <ResponsiveContainer>
              <BarChart data={rendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
                <XAxis dataKey="nombreProfesional" fontSize={11} />
                <YAxis allowDecimals={false} fontSize={12} />
                <Tooltip />
                <Legend />
                <Bar dataKey="visitasRealizadas" name="Visitas" fill="#1a5fb4" />
                <Bar dataKey="asesoriasAtendidas" name="Asesorías" fill="#2a9d8f" />
                <Bar dataKey="capacitacionesDictadas" name="Capacitaciones" fill="#f0a500" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </Panel>

      <Panel titulo="Historial de reportes del cliente">
        {idEmpresa == null ? (
          <div className="placeholder">Selecciona un cliente para ver su historial.</div>
        ) : historial.length === 0 ? (
          <div className="placeholder">Este cliente no tiene reportes generados.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Periodo</th><th>Emitido</th><th>Visitas</th><th>Capacitaciones</th>
                <th>Asesorías</th><th>Llamados</th><th>Accidentes</th><th>Multas</th><th>Costos extra</th><th>PDF</th>
              </tr>
            </thead>
            <tbody>
              {historial.map(r => (
                <tr key={r.id}>
                  <td style={{ fontWeight: 600, color: '#1a3a5c' }}>{MESES[r.mes - 1]} {r.anio}</td>
                  <td>{fmtFecha(r.fechaEmision)}</td>
                  <td>{r.totalVisitas}</td>
                  <td>{r.totalCapacitaciones}</td>
                  <td>{r.totalAsesorias}</td>
                  <td>{r.totalLlamados}</td>
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

      <Modal
        abierto={modalCierre}
        titulo="Ejecutar cierre mensual"
        ancho="sm"
        onCerrar={() => setModalCierre(false)}
        footer={
          <>
            <button className="btn btn-outline" onClick={() => setModalCierre(false)}>Cancelar</button>
            <button className="btn btn-warn" disabled={cerrando} onClick={onCierreMensual}>
              {cerrando ? 'Ejecutando...' : 'Generar y enviar'}
            </button>
          </>
        }
      >
        <div style={{ fontSize: 13, color: '#3d4856' }}>
          Se generará el reporte de <strong>{MESES[mes - 1]} {anio}</strong> para <strong>todos los clientes</strong> y
          se enviará por correo a cada uno con el PDF adjunto. ¿Continuar?
        </div>
      </Modal>
    </>
  );
}

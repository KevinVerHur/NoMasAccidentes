import type {
  ReporteMensualResponse,
  AccidentabilidadMensualResponse,
  RendimientoProfesionalResponse,
} from '../types';
import api from './axiosConfig';

// ---- Reportes mensuales (RF38, RF39) ----
export async function generarReporte(idCliente: number, mes: number, anio: number): Promise<ReporteMensualResponse> {
  const res = await api.post<ReporteMensualResponse>('/api/reportes/generar', null, { params: { idEmpresa: idCliente, mes, anio } });
  return res.data;
}

export async function listarReportesPorCliente(idCliente: number): Promise<ReporteMensualResponse[]> {
  const res = await api.get<ReporteMensualResponse[]>('/api/reportes', { params: { idEmpresa: idCliente } });
  return res.data;
}

// ---- Cierre mensual de todos los clientes (RF46) ----
export async function ejecutarCierreMensual(mes: number, anio: number): Promise<{ reportesGenerados: number }> {
  const res = await api.post<{ reportesGenerados: number }>('/api/reportes/cierre-mensual', null, { params: { mes, anio } });
  return res.data;
}

// ---- Indicadores (RF40, RF41) ----
export async function accidentabilidad(idCliente: number, anio: number): Promise<AccidentabilidadMensualResponse[]> {
  const res = await api.get<AccidentabilidadMensualResponse[]>('/api/indicadores/accidentabilidad', { params: { idEmpresa: idCliente, anio } });
  return res.data;
}

export async function rendimientoProfesional(mes: number, anio: number): Promise<RendimientoProfesionalResponse[]> {
  const res = await api.get<RendimientoProfesionalResponse[]>('/api/indicadores/rendimiento-profesional', { params: { mes, anio } });
  return res.data;
}

function dispararDescarga(blob: Blob, idReporte: number): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `reporte-${idReporte}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export async function descargarReportePdf(idReporte: number): Promise<void> {
  const res = await api.get(`/api/reportes/${idReporte}/descarga`, { responseType: 'blob' });
  dispararDescarga(res.data as Blob, idReporte);
}

// ---- Portal cliente (solo lectura) ----
export async function misReportes(): Promise<ReporteMensualResponse[]> {
  const res = await api.get<ReporteMensualResponse[]>('/api/mis-reportes');
  return res.data;
}

export async function descargarMiReportePdf(idReporte: number): Promise<void> {
  const res = await api.get(`/api/mis-reportes/${idReporte}/descarga`, { responseType: 'blob' });
  dispararDescarga(res.data as Blob, idReporte);
}

export async function miAccidentabilidad(anio: number): Promise<AccidentabilidadMensualResponse[]> {
  const res = await api.get<AccidentabilidadMensualResponse[]>('/api/mis-indicadores/accidentabilidad', { params: { anio } });
  return res.data;
}

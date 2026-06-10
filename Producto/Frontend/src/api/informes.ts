import type { InformeResponse } from '../types';
import api from './axiosConfig';

export async function generarInforme(idVisita: number): Promise<InformeResponse> {
  const res = await api.post<InformeResponse>(`/api/visitas/${idVisita}/informe`);
  return res.data;
}

export async function obtenerInformePorVisita(idVisita: number): Promise<InformeResponse> {
  const res = await api.get<InformeResponse>(`/api/visitas/${idVisita}/informe`);
  return res.data;
}

function dispararDescarga(blob: Blob, idInforme: number): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `informe-${idInforme}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

/** Descarga el PDF del informe y dispara la descarga en el navegador. */
export async function descargarInformePdf(idInforme: number): Promise<void> {
  const res = await api.get(`/api/informes/${idInforme}/descarga`, { responseType: 'blob' });
  dispararDescarga(res.data as Blob, idInforme);
}

// ---- Portal cliente (solo lectura) ----
export async function listarMisInformes(): Promise<InformeResponse[]> {
  const res = await api.get<InformeResponse[]>('/api/mis-informes');
  return res.data;
}

export async function descargarMiInformePdf(idInforme: number): Promise<void> {
  const res = await api.get(`/api/mis-informes/${idInforme}/descarga`, { responseType: 'blob' });
  dispararDescarga(res.data as Blob, idInforme);
}

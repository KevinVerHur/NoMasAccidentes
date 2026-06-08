import api from './axiosConfig';
import type { CrearVisitaRequest, VisitaResponse } from '../types';

interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

export async function listarVisitas(page = 0, size = 50): Promise<Pagina<VisitaResponse>> {
  const res = await api.get<Pagina<VisitaResponse>>('/api/visitas', {
    params: { page, size },
  });

  return res.data;
}

export async function listarMisVisitas(): Promise<VisitaResponse[]> {
  const res = await api.get<VisitaResponse[]>('/api/visitas/me');
  return res.data;
}

export async function listarMisVisitasProgramadas(): Promise<VisitaResponse[]> {
  const res = await api.get<VisitaResponse[]>('/api/visitas/me/programadas');
  return res.data;
}

export async function crearVisita(data: CrearVisitaRequest): Promise<VisitaResponse> {
  const res = await api.post<VisitaResponse>('/api/visitas', data);
  return res.data;
}

export async function iniciarMiVisita(id: number): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/iniciar`);
  return res.data;
}

export async function finalizarMiVisita(id: number): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/finalizar`);
  return res.data;
}

export async function cancelarVisita(id: number): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/cancelar`);
  return res.data;
}
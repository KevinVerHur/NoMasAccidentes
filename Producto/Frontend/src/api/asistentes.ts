import type { AsistenteResponse, AsistenteRequest } from '../types';
import api from './axiosConfig';

export async function listarAsistentesPorCliente(idCliente: number): Promise<AsistenteResponse[]> {
  const res = await api.get<AsistenteResponse[]>(`/api/asistentes/empresa/${idCliente}`);
  return res.data;
}

export async function crearAsistente(data: AsistenteRequest): Promise<AsistenteResponse> {
  const res = await api.post<AsistenteResponse>('/api/asistentes', data);
  return res.data;
}

export async function editarAsistente(id: number, data: AsistenteRequest): Promise<AsistenteResponse> {
  const res = await api.put<AsistenteResponse>(`/api/asistentes/${id}`, data);
  return res.data;
}

export async function eliminarAsistente(id: number): Promise<void> {
  await api.delete(`/api/asistentes/${id}`);
}
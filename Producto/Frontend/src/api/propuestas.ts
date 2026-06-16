import type { PropuestaMejoraResponse, CrearPropuestaMejoraRequest, EstadoPropuesta } from '../types';
import api from './axiosConfig';

export async function listarPropuestas(idInforme: number): Promise<PropuestaMejoraResponse[]> {
  const res = await api.get<PropuestaMejoraResponse[]>('/api/propuestas-mejora', { params: { idInforme } });
  return res.data;
}

export async function obtenerPropuesta(id: number): Promise<PropuestaMejoraResponse> {
  const res = await api.get<PropuestaMejoraResponse>(`/api/propuestas-mejora/${id}`);
  return res.data;
}

export async function crearPropuesta(data: CrearPropuestaMejoraRequest): Promise<PropuestaMejoraResponse> {
  const res = await api.post<PropuestaMejoraResponse>('/api/propuestas-mejora', data);
  return res.data;
}

export async function actualizarEstadoPropuesta(id: number, estado: EstadoPropuesta): Promise<PropuestaMejoraResponse> {
  const res = await api.patch<PropuestaMejoraResponse>(`/api/propuestas-mejora/${id}/estado`, null, { params: { estado } });
  return res.data;
}

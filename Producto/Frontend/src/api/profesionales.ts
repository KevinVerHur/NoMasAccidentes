import type { ProfesionalResponse, CrearProfesionalRequest, ActualizarProfesionalRequest } from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

export async function listarProfesionales(page = 0, size = 20): Promise<Pagina<ProfesionalResponse>> {
  const res = await api.get<Pagina<ProfesionalResponse>>('/api/profesionales', { params: { page, size } });
  return res.data;
}

export async function obtenerProfesional(id: number): Promise<ProfesionalResponse> {
  const res = await api.get<ProfesionalResponse>(`/api/profesionales/${id}`);
  return res.data;
}

export async function crearProfesional(data: CrearProfesionalRequest): Promise<ProfesionalResponse> {
  const res = await api.post<ProfesionalResponse>('/api/profesionales', data);
  return res.data;
}

export async function actualizarProfesional(id: number, data: ActualizarProfesionalRequest): Promise<ProfesionalResponse> {
  const res = await api.put<ProfesionalResponse>(`/api/profesionales/${id}`, data);
  return res.data;
}

export async function eliminarProfesional(id: number): Promise<void> {
  await api.delete(`/api/profesionales/${id}`);
}

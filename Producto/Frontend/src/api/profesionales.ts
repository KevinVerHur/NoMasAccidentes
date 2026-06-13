import api from './axiosConfig';
import type {
  ProfesionalResponse,
  CrearProfesionalRequest,
  ActualizarProfesionalRequest,
  ActualizarEstadoProfesionalRequest,
  ActualizarUbicacionProfesionalRequest,
} from '../types';

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

export async function actualizarEstadoProfesional(
  id: number,
  data: ActualizarEstadoProfesionalRequest
): Promise<ProfesionalResponse> {
  const res = await api.patch<ProfesionalResponse>(`/api/profesionales/${id}/estado`, data);
  return res.data;
}

export async function actualizarUbicacionProfesional(
  id: number,
  data: ActualizarUbicacionProfesionalRequest
): Promise<ProfesionalResponse>{
  const res = await api.patch<ProfesionalResponse>(`/api/profesionales/${id}/ubicacion`, data);
  return res.data;
}

export async function obtenerMiPerfilProfesional(): Promise<ProfesionalResponse> {
  const res = await api.get<ProfesionalResponse>('/api/profesionales/me');
  return res.data;
}

export async function actualizarMiEstadoProfesional(
  data: ActualizarEstadoProfesionalRequest
): Promise<ProfesionalResponse> {
  const res = await api.patch<ProfesionalResponse>('/api/profesionales/me/estado', data);
  return res.data;
}

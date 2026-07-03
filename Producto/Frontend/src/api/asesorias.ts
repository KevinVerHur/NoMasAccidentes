import type { AsesoriaResponse, CrearAsesoriaRequest } from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

export async function listarAsesorias(page = 0, size = 100, idCliente?: number): Promise<Pagina<AsesoriaResponse>> {
  const res = await api.get<Pagina<AsesoriaResponse>>('/api/asesorias', {
    params: { page, size, ...(idCliente != null ? { idEmpresa: idCliente } : {}) },
  });
  return res.data;
}

export async function obtenerAsesoria(id: number): Promise<AsesoriaResponse> {
  const res = await api.get<AsesoriaResponse>(`/api/asesorias/${id}`);
  return res.data;
}

/** Asesorías asignadas al profesional autenticado. */
export async function misAsesorias(): Promise<AsesoriaResponse[]> {
  const res = await api.get<AsesoriaResponse[]>('/api/asesorias/me');
  return res.data;
}

/** Asesorías de la empresa del cliente autenticado (solo lectura, RF07). */
export async function misAsesoriasCliente(): Promise<AsesoriaResponse[]> {
  const res = await api.get<AsesoriaResponse[]>('/api/asesorias/mias');
  return res.data;
}

export async function crearAsesoria(data: CrearAsesoriaRequest): Promise<AsesoriaResponse> {
  const res = await api.post<AsesoriaResponse>('/api/asesorias', data);
  return res.data;
}

export async function atenderAsesoria(id: number): Promise<AsesoriaResponse> {
  const res = await api.patch<AsesoriaResponse>(`/api/asesorias/${id}/atender`);
  return res.data;
}

export async function cerrarAsesoria(id: number): Promise<AsesoriaResponse> {
  const res = await api.patch<AsesoriaResponse>(`/api/asesorias/${id}/cerrar`);
  return res.data;
}

export async function cancelarAsesoria(id: number): Promise<AsesoriaResponse> {
  const res = await api.patch<AsesoriaResponse>(`/api/asesorias/${id}/cancelar`);
  return res.data;
}

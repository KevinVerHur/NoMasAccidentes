import type { EmpresaResponse, CrearEmpresaRequest, ActualizarEmpresaRequest } from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

export async function listarClientes(page = 0, size = 20): Promise<Pagina<EmpresaResponse>> {
  const res = await api.get<Pagina<EmpresaResponse>>('/api/empresas', { params: { page, size } });
  return res.data;
}

export async function obtenerCliente(id: number): Promise<EmpresaResponse> {
  const res = await api.get<EmpresaResponse>(`/api/empresas/${id}`);
  return res.data;
}

export async function crearCliente(data: CrearEmpresaRequest): Promise<EmpresaResponse> {
  const res = await api.post<EmpresaResponse>('/api/empresas', data);
  return res.data;
}

export async function actualizarCliente(id: number, data: ActualizarEmpresaRequest): Promise<EmpresaResponse> {
  const res = await api.put<EmpresaResponse>(`/api/empresas/${id}`, data);
  return res.data;
}

export async function suspenderCliente(id: number): Promise<EmpresaResponse> {
  const res = await api.patch<EmpresaResponse>(`/api/empresas/${id}/suspender`);
  return res.data;
}

export async function reactivarCliente(id: number): Promise<EmpresaResponse> {
  const res = await api.patch<EmpresaResponse>(`/api/empresas/${id}/reactivar`);
  return res.data;
}

export async function eliminarCliente(id: number): Promise<void> {
  await api.delete(`/api/empresas/${id}`);
}

export async function miCliente(): Promise<EmpresaResponse> {
  const res = await api.get<EmpresaResponse>('/api/empresas/me');
  return res.data;
}

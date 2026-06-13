import type { ClienteResponse, CrearClienteRequest, ActualizarClienteRequest } from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

export async function listarClientes(page = 0, size = 20): Promise<Pagina<ClienteResponse>> {
  const res = await api.get<Pagina<ClienteResponse>>('/api/clientes', { params: { page, size } });
  return res.data;
}

export async function obtenerCliente(id: number): Promise<ClienteResponse> {
  const res = await api.get<ClienteResponse>(`/api/clientes/${id}`);
  return res.data;
}

export async function crearCliente(data: CrearClienteRequest): Promise<ClienteResponse> {
  const res = await api.post<ClienteResponse>('/api/clientes', data);
  return res.data;
}

export async function actualizarCliente(id: number, data: ActualizarClienteRequest): Promise<ClienteResponse> {
  const res = await api.put<ClienteResponse>(`/api/clientes/${id}`, data);
  return res.data;
}

export async function suspenderCliente(id: number): Promise<ClienteResponse> {
  const res = await api.patch<ClienteResponse>(`/api/clientes/${id}/suspender`);
  return res.data;
}

export async function reactivarCliente(id: number): Promise<ClienteResponse> {
  const res = await api.patch<ClienteResponse>(`/api/clientes/${id}/reactivar`);
  return res.data;
}

export async function eliminarCliente(id: number): Promise<void> {
  await api.delete(`/api/clientes/${id}`);
}

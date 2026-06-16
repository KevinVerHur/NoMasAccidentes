import type { MultaResponse, CrearMultaRequest, EstadoMulta } from '../types';
import api from './axiosConfig';

export async function listarMultas(idFiscalizacion: number): Promise<MultaResponse[]> {
  const res = await api.get<MultaResponse[]>('/api/multas', { params: { idFiscalizacion } });
  return res.data;
}

export async function obtenerMulta(id: number): Promise<MultaResponse> {
  const res = await api.get<MultaResponse>(`/api/multas/${id}`);
  return res.data;
}

export async function crearMulta(data: CrearMultaRequest): Promise<MultaResponse> {
  const res = await api.post<MultaResponse>('/api/multas', data);
  return res.data;
}

export async function actualizarEstadoMulta(id: number, estado: EstadoMulta): Promise<MultaResponse> {
  const res = await api.patch<MultaResponse>(`/api/multas/${id}/estado-pago`, null, { params: { estado } });
  return res.data;
}

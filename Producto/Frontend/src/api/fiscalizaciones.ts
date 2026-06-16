import type { FiscalizacionResponse, CrearFiscalizacionRequest } from '../types';
import api from './axiosConfig';

export async function listarFiscalizaciones(idAsesoria: number): Promise<FiscalizacionResponse[]> {
  const res = await api.get<FiscalizacionResponse[]>('/api/fiscalizaciones', { params: { idAsesoria } });
  return res.data;
}

export async function obtenerFiscalizacion(id: number): Promise<FiscalizacionResponse> {
  const res = await api.get<FiscalizacionResponse>(`/api/fiscalizaciones/${id}`);
  return res.data;
}

export async function crearFiscalizacion(data: CrearFiscalizacionRequest): Promise<FiscalizacionResponse> {
  const res = await api.post<FiscalizacionResponse>('/api/fiscalizaciones', data);
  return res.data;
}

import type { AccidenteResponse, CrearAccidenteRequest } from '../types';
import api from './axiosConfig';

export async function listarAccidentes(idAsesoria: number): Promise<AccidenteResponse[]> {
  const res = await api.get<AccidenteResponse[]>('/api/accidentes', { params: { idAsesoria } });
  return res.data;
}

export async function obtenerAccidente(id: number): Promise<AccidenteResponse> {
  const res = await api.get<AccidenteResponse>(`/api/accidentes/${id}`);
  return res.data;
}

export async function crearAccidente(data: CrearAccidenteRequest): Promise<AccidenteResponse> {
  const res = await api.post<AccidenteResponse>('/api/accidentes', data);
  return res.data;
}

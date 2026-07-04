import api from './axiosConfig';
import type { ConsultaResponse, CrearConsultaRequest } from '../types';

interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

export async function listarConsultas(page = 0, size = 100): Promise<Pagina<ConsultaResponse>> {
  const res = await api.get<Pagina<ConsultaResponse>>('/api/consultas', {
    params: { page, size },
  });

  return res.data;
}

export async function listarConsultasPorCliente(idCliente: number): Promise<ConsultaResponse[]> {
  const res = await api.get<ConsultaResponse[]>(`/api/consultas/empresa/${idCliente}`);
  return res.data;
}

export async function misConsultas(): Promise<ConsultaResponse[]> {
  const res = await api.get<ConsultaResponse[]>('/api/consultas/mias');
  return res.data;
}

export async function crearConsulta(data: CrearConsultaRequest): Promise<ConsultaResponse> {
  const res = await api.post<ConsultaResponse>('/api/consultas', data);
  return res.data;
}

import type {
  VisitaResponse,
  PlanificarVisitaRequest,
  RegistrarVisitaRequest,
  ListaChequeoResponse,
  ResultadoChequeoResponse,
} from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

/** Lista de chequeo de una empresa, para marcarla al registrar la visita (RF19). */
export async function obtenerListaChequeoEmpresa(idEmpresa: number): Promise<ListaChequeoResponse> {
  const res = await api.get<ListaChequeoResponse>(`/api/listas-chequeo/empresa/${idEmpresa}`);
  return res.data;
}

/** Resultado del chequeo registrado en una visita (RF19). */
export async function obtenerResultadosVisita(id: number): Promise<ResultadoChequeoResponse[]> {
  const res = await api.get<ResultadoChequeoResponse[]>(`/api/visitas/${id}/resultados`);
  return res.data;
}

export async function listarVisitas(page = 0, size = 100, idCliente?: number): Promise<Pagina<VisitaResponse>> {
  const res = await api.get<Pagina<VisitaResponse>>('/api/visitas', {
    params: { page, size, ...(idCliente != null ? { idEmpresa: idCliente } : {}) },
  });
  return res.data;
}

export async function obtenerVisita(id: number): Promise<VisitaResponse> {
  const res = await api.get<VisitaResponse>(`/api/visitas/${id}`);
  return res.data;
}

/** Visitas del cliente autenticado (portal cliente, solo lectura). */
export async function misVisitas(): Promise<VisitaResponse[]> {
  const res = await api.get<VisitaResponse[]>('/api/visitas/mis-visitas');
  return res.data;
}

export async function planificarVisita(data: PlanificarVisitaRequest): Promise<VisitaResponse> {
  const res = await api.post<VisitaResponse>('/api/visitas', data);
  return res.data;
}

export async function iniciarVisita(id: number): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/iniciar`);
  return res.data;
}

export async function registrarVisita(id: number, data: RegistrarVisitaRequest): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/registrar`, data);
  return res.data;
}

export async function cancelarVisita(id: number): Promise<VisitaResponse> {
  const res = await api.patch<VisitaResponse>(`/api/visitas/${id}/cancelar`);
  return res.data;
}

export async function eliminarVisita(id: number): Promise<void> {
  await api.delete(`/api/visitas/${id}`);
}

// Aliases para compatibilidad con DashboardProfesional
export async function listarMisVisitas(): Promise<VisitaResponse[]> {
  const res = await api.get<VisitaResponse[]>('/api/visitas/me');
  return res.data;
}
export const iniciarMiVisita = iniciarVisita;
export const finalizarMiVisita = (id: number) => registrarVisita(id, {});

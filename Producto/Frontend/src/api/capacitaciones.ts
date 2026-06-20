import type {
  CapacitacionResponse,
  CrearCapacitacionRequest,
  InscribirAsistentesRequest,
  ConfirmarAsistenciaRequest,
  AsistenciaResponse,
} from '../types';
import api from './axiosConfig';

interface Pagina<T> { content: T[]; totalElements: number; totalPages: number; }

export async function listarCapacitaciones(page = 0, size = 200): Promise<Pagina<CapacitacionResponse>> {
  const res = await api.get<Pagina<CapacitacionResponse>>('/api/capacitaciones', { params: { page, size } });
  return res.data;
}

export async function crearCapacitacion(data: CrearCapacitacionRequest): Promise<CapacitacionResponse> {
  const res = await api.post<CapacitacionResponse>('/api/capacitaciones', data);
  return res.data;
}

export async function cancelarCapacitacion(id: number): Promise<CapacitacionResponse> {
  const res = await api.patch<CapacitacionResponse>(`/api/capacitaciones/${id}/cancelar`);
  return res.data;
}

export async function inscribirAsistentes(
  idCapacitacion: number,
  data: InscribirAsistentesRequest
): Promise<CapacitacionResponse> {
  const res = await api.post<CapacitacionResponse>(
    `/api/capacitaciones/${idCapacitacion}/asistentes`,
    data
  );
  return res.data;
}

export async function confirmarAsistencia(
  idCapacitacion: number,
  idAsistente: number,
  data?: ConfirmarAsistenciaRequest
): Promise<AsistenciaResponse> {
  const res = await api.post<AsistenciaResponse>(
    `/api/capacitaciones/${idCapacitacion}/asistentes/${idAsistente}/confirmar`,
    data ?? {}
  );
  return res.data;
}

export async function registrarAsistenciaEfectiva(
  idCapacitacion: number,
  idAsistente: number,
  asistio: boolean
): Promise<AsistenciaResponse> {
  const res = await api.patch<AsistenciaResponse>(
    `/api/capacitaciones/${idCapacitacion}/asistentes/${idAsistente}/asistio`,
    null,
    { params: { asistio } }
  );
  return res.data;
}

export async function iniciarCapacitacion(id: number): Promise<CapacitacionResponse> {
  const res = await api.patch<CapacitacionResponse>(`/api/capacitaciones/${id}/iniciar`);
  return res.data;
}

export async function finalizarCapacitacion(id: number): Promise<CapacitacionResponse> {
  const res = await api.patch<CapacitacionResponse>(`/api/capacitaciones/${id}/finalizar`);
  return res.data;
}
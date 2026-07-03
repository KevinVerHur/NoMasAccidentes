import type {
  SolicitudResponse,
  CrearSolicitudRequest,
  AprobarSolicitudRequest,
  RechazarSolicitudRequest,
  EstadoSolicitud,
} from '../types';
import api from './axiosConfig';

// ---- Cliente ----
export async function crearSolicitud(data: CrearSolicitudRequest): Promise<SolicitudResponse> {
  const res = await api.post<SolicitudResponse>('/api/solicitudes', data);
  return res.data;
}

export async function listarMisSolicitudes(): Promise<SolicitudResponse[]> {
  const res = await api.get<SolicitudResponse[]>('/api/solicitudes/mias');
  return res.data;
}

// ---- Admin ----
export async function listarSolicitudes(estado?: EstadoSolicitud): Promise<SolicitudResponse[]> {
  const res = await api.get<SolicitudResponse[]>('/api/solicitudes', {
    params: estado ? { estado } : undefined,
  });
  return res.data;
}

export async function contarSolicitudesPendientes(): Promise<number> {
  const res = await api.get<{ pendientes: number }>('/api/solicitudes/pendientes/count');
  return res.data.pendientes;
}

export async function aprobarSolicitud(
  id: number,
  data: AprobarSolicitudRequest
): Promise<SolicitudResponse> {
  const res = await api.patch<SolicitudResponse>(`/api/solicitudes/${id}/aprobar`, data);
  return res.data;
}

export async function rechazarSolicitud(
  id: number,
  data: RechazarSolicitudRequest
): Promise<SolicitudResponse> {
  const res = await api.patch<SolicitudResponse>(`/api/solicitudes/${id}/rechazar`, data);
  return res.data;
}

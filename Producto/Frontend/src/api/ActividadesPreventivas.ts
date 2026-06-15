import type {
  ActividadPreventivaResponse,
  CrearActividadPreventivaRequest,
  ActualizarActividadPreventivaRequest,
  CambiarEstadoActividadRequest,
  EstadoActividadPreventiva,
} from '../types';
import api from './axiosConfig';

interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

export async function listarActividadesPreventivas(
  page = 0,
  size = 100,
  idCliente?: number,
  estado?: EstadoActividadPreventiva,
): Promise<Pagina<ActividadPreventivaResponse>> {
  const res = await api.get<Pagina<ActividadPreventivaResponse>>('/api/actividades-preventivas', {
    params: {
      page,
      size,
      ...(idCliente ? { idCliente } : {}),
      ...(estado ? { estado } : {}),
    },
  });

  return res.data;
}

export async function crearActividadPreventiva(
  data: CrearActividadPreventivaRequest,
): Promise<ActividadPreventivaResponse> {
  const res = await api.post<ActividadPreventivaResponse>('/api/actividades-preventivas', data);
  return res.data;
}

export async function actualizarActividadPreventiva(
  id: number,
  data: ActualizarActividadPreventivaRequest,
): Promise<ActividadPreventivaResponse> {
  const res = await api.put<ActividadPreventivaResponse>(`/api/actividades-preventivas/${id}`, data);
  return res.data;
}

export async function cambiarEstadoActividadPreventiva(
  id: number,
  data: CambiarEstadoActividadRequest,
): Promise<ActividadPreventivaResponse> {
  const res = await api.patch<ActividadPreventivaResponse>(`/api/actividades-preventivas/${id}/estado`, data);
  return res.data;
}

export async function eliminarActividadPreventiva(id: number): Promise<void> {
  await api.delete(`/api/actividades-preventivas/${id}`);
}

export async function misActividadesPreventivas(): Promise<ActividadPreventivaResponse[]> {
  const res = await api.get<ActividadPreventivaResponse[]>('/api/actividades-preventivas/mis-actividades');
  return res.data;
}
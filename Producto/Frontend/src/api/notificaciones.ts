import type { NotificacionResponse } from '../types';
import api from './axiosConfig';

// ---- Bandeja de notificaciones in-app (Fase 4) ----

export async function listarMisNotificaciones(): Promise<NotificacionResponse[]> {
  const res = await api.get<NotificacionResponse[]>('/api/notificaciones/mias');
  return res.data;
}

export async function contarNoLeidas(): Promise<number> {
  const res = await api.get<{ noLeidas: number }>('/api/notificaciones/mias/no-leidas/count');
  return res.data.noLeidas;
}

export async function marcarLeida(id: number): Promise<NotificacionResponse> {
  const res = await api.patch<NotificacionResponse>(`/api/notificaciones/${id}/leida`);
  return res.data;
}

export async function marcarTodasLeidas(): Promise<void> {
  await api.patch('/api/notificaciones/mias/leer-todas');
}

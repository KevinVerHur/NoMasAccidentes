import type {
  DashboardAdminResponse,
  DashboardClienteResponse,
  DashboardProfesionalResponse,
} from '../types';
import api from './axiosConfig';

// ---- Dashboard administrador (RF45) ----
export async function obtenerDashboardAdmin(): Promise<DashboardAdminResponse> {
  const res = await api.get<DashboardAdminResponse>('/api/dashboard/admin');
  return res.data;
}

// ---- Dashboard cliente (RF45) ----
export async function obtenerDashboardCliente(): Promise<DashboardClienteResponse> {
  const res = await api.get<DashboardClienteResponse>('/api/dashboard/cliente');
  return res.data;
}

// ---- Dashboard profesional: clientes asignados (RF45) ----
export async function obtenerDashboardProfesional(): Promise<DashboardProfesionalResponse> {
  const res = await api.get<DashboardProfesionalResponse>('/api/dashboard/profesional');
  return res.data;
}

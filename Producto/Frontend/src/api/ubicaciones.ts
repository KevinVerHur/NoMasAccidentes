// Producto/Frontend/src/api/ubicaciones.ts
import api from './axiosConfig';
import type { RegistrarUbicacionRequest, UbicacionProfesionalResponse } from '../types';

export async function registrarMiUbicacion(
  data: RegistrarUbicacionRequest
): Promise<UbicacionProfesionalResponse> {
  const res = await api.post<UbicacionProfesionalResponse>('/api/ubicaciones', data);
  return res.data;
}

export async function listarUbicacionesActivas(): Promise<UbicacionProfesionalResponse[]> {
  const res = await api.get<UbicacionProfesionalResponse[]>('/api/ubicaciones/activas');
  return res.data;
}

export async function obtenerMiUltimaUbicacion(): Promise<UbicacionProfesionalResponse> {
  const res = await api.get<UbicacionProfesionalResponse>('/api/ubicaciones/me/ultima');
  return res.data;
}
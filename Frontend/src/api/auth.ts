import type { LoginRequest, LoginResponse, RegistroRequest, UsuarioMe } from '../types';
import api from './axiosConfig';

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/api/auth/login', data);
  return response.data;
}

export async function registro(data: RegistroRequest): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/api/auth/registro', data);
  return response.data;
}

export async function obtenerPerfil(): Promise<UsuarioMe> {
  const response = await api.get<UsuarioMe>('/api/usuarios/me');
  return response.data;
}

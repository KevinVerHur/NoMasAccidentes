import type { LoginRequest, LoginResponse, RegistroRequest, UsuarioMe,UsuarioResponse,
  ActualizarPerfilRequest,
  CambiarPasswordRequest, } from '../types';
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

export async function solicitarRecuperacion(email: string): Promise<void> {
  await api.post('/api/auth/forgot-password', { email });
}

export async function restablecerPassword(token: string, nuevaPassword: string): Promise<void> {
  await api.post('/api/auth/reset-password', { token, nuevaPassword });
}

export async function validarTokenReset(token: string): Promise<boolean> {
  const response = await api.get<{ valido: boolean }>('/api/auth/reset-password/validate', {
    params: { token },
  });
  return response.data.valido;
}

export async function obtenerMiPerfil(): Promise<UsuarioResponse> {
  const response = await api.get<UsuarioResponse>('/api/usuarios/me');
  return response.data;
}

export async function actualizarMiPerfil(
  data: ActualizarPerfilRequest
): Promise<UsuarioResponse> {
  const response = await api.put<UsuarioResponse>('/api/usuarios/me', data);
  return response.data;
}

export async function cambiarMiPassword(
  data: CambiarPasswordRequest
): Promise<void> {
  await api.patch('/api/usuarios/me/password', data);
}
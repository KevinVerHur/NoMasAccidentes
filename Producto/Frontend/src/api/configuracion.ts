import api from './axiosConfig';
import type {
  ActualizarConfiguracionEmpresaRequest,
  ConfiguracionEmpresaResponse,
} from '../types';

export async function obtenerConfiguracionEmpresa(): Promise<ConfiguracionEmpresaResponse> {
  const res = await api.get<ConfiguracionEmpresaResponse>('/api/configuracion/empresa');
  return res.data;
}

export async function actualizarConfiguracionEmpresa(
  data: ActualizarConfiguracionEmpresaRequest
): Promise<ConfiguracionEmpresaResponse> {
  const res = await api.put<ConfiguracionEmpresaResponse>('/api/configuracion/empresa', data);
  return res.data;
}
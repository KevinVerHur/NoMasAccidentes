import type { RepresentanteResponse, CrearRepresentanteRequest } from '../types';
import api from './axiosConfig';

export async function listarRepresentantes(idEmpresa: number): Promise<RepresentanteResponse[]> {
  const res = await api.get<RepresentanteResponse[]>(`/api/empresas/${idEmpresa}/representantes`);
  return res.data;
}

export async function crearRepresentante(
  idEmpresa: number,
  data: CrearRepresentanteRequest,
): Promise<RepresentanteResponse> {
  const res = await api.post<RepresentanteResponse>(`/api/empresas/${idEmpresa}/representantes`, data);
  return res.data;
}

export async function eliminarRepresentante(idEmpresa: number, idRepresentante: number): Promise<void> {
  await api.delete(`/api/empresas/${idEmpresa}/representantes/${idRepresentante}`);
}

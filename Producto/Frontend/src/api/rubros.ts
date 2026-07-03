import type { RubroResponse } from '../types';
import api from './axiosConfig';

export async function listarRubros(): Promise<RubroResponse[]> {
  const res = await api.get<RubroResponse[]>('/api/rubros');
  return res.data;
}

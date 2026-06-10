import type {
  MensualidadResponse, CrearMensualidadRequest,
  PlanPagoResponse, CrearPlanPagoRequest,
  PagoResponse, RegistrarPagoRequest,
} from '../types';
import api from './axiosConfig';

// ---- Mensualidades (catálogo, RF08) ----
export async function listarMensualidades(): Promise<MensualidadResponse[]> {
  const res = await api.get<MensualidadResponse[]>('/api/mensualidades');
  return res.data;
}

export async function crearMensualidad(data: CrearMensualidadRequest): Promise<MensualidadResponse> {
  const res = await api.post<MensualidadResponse>('/api/mensualidades', data);
  return res.data;
}

// ---- Planes de pago (RF08) ----
export async function listarPlanesPorCliente(idCliente: number): Promise<PlanPagoResponse[]> {
  const res = await api.get<PlanPagoResponse[]>('/api/planes-pago', { params: { idCliente } });
  return res.data;
}

export async function crearPlanPago(data: CrearPlanPagoRequest): Promise<PlanPagoResponse> {
  const res = await api.post<PlanPagoResponse>('/api/planes-pago', data);
  return res.data;
}

// ---- Pagos / cuotas (RF09–RF12) ----
export async function historialPagos(idCliente: number): Promise<PagoResponse[]> {
  const res = await api.get<PagoResponse[]>('/api/pagos', { params: { idCliente } });
  return res.data;
}

export async function registrarPago(id: number, data: RegistrarPagoRequest): Promise<PagoResponse> {
  const res = await api.patch<PagoResponse>(`/api/pagos/${id}/registrar`, data);
  return res.data;
}

export async function evaluarMorosidad(): Promise<{ cuotasMarcadas: number }> {
  const res = await api.post<{ cuotasMarcadas: number }>('/api/pagos/evaluar-morosidad');
  return res.data;
}

export async function suspenderMorosos(): Promise<{ clientesSuspendidos: number }> {
  const res = await api.post<{ clientesSuspendidos: number }>('/api/pagos/suspender-morosos');
  return res.data;
}

// ---- Portal cliente (solo lectura) ----
export async function misPagos(): Promise<PagoResponse[]> {
  const res = await api.get<PagoResponse[]>('/api/pagos/mis-pagos');
  return res.data;
}

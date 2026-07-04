import type {
  MensualidadResponse, CrearMensualidadRequest,
  PlanPagoResponse, CrearPlanPagoRequest,
  PagoResponse, RegistrarPagoRequest,CobroExtraResponse
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
  const res = await api.get<PlanPagoResponse[]>('/api/planes-pago', { params: { idEmpresa: idCliente } });
  return res.data;
}

export async function crearPlanPago(data: CrearPlanPagoRequest): Promise<PlanPagoResponse> {
  const res = await api.post<PlanPagoResponse>('/api/planes-pago', data);
  return res.data;
}

// ---- Pagos / cuotas (RF09–RF12) ----
export async function historialPagos(idCliente: number): Promise<PagoResponse[]> {
  const res = await api.get<PagoResponse[]>('/api/pagos', { params: { idEmpresa: idCliente } });
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

export async function suspenderMorosos(): Promise<{ empresasSuspendidas: number }> {
  const res = await api.post<{ empresasSuspendidas: number }>('/api/pagos/suspender-morosos');
  return res.data;
}

// ---- Portal cliente (solo lectura) ----
export async function misPagos(): Promise<PagoResponse[]> {
  const res = await api.get<PagoResponse[]>('/api/pagos/mis-pagos');
  return res.data;
}

export async function listarCobrosExtra(idPago: number): Promise<CobroExtraResponse[]> {
  const res = await api.get<CobroExtraResponse[]>(`/api/pagos/${idPago}/cobros-extra`);
  return res.data;
}

export async function listarMisCobrosExtra(idPago: number): Promise<CobroExtraResponse[]> {
  const res = await api.get<CobroExtraResponse[]>(`/api/pagos/${idPago}/mis-cobros-extra`);
  return res.data;
}

// ---- Pago en línea Webpay (RF09) ----
export interface IniciarWebpayResponse {
  token: string;
  url: string;
}

/** Inicia el pago de una cuota en Webpay y devuelve el token + URL de la pasarela. */
export async function iniciarWebpay(idPago: number): Promise<IniciarWebpayResponse> {
  const res = await api.post<IniciarWebpayResponse>(`/api/pagos/${idPago}/webpay/iniciar`);
  return res.data;
}

/** Nombre de la ventana emergente donde se abre la pasarela Webpay. */
export const NOMBRE_VENTANA_WEBPAY = 'webpayNMA';

/**
 * Abre (en blanco) la ventana emergente de Webpay. Debe llamarse de forma
 * SÍNCRONA dentro del click del usuario; si se abre después de un await el
 * navegador la bloquea como popup. Se pasa un tamaño explícito para que el
 * navegador abra una VENTANA separada y no una pestaña, centrada en pantalla.
 * Devuelve la referencia (o null si se bloqueó).
 */
export function abrirVentanaWebpay(): Window | null {
  const ancho = 520;
  const alto = 720;
  const left = Math.round(window.screenX + Math.max(0, (window.outerWidth - ancho) / 2));
  const top = Math.round(window.screenY + Math.max(0, (window.outerHeight - alto) / 2));
  const caracteristicas =
    `popup=yes,width=${ancho},height=${alto},left=${left},top=${top},resizable=yes,scrollbars=yes`;
  return window.open('', NOMBRE_VENTANA_WEBPAY, caracteristicas);
}

/**
 * Envía el pago a Webpay en la ventana emergente ya abierta: la pasarela exige
 * un POST con el campo `token_ws`, por eso se arma y envía un form apuntando a
 * esa ventana (target) en vez de navegar la pestaña actual.
 */
export function redirigirAWebpay({ token, url }: IniciarWebpayResponse): void {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = url;
  form.target = NOMBRE_VENTANA_WEBPAY;
  const input = document.createElement('input');
  input.type = 'hidden';
  input.name = 'token_ws';
  input.value = token;
  form.appendChild(input);
  document.body.appendChild(form);
  form.submit();
  form.remove();
}

/** Descarga el comprobante de pago PDF de una cuota pagada. */
export async function descargarMiBoleta(idPago: number): Promise<void> {
  const res = await api.get(`/api/pagos/${idPago}/mi-boleta`, { responseType: 'blob' });
  const objectUrl = URL.createObjectURL(res.data as Blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = `comprobante-${idPago}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(objectUrl);
}
import type {
  ClienteResponse,
  MorosidadDetalle,
  MorosidadItem,
  MorosidadNotificacionRequest,
  MorosidadPagoRequest,
  MorosidadResumen,
  PagoResponse,
} from '../types';
import { listarClientes, suspenderCliente } from './clientes';
import { historialPagos, registrarPago } from './pagos';

const esCuotaAdeudada = (pago: PagoResponse) => pago.estadoPago === 'PENDIENTE' || pago.estadoPago === 'ATRASADO';
const fechaTiempo = (fecha: string | null) => fecha ? new Date(fecha).getTime() : 0;

function riesgoPorCliente(cliente: ClienteResponse, cuotasAdeudadas: PagoResponse[]): MorosidadItem['riesgo'] {
  const atrasadas = cuotasAdeudadas.filter((pago) => pago.estadoPago === 'ATRASADO').length;

  if (cliente.estado === 'SUSPENDIDO' || atrasadas >= 2) return 'CRITICO';
  if (cliente.estado === 'MOROSO' || atrasadas >= 1 || cuotasAdeudadas.length >= 2) return 'ALERTA';
  return 'OBSERVACION';
}

function construirItem(cliente: ClienteResponse, pagos: PagoResponse[]): MorosidadItem | null {
  const cuotasAdeudadas = pagos
    .filter(esCuotaAdeudada)
    .sort((a, b) => fechaTiempo(a.fechaVencimiento) - fechaTiempo(b.fechaVencimiento));

  if (cuotasAdeudadas.length === 0 && cliente.estado !== 'MOROSO' && cliente.estado !== 'SUSPENDIDO') {
    return null;
  }

  const pagosRealizados = pagos
    .filter((pago) => pago.estadoPago === 'PAGADO')
    .sort((a, b) => fechaTiempo(b.fechaPago) - fechaTiempo(a.fechaPago));

  return {
    idCliente: cliente.id,
    cliente: cliente.razonSocial,
    email: cliente.email,
    estadoCliente: cliente.estado,
    mesesDeuda: cuotasAdeudadas.length,
    montoAdeudado: cuotasAdeudadas.reduce((total, pago) => total + pago.monto, 0),
    riesgo: riesgoPorCliente(cliente, cuotasAdeudadas),
    suspendido: cliente.estado === 'SUSPENDIDO',
    ultimoPago: pagosRealizados[0]?.fechaPago ?? null,
    pagos,
    cuotasAdeudadas,
  };
}

async function cargarBaseMorosidad(): Promise<MorosidadItem[]> {
  const pagina = await listarClientes(0, 200);
  const resultados = await Promise.allSettled(
    pagina.content.map(async (cliente) => construirItem(cliente, await historialPagos(cliente.id)))
  );

  return resultados
    .flatMap((resultado) => resultado.status === 'fulfilled' && resultado.value ? [resultado.value] : [])
    .sort((a, b) => b.montoAdeudado - a.montoAdeudado);
}

export async function listarMorosidades(): Promise<MorosidadItem[]> {
  return cargarBaseMorosidad();
}

export async function obtenerResumenMorosidad(): Promise<MorosidadResumen> {
  const items = await cargarBaseMorosidad();

  return {
    clientesMora: items.filter((item) => item.mesesDeuda > 0 || item.estadoCliente === 'MOROSO').length,
    montoTotalAdeudado: items.reduce((total, item) => total + item.montoAdeudado, 0),
    serviciosSuspendidos: items.filter((item) => item.suspendido).length,
    notificacionesEnviadas: 0,
  };
}

export async function obtenerDetalleMorosidad(idCliente: number): Promise<MorosidadDetalle> {
  const items = await cargarBaseMorosidad();
  const item = items.find((actual) => actual.idCliente === idCliente);

  if (!item) {
    throw new Error('No se encontraron datos de morosidad para el cliente seleccionado.');
  }

  return {
    ...item,
    historialNotificaciones: [],
  };
}

export async function registrarPagoMorosidad(data: MorosidadPagoRequest): Promise<void> {
  await registrarPago(data.idPago, { medioPago: data.metodo });
}

export async function suspenderClienteMorosidad(idCliente: number): Promise<void> {
  await suspenderCliente(idCliente);
}

export async function notificarDeudaMorosidad(data: MorosidadNotificacionRequest): Promise<void> {
  void data;
  throw new Error('Función pendiente de integración con backend.');
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  nombreCompleto: string;
  rol: string;
}

export interface RegistroRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  idRol: number;
}

export interface UsuarioMe {
  id: number;
  nombre: string;
  email: string;
  rol: string;
}

export type Rol = 'ADMIN' | 'PROFESIONAL' | 'CLIENTE' | 'CAPACITADOR';

export type VarianteKpi = 'default' | 'ok' | 'warn' | 'peligro';
export type VarianteBadge = 'green' | 'red' | 'yellow' | 'blue' | 'gray';
export type VarianteAlerta = 'peligro' | 'warn' | 'info' | 'ok';
export type VarianteBarra = 'default' | 'warn' | 'ok' | 'peligro';

export interface VisitaResumen {
  cliente: string;
  profesional: string;
  fecha: string;
  estado: 'Realizada' | 'Pendiente' | 'No realizada';
}

export interface AlertaDashboard {
  tipo: VarianteAlerta;
  icono: string;
  destacado: string;
  texto: string;
}

export interface PagoResumen {
  cliente: string;
  planMensual: string;
  ultimoPago: string;
  mesesAdeudados: number;
  estado: 'Al día' | 'Atrasado' | 'Moroso';
}

export interface AccidentabilidadResumen {
  cliente: string;
  porcentaje: number;
  tasa: string;
  variante: VarianteBarra;
}

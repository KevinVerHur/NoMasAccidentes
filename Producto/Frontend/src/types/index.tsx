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

// ---- Dashboard Profesional (mock) ----
export interface MiVisita {
  cliente: string;
  fecha: string;
  hora: string;
  direccion: string;
  estado: 'Programada' | 'Realizada' | 'Pendiente';
}

export interface MiClienteAsignado {
  razonSocial: string;
  rubro: string;
  ultimaVisita: string;
  estado: 'ACTIVO' | 'MOROSO' | 'SUSPENDIDO';
}

// ---- Dashboard Cliente (mock) ----
export interface ProximaActividad {
  fecha: string;
  actividad: string;
  profesional: string;
  estado: 'Programada' | 'Por confirmar' | 'Pendiente';
}

export interface AccionImportante {
  tipo: VarianteAlerta;
  titulo: string;
  texto: string;
}

export interface VisitaRecibida {
  fecha: string;
  profesional: string;
  tipo: string;
  estado: 'Realizada' | 'Programada';
}

export interface ReporteDisponible {
  nombre: string;
  fecha: string;
  tipo: string;
}

export interface MiCapacitacion {
  nombre: string;
  fecha: string;
  asistentes: number;
  estado: 'Realizada' | 'Programada';
}

// ---- Cliente ----
export type EstadoCliente = 'ACTIVO' | 'MOROSO' | 'SUSPENDIDO';

export interface ClienteResponse {
  id: number;
  razonSocial: string;
  rut: string;
  nombreContacto: string;
  email: string;
  telefono: string | null;
  rubro: string;
  plan: string;
  estado: EstadoCliente;
  idProfesional: number | null;
  nombreProfesional: string | null;
  activo: boolean;
}

export interface CrearClienteRequest {
  razonSocial: string;
  rut: string;
  nombreContacto: string;
  email: string;
  telefono?: string;
  rubro: string;
  plan: string;
  idProfesional?: number | null;
}

export interface ActualizarClienteRequest extends CrearClienteRequest {
  estado: EstadoCliente;
}

// ---- Profesional ----
export type EstadoProfesional = 'DISPONIBLE' | 'EN_VISITA' | 'EN_CAPACITACION';

export interface ProfesionalResponse {
  id: number;
  idUsuario: number;
  email: string;
  nombreCompleto: string;
  rut: string;
  telefono: string | null;
  especialidad: string | null;
  latitud: number | null;
  longitud: number | null;
  estado: EstadoProfesional;
  activo: boolean;
  cantidadClientes: number;
}

export interface CrearProfesionalRequest {
  email: string;
  password: string;
  nombre: string;
  apellido: string;
  rut: string;
  especialidad?: string;
  telefono?: string;
}

export interface ActualizarProfesionalRequest {
  rut: string;
  telefono?: string;
  especialidad?: string;
}

export interface ActualizarEstadoProfesionalRequest {
  estado: EstadoProfesional;
}

export interface ActualizarUbicacionProfesionalRequest {
  latitud: number;
  longitud: number;
}

export interface RegistrarUbicacionRequest {
  latitud: number;
  longitud: number;
}

export interface UbicacionProfesionalResponse {
  idProfesional: number;
  nombreProfesional: string;
  email: string;
  estado: EstadoProfesional;
  latitud: number;
  longitud: number;
  fechaRegistro: string;
}

export type EstadoVisitaBackend = 'PROGRAMADA' | 'REALIZADA' | 'CANCELADA';

export interface VisitaResponse {
  id: number;
  idCliente: number;
  cliente: string;
  idProfesional: number | null;
  profesional: string | null;
  fechaProgramada: string;
  direccion: string;
  motivo: string | null;
  estado: EstadoVisitaBackend;
}

export interface CrearVisitaRequest {
  idCliente: number;
  idProfesional: number;
  fechaProgramada: string;
  direccion: string;
  motivo?: string | null;
}

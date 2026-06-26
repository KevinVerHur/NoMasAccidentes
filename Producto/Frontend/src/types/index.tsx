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

// ---- Visitas (RF13–RF14) ----
export type EstadoVisita = 'PROGRAMADA' | 'EN_CURSO' | 'REALIZADA' | 'CANCELADA';
/** @deprecated Usar EstadoVisita */
export type EstadoVisitaBackend = EstadoVisita;

export interface VisitaResponse {
  id: number;
  idCliente: number;
  razonSocialCliente: string;
  idProfesional: number;
  nombreProfesional: string;
  idListaChequeo: number;
  tipoRevision: string | null;
  fechaProgramada: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  estado: EstadoVisita;
  latitud: number | null;
  longitud: number | null;
  observaciones: string | null;
  esVisitaExtra: boolean;
}

export interface PlanificarVisitaRequest {
  idCliente: number;
  idProfesional: number;
  fechaProgramada: string;
  tipoRevision?: string;
  esVisitaExtra?: boolean;
}

export interface RegistrarVisitaRequest {
  observaciones?: string;
  latitud?: number;
  longitud?: number;
}

// ---- Pagos (RF08–RF12) ----
export type EstadoPago = 'PENDIENTE' | 'PAGADO' | 'ATRASADO';
export type Periodicidad = 'MENSUAL' | 'TRIMESTRAL' | 'ANUAL';

export interface MensualidadResponse {
  id: number;
  nombrePlan: string;
  montoBase: number;
  visitasIncluidas: number | null;
  asesoriasIncluidas: number | null;
  capacitacionesIncluidas: number | null;
  costoVisitaExtra: number | null;
  costoAsesoriaExtra: number | null;
  costoCapacitacionExtra: number | null;
  costoLlamadoFueraHorario: number | null;
  activo: boolean;
}

export interface CrearMensualidadRequest {
  nombrePlan: string;
  montoBase: number;
  visitasIncluidas?: number;
  asesoriasIncluidas?: number;
  capacitacionesIncluidas?: number;
  costoVisitaExtra?: number;
  costoAsesoriaExtra?: number;
  costoCapacitacionExtra?: number;
  costoLlamadoFueraHorario?: number;
}

export interface PlanPagoResponse {
  id: number;
  idCliente: number;
  razonSocialCliente: string;
  idMensualidad: number;
  nombrePlan: string;
  fechaInicio: string;
  fechaTermino: string | null;
  cuotasTotales: number | null;
  periodicidad: Periodicidad;
  activo: boolean;
}

export interface CrearPlanPagoRequest {
  idCliente: number;
  idMensualidad: number;
  fechaInicio: string;
  cuotasTotales: number;
  periodicidad?: Periodicidad;
}

export interface PagoResponse {
  id: number;
  idPlan: number;
  idCliente: number;
  razonSocialCliente: string;
  numeroCuota: number;
  monto: number;
  fechaEmision: string;
  fechaVencimiento: string;
  fechaPago: string | null;
  medioPago: string | null;
  estadoPago: EstadoPago;
}

export interface RegistrarPagoRequest {
  medioPago?: string;
}

// ---- Informe post-visita (RF15) ----
export type EstadoInforme = 'GENERADO' | 'ANULADO';

export interface InformeResponse {
  id: number;
  idVisita: number;
  razonSocialCliente: string;
  nombreProfesional: string;
  fechaEmision: string;
  estado: EstadoInforme;
  hallazgos: string | null;
  tieneArchivo: boolean;
}


export type EstadoCapacitacion = 'PROGRAMADA' | 'EN_CURSO' | 'REALIZADA' | 'CANCELADA';
 
export interface AsistenciaResponse {
  idAsistencia: number;
  idAsistente: number;
  nombreAsistente: string;
  rutAsistente: string;
  cargoAsistente: string | null;
  confirmado: boolean;
  asistio: boolean;
  fechaConfirmacion: string | null;
  observaciones: string | null;
}
 
export interface CapacitacionResponse {
  id: number;
  idCliente: number;
  cliente: string;
  curso: string;
  idRelator: number;
  relator: string;
  fechaProgramada: string;
  horaProgramada: string;
  lugar:string;
  cupos: number;
  objetivo: string | null;
  fechaRealizacion: string | null;
  estado: EstadoCapacitacion;
  esCapacitacionExtra: boolean;
  cuposDisponibles: number;
  asistencias: AsistenciaResponse[];
}
 
export interface CrearCapacitacionRequest {
  idCliente: number;
  curso: string;
  idRelator: number;
  fechaProgramada: string;
  horaProgramada: string;
  lugar:string
  cupos: number;
  objetivo?: string;
  esCapacitacionExtra: boolean;
}
 
export interface InscribirAsistentesRequest {
  idsAsistentes: number[];
}
 
export interface ConfirmarAsistenciaRequest {
  observaciones?: string;
  firmaDigital?: string;
}
export interface AsistenteResponse {
  id: number;
  idCliente: number;
  cliente: string;
  rut: string;
  nombre: string;
  apellidos: string;
  nombreCompleto: string;
  cargo: string | null;
  area: string | null;
  email: string | null;
}
 
export interface AsistenteRequest {
  idCliente: number;
  rut: string;
  nombre: string;
  apellidos: string;
  cargo?: string;
  area?: string;
  email?: string;
}
export type EstadoActividadPreventiva = 'PENDIENTE' | 'EN_CURSO' | 'CUMPLIDA' | 'VENCIDA';

export interface ActividadPreventivaResponse {
  id: number;
  idCliente: number;
  razonSocialCliente: string;
  titulo: string;
  descripcion: string | null;
  normativa: string | null;
  responsable: string | null;
  fechaPlanificada: string;
  fechaCompromiso: string;
  fechaCumplimiento: string | null;
  estado: EstadoActividadPreventiva;
  observaciones: string | null;
  vencida: boolean;
}

export interface CrearActividadPreventivaRequest {
  idCliente: number;
  titulo: string;
  descripcion?: string;
  normativa?: string;
  responsable?: string;
  fechaPlanificada: string;
  fechaCompromiso: string;
  observaciones?: string;
}

export interface ActualizarActividadPreventivaRequest extends CrearActividadPreventivaRequest {
  estado?: EstadoActividadPreventiva;
}

export interface CambiarEstadoActividadRequest {
  estado: EstadoActividadPreventiva;
  observaciones?: string;
}

// ---- Asesorías (RF22–RF25) ----
export type TipoAsesoria = 'ACCIDENTE' | 'FISCALIZACION';
export type EstadoAsesoria = 'SOLICITADA' | 'EN_PROCESO' | 'CERRADA' | 'CANCELADA';

export interface AsesoriaResponse {
  id: number;
  idCliente: number;
  razonSocialCliente: string;
  idProfesional: number;
  nombreProfesional: string;
  fechaSolicitud: string;
  fechaAtencion: string | null;
  motivo: string;
  tipo: TipoAsesoria;
  estado: EstadoAsesoria;
  esAsesoriaExtra: boolean;
}

export interface CrearAsesoriaRequest {
  idCliente: number;
  idProfesional: number;
  tipo: TipoAsesoria;
  motivo: string;
}

// Accidentes (RF22, RF35)
export type GravedadAccidente = 'LEVE' | 'GRAVE' | 'FATAL';

export interface AccidenteResponse {
  id: number;
  idAsesoria: number;
  fechaOcurrencia: string;
  descripcion: string | null;
  gravedad: GravedadAccidente;
  trabajadorAfectado: string | null;
  diasPerdidos: number | null;
  fueReportadoSusseso: boolean;
}

export interface CrearAccidenteRequest {
  idAsesoria: number;
  fechaOcurrencia: string;
  descripcion?: string;
  gravedad: GravedadAccidente;
  trabajadorAfectado?: string;
  diasPerdidos?: number;
  fueReportadoSusseso: boolean;
}

// Fiscalizaciones (RF22, RF42–RF44)
export type EntidadFiscalizadora = 'DIRECCION_TRABAJO' | 'SUSESO' | 'SEREMI_SALUD' | 'MUTUALIDAD' | 'OTRO';
export type ResultadoFiscalizacion = 'CONFORME' | 'CON_OBSERVACIONES' | 'NO_CONFORME';

export interface FiscalizacionResponse {
  id: number;
  idAsesoria: number;
  fecha: string;
  entidadFiscalizadora: EntidadFiscalizadora;
  motivo: string | null;
  resultado: ResultadoFiscalizacion | null;
  observaciones: string | null;
}

export interface CrearFiscalizacionRequest {
  idAsesoria: number;
  fecha: string;
  entidadFiscalizadora: EntidadFiscalizadora;
  motivo?: string;
  resultado?: ResultadoFiscalizacion;
  observaciones?: string;
}

// Multas (RF42–RF44)
export type EstadoMulta = 'PENDIENTE' | 'PAGADA' | 'APELADA' | 'ANULADA';

export interface MultaResponse {
  id: number;
  idFiscalizacion: number;
  fechaEmision: string;
  monto: number;
  motivo: string | null;
  normativaInfringida: string | null;
  estadoPago: EstadoMulta;
}

export interface CrearMultaRequest {
  idFiscalizacion: number;
  fechaEmision: string;
  monto: number;
  motivo?: string;
  normativaInfringida?: string;
}

// Propuestas de mejora del informe de asesoría (RF25)
export type EstadoPropuesta = 'PENDIENTE' | 'EN_PROCESO' | 'VERIFICADA' | 'DESCARTADA';

export interface PropuestaMejoraResponse {
  id: number;
  idInforme: number;
  descripcion: string;
  fechaPropuesta: string;
  fechaLimite: string | null;
  fechaVerificacion: string | null;
  estado: EstadoPropuesta;
  responsable: string | null;
}

export interface CrearPropuestaMejoraRequest {
  idInforme: number;
  descripcion: string;
  fechaLimite?: string;
  responsable?: string;
}

// Informe de asesoría (RF15 para asesorías, RF25)
export interface InformeAsesoriaResponse {
  id: number;
  idAsesoria: number;
  fechaEmision: string;
  estado: EstadoInforme;
  contenido: string | null;
  hallazgos: string | null;
  tieneArchivo: boolean;
}

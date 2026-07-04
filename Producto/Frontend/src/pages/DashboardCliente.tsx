import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import esLocale from '@fullcalendar/core/locales/es';
import type { EventInput } from '@fullcalendar/core';

import KpiCard from '../components/ui/KpiCard';
import Badge from '../components/ui/Badge';
import Panel from '../components/ui/Panel';
import { useAuth } from '../context/AuthContext';
import { obtenerDashboardCliente } from '../api/dashboard';
import { misVisitas } from '../api/visitas';
import { misCapacitaciones } from '../api/capacitaciones';
import { misAsesoriasCliente } from '../api/asesorias';
import { misActividadesPreventivas } from '../api/ActividadesPreventivas';
import type {
  DashboardClienteResponse,
  VarianteBadge,
  VarianteAlerta,
  VarianteKpi,
  VisitaResponse,
  CapacitacionResponse,
  AsesoriaResponse,
  ActividadPreventivaResponse,
} from '../types';

const claseAccion: Record<VarianteAlerta, string> = {
  peligro: 'alert-item alert-item--peligro',
  warn: 'alert-item alert-item--warn',
  info: 'alert-item alert-item--info',
  ok: 'alert-item alert-item--ok',
};

function badgeActividad(estado: string): VarianteBadge {
  if (estado === 'Programada') return 'blue';
  if (estado === 'Por confirmar') return 'yellow';
  return 'gray';
}

function varianteEstadoPago(estado: string): VarianteKpi {
  if (estado === 'Al dia' || estado === 'Al día' || estado === 'Al dÃ­a') return 'ok';
  if (estado === 'Suspendido') return 'peligro';
  return 'warn';
}

function servicioBadge(estado: string): { variante: VarianteBadge; texto: string } {
  if (estado === 'Suspendido') return { variante: 'red', texto: 'Servicio suspendido' };
  if (estado === 'Moroso' || estado === 'Atrasado') return { variante: 'yellow', texto: 'Pago pendiente' };
  return { variante: 'green', texto: 'Servicio activo' };
}

const fmtFecha = (iso: string | null) =>
  iso
    ? new Date(`${iso}T00:00:00`).toLocaleDateString('es-CL', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      })
    : '-';

const mesActual = new Date().toLocaleDateString('es-CL', { month: 'long', year: 'numeric' });

function esFechaPasada(fecha: string) {
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);

  const f = new Date(`${fecha}T00:00:00`);
  f.setHours(0, 0, 0, 0);

  return f < hoy;
}

function colorEventoCliente(
  tipo: 'visita' | 'capacitacion' | 'asesoria' | 'actividad',
  estado: string,
  fecha: string,
  vencida = false,
) {
  if (estado === 'CANCELADA') return '#9ca3af';

  const pasado = esFechaPasada(fecha);

  if (tipo === 'visita') {
    if (estado === 'REALIZADA') return '#16a34a';
    if (estado === 'EN_CURSO') return '#f59e0b';
    return pasado ? '#93c5fd' : '#2563eb';
  }

  if (tipo === 'capacitacion') {
    if (estado === 'REALIZADA') return '#16a34a';
    if (estado === 'EN_CURSO') return '#0891b2';
    return pasado ? '#a5f3fc' : '#06b6d4';
  }

  if (tipo === 'asesoria') {
    if (estado === 'CERRADA') return '#15803d';
    if (estado === 'EN_PROCESO') return '#f97316';
    return pasado ? '#fed7aa' : '#ea580c';
  }

  if (estado === 'CUMPLIDA') return '#16a34a';
  if (estado === 'VENCIDA' || vencida) return '#dc2626';
  if (estado === 'EN_CURSO') return '#f59e0b';
  return pasado ? '#cbd5e1' : '#64748b';
}

function eventosVisitasCliente(visitas: VisitaResponse[]): EventInput[] {
  return visitas.map((v) => {
    const color = colorEventoCliente('visita', v.estado, v.fechaProgramada);

    return {
      id: `visita-${v.id}`,
      title: 'Visita preventiva',
      start: v.fechaProgramada,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Visita',
        estado: v.estado,
        profesional: v.nombreProfesional,
        detalle: v.tipoRevision ?? 'Sin tipo',
      },
    };
  });
}

function eventosCapacitacionesCliente(capacitaciones: CapacitacionResponse[]): EventInput[] {
  return capacitaciones.map((c) => {
    const color = colorEventoCliente('capacitacion', c.estado, c.fechaProgramada);

    return {
      id: `capacitacion-${c.id}`,
      title: `Capacitacion - ${c.curso}`,
      start: c.fechaProgramada,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Capacitacion',
        estado: c.estado,
        profesional: c.relator,
        detalle: c.lugar,
      },
    };
  });
}

function eventosAsesoriasCliente(asesorias: AsesoriaResponse[]): EventInput[] {
  return asesorias.map((a) => {
    const fecha = a.fechaAtencion ?? a.fechaSolicitud;
    const color = colorEventoCliente('asesoria', a.estado, fecha);

    return {
      id: `asesoria-${a.id}`,
      title: `Asesoria - ${a.tipo}`,
      start: fecha,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Asesoria',
        estado: a.estado,
        profesional: a.nombreProfesional,
        detalle: a.motivo,
      },
    };
  });
}

function eventosActividadesCliente(actividades: ActividadPreventivaResponse[]): EventInput[] {
  return actividades.map((a) => {
    const color = colorEventoCliente('actividad', a.estado, a.fechaCompromiso, a.vencida);

    return {
      id: `actividad-${a.id}`,
      title: `Actividad preventiva - ${a.titulo}`,
      start: a.fechaCompromiso,
      allDay: true,
      backgroundColor: color,
      borderColor: color,
      extendedProps: {
        tipo: 'Actividad preventiva',
        estado: a.estado,
        profesional: a.responsable ?? 'Sin responsable',
        detalle: a.descripcion ?? a.observaciones ?? '-',
      },
    };
  });
}

export default function DashboardCliente() {
  const { email } = useAuth();
  const navigate = useNavigate();
  const [datos, setDatos] = useState<DashboardClienteResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [visitasAgenda, setVisitasAgenda] = useState<VisitaResponse[]>([]);
  const [capacitacionesAgenda, setCapacitacionesAgenda] = useState<CapacitacionResponse[]>([]);
  const [asesoriasAgenda, setAsesoriasAgenda] = useState<AsesoriaResponse[]>([]);
  const [actividadesAgenda, setActividadesAgenda] = useState<ActividadPreventivaResponse[]>([]);
  const [errorAgenda, setErrorAgenda] = useState<string | null>(null);

  useEffect(() => {
    obtenerDashboardCliente()
      .then(setDatos)
      .catch(() => setError('No se pudieron cargar los datos de tu panel.'));
  }, []);

  useEffect(() => {
    async function cargarAgendaCliente() {
      setErrorAgenda(null);

      try {
        const [visitasData, capacitacionesData, asesoriasData, actividadesData] = await Promise.all([
          misVisitas(),
          misCapacitaciones(),
          misAsesoriasCliente(),
          misActividadesPreventivas(),
        ]);

        setVisitasAgenda(visitasData);
        setCapacitacionesAgenda(capacitacionesData);
        setAsesoriasAgenda(asesoriasData);
        setActividadesAgenda(actividadesData);
      } catch {
        setVisitasAgenda([]);
        setCapacitacionesAgenda([]);
        setAsesoriasAgenda([]);
        setActividadesAgenda([]);
        setErrorAgenda('No se pudieron cargar los eventos de la agenda.');
      }
    }

    cargarAgendaCliente();
  }, []);

  const kpis = datos?.kpis;
  const acciones = datos?.accionesImportantes ?? [];
  const actividades = datos?.proximasActividades ?? [];
  const resumen = datos?.resumen;
  const servicio = servicioBadge(kpis?.estadoPago ?? 'Al dia');

  const resumenFilas = resumen
    ? [
        { label: 'Accidentes registrados en el mes', valor: resumen.accidentesMes },
        { label: 'Dias perdidos en el mes', valor: resumen.diasPerdidosMes },
        { label: 'Accidentes acumulados del anio', valor: resumen.accidentesAnio },
        { label: 'Capacitaciones realizadas en el anio', valor: resumen.capacitacionesRealizadasAnio },
      ]
    : [];

  const eventosAgenda = useMemo<EventInput[]>(
    () => [
      ...eventosVisitasCliente(visitasAgenda),
      ...eventosCapacitacionesCliente(capacitacionesAgenda),
      ...eventosAsesoriasCliente(asesoriasAgenda),
      ...eventosActividadesCliente(actividadesAgenda),
    ],
    [visitasAgenda, capacitacionesAgenda, asesoriasAgenda, actividadesAgenda],
  );

  return (
    <>
      <div className="page-title">
        Portal de Cliente <Badge variante={servicio.variante}>{servicio.texto}</Badge>
      </div>
      <div className="page-subtitle" style={{ textTransform: 'capitalize' }}>
        {email} - Seguimiento de visitas, capacitaciones, reportes y pagos - {mesActual}
      </div>

      {error && (
        <div className="alert-item alert-item--peligro" style={{ marginBottom: 12 }}>
          <div>{error}</div>
        </div>
      )}

      <div className="kpi-row">
        <KpiCard
          label="Visitas del mes"
          value={`${kpis?.visitasRealizadasMes ?? 0}/${kpis?.visitasProgramadasMes ?? 0}`}
          sub="Realizadas / programadas"
          variante={
            kpis && kpis.visitasProgramadasMes > 0 && kpis.visitasRealizadasMes >= kpis.visitasProgramadasMes
              ? 'ok'
              : 'warn'
          }
          progreso={
            kpis && kpis.visitasProgramadasMes > 0
              ? Math.min(100, (kpis.visitasRealizadasMes / kpis.visitasProgramadasMes) * 100)
              : 0
          }
        />
        <KpiCard
          label="Capacitaciones pendientes"
          value={kpis?.capacitacionesPendientes ?? 0}
          sub="Programadas por realizar"
          variante={(kpis?.capacitacionesPendientes ?? 0) > 0 ? 'warn' : 'ok'}
        />
        <KpiCard
          label="Asesorias usadas"
          value={`${kpis?.asesoriasUsadas ?? 0}/${kpis?.asesoriasLimite ?? 10}`}
          sub={`${Math.max(0, (kpis?.asesoriasLimite ?? 10) - (kpis?.asesoriasUsadas ?? 0))} disponibles dentro del plan`}
          variante="default"
          progreso={
            kpis && kpis.asesoriasLimite > 0
              ? Math.min(100, (kpis.asesoriasUsadas / kpis.asesoriasLimite) * 100)
              : 0
          }
        />
        <KpiCard
          label="Estado de pago"
          value={kpis?.estadoPago ?? '-'}
          sub={
            kpis?.proximoVencimiento
              ? `Proximo vencimiento: ${fmtFecha(kpis.proximoVencimiento)}`
              : 'Sin cuotas pendientes'
          }
          variante={varianteEstadoPago(kpis?.estadoPago ?? 'Al dia')}
        />
      </div>

      <Panel titulo="Agenda semanal">
        {errorAgenda && (
          <div className="alert-item alert-item--warn" style={{ margin: 12 }}>
            {errorAgenda}
          </div>
        )}

        <div className="agenda-semanal">
          <FullCalendar
            plugins={[dayGridPlugin, interactionPlugin]}
            initialView="dayGridWeek"
            locale={esLocale}
            firstDay={1}
            height="auto"
            events={eventosAgenda}
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridWeek,dayGridMonth',
            }}
            buttonText={{
              today: 'Hoy',
              week: 'Semana',
              month: 'Mes',
            }}
            eventClick={(info) => {
              const props = info.event.extendedProps;
              alert(
                `${props.tipo}\n${info.event.title}\nEstado: ${props.estado}\nProfesional/Responsable: ${props.profesional ?? 'Sin asignar'}\nDetalle: ${props.detalle ?? '-'}`
              );
            }}
          />
        </div>
      </Panel>

      <div className="grid-2">
        <Panel titulo="Acciones importantes">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12 }}>
            {acciones.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                Sin acciones pendientes.
              </div>
            ) : (
              acciones.map((a, i) => (
                <div key={i} className={claseAccion[a.severidad]}>
                  <div>
                    <b>{a.titulo}:</b> {a.detalle}
                  </div>
                </div>
              ))
            )}
          </div>
        </Panel>

        <Panel titulo="Accesos rapidos">
          <div className="grid-2" style={{ padding: 14, gap: 10 }}>
            <button
              className="btn btn-primary"
              onClick={() => navigate('/mis-solicitudes', { state: { tipoInicial: 'ASESORIA' } })}
            >
              Solicitar asesoria
            </button>
            <button
              className="btn btn-danger"
              onClick={() => navigate('/mis-solicitudes', { state: { tipoInicial: 'ACCIDENTE' } })}
            >
              🚨 Reportar accidente
            </button>
            <button
              className="btn btn-outline"
              onClick={() => navigate('/mis-solicitudes', { state: { tipoInicial: 'CAPACITACION' } })}
            >
              Solicitar capacitacion
            </button>
            <button className="btn btn-outline" onClick={() => navigate('/configuracion')}>
              Actualizar contacto
            </button>
          </div>
        </Panel>
      </div>

      <div className="grid-2">
        <Panel titulo="Proximas actividades">
          <table className="app-table">
            <thead>
              <tr>
                {['Fecha', 'Actividad', 'Profesional', 'Estado'].map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {actividades.length === 0 ? (
                <tr>
                  <td colSpan={4} style={{ textAlign: 'center', color: '#9ca3af', padding: 16 }}>
                    Sin actividades proximas.
                  </td>
                </tr>
              ) : (
                actividades.map((a, i) => (
                  <tr key={i}>
                    <td>{fmtFecha(a.fecha)}</td>
                    <td>{a.actividad}</td>
                    <td>{a.profesional}</td>
                    <td>
                      <Badge variante={badgeActividad(a.estado)}>{a.estado}</Badge>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </Panel>

        <Panel titulo="Resumen preventivo">
          <div style={{ padding: '14px 16px' }}>
            {resumenFilas.map((r, i) => (
              <div
                key={i}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  padding: '7px 0',
                  fontSize: 12,
                  color: '#5e6a78',
                }}
              >
                <span>{r.label}</span>
                <b style={{ color: '#243041' }}>{r.valor}</b>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </>
  );
}
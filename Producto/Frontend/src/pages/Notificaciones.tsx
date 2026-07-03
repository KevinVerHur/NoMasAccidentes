import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Panel from '../components/ui/Panel';
import type { NotificacionResponse, TipoNotificacion } from '../types';
import {
  listarMisNotificaciones,
  marcarLeida,
  marcarTodasLeidas,
} from '../api/notificaciones';

const iconoPorTipo: Record<TipoNotificacion, string> = {
  VISITA_PLANIFICADA: '📅',
  CAPACITACION_PROGRAMADA: '🎓',
  ASESORIA_REGISTRADA: '📋',
};

const fmtFecha = (iso: string) =>
  new Date(iso).toLocaleString('es-CL', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

export default function Notificaciones() {
  const navigate = useNavigate();
  const [items, setItems] = useState<NotificacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    listarMisNotificaciones()
      .then(setItems)
      .catch(() => {})
      .finally(() => setCargando(false));
  }, []);

  const noLeidas = items.filter((n) => !n.leida).length;

  async function abrir(n: NotificacionResponse) {
    if (!n.leida) {
      try {
        await marcarLeida(n.id);
        setItems((prev) =>
          prev.map((x) => (x.id === n.id ? { ...x, leida: true } : x))
        );
      } catch {
        /* si falla el marcado, igual navegamos */
      }
    }
    if (n.enlace) navigate(n.enlace);
  }

  async function leerTodas() {
    await marcarTodasLeidas();
    setItems((prev) => prev.map((x) => ({ ...x, leida: true })));
  }

  return (
    <>
      <div className="page-title">Notificaciones</div>
      <div className="page-subtitle">
        {noLeidas > 0 ? `Tienes ${noLeidas} sin leer` : 'Estás al día'}
      </div>

      {noLeidas > 0 && (
        <div style={{ marginBottom: 12 }}>
          <button className="btn btn-secundario" onClick={leerTodas}>
            Marcar todas como leídas
          </button>
        </div>
      )}

      <Panel titulo="🔔 Tu bandeja">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : items.length === 0 ? (
          <div className="placeholder">No tienes notificaciones.</div>
        ) : (
          <div className="flex flex-col divide-y divide-gray-100">
            {items.map((n) => (
              <div
                key={n.id}
                onClick={() => abrir(n)}
                className={`flex items-start gap-3 px-3 py-3 cursor-pointer hover:bg-blue-50 ${
                  n.leida ? 'opacity-70' : 'bg-blue-50/40'
                }`}
              >
                <span className="text-lg leading-none mt-[2px]">
                  {iconoPorTipo[n.tipo] ?? '🔔'}
                </span>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-[13px] ${
                        n.leida ? 'text-gray-600' : 'font-bold text-azul'
                      }`}
                    >
                      {n.titulo}
                    </span>
                    {!n.leida && (
                      <span className="w-[7px] h-[7px] rounded-full bg-peligro shrink-0" />
                    )}
                  </div>
                  <div className="text-[12px] text-gray-600">{n.mensaje}</div>
                  <div className="text-[11px] text-gray-400 mt-[2px]">
                    {fmtFecha(n.fechaCreacion)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Panel>
    </>
  );
}

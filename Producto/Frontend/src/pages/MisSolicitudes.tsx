import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type {
  SolicitudResponse,
  TipoSolicitud,
  EstadoSolicitud,
  VarianteBadge,
} from '../types';
import { listarMisSolicitudes, crearSolicitud } from '../api/solicitudes';

const badgePorEstado: Record<EstadoSolicitud, VarianteBadge> = {
  PENDIENTE: 'yellow',
  APROBADA: 'green',
  RECHAZADA: 'red',
};

const etiquetaTipo: Record<TipoSolicitud, string> = {
  ASESORIA: 'Asesoría',
  CAPACITACION: 'Capacitación',
  VISITA: 'Visita',
};

const fmtFecha = (iso: string | null) =>
  iso ? new Date(iso).toLocaleDateString('es-CL') : '—';

export default function MisSolicitudes() {
  const location = useLocation();
  const tipoInicial =
    (location.state as { tipoInicial?: TipoSolicitud } | null)?.tipoInicial ?? 'ASESORIA';
  const [items, setItems] = useState<SolicitudResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [tipo, setTipo] = useState<TipoSolicitud>(tipoInicial);
  const [descripcion, setDescripcion] = useState('');
  const [fechaPreferida, setFechaPreferida] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function cargar() {
    listarMisSolicitudes()
      .then(setItems)
      .catch(() => {})
      .finally(() => setCargando(false));
  }

  useEffect(cargar, []);

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    if (!descripcion.trim()) {
      setError('Describe brevemente lo que necesitas.');
      return;
    }
    setEnviando(true);
    setError(null);
    try {
      await crearSolicitud({
        tipo,
        descripcion: descripcion.trim(),
        fechaPreferida: fechaPreferida || null,
      });
      setDescripcion('');
      setFechaPreferida('');
      setTipo('ASESORIA');
      cargar();
    } catch {
      setError('No se pudo enviar la solicitud. Intenta nuevamente.');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="page-title">Solicitudes</div>
      <div className="page-subtitle">
        Solicita una asesoría, capacitación o visita a tu consultora
      </div>

      <Panel titulo="➕ Nueva solicitud">
        <form onSubmit={enviar} className="flex flex-col gap-3 max-w-[520px]">
          <label className="text-[13px] text-gray-600">
            Tipo de servicio
            <select
              className="auth-input mt-1"
              value={tipo}
              onChange={(e) => setTipo(e.target.value as TipoSolicitud)}
            >
              <option value="ASESORIA">Asesoría</option>
              <option value="CAPACITACION">Capacitación</option>
              <option value="VISITA">Visita</option>
            </select>
          </label>

          <label className="text-[13px] text-gray-600">
            ¿Qué necesitas?
            <textarea
              className="auth-input mt-1"
              rows={3}
              maxLength={500}
              placeholder="Describe el motivo o detalle de tu solicitud"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
            />
          </label>

          <label className="text-[13px] text-gray-600">
            Fecha preferida (opcional)
            <input
              type="date"
              className="auth-input mt-1"
              value={fechaPreferida}
              onChange={(e) => setFechaPreferida(e.target.value)}
            />
          </label>

          {error && <div className="text-[13px] text-peligro">{error}</div>}

          <div>
            <button type="submit" className="btn btn-primary" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar solicitud'}
            </button>
          </div>
        </form>
      </Panel>

      <Panel titulo="📋 Mis solicitudes">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : items.length === 0 ? (
          <div className="placeholder">Aún no has enviado solicitudes.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Tipo</th>
                <th>Detalle</th>
                <th>Fecha preferida</th>
                <th>Enviada</th>
                <th>Estado</th>
                <th>Respuesta</th>
              </tr>
            </thead>
            <tbody>
              {items.map((s) => (
                <tr key={s.id}>
                  <td>{etiquetaTipo[s.tipo]}</td>
                  <td>{s.descripcion}</td>
                  <td>{fmtFecha(s.fechaPreferida)}</td>
                  <td>{fmtFecha(s.fechaCreacion)}</td>
                  <td>
                    <Badge variante={badgePorEstado[s.estado]}>{s.estado}</Badge>
                    {s.estado === 'APROBADA' && s.esExtra && (
                      <span className="ml-1 text-[11px] text-warn font-bold">EXTRA</span>
                    )}
                  </td>
                  <td className="text-[12px] text-gray-600">
                    {s.respuestaAdmin ?? '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>
    </>
  );
}

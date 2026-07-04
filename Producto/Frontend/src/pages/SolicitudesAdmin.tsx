import { useEffect, useState } from 'react';
import Panel from '../components/ui/Panel';
import Badge from '../components/ui/Badge';
import type {
  SolicitudResponse,
  TipoSolicitud,
  EstadoSolicitud,
  VarianteBadge,
  ProfesionalResponse,
  AprobarSolicitudRequest,
} from '../types';
import {
  listarSolicitudes,
  aprobarSolicitud,
  rechazarSolicitud,
} from '../api/solicitudes';
import { listarProfesionales } from '../api/profesionales';

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

type Modo = 'aprobar' | 'rechazar';

export default function SolicitudesAdmin() {
  const [items, setItems] = useState<SolicitudResponse[]>([]);
  const [profesionales, setProfesionales] = useState<ProfesionalResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [soloPendientes, setSoloPendientes] = useState(true);

  // Modal
  const [sel, setSel] = useState<SolicitudResponse | null>(null);
  const [modo, setModo] = useState<Modo>('aprobar');
  const [form, setForm] = useState<AprobarSolicitudRequest>({});
  const [comentario, setComentario] = useState('');
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function cargar() {
    setCargando(true);
    listarSolicitudes(soloPendientes ? 'PENDIENTE' : undefined)
      .then(setItems)
      .catch(() => {})
      .finally(() => setCargando(false));
  }

  useEffect(cargar, [soloPendientes]);

  useEffect(() => {
    listarProfesionales(0, 100)
      .then((p) => setProfesionales(p.content))
      .catch(() => {});
  }, []);

  function abrir(s: SolicitudResponse, m: Modo) {
    setSel(s);
    setModo(m);
    setError(null);
    setComentario('');
    setForm({ esExtra: s.tipo === 'ASESORIA' ? s.sugerenciaExtra ?? false : false });
  }

  function cerrar() {
    setSel(null);
  }

  const set = (patch: Partial<AprobarSolicitudRequest>) =>
    setForm((f) => ({ ...f, ...patch }));

  async function confirmar() {
    if (!sel) return;
    setGuardando(true);
    setError(null);
    try {
      if (modo === 'rechazar') {
        if (!comentario.trim()) {
          setError('Indica el motivo del rechazo.');
          setGuardando(false);
          return;
        }
        await rechazarSolicitud(sel.id, { comentario: comentario.trim() });
      } else {
        await aprobarSolicitud(sel.id, { ...form, comentario: comentario.trim() || null });
      }
      cerrar();
      cargar();
    } catch (e: unknown) {
      const data = (e as { response?: { data?: { message?: string; errores?: string[] } } })?.response?.data;
      const msg =
        data?.message ??
        (data?.errores && data.errores.length ? data.errores.join(' · ') : null) ??
        'No se pudo completar la acción. Revisa los datos.';
      setError(msg);
    } finally {
      setGuardando(false);
    }
  }

  const pendientes = items.filter((s) => s.estado === 'PENDIENTE').length;

  // Restricciones de fecha: la visita no puede ser en el pasado; la capacitación
  // exige al menos 15 días de anticipación (RF-CAP1).
  const hoyISO = new Date().toISOString().slice(0, 10);
  const minCapacitacionISO = new Date(new Date().getTime() + 15 * 86_400_000).toISOString().slice(0, 10);

  return (
    <>
      <div className="page-title">Solicitudes de clientes</div>
      <div className="page-subtitle">
        Aprueba (creando el servicio) o rechaza las solicitudes del portal
      </div>

      <div className="responsive-inline-fields mb-3">
        <label className="text-[13px] text-gray-600 flex items-center gap-2">
          <input
            type="checkbox"
            checked={soloPendientes}
            onChange={(e) => setSoloPendientes(e.target.checked)}
          />
          Solo pendientes {pendientes > 0 && `(${pendientes})`}
        </label>
      </div>

      <Panel titulo="📥 Bandeja de solicitudes">
        {cargando ? (
          <div className="placeholder">Cargando...</div>
        ) : items.length === 0 ? (
          <div className="placeholder">No hay solicitudes.</div>
        ) : (
          <table className="app-table">
            <thead>
              <tr>
                <th>Empresa</th>
                <th>Tipo</th>
                <th>Detalle</th>
                <th>Fecha preferida</th>
                <th>Enviada</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {items.map((s) => (
                <tr key={s.id}>
                  <td>{s.razonSocialEmpresa}</td>
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
                  <td>
                    {s.estado === 'PENDIENTE' ? (
                      <div className="btn-group">
                        <button className="btn btn-sm btn-success" onClick={() => abrir(s, 'aprobar')}>
                          Aprobar
                        </button>
                        <button className="btn btn-sm btn-danger" onClick={() => abrir(s, 'rechazar')}>
                          Rechazar
                        </button>
                      </div>
                    ) : (
                      <span className="text-[12px] text-gray-500">{s.respuestaAdmin ?? '—'}</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Panel>

      {sel && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-3">
          <div className="responsive-modal-shell bg-white rounded-lg p-5 overflow-y-auto">
            <div className="font-bold text-azul mb-1">
              {modo === 'aprobar' ? 'Aprobar' : 'Rechazar'} solicitud — {etiquetaTipo[sel.tipo]}
            </div>
            <div className="text-[12px] text-gray-500 mb-3">
              {sel.razonSocialEmpresa} · {sel.descripcion}
            </div>

            {modo === 'aprobar' ? (
              <div className="flex flex-col gap-3">
                <label className="text-[13px] text-gray-600">
                  Profesional {sel.tipo === 'CAPACITACION' ? '(relator)' : 'asignado'}
                  <select
                    className="auth-input mt-1"
                    value={form.idProfesional ?? ''}
                    onChange={(e) => set({ idProfesional: Number(e.target.value) || null })}
                  >
                    <option value="">Selecciona…</option>
                    {profesionales.map((p) => (
                      <option key={p.id} value={p.id}>{p.nombreCompleto}</option>
                    ))}
                  </select>
                </label>

                {sel.tipo === 'ASESORIA' && (
                  <label className="text-[13px] text-gray-600">
                    Tipo de asesoría
                    <select
                      className="auth-input mt-1"
                      value={form.tipoAsesoria ?? ''}
                      onChange={(e) => set({ tipoAsesoria: (e.target.value || null) as AprobarSolicitudRequest['tipoAsesoria'] })}
                    >
                      <option value="">Selecciona…</option>
                      <option value="ACCIDENTE">Accidente</option>
                      <option value="FISCALIZACION">Fiscalización</option>
                    </select>
                  </label>
                )}

                {sel.tipo === 'VISITA' && (
                  <>
                    <label className="text-[13px] text-gray-600">
                      Fecha de la visita
                      <input type="date" className="auth-input mt-1" min={hoyISO}
                        value={form.fechaProgramada ?? ''}
                        onChange={(e) => set({ fechaProgramada: e.target.value || null })} />
                    </label>
                    <label className="text-[13px] text-gray-600">
                      Tipo de revisión (opcional)
                      <input className="auth-input mt-1" maxLength={20}
                        value={form.tipoRevision ?? ''}
                        onChange={(e) => set({ tipoRevision: e.target.value || null })} />
                    </label>
                  </>
                )}

                {sel.tipo === 'CAPACITACION' && (
                  <>
                    <label className="text-[13px] text-gray-600">
                      Curso
                      <input className="auth-input mt-1" maxLength={150}
                        value={form.curso ?? ''}
                        onChange={(e) => set({ curso: e.target.value || null })} />
                    </label>
                    <div className="responsive-inline-fields">
                      <label className="text-[13px] text-gray-600 flex-1">
                        Fecha
                        <input type="date" className="auth-input mt-1" min={minCapacitacionISO}
                          value={form.fechaProgramada ?? ''}
                          onChange={(e) => set({ fechaProgramada: e.target.value || null })} />
                      </label>
                      <label className="text-[13px] text-gray-600 flex-1">
                        Hora
                        <input type="time" className="auth-input mt-1"
                          value={form.horaProgramada ?? ''}
                          onChange={(e) => set({ horaProgramada: e.target.value || null })} />
                      </label>
                    </div>
                    <div className="text-[11px] text-gray-400 -mt-1">
                      La capacitación requiere al menos 15 días de anticipación (RF-CAP1).
                    </div>
                    <div className="responsive-inline-fields">
                      <label className="text-[13px] text-gray-600 flex-1">
                        Lugar
                        <input className="auth-input mt-1" maxLength={150}
                          value={form.lugar ?? ''}
                          onChange={(e) => set({ lugar: e.target.value || null })} />
                      </label>
                      <label className="text-[13px] text-gray-600">
                        Cupos
                        <input type="number" min={1} className="auth-input mt-1"
                          value={form.cupos ?? ''}
                          onChange={(e) => set({ cupos: Number(e.target.value) || null })} />
                      </label>
                    </div>
                    <label className="text-[13px] text-gray-600">
                      Objetivo (opcional)
                      <textarea className="auth-input mt-1" rows={2} maxLength={500}
                        value={form.objetivo ?? ''}
                        onChange={(e) => set({ objetivo: e.target.value || null })} />
                    </label>
                  </>
                )}

                <label className="text-[13px] text-gray-700 flex items-center gap-2 bg-gray-50 rounded p-2">
                  <input type="checkbox"
                    checked={!!form.esExtra}
                    onChange={(e) => set({ esExtra: e.target.checked })} />
                  Cobrar como servicio adicional (fuera del plan)
                  {sel.tipo === 'ASESORIA' && sel.sugerenciaExtra && (
                    <span className="text-[11px] text-warn font-bold">sugerido: EXTRA</span>
                  )}
                </label>

                <label className="text-[13px] text-gray-600">
                  Comentario para el cliente (opcional)
                  <textarea className="auth-input mt-1" rows={2} maxLength={500}
                    value={comentario} onChange={(e) => setComentario(e.target.value)} />
                </label>
              </div>
            ) : (
              <label className="text-[13px] text-gray-600">
                Motivo del rechazo
                <textarea className="auth-input mt-1" rows={3} maxLength={500}
                  value={comentario} onChange={(e) => setComentario(e.target.value)} />
              </label>
            )}

            {error && <div className="text-[13px] text-peligro mt-2">{error}</div>}

            <div className="btn-group mt-4 justify-end flex">
              <button className="btn btn-outline" onClick={cerrar} disabled={guardando}>
                Cancelar
              </button>
              <button
                className={`btn ${modo === 'aprobar' ? 'btn-success' : 'btn-danger'}`}
                onClick={confirmar}
                disabled={guardando}
              >
                {guardando ? 'Guardando…' : modo === 'aprobar' ? 'Aprobar y crear' : 'Rechazar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

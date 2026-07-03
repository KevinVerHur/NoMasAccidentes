import SeguimientoPreventivoPanel from '../components/actividades/SeguimientoPreventivoPanel';

export default function MiCumplimiento() {
  return (
    <>
      <div className="page-title">Cumplimiento normativo</div>
      <div className="page-subtitle">
        Actividades preventivas comprometidas, sus plazos y estado de cumplimiento
        (solo lectura)
      </div>

      <SeguimientoPreventivoPanel titulo="🛡️ Actividades preventivas" modoCliente />
    </>
  );
}

import SeguimientoPreventivoPanel from '../components/actividades/SeguimientoPreventivoPanel';

export default function MisActividades() {
  return (
    <>
      <div className="page-title">Mis actividades</div>
      <div className="page-subtitle">
        Actividades preventivas, cumplimiento y evidencias pendientes
      </div>

      <SeguimientoPreventivoPanel
        titulo="Actividades preventivas"
        modoCliente
      />
    </>
  );
}
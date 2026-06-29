import { useAuth } from '../context/AuthContext';
import ReportesGestion from './ReportesGestion';
import MisReportes from './MisReportes';

export default function Reportes() {
  const { rol } = useAuth();
  if (rol === 'CLIENTE') return <MisReportes />;
  return <ReportesGestion />;
}

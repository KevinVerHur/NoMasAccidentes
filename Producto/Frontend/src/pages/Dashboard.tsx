import { useAuth } from '../context/AuthContext';
import DashboardAdmin from './DashboardAdmin';
import DashboardProfesional from './DashboardProfesional';
import DashboardCliente from './DashboardCliente';

export default function Dashboard() {
  const { rol } = useAuth();

  switch (rol) {
    case 'PROFESIONAL':
      return <DashboardProfesional />;
    case 'CLIENTE':
      return <DashboardCliente />;
    case 'ADMIN':
    case 'CAPACITADOR':
    default:
      return <DashboardAdmin />;
  }
}

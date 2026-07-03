import { useAuth } from '../context/AuthContext';
// import  from './ConfiguracionCliente';
import ConfiguracionProfesional from './ConfiguracionProfesional';
import ConfiguracionAdmin from './ConfiguracionAdmin';
import ConfiguracionProfesional from './ConfiguracionProfesional';
 

export default function Configuracion() {
  const { rol } = useAuth();

  switch (rol) {
    case 'PROFESIONAL':
      return <ConfiguracionProfesional />;
    //case 'CLIENTE':
    //   return <ConfiguracionCliente />;
     case 'ADMIN':
    default:
      return <ConfiguracionAdmin />;
  }
}
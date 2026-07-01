import { useAuth } from '../context/AuthContext';
// import  from './ConfiguracionCliente';
// import  from './ConfiguracionProfesional';
import ConfiguracionAdmin from './ConfiguracionAdmin';
 

export default function Configuracion() {
  const { rol } = useAuth();

  switch (rol) {
    // case 'PROFESIONAL':
    //   return <ConfiguracionProfesional />;
    // case 'CLIENTE':
    //   return <ConfiguracionCliente />;
     case 'ADMIN':
    default:
      return <ConfiguracionAdmin />;
  }
}
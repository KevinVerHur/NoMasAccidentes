import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import type { Rol } from '../../types';

const navLinks: { label: string; path: string; roles: Rol[] }[] = [
    // ADMIN / PROFESIONAL
    { label: 'Dashboard',     path: '/dashboard',     roles: ['ADMIN', 'PROFESIONAL', 'CAPACITADOR'] },
    { label: 'Clientes',      path: '/clientes',      roles: ['ADMIN'] },
    { label: 'Profesionales', path: '/profesionales', roles: ['ADMIN'] },
    { label: 'Visitas',       path: '/visitas',       roles: ['ADMIN', 'PROFESIONAL'] },
    { label: 'Capacitaciones', path: '/capacitaciones',  roles: ['ADMIN','PROFESIONAL'] },
    { label: 'Asesorias', path: '/asesorias',  roles: ['PROFESIONAL'] },
    { label: 'Reportes',      path: '/reportes',      roles: ['ADMIN', 'PROFESIONAL'] },
    // CLIENTE (vistas aún no construidas, solo navegación)
    { label: 'Inicio',         path: '/dashboard',       roles: ['CLIENTE'] },
    // { label: 'Actividades',    path: '/mis-actividades', roles: ['CLIENTE'] },
    { label: 'Capacitaciones', path: '/capacitaciones',  roles: ['CLIENTE'] },
    { label: 'Reportes',       path: '/reportes',        roles: ['CLIENTE'] },
    { label: 'Pagos',          path: '/mis-pagos',       roles: ['CLIENTE'] },
];

export default function Topbar() {
    const { email, rol, cerrarSesion } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const linksVisibles = navLinks.filter((l) => rol != null && l.roles.includes(rol));

    function handleCerrarSesion() {
        cerrarSesion();
        navigate('/login');
    }

    return (
        <header className="fixed top-0 left-0 right-0 h-[54px] bg-azul text-white flex items-center justify-between px-5 z-50">
            <div className="text-lg font-bold flex items-center gap-2 shrink-0">
                🦺 <span className="text-dorado">No Más</span>&nbsp;Accidentes
            </div>

            <nav className="flex">
                {linksVisibles.map(({ label, path }) => {
                    const activo = location.pathname === path;
                    return (
                        <a
                            key={path}
                            onClick={() => navigate(path)}
                            className={`text-[13px] px-[18px] cursor-pointer no-underline pb-1 ${
                                activo
                                    ? 'text-white border-b-2 border-dorado'
                                    : 'text-gray-300 hover:text-white'
                            }`}
                        >
                            {label}
                        </a>
                    );
                })}
            </nav>

            <div className="flex items-center gap-3 text-[12px] text-gray-300 shrink-0">
                <span>👤 {email}</span>
                <span>|</span>
                <span className="text-dorado">{rol}</span>
                <button
                    onClick={handleCerrarSesion}
                    className="bg-dorado text-azul px-3 py-1 rounded text-[12px] font-bold cursor-pointer border-none"
                >
                    Cerrar sesión
                </button>
            </div>
        </header>
    );
}

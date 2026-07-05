import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { contarNoLeidas } from '../../api/notificaciones';
import { contarSolicitudesPendientes } from '../../api/solicitudes';
import type { Rol } from '../../types';

interface ItemNav {
    icono: string;
    label: string;
    path: string;
    badge?: number;
    roles: Rol[];
}

interface SeccionNav {
    titulo: string;
    items: ItemNav[];
}

interface SidebarProps {
    abierto: boolean;
    onCerrar: () => void;
}

const TODOS: Rol[] = ['ADMIN', 'PROFESIONAL', 'CLIENTE', 'CAPACITADOR'];
const RUTAS_COMENTADAS = new Set<string>();

// Menú operativo: ADMIN y PROFESIONAL.
const seccionesOperativas: SeccionNav[] = [
    {
        titulo: 'Gestión',
        items: [
            { icono: '📊', label: 'Dashboard', path: '/dashboard', roles: TODOS },
            { icono: '👥', label: 'Clientes', path: '/clientes', roles: ['ADMIN'] },
            { icono: '🧑‍💼', label: 'Profesionales', path: '/profesionales', roles: ['ADMIN'] },
            { icono: '📅', label: 'Visitas', path: '/visitas', roles: ['ADMIN', 'PROFESIONAL'] },
            { icono: '🎓', label: 'Capacitaciones', path: '/capacitaciones', roles: ['ADMIN', 'PROFESIONAL', 'CAPACITADOR'] },
            { icono: '📋', label: 'Asesorías', path: '/asesorias', roles: ['ADMIN', 'PROFESIONAL'] },
            { icono: '📞', label: 'Comunicaciones', path: '/comunicaciones', roles: ['ADMIN', 'PROFESIONAL'] },
            { icono: '📨', label: 'Solicitudes', path: '/solicitudes', roles: ['ADMIN'] },
            { icono: '✅', label: 'Seguimiento preventivo', path: '/seguimiento-preventivo', roles: ['ADMIN', 'PROFESIONAL'] },
        ],
    },
    {
        titulo: 'Finanzas',
        items: [
            { icono: '💰', label: 'Pagos', path: '/pagos', roles: ['ADMIN'] },
            { icono: '⚠️', label: 'Morosidades', path: '/morosidades', roles: ['ADMIN'] },
        ],
    },
    {
        titulo: 'Sistema',
        items: [
            { icono: '📄', label: 'Reportes', path: '/reportes', roles: ['ADMIN', 'PROFESIONAL'] },
        ],
    },
    {
        titulo: 'Cuenta',
        items: [
            { icono: '🔔', label: 'Notificaciones', path: '/notificaciones', roles: ['PROFESIONAL', 'ADMIN'] },
            { icono: '⚙️', label: 'Configuración', path: '/configuracion', roles: ['ADMIN', 'PROFESIONAL'] },
        ],
    },
];

// Portal del cliente. Las vistas aún no se construyen: por ahora solo navegación.
const seccionesCliente: SeccionNav[] = [
    {
        titulo: 'Portal Cliente',
        items: [
            { icono: '🏠', label: 'Inicio', path: '/dashboard', roles: ['CLIENTE'] },
            { icono: '🎓', label: 'Capacitaciones', path: '/mis-capacitaciones', roles: ['CLIENTE'] },
            { icono: '📅', label: 'Mis actividades', path: '/mis-actividades', roles: ['CLIENTE'] },
            { icono: '🛡️', label: 'Cumplimiento', path: '/mi-cumplimiento', roles: ['CLIENTE'] },
            { icono: '📄', label: 'Reportes', path: '/mis-reportes', roles: ['CLIENTE'] },
            { icono: '💰', label: 'Pagos', path: '/mis-pagos', roles: ['CLIENTE'] },
            { icono: '📋', label: 'Solicitudes', path: '/mis-solicitudes', roles: ['CLIENTE'] },
        ],
    },
    {
        titulo: 'Cuenta',
        items: [
            { icono: '🔔', label: 'Notificaciones', path: '/notificaciones', roles: ['CLIENTE'] },
            { icono: '⚙️', label: 'Configuración', path: '/configuracion', roles: ['CLIENTE'] },
        ],
    },
];

export default function Sidebar({ abierto, onCerrar }: SidebarProps) {
    const navigate = useNavigate();
    const location = useLocation();
    const { rol } = useAuth();
    const [noLeidas, setNoLeidas] = useState(0);
    const [solicitudesPendientes, setSolicitudesPendientes] = useState(0);

    // Badge real de notificaciones sin leer (bandeja disponible para los 3 roles).
    useEffect(() => {
        if (rol === 'CLIENTE' || rol === 'PROFESIONAL' || rol === 'ADMIN') {
            contarNoLeidas().then(setNoLeidas).catch(() => setNoLeidas(0));
        }
        if (rol === 'ADMIN') {
            contarSolicitudesPendientes().then(setSolicitudesPendientes).catch(() => setSolicitudesPendientes(0));
        }
    }, [rol, location.pathname]);

    const fuente = rol === 'CLIENTE' ? seccionesCliente : seccionesOperativas;

    const seccionesVisibles = fuente
        .map((seccion) => ({
            ...seccion,
            items: seccion.items.filter((item) =>
                rol != null && item.roles.includes(rol) && !RUTAS_COMENTADAS.has(item.path)
            ),
        }))
        .filter((seccion) => seccion.items.length > 0);

    return (
        <>
            <button
                type="button"
                className={`sidebar-overlay ${abierto ? 'sidebar-overlay--visible' : ''}`}
                onClick={onCerrar}
                aria-label="Cerrar menu"
            />
            <aside className={`app-sidebar ${abierto ? 'app-sidebar--open' : ''}`}>
            {seccionesVisibles.map((seccion) => (
                <div key={seccion.titulo}>
                    <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest px-4 pt-3 pb-1">
                        {seccion.titulo}
                    </div>
                    {seccion.items.map((item) => {
                        const activo = location.pathname === item.path;
                        const badge =
                            item.path === '/notificaciones' ? noLeidas :
                            item.path === '/solicitudes' ? solicitudesPendientes :
                            item.badge;
                        return (
                            <a
                                key={item.path}
                                onClick={() => {
                                    navigate(item.path);
                                    onCerrar();
                                }}
                                className={`flex items-center gap-2 px-4 py-[9px] text-[13px] cursor-pointer no-underline ${
                                    activo
                                        ? 'bg-blue-50 text-azul font-bold border-l-[3px] border-azul'
                                        : 'text-gray-600 hover:bg-blue-50 hover:text-azul'
                                }`}
                            >
                                <span>{item.icono}</span>
                                <span className="flex-1">{item.label}</span>
                                {badge ? (
                                    <span className="bg-peligro text-white text-[9px] rounded-full min-w-[14px] h-[14px] px-1 flex items-center justify-center font-bold">
                                        {badge}
                                    </span>
                                ) : null}
                            </a>
                        );
                    })}
                </div>
            ))}
            </aside>
        </>
    );
}

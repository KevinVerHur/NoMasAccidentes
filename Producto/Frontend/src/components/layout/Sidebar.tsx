import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
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

const TODOS: Rol[] = ['ADMIN', 'PROFESIONAL', 'CLIENTE', 'CAPACITADOR'];
// Pestañas comentadas temporalmente: Comunicaciones y Seguimiento preventivo
// para ADMIN/PROFESIONAL, y Mis actividades para CLIENTE.
const RUTAS_COMENTADAS = new Set(['/comunicaciones', '/seguimiento-preventivo', '/mis-actividades']);

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
            { icono: '✅', label: 'Seguimiento preventivo', path: '/seguimiento-preventivo', roles: ['ADMIN', 'PROFESIONAL'] },
            { icono: '☎️', label: 'Comunicaciones', path: '/comunicaciones', roles: ['ADMIN', 'PROFESIONAL'] },
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
            { icono: '🔔', label: 'Alertas', path: '/alertas', badge: 5, roles: ['ADMIN'] },
            { icono: '⚙️', label: 'Configuración', path: '/configuracion', roles: ['ADMIN'] },
        ],
    },
];

// Portal del cliente. Las vistas aún no se construyen: por ahora solo navegación.
const seccionesCliente: SeccionNav[] = [
    {
        titulo: 'Portal Cliente',
        items: [
            { icono: '🏠', label: 'Inicio', path: '/dashboard', roles: ['CLIENTE'] },
            { icono: '📅', label: 'Mis actividades', path: '/mis-actividades', roles: ['CLIENTE'] },
            { icono: '🎓', label: 'Capacitaciones', path: '/capacitaciones', roles: ['CLIENTE'] },
            { icono: '📄', label: 'Reportes', path: '/reportes', roles: ['CLIENTE'] },
            { icono: '💰', label: 'Pagos', path: '/mis-pagos', roles: ['CLIENTE'] },
            { icono: '📋', label: 'Solicitudes', path: '/mis-solicitudes', roles: ['CLIENTE'] },
        ],
    },
    {
        titulo: 'Cuenta',
        items: [
            { icono: '🔔', label: 'Notificaciones', path: '/notificaciones', badge: 3, roles: ['CLIENTE'] },
            { icono: '🏢', label: 'Mi empresa', path: '/mi-empresa', roles: ['CLIENTE'] },
        ],
    },
];

export default function Sidebar() {
    const navigate = useNavigate();
    const location = useLocation();
    const { rol } = useAuth();

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
        <aside className="fixed top-[54px] left-0 bottom-0 w-[190px] bg-white border-r border-gray-200 overflow-y-auto z-40">
            {seccionesVisibles.map((seccion) => (
                <div key={seccion.titulo}>
                    <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest px-4 pt-3 pb-1">
                        {seccion.titulo}
                    </div>
                    {seccion.items.map((item) => {
                        const activo = location.pathname === item.path;
                        return (
                            <a
                                key={item.path}
                                onClick={() => navigate(item.path)}
                                className={`flex items-center gap-2 px-4 py-[9px] text-[13px] cursor-pointer no-underline ${
                                    activo
                                        ? 'bg-blue-50 text-azul font-bold border-l-[3px] border-azul'
                                        : 'text-gray-600 hover:bg-blue-50 hover:text-azul'
                                }`}
                            >
                                <span>{item.icono}</span>
                                <span className="flex-1">{item.label}</span>
                                {item.badge && (
                                    <span className="bg-peligro text-white text-[9px] rounded-full w-[14px] h-[14px] flex items-center justify-center font-bold">
                                        {item.badge}
                                    </span>
                                )}
                            </a>
                        );
                    })}
                </div>
            ))}
        </aside>
    );
}

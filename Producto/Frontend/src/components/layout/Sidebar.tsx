


import { useNavigate, useLocation } from 'react-router-dom';

interface ItemNav {
    icono: string;
    label: string;
    path: string;
    badge?: number;
}

const secciones: { titulo: string; items: ItemNav[] }[] = [
    {
        titulo: 'Gestión',
        items: [
            { icono: '📊', label: 'Dashboard',      path: '/dashboard' },
            { icono: '👥', label: 'Clientes',       path: '/clientes' },
            { icono: '🧑‍💼', label: 'Profesionales', path: '/profesionales' },
            { icono: '📅', label: 'Visitas',        path: '/visitas' },
            { icono: '🎓', label: 'Capacitaciones', path: '/capacitaciones' },
            { icono: '📋', label: 'Asesorías',      path: '/asesorias' },
        ],
    },
    {
        titulo: 'Finanzas',
        items: [
            { icono: '💰', label: 'Pagos',       path: '/pagos' },
            { icono: '⚠️', label: 'Morosidades', path: '/morosidades' },
        ],
    },
    {
        titulo: 'Sistema',
        items: [
            { icono: '📄', label: 'Reportes',      path: '/reportes' },
            { icono: '🔔', label: 'Alertas',       path: '/alertas', badge: 5 },
            { icono: '⚙️', label: 'Configuración', path: '/configuracion' },
        ],
    },
];

export default function Sidebar() {
    const navigate = useNavigate();
    const location = useLocation();

    return (
        <aside className="fixed top-[54px] left-0 bottom-0 w-[190px] bg-white border-r border-gray-200 overflow-y-auto z-40">
            {secciones.map((seccion) => (
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

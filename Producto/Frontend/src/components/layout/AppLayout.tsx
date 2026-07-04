import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Topbar from './TopBar';
import Sidebar from './Sidebar';

export default function AppLayout() {
    const [menuAbierto, setMenuAbierto] = useState(false);
    const location = useLocation();

    useEffect(() => {
        setMenuAbierto(false);
    }, [location.pathname]);

    return (
        <div className="app-shell min-h-screen bg-fondo">
            <Topbar onAbrirMenu={() => setMenuAbierto(true)} />
            <div className="app-layout flex">
                <Sidebar abierto={menuAbierto} onCerrar={() => setMenuAbierto(false)} />
                <main className="app-main flex-1">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

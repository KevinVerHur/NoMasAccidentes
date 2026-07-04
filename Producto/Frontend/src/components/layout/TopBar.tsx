import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface TopbarProps {
    onAbrirMenu: () => void;
}

export default function Topbar({ onAbrirMenu }: TopbarProps) {
    const { email, rol, cerrarSesion } = useAuth();
    const navigate = useNavigate();

    function handleCerrarSesion() {
        cerrarSesion();
        navigate('/login');
    }

    return (
        <header className="app-topbar fixed top-0 left-0 right-0 h-[54px] bg-azul text-white flex items-center justify-between px-5 z-50">
            <div className="topbar-brand-wrap">
                <button
                    type="button"
                    className="mobile-menu-btn"
                    onClick={onAbrirMenu}
                    aria-label="Abrir menu"
                >
                    ☰
                </button>
                <div className="topbar-brand text-lg font-bold flex items-center gap-2 shrink-0">
                    <span aria-hidden="true">🦺</span>
                    <span className="text-dorado">No Más</span>
                    <span>Accidentes</span>
                </div>
            </div>

            <div className="topbar-user flex items-center gap-3 text-[12px] text-gray-300 shrink-0">
                <span className="topbar-email">👤 {email}</span>
                <span className="topbar-separator">|</span>
                <span className="topbar-role text-dorado">{rol}</span>
                <button
                    onClick={handleCerrarSesion}
                    className="topbar-logout bg-dorado text-azul px-3 py-1 rounded text-[12px] font-bold cursor-pointer border-none"
                >
                    Cerrar sesión
                </button>
            </div>
        </header>
    );
}

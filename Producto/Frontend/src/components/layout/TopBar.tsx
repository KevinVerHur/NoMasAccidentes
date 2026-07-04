import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Topbar() {
    const { email, rol, cerrarSesion } = useAuth();
    const navigate = useNavigate();

    function handleCerrarSesion() {
        cerrarSesion();
        navigate('/login');
    }

    return (
        <header className="fixed top-0 left-0 right-0 h-[54px] bg-azul text-white flex items-center justify-between px-5 z-50">
            <div className="text-lg font-bold flex items-center gap-2 shrink-0">
                🦺 <span className="text-dorado">No Más</span>&nbsp;Accidentes
            </div>

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

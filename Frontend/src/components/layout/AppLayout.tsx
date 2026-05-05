import { Outlet } from 'react-router-dom';
import Topbar from './Topbar';
import Sidebar from './Sidebar';

export default function AppLayout() {
    return (
        <div className="min-h-screen bg-fondo">
            <Topbar />
            <div className="flex">
                <Sidebar />
                <main className="ml-[190px] mt-[54px] flex-1 p-5 min-h-[calc(100vh-54px)]">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

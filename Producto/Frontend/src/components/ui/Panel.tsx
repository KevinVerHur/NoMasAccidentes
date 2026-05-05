import type { ReactNode } from 'react';

interface PanelProps {
    titulo?: string;
    accion?: ReactNode;
    children: ReactNode;
    className?: string;
}

export default function Panel({ titulo, accion, children, className = '' }: PanelProps) {
    return (
        <div className={`bg-white rounded-lg shadow-sm overflow-hidden mb-4 ${className}`}>
            {titulo && (
                <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                    <span className="font-bold text-azul text-[13px]">{titulo}</span>
                    {accion}
                </div>
            )}
            {children}
        </div>
    );
}

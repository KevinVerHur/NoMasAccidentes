import type { ReactNode } from 'react';
import { useEffect } from 'react';

interface ModalProps {
    abierto: boolean;
    titulo: string;
    onCerrar: () => void;
    children: ReactNode;
    footer?: ReactNode;
    ancho?: 'sm' | 'md' | 'lg';
}

const anchos = { sm: 420, md: 560, lg: 760 };

export default function Modal({ abierto, titulo, onCerrar, children, footer, ancho = 'md' }: ModalProps) {
    useEffect(() => {
        function onKey(e: KeyboardEvent) { if (e.key === 'Escape') onCerrar(); }
        if (abierto) document.addEventListener('keydown', onKey);
        return () => document.removeEventListener('keydown', onKey);
    }, [abierto, onCerrar]);

    if (!abierto) return null;

    return (
        <div
            style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.45)', zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18 }}
            onClick={(e) => { if (e.target === e.currentTarget) onCerrar(); }}
        >
            <div style={{ background: 'white', borderRadius: 12, boxShadow: '0 12px 40px rgba(0,0,0,.22)', width: '100%', maxWidth: anchos[ancho], maxHeight: '92vh', overflow: 'auto' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #e8edf3', background: '#f8fafc' }}>
                    <span style={{ fontWeight: 'bold', color: '#18395a', fontSize: 15 }}>{titulo}</span>
                    <button onClick={onCerrar} style={{ background: 'none', border: 'none', fontSize: 22, cursor: 'pointer', color: '#8994a3', lineHeight: 1 }}>×</button>
                </div>
                <div style={{ padding: '16px 18px' }}>{children}</div>
                {footer && (
                    <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', padding: '12px 18px', borderTop: '1px solid #e8edf3', background: '#fafbfc' }}>
                        {footer}
                    </div>
                )}
            </div>
        </div>
    );
}

import type { ReactNode } from 'react';
import type { VarianteBadge } from '../../types';

interface BadgeProps {
    variante: VarianteBadge;
    children: ReactNode;
}

const estilos: Record<VarianteBadge, string> = {
    green:  'bg-green-100 text-green-700',
    red:    'bg-red-100 text-red-700',
    yellow: 'bg-yellow-100 text-yellow-700',
    blue:   'bg-blue-100 text-blue-700',
    gray:   'bg-gray-100 text-gray-600',
};

export default function Badge({ variante, children }: BadgeProps) {
    return (
        <span className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-bold ${estilos[variante]}`}>
      {children}
    </span>
    );
}




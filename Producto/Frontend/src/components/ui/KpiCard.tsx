import type { VarianteKpi } from '../../types';

interface KpiCardProps {
    label: string;
    value: string | number;
    sub?: string;
    variante?: VarianteKpi;
    /** Porcentaje 0–100; si se entrega, muestra una barra de progreso bajo el valor. */
    progreso?: number;
}

const estilos: Record<VarianteKpi, { borde: string; texto: string; barra: string }> = {
    default: { borde: 'border-l-azul',    texto: 'text-azul',    barra: 'bg-azul' },
    ok:      { borde: 'border-l-ok',      texto: 'text-ok',      barra: 'bg-ok' },
    warn:    { borde: 'border-l-warn',    texto: 'text-warn',    barra: 'bg-warn' },
    peligro: { borde: 'border-l-peligro', texto: 'text-peligro', barra: 'bg-peligro' },
};

export default function KpiCard({ label, value, sub, variante = 'default', progreso }: KpiCardProps) {
    const { borde, texto, barra } = estilos[variante];
    return (
        <div className={`kpi-card flex-1 min-w-[130px] bg-white rounded-lg px-4 py-3 border-l-4 ${borde} shadow-sm`}>
            <div className="text-[11px] text-gray-400 mb-1">{label}</div>
            <div className={`text-[22px] font-bold ${texto}`}>{value}</div>
            {sub && <div className="text-[10px] text-gray-300 mt-0.5">{sub}</div>}
            {progreso != null && (
                <div className="mt-2 h-[6px] bg-gray-200 rounded-full overflow-hidden">
                    <div className={`h-full rounded-full ${barra}`} style={{ width: `${Math.min(100, Math.max(0, progreso))}%` }} />
                </div>
            )}
        </div>
    );
}

import type { VarianteKpi } from '../../types';

interface KpiCardProps {
    label: string;
    value: string | number;
    sub?: string;
    variante?: VarianteKpi;
}

const estilos: Record<VarianteKpi, { borde: string; texto: string }> = {
    default: { borde: 'border-l-azul',    texto: 'text-azul' },
    ok:      { borde: 'border-l-ok',      texto: 'text-ok' },
    warn:    { borde: 'border-l-warn',    texto: 'text-warn' },
    peligro: { borde: 'border-l-peligro', texto: 'text-peligro' },
};

export default function KpiCard({ label, value, sub, variante = 'default' }: KpiCardProps) {
    const { borde, texto } = estilos[variante];
    return (
        <div className={`flex-1 min-w-[130px] bg-white rounded-lg px-4 py-3 border-l-4 ${borde} shadow-sm`}>
            <div className="text-[11px] text-gray-400 mb-1">{label}</div>
            <div className={`text-[22px] font-bold ${texto}`}>{value}</div>
            {sub && <div className="text-[10px] text-gray-300 mt-0.5">{sub}</div>}
        </div>
    );
}

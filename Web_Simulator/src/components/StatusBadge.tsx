import type { ReactNode } from 'react';

interface StatusBadgeProps {
  tone: 'success' | 'warning' | 'danger' | 'neutral';
  children: ReactNode;
}

const toneClasses: Record<StatusBadgeProps['tone'], string> = {
  success: 'border-emerald-400/40 bg-emerald-500/10 text-emerald-200',
  warning: 'border-amber-400/40 bg-amber-500/10 text-amber-200',
  danger: 'border-rose-400/40 bg-rose-500/10 text-rose-200',
  neutral: 'border-slate-600/80 bg-slate-700/60 text-slate-200',
};

export function StatusBadge({ tone, children }: StatusBadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.18em] ${toneClasses[tone]}`}>
      {children}
    </span>
  );
}

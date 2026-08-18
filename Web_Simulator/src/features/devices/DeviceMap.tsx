import { motion } from 'framer-motion';

import type { Device, FloorId } from '../../types';

interface DeviceMapProps {
  devices: Device[];
  selectedFloor: FloorId;
  rows: number;
  cols: number;
  imageUrl?: string;
  onSelectDevice: (device: Device) => void;
}

const resolveMarkerColor = (device: Device) => {
  if (device.type === 'MULTI_SWITCH' && device.switches?.length) {
    const activeCount = device.switches.filter((switchItem) => switchItem.on).length;
    return activeCount > 0 ? 'bg-emerald-500' : 'bg-slate-600';
  }

  if (device.status === 'ON') {
    return 'bg-emerald-500';
  }

  if (device.status === 'ERROR' || device.status === 'DISCONNECTED') {
    return 'bg-rose-500';
  }

  return 'bg-slate-500';
};

const labelForDevice = (device: Device) => {
  if (device.type === 'MULTI_SWITCH' && device.switches?.length) {
    const active = device.switches.filter((switchItem) => switchItem.on || switchItem.isOn).length;
    return `${active}/${device.switches.length} ON`;
  }

  return device.name;
};

export function DeviceMap({ devices, selectedFloor, rows, cols, imageUrl, onSelectDevice }: DeviceMapProps) {
  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-950/60 p-4 shadow-2xl shadow-slate-950/40">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div>
          <p className="text-[10px] font-medium uppercase tracking-[0.2em] text-slate-400">Floor layout</p>
          <h2 className="mt-1 text-xl font-semibold text-white">{selectedFloor.replace(/[-_]/g, ' ').toUpperCase()}</h2>
        </div>
        <span className="rounded-full border border-slate-700 bg-slate-900 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-300">
          {devices.length} devices
        </span>
      </div>

      <div className="relative h-[420px] overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">
        {imageUrl ? (
          <img src={imageUrl} alt="Floor plan" className="absolute inset-0 h-full w-full object-cover opacity-40" />
        ) : (
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(15,118,110,0.18),_rgba(15,23,42,0)_45%),linear-gradient(180deg,_rgba(15,23,42,0.96),_rgba(2,6,23,0.98))]" />
        )}
        <div className="absolute inset-0 bg-[linear-gradient(rgba(148,163,184,0.08)_1px,transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.08)_1px,transparent_1px)] bg-[size:28px_28px]" />

        {devices.map((device) => (
          <motion.button
            type="button"
            key={device.id}
            whileHover={{ scale: 1.04 }}
            whileTap={{ scale: 0.96 }}
            onClick={() => onSelectDevice(device)}
            className="absolute -translate-x-1/2 -translate-y-1/2"
            style={{
              left: `${(device.position.x / cols) * 100}%`,
              top: `${(device.position.y / rows) * 100}%`,
            }}
          >
            <span className={`flex h-6 w-6 items-center justify-center rounded-full border-2 border-slate-950 shadow-lg shadow-black/40 ${resolveMarkerColor(device)}`}>
              <span className="sr-only">{device.name}</span>
            </span>
            <span className="mt-2 block w-max max-w-[8rem] rounded-full border border-slate-700 bg-slate-950/80 px-2 py-1 text-[10px] font-medium text-slate-200">
              {labelForDevice(device)}
            </span>
          </motion.button>
        ))}
      </div>
    </div>
  );
}

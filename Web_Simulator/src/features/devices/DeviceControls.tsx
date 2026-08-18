import { motion } from 'framer-motion';

import { StatusBadge } from '../../components/StatusBadge';
import type { Device, DeviceStatus, DeviceSwitch } from '../../types';

interface DeviceControlsProps {
  devices: Device[];
  selectedDeviceId: string | null;
  onSelectDevice: (device: Device) => void;
  onToggleDevice: (device: Device, nextStatus: DeviceStatus) => void;
  onToggleSwitch: (device: Device, switchId: number) => void;
}

const formatDuration = (minutes: number | null) => {
  if (!minutes) {
    return 'No limit';
  }

  return `${minutes} mins`;
};

const hazardRuntimeMinutes = (device: Device) => {
  if (!device.turnedOnAt) {
    return 0;
  }

  return Math.max(0, Math.floor((Date.now() - device.turnedOnAt) / 60000));
};

const statusTone = (status: Device['status']) => {
  if (status === 'ON') {
    return 'success';
  }

  if (status === 'ERROR' || status === 'DISCONNECTED') {
    return 'danger';
  }

  if (status === 'OFF') {
    return 'neutral';
  }

  return 'warning';
};

export function DeviceControls({ devices, selectedDeviceId, onSelectDevice, onToggleDevice, onToggleSwitch }: DeviceControlsProps) {
  return (
    <div className="space-y-4">
      {devices.map((device) => {
        const isSelected = selectedDeviceId === device.id;
        const runtimeMinutes = hazardRuntimeMinutes(device);
        const exceeded = Boolean(device.maxOnDurationMins && runtimeMinutes > device.maxOnDurationMins);

        return (
          <motion.article
            key={device.id}
            layout
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className={`rounded-2xl border p-4 shadow-lg ${
              isSelected ? 'border-cyan-500/60 bg-slate-900/90' : 'border-slate-800 bg-slate-900/70'
            }`}
          >
            <button type="button" className="w-full text-left" onClick={() => onSelectDevice(device)}>
              <div className="mb-3 flex items-center justify-between gap-3">
                <div>
                  <p className="text-[10px] uppercase tracking-[0.24em] text-slate-400">{device.type}</p>
                  <h3 className="mt-1 text-lg font-semibold text-white">{device.name}</h3>
                </div>
                <StatusBadge tone={statusTone(device.status)}>{device.status}</StatusBadge>
              </div>
            </button>

            {device.type === 'OUTLET' && (
              <div className="flex items-center justify-between rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                <span className="text-sm text-slate-300">Power</span>
                <button
                  type="button"
                  onClick={() => onToggleDevice(device, device.status === 'ON' ? 'OFF' : 'ON')}
                  className={`relative inline-flex h-7 w-12 items-center rounded-full transition ${
                    device.status === 'ON' ? 'bg-emerald-500' : 'bg-slate-600'
                  }`}
                >
                  <span
                    className={`inline-block h-5 w-5 rounded-full bg-white transition ${
                      device.status === 'ON' ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
            )}

            {device.type === 'MULTI_SWITCH' && (
              <div className="space-y-2">
                {device.switches?.map((switchItem: DeviceSwitch) => (
                  <div key={switchItem.id} className="flex items-center justify-between rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                    <span className="text-sm text-slate-200">{switchItem.label}</span>
                    <button
                      type="button"
                      onClick={() => onToggleSwitch(device, switchItem.id)}
                      className={`relative inline-flex h-7 w-12 items-center rounded-full transition ${
                        switchItem.on ? 'bg-emerald-500' : 'bg-slate-600'
                      }`}
                    >
                      <span
                        className={`inline-block h-5 w-5 rounded-full bg-white transition ${switchItem.on ? 'translate-x-6' : 'translate-x-1'}`}
                      />
                    </button>
                  </div>
                ))}
              </div>
            )}

            {device.type === 'SCHEDULED_HAZARD' && (
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                    <div className="text-slate-400">Max duration</div>
                    <div className="mt-1 font-semibold text-white">{formatDuration(device.maxOnDurationMins)}</div>
                  </div>
                  <div className="rounded-xl border border-slate-800 bg-slate-950/60 p-3">
                    <div className="text-slate-400">Runtime</div>
                    <div className="mt-1 font-semibold text-white">{runtimeMinutes} mins</div>
                  </div>
                </div>

                {exceeded && (
                  <StatusBadge tone="warning">Timeout warning</StatusBadge>
                )}

                <button
                  type="button"
                  onClick={() => onToggleDevice(device, device.status === 'ON' ? 'OFF' : 'ON')}
                  className={`w-full rounded-xl px-4 py-3 font-medium text-white transition ${
                    device.status === 'ON' ? 'bg-rose-500 hover:bg-rose-400' : 'bg-emerald-500 hover:bg-emerald-400'
                  }`}
                >
                  {device.status === 'ON' ? 'Turn off hazard' : 'Turn on hazard'}
                </button>
              </div>
            )}

            {device.type === 'CAMERA' && (
              <div className="space-y-3">
                <div className="overflow-hidden rounded-xl border border-slate-800 bg-slate-950/60">
                  {device.status === 'ON' ? (
                    device.cameraUrl ? (
                      <img src={device.cameraUrl} alt={device.name} className="h-40 w-full object-cover" />
                    ) : (
                      <div className="flex h-40 items-center justify-center bg-slate-800 text-sm text-slate-400">
                        Camera unavailable
                      </div>
                    )
                  ) : (
                    <div className="flex h-40 flex-col items-center justify-center bg-slate-900/90 text-sm">
                      <div className="mb-2 text-slate-500">
                        <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                        </svg>
                      </div>
                      <span className="font-medium text-slate-400">Stream Disabled</span>
                      <span className="mt-1 text-[10px] text-slate-600">Privacy Mode Active</span>
                    </div>
                  )}
                </div>

                <button
                  type="button"
                  onClick={() => onToggleDevice(device, device.status === 'ON' ? 'OFF' : 'ON')}
                  className={`w-full rounded-xl px-4 py-3 font-medium text-white transition ${
                    device.status === 'ON' ? 'bg-slate-700 hover:bg-slate-600' : 'bg-emerald-500 hover:bg-emerald-400'
                  }`}
                >
                  {device.status === 'ON' ? 'Disable stream' : 'Enable stream'}
                </button>
              </div>
            )}
          </motion.article>
        );
      })}
    </div>
  );
}

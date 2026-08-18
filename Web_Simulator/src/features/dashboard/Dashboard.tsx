import { motion } from 'framer-motion';

import { StatusBadge } from '../../components/StatusBadge';
import { DeviceControls } from '../devices/DeviceControls';
import { DeviceMap } from '../devices/DeviceMap';
import { TelemetryPanel } from '../telemetry/TelemetryPanel';
import { type Device, type DeviceStatus, type FloorId, type TelemetryEvent, type Floor } from '../../types';

interface DashboardProps {
  devices: Device[];
  allDevices: Device[];
  telemetry: TelemetryEvent[];
  floors: Floor[];
  selectedFloor: FloorId;
  selectedDeviceId: string | null;
  connectionLive: boolean;
  eventTypeFilter: string;
  floorFilter: FloorId | 'ALL';
  deviceTypeFilter: string;
  onFloorChange: (value: FloorId) => void;
  onSelectDevice: (device: Device) => void;
  onToggleDevice: (device: Device, nextStatus: DeviceStatus) => void;
  onToggleSwitch: (device: Device, switchId: number) => void;
  onEventTypeChange: (value: string) => void;
  onTelemetryFloorChange: (value: FloorId | 'ALL') => void;
  onDeviceTypeChange: (value: string) => void;
}

export function Dashboard({
  devices,
  allDevices,
  telemetry,
  floors,
  selectedFloor,
  selectedDeviceId,
  connectionLive,
  eventTypeFilter,
  floorFilter,
  deviceTypeFilter,
  onFloorChange,
  onSelectDevice,
  onToggleDevice,
  onToggleSwitch,
  onEventTypeChange,
  onTelemetryFloorChange,
  onDeviceTypeChange,
}: DashboardProps) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-50">
      <div className="mx-auto max-w-7xl px-4 py-5 sm:px-6 lg:px-8">
        <header className="rounded-2xl border border-slate-800 bg-slate-900/80 px-5 py-4 shadow-xl shadow-slate-950/50">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-[10px] font-medium uppercase tracking-[0.28em] text-cyan-300">Smart home</p>
              <h1 className="mt-2 text-2xl font-bold tracking-tight text-white sm:text-3xl">Companion Hardware Simulator</h1>
            </div>
            <StatusBadge tone={connectionLive ? 'success' : 'warning'}>{connectionLive ? 'LIVE' : 'RECONNECTING'}</StatusBadge>
          </div>
        </header>

        <div className="mt-5 flex flex-wrap gap-2">
          {floors.map((floor) => (
            <button
              key={floor.id}
              type="button"
              onClick={() => onFloorChange(floor.id)}
              className={`rounded-full border px-4 py-2 text-sm font-medium transition ${
                selectedFloor === floor.id
                  ? 'border-cyan-500 bg-cyan-500/10 text-cyan-200'
                  : 'border-slate-700 bg-slate-900 text-slate-300 hover:border-slate-500'
              }`}
            >
              {floor.title}
            </button>
          ))}
        </div>

        <motion.main layout className="mt-6 grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
          <div className="space-y-6">
            {(() => {
              const currentFloor = floors.find(f => f.id === selectedFloor);
              return (
                <DeviceMap
                  devices={devices}
                  selectedFloor={selectedFloor}
                  rows={currentFloor?.rows ?? 8}
                  cols={currentFloor?.cols ?? 10}
                  imageUrl={currentFloor?.imageUrl}
                  onSelectDevice={onSelectDevice}
                />
              );
            })()}
          </div>

          <div className="space-y-6">
            <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-4 shadow-xl shadow-slate-950/40">
              <div className="mb-4 flex items-center justify-between gap-3">
                <div>
                  <p className="text-[10px] font-medium uppercase tracking-[0.2em] text-slate-400">Device controls</p>
                  <h2 className="mt-1 text-xl font-semibold text-white">Hardware</h2>
                </div>
                <span className="rounded-full border border-slate-700 bg-slate-950 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-300">
                  {devices.length} active
                </span>
              </div>

              <DeviceControls
                devices={devices}
                selectedDeviceId={selectedDeviceId}
                onSelectDevice={onSelectDevice}
                onToggleDevice={onToggleDevice}
                onToggleSwitch={onToggleSwitch}
              />
            </div>
          </div>
        </motion.main>

        <div className="mt-6">
          <TelemetryPanel
            telemetry={telemetry}
            devices={allDevices}
            floors={floors}
            eventTypeFilter={eventTypeFilter}
            floorFilter={floorFilter}
            deviceTypeFilter={deviceTypeFilter}
            onEventTypeChange={onEventTypeChange}
            onFloorChange={onTelemetryFloorChange}
            onDeviceTypeChange={onDeviceTypeChange}
          />
        </div>
      </div>
    </div>
  );
}

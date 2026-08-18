import type { Device, FloorId, TelemetryEvent, Floor } from '../../types';

interface TelemetryPanelProps {
  telemetry: TelemetryEvent[];
  devices: Device[];
  floors: Floor[];
  eventTypeFilter: string;
  floorFilter: FloorId | 'ALL';
  deviceTypeFilter: string;
  onEventTypeChange: (value: string) => void;
  onFloorChange: (value: FloorId | 'ALL') => void;
  onDeviceTypeChange: (value: string) => void;
}

const formatTime = (timestamp: number) => new Date(timestamp).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });

export function TelemetryPanel({ telemetry, devices, floors, eventTypeFilter, floorFilter, deviceTypeFilter, onEventTypeChange, onFloorChange, onDeviceTypeChange }: TelemetryPanelProps) {
  const eventTypes = Array.from(new Set(telemetry.map((item) => item.eventType))); 
  const floorOptions: Array<{ id: FloorId | 'ALL', title: string }> = [
    { id: 'ALL', title: 'All floors' },
    ...floors.map(f => ({ id: f.id, title: f.title }))
  ];
  const deviceTypes = Array.from(new Set(devices.map((device) => device.type))); 

  const devicesById = new Map(devices.map((device) => [device.id, device]));
  const filteredTelemetry = telemetry.filter((event) => {
    const matchesEventType = eventTypeFilter === 'ALL' || event.eventType === eventTypeFilter;
    const device = devicesById.get(event.deviceId);
    const matchesFloor = floorFilter === 'ALL' || device?.floorId === floorFilter;
    const matchesDeviceType = deviceTypeFilter === 'ALL' || device?.type === deviceTypeFilter;

    return matchesEventType && matchesFloor && matchesDeviceType;
  });

  const totalEvents = telemetry.length;
  const autoCutoffs = telemetry.filter((event) => event.eventType === 'AUTO_CUTOFF').length;
  const activeHazards = devices.filter((device) => device.type === 'SCHEDULED_HAZARD' && device.status === 'ON').length;

  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-950/60 p-4 shadow-2xl shadow-slate-950/40">
      <div className="mb-4 flex items-center justify-between gap-4">
        <div>
          <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Telemetry</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Live activity</h2>
        </div>
      </div>

      <div className="mb-4 grid gap-3 sm:grid-cols-3">
        <div className="rounded-xl border border-slate-800 bg-slate-900/80 p-3">
          <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Total events</p>
          <p className="mt-2 text-2xl font-bold text-white">{totalEvents}</p>
        </div>
        <div className="rounded-xl border border-slate-800 bg-slate-900/80 p-3">
          <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Auto cutoffs</p>
          <p className="mt-2 text-2xl font-bold text-white">{autoCutoffs}</p>
        </div>
        <div className="rounded-xl border border-slate-800 bg-slate-900/80 p-3">
          <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Active hazards</p>
          <p className="mt-2 text-2xl font-bold text-white">{activeHazards}</p>
        </div>
      </div>

      <div className="mb-4 grid gap-3 sm:grid-cols-3">
        <label className="text-sm text-slate-300">
          <span className="mb-1 block text-xs uppercase tracking-[0.18em] text-slate-500">Event type</span>
          <select value={eventTypeFilter} onChange={(event) => onEventTypeChange(event.target.value)} className="w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none ring-0">
            <option value="ALL">All</option>
            {eventTypes.map((eventType) => (
              <option key={eventType} value={eventType}>{eventType}</option>
            ))}
          </select>
        </label>

        <label className="text-sm text-slate-300">
          <span className="mb-1 block text-xs uppercase tracking-[0.18em] text-slate-500">Floor</span>
          <select value={floorFilter} onChange={(event) => onFloorChange(event.target.value as FloorId | 'ALL')} className="w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none ring-0">
            {floorOptions.map((option) => (
              <option key={option.id} value={option.id}>{option.title}</option>
            ))}
          </select>
        </label>

        <label className="text-sm text-slate-300">
          <span className="mb-1 block text-xs uppercase tracking-[0.18em] text-slate-500">Device type</span>
          <select value={deviceTypeFilter} onChange={(event) => onDeviceTypeChange(event.target.value)} className="w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none ring-0">
            <option value="ALL">All</option>
            {deviceTypes.map((deviceType) => (
              <option key={deviceType} value={deviceType}>{deviceType}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="max-h-[420px] space-y-3 overflow-auto pr-1">
        {filteredTelemetry.length === 0 && (
          <div className="rounded-xl border border-dashed border-slate-700 bg-slate-900/40 p-8 text-center text-sm text-slate-300">
            No telemetry matches the active filters.
          </div>
        )}

        {filteredTelemetry.map((event) => {
          const device = devicesById.get(event.deviceId);

          return (
            <div key={event.id} className="rounded-xl border border-slate-800 bg-slate-900/80 p-3">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-xs uppercase tracking-[0.18em] text-cyan-300">{event.eventType}</p>
                  <p className="mt-1 text-sm font-medium text-white">{device?.name ?? event.deviceId}</p>
                </div>
                <span className="text-[10px] uppercase tracking-[0.16em] text-slate-400">{formatTime(event.timestamp)}</span>
              </div>
              <p className="mt-2 text-sm text-slate-300">{event.message}</p>
            </div>
          );
        })}
      </div>
    </div>
  );
}

import { doc, setDoc, updateDoc } from 'firebase/firestore';
import { useEffect, useMemo, useState } from 'react';

import { Dashboard } from './features/dashboard/Dashboard';
import { useDevices } from './hooks/useDevices';
import { useFloors } from './hooks/useFloors';
import { useTelemetry } from './hooks/useTelemetry';
import { db, firebaseEnabled } from './lib/firebase';
import type { Device, DeviceStatus, FloorId, Floor } from './types';

function App() {
  const [selectedFloor, setSelectedFloor] = useState<FloorId>('');
  const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null);
  const [eventTypeFilter, setEventTypeFilter] = useState<string>('ALL');
  const [floorFilter, setFloorFilter] = useState<FloorId | 'ALL'>('ALL');
  const [deviceTypeFilter, setDeviceTypeFilter] = useState<string>('ALL');

  const { floors, loading: floorsLoading, error: floorsError } = useFloors();
  const { devices: allDevices, loading: allDevicesLoading, error: allDevicesError } = useDevices('ALL');
  const { devices, loading: devicesLoading, error: deviceError } = useDevices(selectedFloor);
  const { telemetry, loading: telemetryLoading, error: telemetryError } = useTelemetry();

  useEffect(() => {
    if (floors.length > 0 && !selectedFloor) {
      setSelectedFloor(floors[0].id);
    }
  }, [floors, selectedFloor]);

  useEffect(() => {
    if (!devices.length) {
      return;
    }

    if (!selectedDeviceId || !devices.some((device) => device.id === selectedDeviceId)) {
      setSelectedDeviceId(devices[0].id);
    }
  }, [devices, selectedDeviceId]);

  const selectedDevice = useMemo(
    () => devices.find((device) => device.id === selectedDeviceId) ?? null,
    [devices, selectedDeviceId],
  );

  const handleRecordTelemetry = async (device: Device, eventType: string, message: string) => {
    if (!db) {
      return;
    }

    const timestamp = Date.now();
    const customId = `${device.id}_${eventType}_${timestamp}`;

    await setDoc(doc(db, 'telemetry', customId), {
      id: customId,
      deviceId: device.id,
      eventType,
      timestamp,
      message,
    });
  };

  const handleToggleDevice = async (device: Device, nextStatus: DeviceStatus) => {
    if (!db) {
      return;
    }

    const updatePayload: Partial<Device> = {
      status: nextStatus,
    };

    if (device.type === 'SCHEDULED_HAZARD') {
      updatePayload.turnedOnAt = nextStatus === 'ON' ? Date.now() : null;
    }

    await updateDoc(doc(db, 'devices', device.id), updatePayload);

    const actionLabel = nextStatus === 'ON' ? (device.type === 'CAMERA' ? 'Stream Enabled' : 'ON') : (device.type === 'CAMERA' ? 'Stream Disabled' : 'OFF');

    await handleRecordTelemetry(
      device,
      'MANUAL_TOGGLE',
      `${device.name} ${actionLabel} from the web simulator`,
    );
  };

  const handleToggleSwitch = async (device: Device, switchId: number) => {
    if (!db || !device.switches) {
      return;
    }

    const nextSwitches = device.switches.map((switchItem) => {
      if (switchItem.id === switchId) {
        const nextOn = !switchItem.on;
        return { ...switchItem, on: nextOn, isOn: nextOn };
      }
      return switchItem;
    });
    const activeCount = nextSwitches.filter((switchItem) => switchItem.on).length;
    const nextStatus: DeviceStatus = activeCount > 0 ? 'ON' : 'OFF';

    await updateDoc(doc(db, 'devices', device.id), {
      switches: nextSwitches,
      status: nextStatus,
    });

    const toggledSwitch = nextSwitches.find((switchItem) => switchItem.id === switchId);
    await handleRecordTelemetry(
      device,
      'SWITCH_TOGGLE',
      `${toggledSwitch?.label ?? 'Switch'} toggled ${toggledSwitch?.on ? 'ON' : 'OFF'} in ${device.name}`,
    );
  };

  const statusMessage =
    allDevicesError || deviceError || telemetryError || floorsError
      ? allDevicesError ?? deviceError ?? telemetryError ?? floorsError
      : allDevicesLoading || devicesLoading || telemetryLoading || floorsLoading
        ? 'Syncing with Firestore…'
        : 'Realtime dashboard active';

  return (
    <>
      <Dashboard
        devices={devices}
        allDevices={allDevices}
        telemetry={telemetry}
        floors={floors}
        selectedFloor={selectedFloor}
        selectedDeviceId={selectedDeviceId}
        connectionLive={firebaseEnabled && !allDevicesError && !deviceError && !telemetryError && !floorsError}
        eventTypeFilter={eventTypeFilter}
        floorFilter={floorFilter}
        deviceTypeFilter={deviceTypeFilter}
        onFloorChange={setSelectedFloor}
        onSelectDevice={(device) => setSelectedDeviceId(device.id)}
        onToggleDevice={handleToggleDevice}
        onToggleSwitch={handleToggleSwitch}
        onEventTypeChange={setEventTypeFilter}
        onTelemetryFloorChange={setFloorFilter}
        onDeviceTypeChange={setDeviceTypeFilter}
      />

      <div className="border-t border-slate-800 bg-slate-950/90 px-4 py-3 text-center text-xs text-slate-400">
        {statusMessage}
        {selectedDevice && <span className="ml-2 text-slate-500">Selected: {selectedDevice.name}</span>}
      </div>
    </>
  );
}

export default App;

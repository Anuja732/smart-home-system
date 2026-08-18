export type FloorId = string;
export type DeviceType = 'OUTLET' | 'MULTI_SWITCH' | 'SCHEDULED_HAZARD' | 'CAMERA';
export type DeviceStatus = 'ON' | 'OFF' | 'ERROR' | 'DISCONNECTED';

export interface Floor {
  id: string;
  title: string;
  description: string;
  rows: number;
  cols: number;
  imageUrl?: string;
  createdAt: number;
}

export interface DeviceSwitch {
  id: number;
  label: string;
  on: boolean;
  isOn?: boolean;
}

export interface Position {
  x: number;
  y: number;
}

export interface Device {
  id: string;
  floorId: FloorId;
  name: string;
  type: DeviceType;
  status: DeviceStatus;
  position: Position;
  switches: DeviceSwitch[] | null;
  maxOnDurationMins: number | null;
  turnedOnAt: number | null;
  cameraUrl: string | null;
}

export interface TelemetryEvent {
  id: string;
  deviceId: string;
  eventType: string;
  timestamp: number;
  message: string;
}

export const FLOOR_OPTIONS: FloorId[] = ['floor_1', 'floor_2', 'floor_3'];

export const DEVICE_TYPE_LABELS: Record<DeviceType, string> = {
  OUTLET: 'Outlet',
  MULTI_SWITCH: 'Multi-switch',
  SCHEDULED_HAZARD: 'Scheduled hazard',
  CAMERA: 'Camera',
};

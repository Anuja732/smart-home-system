import { collection, onSnapshot, query } from 'firebase/firestore';
import { useEffect, useState } from 'react';

import { db } from '../lib/firebase';
import { getMockDevicesForFloor } from '../lib/mockData';
import type { Device, FloorId } from '../types';

export const useDevices = (floorId: FloorId | 'ALL') => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!db) {
      const mockDevices = floorId === 'ALL' ? getMockDevicesForFloor('floor_1').concat(getMockDevicesForFloor('floor_2'), getMockDevicesForFloor('floor_3')) : getMockDevicesForFloor(floorId);
      setDevices(mockDevices);
      setLoading(false);
      setError('Firebase is not configured. Showing mock device data.');
      return undefined;
    }

    const deviceQuery = query(collection(db, 'devices'));

    const unsubscribe = onSnapshot(
      deviceQuery,
      (snapshot) => {
        const nextDevices = snapshot.docs
          .map((doc) => ({ id: doc.id, ...(doc.data() as Omit<Device, 'id'>) }))
          .filter((device) => floorId === 'ALL' || device.floorId === floorId)
          .sort((left, right) => left.name.localeCompare(right.name));

        setDevices(nextDevices);
        setLoading(false);
        setError(null);
      },
      (snapshotError) => {
        setError(snapshotError.message);
        setLoading(false);
      },
    );

    return () => unsubscribe();
  }, [floorId]);

  return { devices, loading, error };
};

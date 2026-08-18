import { collection, onSnapshot, orderBy, query } from 'firebase/firestore';
import { useEffect, useState } from 'react';

import { db } from '../lib/firebase';
import { mockTelemetry } from '../lib/mockData';
import type { TelemetryEvent } from '../types';

export const useTelemetry = () => {
  const [telemetry, setTelemetry] = useState<TelemetryEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!db) {
      setTelemetry(mockTelemetry.slice().sort((left, right) => right.timestamp - left.timestamp));
      setLoading(false);
      setError('Firebase is not configured. Showing mock telemetry.');
      return undefined;
    }

    const telemetryQuery = query(collection(db, 'telemetry'), orderBy('timestamp', 'desc'));

    const unsubscribe = onSnapshot(
      telemetryQuery,
      (snapshot) => {
        const nextTelemetry = snapshot.docs
          .map((doc) => ({ id: doc.id, ...(doc.data() as Omit<TelemetryEvent, 'id'>) }))
          .sort((left, right) => right.timestamp - left.timestamp);

        setTelemetry(nextTelemetry);
        setLoading(false);
        setError(null);
      },
      (snapshotError) => {
        setError(snapshotError.message);
        setLoading(false);
      },
    );

    return () => unsubscribe();
  }, []);

  return { telemetry, loading, error };
};

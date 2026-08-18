import { collection, onSnapshot, query, orderBy } from 'firebase/firestore';
import { useEffect, useState } from 'react';

import { db } from '../lib/firebase';
import type { Floor } from '../types';

export const useFloors = () => {
  const [floors, setFloors] = useState<Floor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!db) {
      setLoading(false);
      setError('Firebase is not configured.');
      return undefined;
    }

    const floorsQuery = query(collection(db, 'floors'), orderBy('createdAt', 'asc'));

    const unsubscribe = onSnapshot(
      floorsQuery,
      (snapshot) => {
        const nextFloors = snapshot.docs.map((doc) => ({
          id: doc.id,
          ...(doc.data() as Omit<Floor, 'id'>),
        }));
        setFloors(nextFloors);
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

  return { floors, loading, error };
};

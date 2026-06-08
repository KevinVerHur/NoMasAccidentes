import { useCallback, useEffect, useRef, useState } from 'react';
import { registrarMiUbicacion } from '../api/ubicaciones';

interface UbicacionActual {
  latitud: number;
  longitud: number;
  precision: number;
  fecha: Date;
}

export function useSeguimientoUbicacion() {
  const [ubicacion, setUbicacion] = useState<UbicacionActual | null>(null);
  const [siguiendo, setSiguiendo] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const watchIdRef = useRef<number | null>(null);
  const ultimoEnvioRef = useRef(0);

  const enviarUbicacion = useCallback(async (latitud: number, longitud: number) => {
    const ahora = Date.now();

    if (ahora - ultimoEnvioRef.current < 10000) return;

    ultimoEnvioRef.current = ahora;
    await registrarMiUbicacion({ latitud, longitud });
  }, []);

  const iniciar = useCallback(() => {
    setError(null);

    if (!navigator.geolocation) {
      setError('Este navegador no soporta geolocalización.');
      return;
    }

    if (watchIdRef.current !== null) return;

    const id = navigator.geolocation.watchPosition(
      async (position) => {
        const latitud = position.coords.latitude;
        const longitud = position.coords.longitude;

        setUbicacion({
          latitud,
          longitud,
          precision: position.coords.accuracy,
          fecha: new Date(),
        });

        try {
          await enviarUbicacion(latitud, longitud);
        } catch {
          setError('No se pudo enviar la ubicación al servidor.');
        }
      },
      () => {
        setError('No se pudo obtener la ubicación. Revisa los permisos del navegador.');
        setSiguiendo(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 5000,
      }
    );

    watchIdRef.current = id;
    setSiguiendo(true);
  }, [enviarUbicacion]);

  const detener = useCallback(() => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }

    setSiguiendo(false);
  }, []);

  useEffect(() => detener, [detener]);

  return {
    ubicacion,
    siguiendo,
    error,
    iniciar,
    detener,
  };
}
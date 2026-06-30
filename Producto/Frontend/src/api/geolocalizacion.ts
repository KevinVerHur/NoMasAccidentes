// Producto/Frontend/src/api/geolocalizacion.ts
export function obtenerUbicacionActual(): Promise<{ latitud: number; longitud: number }> {
  return new Promise((resolve, reject) => {
    if (!window.isSecureContext && location.hostname !== 'localhost') {
      reject(new Error('La geolocalizacion requiere HTTPS. Estara disponible tras el despliegue seguro.'));
      return;
    }

    if (!navigator.geolocation) {
      reject(new Error('El navegador no soporta geolocalizacion.'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitud: position.coords.latitude,
          longitud: position.coords.longitude,
        });
      },
      reject,
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000,
      }
    );
  });
}

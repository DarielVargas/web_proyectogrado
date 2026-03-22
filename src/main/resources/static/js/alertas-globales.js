(function () {
  const DURACION_MS = 10000;
  const POLL_MS = 12000;
  const STORAGE_KEY_ESTADOS = 'alertas-globales-estados-estacion';
  const vistos = new Set();

  function ensureContainer() {
    let container = document.getElementById('alertas-globales-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'alertas-globales-container';
      container.className = 'alertas-globales-container';
      document.body.appendChild(container);
    }
    return container;
  }

  function marcarAtendida(alertaId, medicionId) {
    if (!alertaId || !medicionId) return;

    const params = new URLSearchParams({
      alertaId: String(alertaId),
      medicionId: String(medicionId)
    });

    fetch(`/api/alertas/notificaciones/atender?${params.toString()}`)
      .catch(() => {
        // Si falla el marcado, en el siguiente ciclo se reintentará desde el backend.
      });
  }

  function mostrarToast({ mensaje, idUnico, variante = 'alerta' }) {
    if (!mensaje || !idUnico || vistos.has(idUnico)) {
      return;
    }
    vistos.add(idUnico);

    const container = ensureContainer();
    const toast = document.createElement('div');
    toast.className = 'alerta-global-toast';
    if (variante === 'estado-estacion') {
      toast.classList.add('estado-estacion');
    }
    toast.textContent = mensaje;
    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('out');
      setTimeout(() => toast.remove(), 300);
    }, DURACION_MS);
  }

  function obtenerEstadosGuardados() {
    try {
      const raw = window.sessionStorage.getItem(STORAGE_KEY_ESTADOS);
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      return parsed && typeof parsed === 'object' ? parsed : null;
    } catch (_) {
      return null;
    }
  }

  function guardarEstados(estados) {
    try {
      window.sessionStorage.setItem(STORAGE_KEY_ESTADOS, JSON.stringify(estados));
    } catch (_) {
      // Ignorar si el storage no está disponible.
    }
  }

  function construirMensajeEstado(codigo, activa) {
    return activa
      ? `La estación ${codigo} pasó a estado operativa`
      : `La estación ${codigo} pasó a estado inactiva`;
  }

  async function cargarAlertas() {
    try {
      const res = await fetch('/api/alertas/notificaciones', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      if (!Array.isArray(data)) return;

      data.forEach((noti) => {
        const idUnico = `alerta-${noti.alertaId}-${noti.medicionId}`;
        mostrarToast({ mensaje: noti.mensaje, idUnico });
        marcarAtendida(noti.alertaId, noti.medicionId);
      });
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  async function cargarEstadosEstacion() {
    try {
      const res = await fetch('/api/estaciones/estados', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      if (!Array.isArray(data)) return;

      const estadosActuales = data.reduce((acc, item) => {
        if (!item || !item.estacionCodigo) return acc;
        acc[item.estacionCodigo] = Boolean(item.activa);
        return acc;
      }, {});

      const estadosPrevios = obtenerEstadosGuardados();
      if (estadosPrevios) {
        Object.entries(estadosActuales).forEach(([codigo, activa]) => {
          if (!(codigo in estadosPrevios) || estadosPrevios[codigo] === activa) {
            return;
          }

          mostrarToast({
            mensaje: construirMensajeEstado(codigo, activa),
            idUnico: `estado-estacion-${codigo}-${activa}`,
            variante: 'estado-estacion'
          });
        });
      }

      guardarEstados(estadosActuales);
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    ensureContainer();
    cargarAlertas();
    cargarEstadosEstacion();
    setInterval(cargarAlertas, POLL_MS);
    setInterval(cargarEstadosEstacion, POLL_MS);
  });
})();

(function () {
  const DURACION_MS = 10000;
  const POLL_MS = 12000;
  const STORAGE_KEY_ESTADOS = 'agro.estaciones.estados';
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

  function mostrarToast(mensaje, idUnico, onClose) {
    if (!mensaje || !idUnico || vistos.has(idUnico)) {
      return;
    }
    vistos.add(idUnico);

    const container = ensureContainer();
    const toast = document.createElement('div');
    toast.className = 'alerta-global-toast';
    toast.textContent = mensaje;
    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('out');
      if (typeof onClose === 'function') {
        onClose();
      }
      setTimeout(() => toast.remove(), 300);
    }, DURACION_MS);
  }

  function mostrarToastAlerta(mensaje, alertaId, medicionId) {
    const idUnico = `alerta-${alertaId}-${medicionId}`;
    mostrarToast(mensaje, idUnico, () => marcarAtendida(alertaId, medicionId));
  }

  function cargarEstadosGuardados() {
    try {
      const raw = window.localStorage.getItem(STORAGE_KEY_ESTADOS);
      if (!raw) return {};
      const parsed = JSON.parse(raw);
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch (_) {
      return {};
    }
  }

  function guardarEstados(estados) {
    try {
      window.localStorage.setItem(STORAGE_KEY_ESTADOS, JSON.stringify(estados));
    } catch (_) {
      // Si el navegador bloquea storage, la UI sigue funcionando sin persistencia.
    }
  }

  function procesarEstadosEstaciones(data) {
    if (!Array.isArray(data)) return;

    const previos = cargarEstadosGuardados();
    const actuales = {};

    data.forEach((estacion) => {
      if (!estacion || !estacion.estacionCodigo) return;

      const codigo = estacion.estacionCodigo;
      const activa = Boolean(estacion.activa);
      actuales[codigo] = activa;

      if (previos[codigo] === true && !activa) {
        mostrarToast(
          `La estación ${codigo} pasó a estado inactiva`,
          `estacion-inactiva-${codigo}-${Date.now()}`
        );
      }
    });

    guardarEstados(actuales);
  }

  async function cargarAlertas() {
    try {
      const res = await fetch('/api/alertas/notificaciones', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      if (!Array.isArray(data)) return;

      data.forEach((noti) => {
        mostrarToastAlerta(noti.mensaje, noti.alertaId, noti.medicionId);
      });
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  async function cargarEstadosEstaciones() {
    try {
      const res = await fetch('/api/estaciones/estados', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      procesarEstadosEstaciones(data);
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    ensureContainer();
    cargarAlertas();
    cargarEstadosEstaciones();
    setInterval(cargarAlertas, POLL_MS);
    setInterval(cargarEstadosEstaciones, POLL_MS);
  });
})();

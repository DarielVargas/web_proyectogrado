(function () {
  const DURACION_MS = 10000;
  const POLL_MS = 12000;
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

  function mostrarToast(mensaje, alertaId, medicionId) {
    const idUnico = `${alertaId}-${medicionId}`;
    if (!mensaje || vistos.has(idUnico)) {
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
      marcarAtendida(alertaId, medicionId);
      setTimeout(() => toast.remove(), 300);
    }, DURACION_MS);
  }

  async function cargarAlertas() {
    try {
      const res = await fetch('/api/alertas/notificaciones', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      if (!Array.isArray(data)) return;

      data.forEach((noti) => {
        mostrarToast(noti.mensaje, noti.alertaId, noti.medicionId);
      });
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    ensureContainer();
    cargarAlertas();
    setInterval(cargarAlertas, POLL_MS);
  });
})();

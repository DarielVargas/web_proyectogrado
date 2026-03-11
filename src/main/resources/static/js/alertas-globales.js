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

  function mostrarToast(mensaje, id) {
    if (!mensaje || vistos.has(id)) {
      return;
    }
    vistos.add(id);

    const container = ensureContainer();
    const toast = document.createElement('div');
    toast.className = 'alerta-global-toast';
    toast.textContent = mensaje;
    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('out');
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
        const id = `${noti.alertaId}-${noti.medicionId}`;
        mostrarToast(noti.mensaje, id);
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

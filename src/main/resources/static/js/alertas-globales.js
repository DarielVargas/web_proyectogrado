(function () {
  const DURACION_MS = 10000;
  const POLL_MS = 12000;
  const STORAGE_KEY_ESTADOS = 'alertas-globales-estados-estacion';
  const vistos = new Set();
  const ultimoEstadoNotificado = new Map();
  let websocket = null;
  let reconnectTimer = null;

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

  function actualizarEstadoGuardado(codigo, activa) {
    const estados = obtenerEstadosGuardados() || {};
    estados[codigo] = Boolean(activa);
    guardarEstados(estados);
  }

  function construirMensajeEstado(codigo, activa) {
    return activa
      ? `La estación ${codigo} pasó a estado operativa`
      : `La estación ${codigo} pasó a estado inactiva`;
  }



  function notificarCambioEstado(codigo, activa, mensaje) {
    if (!codigo) return;

    const estadoActual = Boolean(activa);
    const estadoAnteriorNotificado = ultimoEstadoNotificado.get(codigo);
    if (estadoAnteriorNotificado === estadoActual) {
      return;
    }

    ultimoEstadoNotificado.set(codigo, estadoActual);
    mostrarToast({
      mensaje: mensaje || construirMensajeEstado(codigo, estadoActual),
      idUnico: `estado-estacion-${codigo}-${estadoActual}-${Date.now()}`,
      variante: 'estado-estacion'
    });
  }

  function parseValorSensor(valor) {
    const limpio = (valor || '').trim();
    if (!limpio || limpio === '--') {
      return { numero: '--', unidad: '' };
    }

    const match = limpio.match(/^(.+?)(?:\s+([^\s].*))?$/);
    return {
      numero: match?.[1] || limpio,
      unidad: match?.[2] || ''
    };
  }

  function obtenerIconoSensor(tipoSensor) {
    switch (tipoSensor) {
      case 'Temperatura Ambiental': return '🌡️';
      case 'Humedad Ambiental': return '💧';
      case 'Humedad del Suelo': return '🌱';
      case 'Intensidad de Luz Solar': return '☀️';
      case 'pH del Suelo': return '🧪';
      case 'Conductividad Eléctrica': return '⚡';
      case 'Nitrógeno (N)': return 'N';
      case 'Fósforo (P)': return 'P';
      case 'Potasio (K)': return 'K';
      default: return '•';
    }
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function obtenerNumeroSensor(valor) {
    if (valor == null) {
      return null;
    }

    const { numero } = parseValorSensor(String(valor));
    if (!numero || numero === '--') {
      return null;
    }

    const baseNumero = Number(String(numero).replace(',', '.'));
    return Number.isFinite(baseNumero) ? baseNumero : null;
  }

  function calcularEstadoSensor(tipoSensor, valor) {
    const baseNumero = obtenerNumeroSensor(valor);
    if (baseNumero == null) {
      return '';
    }

    const rangos = {
      'Temperatura Ambiental': [15, 18, 30, 35],
      'Humedad Ambiental': [50, 60, 85, 92],
      'Humedad del Suelo': [30, 45, 75, 85],
      'Intensidad de Luz Solar': [5000, 10000, 30000, 40000],
      'pH del Suelo': [5.0, 5.5, 7.0, 7.5],
      'Conductividad Electrica': [0.3, 0.5, 2.0, 3.0],
      'Conductividad Eléctrica': [0.3, 0.5, 2.0, 3.0],
      'Nitrogeno (N)': [50, 100, 200, 280],
      'Nitrógeno (N)': [50, 100, 200, 280],
      'Fosforo (P)': [10, 25, 80, 120],
      'Fósforo (P)': [10, 25, 80, 120],
      'Potasio (K)': [80, 120, 300, 400]
    };

    const rango = rangos[tipoSensor];
    if (!rango) {
      return '';
    }

    const [criticoBajoMax, idealMin, idealMax, criticoAltoMin] = rango;
    if (baseNumero < criticoBajoMax || baseNumero > criticoAltoMin) {
      return 'estado-critico';
    }

    if (baseNumero >= idealMin && baseNumero <= idealMax) {
      return 'estado-ideal';
    }

    return 'estado-advertencia';
  }

  function calcularProgreso(sensor) {
    if (!sensor || !sensor.valor) {
      return 0;
    }

    const baseNumero = obtenerNumeroSensor(sensor.valor);
    if (baseNumero == null) {
      return 0;
    }

    if (sensor.tipoSensor === 'Intensidad de Luz Solar') {
      return Math.min(100, Math.max(0, baseNumero / 10));
    }

    return Math.min(100, Math.max(0, baseNumero));
  }

  function renderSensor(sensor) {
    const { numero, unidad } = parseValorSensor(sensor.valor);
    const progreso = calcularProgreso(sensor);
    const mostrarBarra = sensor.tipoSensor === 'Humedad del Suelo' || sensor.tipoSensor === 'Intensidad de Luz Solar';
    const estadoSensor = calcularEstadoSensor(sensor.tipoSensor, sensor.valor);
    const clasesSensor = ['sensor-row', estadoSensor, sensor.activo ? '' : 'inactivo'].filter(Boolean).join(' ');
    const rawValor = numero === '--' ? '--' : String(numero).replace(',', '.');

    return `
      <div class="${clasesSensor}" data-sensor-tipo="${escapeHtml(sensor.tipoSensor)}" data-sensor-valor="${escapeHtml(rawValor)}">
        <div class="sensor-main">
          <div class="sensor-info">
            <span class="sensor-icon">${escapeHtml(obtenerIconoSensor(sensor.tipoSensor))}</span>
            <span class="sensor-name">${escapeHtml(sensor.tipoSensor)}</span>
            ${sensor.activo ? '' : '<span class="sensor-badge-off">Sensor desactivado</span>'}
          </div>
          <div class="sensor-value-wrap">
            <span class="sensor-value">${escapeHtml(numero)}</span>
            ${unidad ? `<span class="sensor-unit">${escapeHtml(unidad)}</span>` : ''}
          </div>
        </div>
        ${mostrarBarra ? `
          <div class="progress-track">
            <div class="progress-fill${sensor.tipoSensor === 'Intensidad de Luz Solar' ? ' luz' : ''}" style="--target-width:${progreso.toFixed(0)}%; width:${progreso.toFixed(0)}%"></div>
          </div>` : ''}
      </div>`;
  }

  function renderEstacionDashboard(estacion) {
    return `
      <article class="estacion-card animate-item is-visible">
        <div class="estacion-head">
          <div>
            <h2 class="estacion-title">${escapeHtml(estacion.estacionCodigo)}</h2>
            <p class="estacion-sub">${escapeHtml(estacion.estacionDescripcion)}</p>
          </div>
          <div class="estacion-status-wrap">
            <span class="estado-tag ${estacion.activa ? 'estado-ok' : 'estado-off'}">${estacion.activa ? 'Operativa' : 'Inactiva'}</span>
            <button type="button" class="btn-info-rangos" data-rangos-toggle aria-label="Ver guía de colores y rangos de sensores" aria-controls="rangos-sensores-popover" aria-expanded="false">i</button>
          </div>
        </div>
        <div class="sensor-list">${(estacion.sensores || []).map(renderSensor).join('')}</div>
      </article>`;
  }

  function actualizarDashboard(snapshot) {
    if (!snapshot) return;

    const sensoresActivosEl = document.querySelector('[data-dashboard-sensores-activos]');
    const sensoresSubEl = document.querySelector('[data-dashboard-sensores-sub]');
    const tempEl = document.querySelector('[data-dashboard-temp]');
    const humEl = document.querySelector('[data-dashboard-hum]');
    const alertasEl = document.querySelector('[data-dashboard-alertas]');
    const estacionesEl = document.querySelector('[data-dashboard-estaciones]');

    if (sensoresActivosEl) sensoresActivosEl.textContent = String(snapshot.sensoresActivos ?? 0);
    if (sensoresSubEl) sensoresSubEl.textContent = `${snapshot.sensoresActivos ?? 0} sensores activos de ${snapshot.sensoresRegistrados ?? 0} sensores`;
    if (tempEl) tempEl.textContent = snapshot.tempPromedioTxt || '--';
    if (humEl) humEl.textContent = snapshot.humPromedioTxt || '--';
    if (alertasEl) alertasEl.textContent = String(snapshot.totalAlertasConfiguradas ?? 0);
    if (estacionesEl && Array.isArray(snapshot.estaciones)) {
      estacionesEl.innerHTML = snapshot.estaciones.map(renderEstacionDashboard).join('');
    }
  }

  function actualizarListadoEstaciones(estados) {
    if (!estados || typeof estados !== 'object') return;

    document.querySelectorAll('[data-estacion-codigo]').forEach((row) => {
      const codigo = row.getAttribute('data-estacion-codigo');
      if (!codigo || !(codigo in estados)) return;

      const activa = Boolean(estados[codigo]);
      const dot = row.querySelector('[data-estacion-estado-dot]');
      const button = row.querySelector('[data-estacion-toggle]');

      if (dot) {
        dot.classList.toggle('activa', activa);
        dot.classList.toggle('inactiva', !activa);
      }

      if (button) {
        button.classList.toggle('on', activa);
        button.classList.toggle('off', !activa);
        button.textContent = activa ? 'Desactivar' : 'Activar';
      }
    });
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

          notificarCambioEstado(codigo, activa);
        });
      }

      actualizarListadoEstaciones(estadosActuales);
      guardarEstados(estadosActuales);
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  async function cargarDashboard() {
    if (!document.querySelector('[data-dashboard-estaciones]')) {
      return;
    }

    try {
      const res = await fetch('/api/dashboard/resumen', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) return;
      const data = await res.json();
      actualizarDashboard(data);
    } catch (_) {
      // Ignorar errores intermitentes para no romper la UI.
    }
  }

  function obtenerPopoverRangos() {
    return document.querySelector('[data-rangos-popover]');
  }

  function obtenerBotonRangosActivo() {
    return document.querySelector('[data-rangos-toggle].is-open');
  }

  function posicionarPopoverRangos(button, popover) {
    const margen = 14;
    const rect = button.getBoundingClientRect();
    const ancho = popover.offsetWidth || 420;
    const alto = popover.offsetHeight || 520;
    const topPreferido = rect.bottom + 10;
    const espacioInferior = window.innerHeight - topPreferido - margen;
    const top = espacioInferior < Math.min(alto, 360)
      ? Math.max(margen, rect.top - alto - 10)
      : topPreferido;
    const left = Math.min(
      Math.max(margen, rect.right - ancho),
      window.innerWidth - ancho - margen
    );

    popover.style.top = `${top}px`;
    popover.style.left = `${left}px`;
  }

  function cerrarPopoverRangos() {
    const popover = obtenerPopoverRangos();
    if (!popover || popover.hidden) {
      return;
    }

    document.querySelectorAll('[data-rangos-toggle].is-open').forEach((button) => {
      button.classList.remove('is-open');
      button.setAttribute('aria-expanded', 'false');
    });
    popover.classList.remove('is-open');

    window.setTimeout(() => {
      if (!popover.classList.contains('is-open')) {
        popover.hidden = true;
      }
    }, 180);
  }

  function abrirPopoverRangos(button) {
    const popover = obtenerPopoverRangos();
    if (!button || !popover) {
      return;
    }

    document.querySelectorAll('[data-rangos-toggle].is-open').forEach((otroBoton) => {
      if (otroBoton !== button) {
        otroBoton.classList.remove('is-open');
        otroBoton.setAttribute('aria-expanded', 'false');
      }
    });

    popover.hidden = false;
    posicionarPopoverRangos(button, popover);
    window.requestAnimationFrame(() => {
      popover.classList.add('is-open');
      button.classList.add('is-open');
      button.setAttribute('aria-expanded', 'true');
    });
  }

  function alternarPopoverRangos(button) {
    const popover = obtenerPopoverRangos();
    if (!popover) {
      return;
    }

    if (!popover.hidden && button.classList.contains('is-open')) {
      cerrarPopoverRangos();
      return;
    }

    abrirPopoverRangos(button);
  }

  function configurarPopoverRangos() {
    document.addEventListener('click', (event) => {
      const toggle = event.target.closest('[data-rangos-toggle]');
      if (toggle) {
        event.preventDefault();
        event.stopPropagation();
        alternarPopoverRangos(toggle);
        return;
      }

      if (event.target.closest('[data-rangos-close]')) {
        event.preventDefault();
        cerrarPopoverRangos();
        return;
      }

      const popover = obtenerPopoverRangos();
      if (popover && !popover.hidden && !event.target.closest('[data-rangos-popover]')) {
        cerrarPopoverRangos();
      }
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        cerrarPopoverRangos();
      }
    });

    ['resize', 'scroll'].forEach((evento) => {
      window.addEventListener(evento, () => {
        const popover = obtenerPopoverRangos();
        const button = obtenerBotonRangosActivo();
        if (popover && button && !popover.hidden) {
          posicionarPopoverRangos(button, popover);
        }
      }, { passive: true });
    });
  }

  function conectarWebSocket() {
    if (!('WebSocket' in window)) {
      return;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const url = `${protocol}//${window.location.host}/ws/tiempo-real`;
    websocket = new WebSocket(url);

    websocket.addEventListener('open', () => {
      if (reconnectTimer) {
        window.clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }

      cargarDashboard();
      cargarEstadosEstacion();
      cargarAlertas();
    });

    websocket.addEventListener('message', (event) => {
      try {
        const data = JSON.parse(event.data);
        switch (data.type) {
          case 'dashboard-update':
            actualizarDashboard(data.payload);
            cargarAlertas();
            break;
          case 'station-status-changed':
            if (data.payload?.estacionCodigo) {
              notificarCambioEstado(data.payload.estacionCodigo, data.payload.activa, data.payload?.mensaje);
              actualizarEstadoGuardado(data.payload.estacionCodigo, data.payload.activa);
              actualizarListadoEstaciones({ [data.payload.estacionCodigo]: Boolean(data.payload.activa) });
            }
            break;
          case 'alerts-refresh':
            cargarAlertas();
            cargarEstadosEstacion();
            cargarDashboard();
            break;
          case 'connected':
          default:
            break;
        }
      } catch (_) {
        // Ignorar mensajes inválidos.
      }
    });

    websocket.addEventListener('close', () => {
      reconnectTimer = window.setTimeout(conectarWebSocket, 5000);
    });

    websocket.addEventListener('error', () => {
      websocket?.close();
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    ensureContainer();
    configurarPopoverRangos();
    cargarAlertas();
    cargarEstadosEstacion();
    cargarDashboard();
    conectarWebSocket();
    setInterval(cargarAlertas, POLL_MS);
    setInterval(cargarEstadosEstacion, POLL_MS);
    setInterval(cargarDashboard, POLL_MS);
  });
})();
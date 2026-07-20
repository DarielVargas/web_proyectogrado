import os
from datetime import datetime, timedelta

import ee
import requests

# ==========================================================
# INICIALIZAR EARTH ENGINE
# ==========================================================
ee.Initialize(project='agro-iot-496315')

BACKEND_URL = os.getenv("AGRO_BACKEND_URL", "http://localhost:8081")
INTERVALO_ANALISIS = timedelta(days=5)
HTTP_TIMEOUT_SEGUNDOS = 30

# ==========================================================
# ÁREA AGRÍCOLA
# ==========================================================
geometry = ee.Geometry.Polygon([
    [
        [-70.996281, 19.550604],
        [-70.996003, 19.550606],
        [-70.996001, 19.550455],
        [-70.996275, 19.550453],
        [-70.996281, 19.550604]
    ]
])

# ==========================================================
# CONSULTAR ÚLTIMO ANÁLISIS AL BACKEND
# ==========================================================
def obtener_fecha_ultimo_analisis():
    respuesta = requests.get(
        f"{BACKEND_URL}/api/indices-satelitales/ultimo",
        timeout=HTTP_TIMEOUT_SEGUNDOS
    )

    if respuesta.status_code == 204:
        return None

    respuesta.raise_for_status()
    fecha_texto = respuesta.json().get("fecha")
    if not fecha_texto:
        raise ValueError("El backend no devolvió la fecha del último análisis satelital")

    return datetime.fromisoformat(fecha_texto.replace("Z", "+00:00"))


def corresponde_ejecutar_analisis(ahora=None):
    fecha_ultimo_analisis = obtener_fecha_ultimo_analisis()
    if fecha_ultimo_analisis is None:
        print("No existe un análisis satelital previo. Se ejecutará el análisis.")
        return True

    ahora = ahora or datetime.now(fecha_ultimo_analisis.tzinfo)
    proxima_fecha = fecha_ultimo_analisis + INTERVALO_ANALISIS

    if ahora < proxima_fecha:
        print(f"Último análisis: {fecha_ultimo_analisis.isoformat()}")
        print(f"Aún no han pasado 5 días. Próximo análisis permitido: {proxima_fecha.isoformat()}")
        return False

    print(f"Han pasado al menos 5 días desde el último análisis ({fecha_ultimo_analisis.isoformat()}).")
    return True

# ==========================================================
# LIMPIAR NUBES
# ==========================================================
def remove_clouds(image):

    cloud_prob = image.select('MSK_CLDPRB')
    scl = image.select('SCL')

    cloud_mask = cloud_prob.lt(20)

    scl_mask = (
        scl.neq(3)
        .And(scl.neq(8))
        .And(scl.neq(9))
        .And(scl.neq(10))
    )

    return (
        image
        .updateMask(cloud_mask)
        .updateMask(scl_mask)
        .divide(10000)
    )

# ==========================================================
# EJECUTAR CONSULTA SATELITAL
# ==========================================================
def ejecutar_consulta():

    print("\n==============================")
    print("CONSULTANDO EARTH ENGINE...")
    print("==============================")

    # ------------------------------------------------------
    # COLECCIÓN
    # ------------------------------------------------------
    coleccion = (
        ee.ImageCollection('COPERNICUS/S2_SR_HARMONIZED')
        .filterBounds(geometry)
        .filterDate('2026-01-01', '2026-05-01')
        .filter(ee.Filter.lt('CLOUDY_PIXEL_PERCENTAGE', 20))
        .map(remove_clouds)
    )

    # ------------------------------------------------------
    # IMAGEN
    # ------------------------------------------------------
    imagen = coleccion.median()

    # ------------------------------------------------------
    # NDVI
    # ------------------------------------------------------
    ndvi = imagen.normalizedDifference(['B8', 'B4']).rename('NDVI')

    # ------------------------------------------------------
    # NDWI
    # ------------------------------------------------------
    ndwi = imagen.normalizedDifference(['B8', 'B11']).rename('NDWI')

    # ------------------------------------------------------
    # PROMEDIOS
    # ------------------------------------------------------
    promedio_ndvi = ndvi.reduceRegion(
        reducer=ee.Reducer.mean(),
        geometry=geometry,
        scale=10,
        maxPixels=1e9
    )

    promedio_ndwi = ndwi.reduceRegion(
        reducer=ee.Reducer.mean(),
        geometry=geometry,
        scale=10,
        maxPixels=1e9
    )

    # ------------------------------------------------------
    # VALORES
    # ------------------------------------------------------
    valor_ndvi = promedio_ndvi.get('NDVI').getInfo()
    valor_ndwi = promedio_ndwi.get('NDWI').getInfo()

    print("NDVI REAL:", valor_ndvi)
    print("NDWI REAL:", valor_ndwi)

    # ------------------------------------------------------
    # ESTADO VEGETACIÓN
    # ------------------------------------------------------
    if valor_ndvi >= 0.6:
        estado_vegetacion = "Saludable"
    elif valor_ndvi >= 0.3:
        estado_vegetacion = "Moderado"
    else:
        estado_vegetacion = "Critico"

    # ------------------------------------------------------
    # ESTADO HÍDRICO
    # ------------------------------------------------------
    if valor_ndwi >= 0.2:
        estado_hidrico = "Optimo"
    elif valor_ndwi >= 0:
        estado_hidrico = "Moderado"
    else:
        estado_hidrico = "Critico"

    # ------------------------------------------------------
    # JSON
    # ------------------------------------------------------
    payload = {
        "fecha": datetime.now().isoformat(),
        "ndvi": round(valor_ndvi, 3),
        "ndwi": round(valor_ndwi, 3),
        "estadoVegetacion": estado_vegetacion,
        "estadoHidrico": estado_hidrico
    }

    print(payload)

    # ------------------------------------------------------
    # ENVIAR A SPRING BOOT
    # ------------------------------------------------------
    respuesta = requests.post(
        f"{BACKEND_URL}/api/indices-satelitales",
        json=payload,
        timeout=HTTP_TIMEOUT_SEGUNDOS
    )
    respuesta.raise_for_status()

    print("STATUS:", respuesta.status_code)
    print(respuesta.text)


def main():
    try:
        if corresponde_ejecutar_analisis():
            ejecutar_consulta()
        else:
            print("El proceso finalizó sin generar un nuevo registro.")
    except Exception as error:
        print("ERROR:", error)
        raise


if __name__ == "__main__":
    main()
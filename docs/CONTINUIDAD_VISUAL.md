# Continuidad visual y recursos aprobados · caso 004

## Alcance y conservación

Se revisaron las 24 uniones entre vecinos del mapa 4×4 y los cuatro spritesheets. Se conservan **los bytes originales de los 20 PNG**; `SHA256_ORIGINALES.json` permite verificarlo. No se han recortado, rotado, recoloreado ni retocado fondos o sprites. Los 16 fondos son RGB de 256×256, se muestran completos con `ContentScale.Fit` y muestreo sin suavizado. La vista conjunta suministrada es orientativa, no un tile adicional.

El mapa del manifiesto se aplica exclusivamente al caso 004, donde el despacho ya era B1 y los sospechosos son Alma, Darío, Irene y Tomás. Se mantienen intactos los cuatro JSON de casos, las pistas, soluciones, el motor de deducción y el código/formato de persistencia. En particular, el caso 001 conserva D2 como despacho y su escena anterior.

## Conexiones independientes

`core/src/main/resources/maps/mansion.json` define los 16 nodos, las posiciones de los pies y 21 conexiones bidireccionales. `Navigation.matrix` produce una matriz 16×16: 42 entradas dirigidas, diagonal vacía. Los portales de entrada/salida coinciden matemáticamente en coordenadas normalizadas del mapa. No se deducen colisiones del color de los píxeles ni se confunden muebles/plantas con paredes.

Todos los vecinos interiores están conectados por umbrales o arcos centrales; la fachada solo permite B3↔B4. Los cuatro exteriores se conectan lateralmente. A3↔A4, C3↔C4 y D3↔D4 son paredes. Un destino lejano usa un camino por puertas; no implica atravesar directamente cada borde que cruce el dedo. El personaje levantado durante el arrastre es una previsualización; la posición lógica solo cambia al soltar en un destino válido. La animación confirmada sigue los portales. Las casillas intermedias de la animación no son colocaciones de deducción.

La entrada B3↔B4 es una decisión de topología explícita y revisable: las imágenes no contienen un plano de colisiones aprobado. Las aberturas se configuran por datos y no se corrigen dibujando decoración nueva. Todos los destinos siguen siendo alcanzables por rutas legales.

## Informe de las 24 costuras

Ninguna unión debe considerarse un empalme artístico exacto. La diferencia media RGB de los píxeles del borde (escala 0–255, sin cambiar imágenes) es un diagnóstico de costura, **no** una prueba de que exista una puerta. Los valores se reproducen con `python scripts/audit_art.py` (requiere Pillow).

| Unión | Diferencia RGB media | Revisión |
|---|---:|---|
| A1 ↔ B1 | 25.69 | Portales laterales centrados; marco, anchura del tapete y tonos no casan píxel a píxel. |
| A1 ↔ A2 | 46.04 | Umbral inferior de biblioteca frente al arco superior de estar; eje central aproximado, distinta perspectiva/profundidad. |
| B1 ↔ C1 | 10.04 | Eje central compatible; umbral y pilares de anchura diferente. |
| B1 ↔ B2 | 20.1 | Alfombra del despacho frente al acceso elevado del vestíbulo; escala y profundidad del umbral diferentes. |
| C1 ↔ D1 | 8.83 | Paso lateral centrado; transición madera/baldosa y marcos diferentes. |
| C1 ↔ C2 | 11.45 | Acceso del dormitorio frente al nicho/acceso norte de galería; la ilustración frontal no forma un agujero continuo en el borde. |
| D1 ↔ D2 | 13.59 | Umbral del baño frente al arco del invernadero; salto de material y marcos. |
| A2 ↔ B2 | 37.27 | Arco de sala de estar frente al portal rojo del vestíbulo; anchura y profundidad no coinciden. |
| A2 ↔ A3 | 28.77 | Paso inferior de estar frente al arco de estrategia; sombras y grosor de pared no coinciden. |
| B2 ↔ C2 | 19.02 | Portales centrados; columnas, mármol y tapete presentan un salto visual. |
| B2 ↔ B3 | 13.12 | Umbral de mármol frente a la alfombra de cocina; salto de material y ancho. |
| C2 ↔ D2 | 23.62 | Portal de galería frente a columnas/vegetación; bordes y suelos no continúan exactamente. |
| C2 ↔ C3 | 14.79 | Umbral de galería frente al arco de bodega; diferencia de profundidad y escala. |
| D2 ↔ D3 | 14.49 | Umbral de invernadero frente al arco/cortinas de música; decoraciones no continuas. |
| A3 ↔ B3 | 11.74 | Portales centrales compatibles; zócalos y suelo difieren. |
| A3 ↔ A4 | 43.09 | SIN CONEXIÓN: la fachada exterior muestra muro/ventana, no acceso. No se cruza directamente. |
| B3 ↔ C3 | 8.7 | Portales centrales; grosor de pared y baldosas diferentes. |
| B3 ↔ B4 | 36.33 | Única entrada de fachada habilitada. Eje X=0.5 alineado; escalera y puerta de B4 no casan con el umbral de cocina. La hoja está dibujada cerrada, aunque el portal lógico está abierto. |
| C3 ↔ D3 | 9.1 | Portales centrales; barriles, cortinas y sombras no comparten costura. |
| C3 ↔ C4 | 41.47 | SIN CONEXIÓN: fachada y ventana detrás del jardín. No se cruza directamente. |
| D3 ↔ D4 | 42.59 | SIN CONEXIÓN: fachada tras la zona de cabaña. No se cruza directamente. |
| A4 ↔ B4 | 23.02 | Camino junto al tenis no continúa con igual anchura/cota en la entrada; bordillos y setos cambian. |
| B4 ↔ C4 | 43.15 | La entrada recta desemboca en un jardín de senderos curvos; no hay costura de camino garantizada en Y=0.79. |
| C4 ↔ D4 | 42.57 | Curvas, flores y bordillos cambian de cota; continuidad temática, no geométrica exacta. |

## Sprites y aspectos pendientes

- Los cuatro originales son RGBA de 192×256; cada uno contiene 16 fotogramas de 48×64 con píxeles realmente transparentes. No se añade ningún fondo opaco en la capa de personajes.
- Se respetan las filas suministradas: abajo=0, izquierda=1, derecha=2 y arriba=3. Al caminar se recorren las columnas 0–3 cada 120 ms; al quedar quieto se usa el fotograma 0 de la última dirección de marcha.
- Se ancla el centro inferior de la silueta visible al suelo. Los pies quedan en la misma cota aunque cambien los márgenes transparentes. No se alteran los píxeles originales. La altura del sprite es el 34% de la habitación (aproximadamente 87 de sus 256 píxeles), en una capa independiente y con proporción 48:64.
- La auditoría encontró variaciones de anchura entre fotogramas: Alma 29–37 px, Darío 27–34, Irene 24–35 y Tomás 28–37. Todos terminan su silueta visible en Y=62 dentro del frame de 64. Se conserva esa variación artística: puede apreciarse una ligera oscilación de volumen.
- La separación de fondo original fue automática, según el README del proveedor. Se aprecian contornos oscuros y posibles halos marrones en algunos bordes; no se han limpiado ni inventado píxeles. Conviene revisarlos sobre cada suelo antes de publicar una versión final.
- La perspectiva de los fondos varía: combinan suelo cenital con muebles, arcos y fachadas frontales. No puede lograrse continuidad arquitectónica exacta únicamente con código y sin retocar los recursos aprobados.

Queda una revisión manual en móvil del tamaño de los personajes, los bordes y el desplazamiento durante una ruta larga. Las pruebas automatizadas verifican carga, transparencia, selección, topología, animación por dirección y conservación de la lógica. Esta entrega no afirma que las costuras artísticas estén corregidas.

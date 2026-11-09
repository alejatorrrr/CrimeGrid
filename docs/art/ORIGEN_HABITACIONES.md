# CrimeGrid - 16 fondos seleccionados

- Cada archivo de casilla es PNG RGB 256x256, orden A1-D4, columnas A-D, filas 1-4.
- `vista_mapa_4x4.png` es una vista de conjunto, no un tile adicional.
- `manifest.json` vincula IDs con archivos.
- A2 es la versión nueva aprobada, sin fachada ni ventanas inferiores.
- C4 es la versión revisada del jardín, D4 tiene cabaña.

## Atención: geometría
Las imágenes son fondos artísticos, NO un mapa de colisiones ni una garantía de continuidad exacta en píxeles. No se ha retocado su geometría ni ajustado las costuras. Los accesos visibles pueden no coincidir con los accesos lógicos. El juego debe tener una matriz de conexiones y puntos de entrada explícitos. Comprobar en dispositivo el tamaño, recorte y conexiones antes de considerarlo acabado.

## Integración
Importar fondos a recursos Android con nombres en minúsculas y sin espacios, o conservar los archivos en assets y leerlos por ID. Renderizar cada fondo sin recorte, con escalado proporcional, y superponer los sprites en una capa independiente. No alterar las reglas del caso ni la solución única.

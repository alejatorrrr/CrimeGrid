# CrimeGrid — sprites de personajes

Spritesheets PNG RGBA de 192×256 px; 4 columnas × 4 filas, 48×64 px por frame.

Filas (índice desde 0): 0=abajo, 1=izquierda, 2=derecha, 3=arriba. Columnas 0–3: animación de caminar.

Archivos: `alma_walk_48x64.png`, `dario_walk_48x64.png`, `irene_walk_48x64.png`, `tomas_walk_48x64.png`. Cada carpeta contiene además los 16 frames separados.

Para renderizar en Compose, usar `srcOffset = IntOffset(frameIndex*48, directionRow*64)` y `srcSize = IntSize(48,64)` al dibujar sobre Canvas. Mantener escalado nearest-neighbor / FilterQuality.None. Anclar personajes por el centro inferior del sprite y escalar según espacio disponible.

**Aviso de calidad:** Las fuentes originales tenían un fondo marrón degradado y etiquetas, no transparencia real. La separación se ha realizado automáticamente por color/contraste y puede presentar halos o pérdida de píxeles oscuros; inspeccionar en juego antes de publicar. Los fotogramas de origen no son idénticos en escala/posición.

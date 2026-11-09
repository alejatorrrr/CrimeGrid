# CrimeGrid 0.5 · revisión de las 24 uniones

## Resultado y límites

La mansión se presenta como un solo atlas cuadrado, sin líneas, marcos, coordenadas o nombres superpuestos. Los 16 originales y los cuatro spritesheets aprobados siguen intactos. El mapa se compone una vez en memoria y recibe seis parches PNG pequeños y opacos; los PNG originales nunca se sobrescriben.

Se ensayó una edición generativa de las costuras a partir de la vista conjunta aprobada. La propuesta completa alteraba texturas fuera de las costuras y cambiaba la resolución, por lo que **no se incorporó completa**. Solo se aprovecharon correcciones cromáticas acotadas, con un cambio máximo de 16 niveles por canal dentro de bandas de ocho píxeles. Se excluyeron las esquinas y la decoración interior. El borde exterior de cada parche conserva sus píxeles originales.

**Seis uniones reciben una corrección parcial de contraste/material. Las otras dieciocho conservan todos sus píxeles originales. No se afirma que se hayan reparado completamente las discontinuidades arquitectónicas.** Las reparaciones cambian 8.273 de 1.048.576 píxeles; se conserva el 99,21 % del atlas original. No se sustituyen habitaciones, muebles ni la distribución.

Las puertas centrales mantienen su eje lógico y las imágenes no son un mapa de colisiones. La matriz de 21 conexiones, los portales y las tres paredes de fachada cerradas siguen siendo los de 0.4. Los parches no cambian conectividad, pistas o soluciones.

## Tabla de las 24 uniones

La diferencia RGB es un diagnóstico de color del borde (0–255), no una medida de accesibilidad ni una prueba de continuidad geométrica. Una mejora cromática no elimina una columna duplicada o endereza un camino.

| Unión | Antes | Después | Tratamiento |
|---|---:|---:|---|
| A1 ↔ B1 | 26.96 | 26.96 | Original conservado |
| A1 ↔ A2 | 44.21 | 44.21 | Original conservado |
| B1 ↔ C1 | 10.45 | 10.45 | Original conservado |
| B1 ↔ B2 | 20.68 | 11.83 | Parche cromático conservador; geometría preservada |
| C1 ↔ D1 | 9.29 | 9.29 | Original conservado |
| C1 ↔ C2 | 11.68 | 7.31 | Parche cromático conservador; geometría preservada |
| D1 ↔ D2 | 13.92 | 8.39 | Parche cromático conservador; geometría preservada |
| A2 ↔ B2 | 40.38 | 36.33 | Parche cromático conservador; geometría preservada |
| A2 ↔ A3 | 29.74 | 18.61 | Parche cromático conservador; geometría preservada |
| B2 ↔ C2 | 19.23 | 19.23 | Original conservado |
| B2 ↔ B3 | 13.76 | 13.76 | Original conservado |
| C2 ↔ D2 | 24.07 | 24.07 | Original conservado |
| C2 ↔ C3 | 15.6 | 12.05 | Parche cromático conservador; geometría preservada |
| D2 ↔ D3 | 15.41 | 15.41 | Original conservado |
| A3 ↔ B3 | 12.08 | 12.08 | Original conservado |
| A3 ↔ A4 | 42.83 | 42.83 | Original conservado |
| B3 ↔ C3 | 9.14 | 9.14 | Original conservado |
| B3 ↔ B4 | 37.29 | 37.29 | Original conservado |
| C3 ↔ D3 | 9.24 | 9.24 | Original conservado |
| C3 ↔ C4 | 40.65 | 40.65 | Original conservado |
| D3 ↔ D4 | 42.33 | 42.33 | Original conservado |
| A4 ↔ B4 | 24.3 | 24.3 | Original conservado |
| B4 ↔ C4 | 45.22 | 45.22 | Original conservado |
| C4 ↔ D4 | 45.8 | 45.8 | Original conservado |

## Problemas arquitectónicos que siguen pendientes

- Los arcos y pasillos interiores están centrados lógicamente, pero sus anchos, zócalos, sombras y perspectivas no coinciden en todos los bordes. La corrección segura de color no sustituye un redibujo arquitectónico.
- Persisten dobles columnas y muros gruesos en algunas juntas. Reducirlos sin tocar estanterías, plantas u otros detalles exige una revisión artística más amplia; se conservan en lugar de recortar o deformar habitaciones.
- A3↔A4, C3↔C4 y D3↔D4 son paredes cerradas. No se abren puertas nuevas para hacerlas parecer pasillos. La única entrada lógica de la fachada continúa siendo B3↔B4; la hoja exterior está dibujada cerrada y los peldaños no empalman exactamente con el suelo interior.
- A4↔B4, B4↔C4 y C4↔D4 conservan las curvas, bordillos y cotas originales. El jardín se muestra contiguo, pero sus caminos no están unificados geométricamente. La propuesta generativa movía plantaciones y límites de elementos aprobados; se descartó esa parte para no aplicar una modificación destructiva.
- Los posibles halos de los sprites y las variaciones de anchura de sus frames permanecen como en los originales. No se han generado personajes nuevos ni cambiado los frames, la cadencia o las proporciones.

## Reproducción y controles

- `scripts/audit_continuity_05.py` verifica las 24 entradas, los 20 PNG originales, la máscara exacta, el número de píxeles alterados y el límite cromático.
- `REPARACIONES.json` recoge las bandas y los hashes de la propuesta y la composición de referencia.
- `app/src/main/assets/pixelart/mansion/patches/manifest.json` contiene únicamente las seis posiciones de los parches visuales.
- `scripts/repair_seams_05.py <propuesta.png>` permite repetir el proceso con la propuesta gráfica. Requiere Pillow; no interviene en la compilación Android ni en el juego.
- Para el inventario visual detallado previo de cada junta, véase `../CONTINUIDAD_VISUAL.md`, conservado sin borrar el informe de 0.4.

Las limitaciones anteriores son las excepciones de conservación previstas en el encargo. No se presentan como reparaciones terminadas ni se sustituyen con decoración inventada.

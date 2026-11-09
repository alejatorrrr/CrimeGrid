# Castillo encantado · caso 003

## Identidad y conservación

El usuario confirmó que el castillo corresponde al **003**, aunque el ZIP y el manifiesto original contienen el identificador 005. Se conserva ese manifiesto sin cambios como procedencia; no se ha creado un caso 005.

Se adapta el caso 003 que ya existía. Sus sospechosos (Ada, Gael, Lara y Víctor), seis pistas, ayudas, posición B1 del crimen y solución única se conservan. Solo cambian título, ambientación y las menciones narrativas del lugar: B1 es el salón del trono en el arte suministrado. La culpable sigue siendo Lara; no se inventa una solución nueva. `LOGICA_003_BASE.json` registra los datos lógicos anteriores para compararlos.

El caso 004 conserva los fondos, sprites, parches, paleta, tipografía y disposición de 0.5. Los casos 001 y 002 siguen siendo los casos de prueba existentes. El menú, cuaderno, controles, restricciones y persistencia se conservan. El nuevo inspector añade una acción; no sustituye el zoom corto existente.

## Recursos originales

- Los 16 PNG de 384×384 se copian byte a byte a `app/src/main/assets/pixelart/castle003/`.
- El tablero de 1536×1536 se compone una sola vez en memoria usando `source_crop` e IDs del manifiesto. No se genera un PNG gigante duplicado para el APK.
- No se regenera, retoca, recolorea, recorta otra vez ni suaviza ninguna imagen original. Todo dibujo utiliza vecino más cercano (`FilterQuality.None`).
- Las 16 regiones ocupan exactamente el atlas 4×4, sin solapamientos, bandas perdidas, bordes, rótulos ni separación de UI.
- Los personajes reutilizan las hojas actuales: Ada→sprite de Alma, Gael→Darío, Lara→Irene y Víctor→Tomás. Esto es una asociación gráfica; los IDs, nombres y reglas de los sospechosos del 003 no cambian. Las hojas, frames 48×64, cadencia y proporciones permanecen intactos.

## Visor y prioridad de gestos

- Toque breve sin selección: se mantiene la ampliación del mapa de 0.5.
- Toque breve con selección: colocar al sospechoso según las restricciones habituales.
- Pulsación prolongada sobre el personaje: arrastrar, incluso sin selección previa.
- Pulsación prolongada en suelo libre sin selección: abrir un visor de solo lectura de esa habitación.
- Pulsación prolongada en suelo con selección: no abre el visor ni coloca accidentalmente.
- En el visor: pellizco con dos dedos, desplazamiento con uno o dos dedos, zoom limitado 1×–6× y cierre. No se rota la imagen ni se revelan nombre o coordenadas.

El reconocedor único decide el gesto al inicio y da prioridad al personaje, con un área táctil generosa. El visor no modifica posiciones, selección, pistas o progreso. El fondo de su capa impide que los toques lleguen al tablero de detrás. El caso 004 recibe el mismo inspector sin cambios de estilo.

El arte no suministra un mapa de colisiones del castillo: no se inventan puertas o restricciones de navegación. El 003 conserva su validación de colocación por filas/columnas. La caminata física por portales de la mansión y su matriz siguen activas únicamente en el 004; al arrastrar en el castillo se mantienen los frames direccionales y el movimiento de la ficha.

## Comprobación de las 24 uniones

Las coordenadas de recorte cubren los 1536×1536 píxeles sin huecos. La prueba Android compara **todos los píxeles de los 16 recortes** con sus regiones del atlas; la auditoría compara además los hashes con el ZIP. La composición no añade discontinuidades. La revisión visual de conjunto confirma la continuidad del corredor central y la mampostería que cruza las fronteras de recorte.

No se incluyó la imagen madre original en el ZIP, por lo que no se puede hacer una comparación binaria contra ella. La diferencia RGB siguiente mide el cambio natural entre dos columnas/filas vecinas; no demuestra por sí sola la existencia de una puerta ni obliga a retocar el arte. La arquitectura y cualquier detalle ya dibujado se conservan tal cual.

| Unión | Orientación | Diferencia RGB media |
|---|---|---:|
| A1 ↔ B1 | horizontal | 5.15 |
| A1 ↔ A2 | vertical | 9.52 |
| B1 ↔ C1 | horizontal | 11.4 |
| B1 ↔ B2 | vertical | 10.34 |
| C1 ↔ D1 | horizontal | 5.92 |
| C1 ↔ C2 | vertical | 7.42 |
| D1 ↔ D2 | vertical | 8.26 |
| A2 ↔ B2 | horizontal | 7.48 |
| A2 ↔ A3 | vertical | 10.25 |
| B2 ↔ C2 | horizontal | 11.28 |
| B2 ↔ B3 | vertical | 11.27 |
| C2 ↔ D2 | horizontal | 7.92 |
| C2 ↔ C3 | vertical | 9.2 |
| D2 ↔ D3 | vertical | 10.6 |
| A3 ↔ B3 | horizontal | 6.85 |
| A3 ↔ A4 | vertical | 13.51 |
| B3 ↔ C3 | horizontal | 14.61 |
| B3 ↔ B4 | vertical | 9.15 |
| C3 ↔ D3 | horizontal | 6.84 |
| C3 ↔ C4 | vertical | 9.38 |
| D3 ↔ D4 | vertical | 16.19 |
| A4 ↔ B4 | horizontal | 6.68 |
| B4 ↔ C4 | horizontal | 12.67 |
| C4 ↔ D4 | horizontal | 6.04 |

## Auditoría y capturas

`python scripts/audit_castle003.py` (Pillow) comprueba bytes originales, dimensiones, IDs, recortes, 24 uniones y preservación de los datos lógicos del 003. `MAPA.png` y `VISOR.png` son capturas nativas de Robolectric, no imágenes regeneradas. Queda revisión en dispositivos físicos de distintas densidades; no se afirma que se haya ensayado cada móvil.

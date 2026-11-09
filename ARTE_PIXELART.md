> Documento histórico de preparación de la versión 0.3. El plano aprobado vigente es el del caso 004, descrito en `docs/CONTINUIDAD_VISUAL.md` y `maps/mansion.json`. El caso 001 conserva el plano anterior.

# Preparación de imágenes · mapa de la mansión

La estructura de la primera mansión será **4 columnas × 3 filas de habitaciones**, más una fila inferior de exterior: 12 interiores y 4 destinos exteriores. El tablero lógico sigue siendo 4×4. El exterior puede ser una imagen continua, pero cada uno de sus cuatro sectores admite una ficha y comparte las restricciones de fila y columna.

Esta primera escena está asignada al **caso 001**. Los otros tres casos mantienen su cuadrícula, con los mismos personajes arrastrables. Los nombres siguientes son provisionales y pueden cambiar al recibir las imágenes. El despacho debe continuar en D2 para mantener la coherencia del caso original.

## Formato común

- PNG, vista cenital ortogonal a 90°, sin perspectiva isométrica.
- Interiores: 256×256 píxeles cada uno, opacos y cuadrados.
- Misma paleta, escala del mobiliario, iluminación y grosor de paredes en todas las imágenes.
- Se recomienda un detalle equivalente a una base de 64×64 ampliada a 256×256 con vecino más próximo; evitar detalles diminutos que desaparezcan en el móvil.
- Sin nombres, letras, números, coordenadas, personajes ni interfaz dibujados. La app añade etiquetas y personajes por separado.
- Dejar una zona central suficientemente despejada para leer el personaje sobre el fondo.
- No añadir márgenes transparentes ni bordes exteriores decorativos a cada habitación.

La app utiliza muestreo sin suavizado. Las coordenadas pequeñas y los nombres se superponen para leer las pistas; las imágenes no deben incorporarlos.

## Habitaciones

Colocar los PNG en `app/src/main/assets/pixelart/rooms/`:

| Posición | Nombre provisional | Archivo |
|---|---|---|
| A1 | Vestíbulo | vestibulo.png |
| B1 | Biblioteca | biblioteca.png |
| C1 | Salón | salon.png |
| D1 | Comedor | comedor.png |
| A2 | Cocina | cocina.png |
| B2 | Galería | galeria.png |
| C2 | Sala de música | musica.png |
| D2 | Despacho | despacho.png |
| A3 | Dormitorio | dormitorio.png |
| B3 | Invernadero | invernadero.png |
| C3 | Sala de billar | billar.png |
| D3 | Archivo | archivo.png |

El orden y los nombres se configuran en `core/src/main/resources/cases/case001.json`, dentro de `scene.rooms`.

## Exterior continuo

Opción recomendada: **una imagen de 1024×256** en `app/src/main/assets/pixelart/exterior.png`. De izquierda a derecha:

1. A4: pista de tenis, píxeles X de 0 a 255.
2. B4: entrada de la mansión, X de 256 a 511.
3. C4: jardín, X de 512 a 767.
4. D4: fuente o patio, X de 768 a 1023.

Son cuatro zonas de igual anchura, sin rótulos ni divisores dibujados. La app superpone los cuatro destinos y una separación mínima. Todas las zonas exteriores comparten la fila 4: **solo se puede colocar un sospechoso en todo el exterior**, por la regla de una persona por fila. Si en el futuro se quieren varios fuera a la vez, habrá que rediseñar esa regla y los casos.

Alternativa: cuatro imágenes cuadradas en `pixelart/rooms/tenis.png`, `entrada.png`, `jardin.png` y `fuente.png`. Las imágenes individuales, si existen, se dibujan por encima de la franja continua.

## Personajes

- PNG transparente de 32×48 o 48×64, todos con el mismo tamaño base y estilo.
- Una sola pose de pie en esta entrega, reconocible desde arriba, sin fondo ni nombre.
- Proporciones, orientación, luz y escala coherentes entre personajes. Pies centrados en la base y transparencia alrededor de la silueta.
- Carpeta: `app/src/main/assets/pixelart/characters/`.
- Caso 001: `bruno.png`, `clara.png`, `damian.png`, `elias.png`.
- Caso 002: `ines.png`, `mateo.png`, `nora.png`, `oscar.png`.
- Caso 003: `ada.png`, `gael.png`, `lara.png`, `victor.png`.
- Caso 004: `alma.png`, `dario.png`, `irene.png`, `tomas.png`.

Los acentos del nombre visible se mantienen; los nombres de archivo son minúsculos sin acentos. No hace falta crear spritesheets ni animaciones todavía. Hasta tener los PNG, se muestran siluetas pixeladas provisionales y fondos de color.

## Texto para el chat de generación

«Crea una habitación de una mansión de misterio en pixelart original, vista cenital ortogonal estricta a 90°, sin perspectiva isométrica. PNG cuadrado de 256×256, con detalle equivalente a pixelart de 64×64 ampliado sin suavizado. Usa la misma paleta, escala de muebles, iluminación y grosor de paredes que las habitaciones de referencia. Sin texto, letras, números, personajes ni interfaz. Sin márgenes transparentes. Deja espacio central para superponer un personaje. Habitación: [NOMBRE].»

Para el exterior, sustituir por: «Franja exterior cenital de 1024×256, con cuatro sectores de igual anchura: pista de tenis, entrada de la mansión, jardín y fuente/patio; conectados visualmente mediante suelo o caminos, sin rótulos ni divisores dibujados».

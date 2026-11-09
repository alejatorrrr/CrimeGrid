# CrimeGrid · 0.4.0

Juego Android nativo de deducción espacial, Kotlin + Jetpack Compose. Cuatro investigaciones originales, sin anuncios, compras, permisos de red ni servicios externos durante el juego.

## Instalar o compilar

Requisitos de compilación: JDK 21 y Android SDK 35 con Build Tools 35. Android Studio puede instalar el SDK. Configura `ANDROID_HOME` o `sdk.dir` en `local.properties`, que no se incluye en la entrega.

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Compatible con Android 8.0 (API 26) o posterior. El APK está en `app/build/outputs/apk/debug/app-debug.apk`. La versión 0.4.0 tiene `versionCode=4`: puede actualizar la primera entrega conservando su progreso si se firma con la misma clave debug, como ocurre con el APK entregado en esta sesión.

En GitHub, el workflow **Android debug APK** ejecuta las pruebas y compila. Descarga `CrimeGrid-debug` en Artifacts. Puede iniciarse manualmente desde Actions y no necesita secretos; usa la firma debug estándar. La firma debug de una ejecución de CI distinta puede diferir de la del APK local.

## Integración gráfica definitiva · caso 004

El caso 004 muestra los 16 fondos originales aprobados según su `manifest.json`: Biblioteca, Despacho, Dormitorio, Baño; Sala de estar, Vestíbulo, Galería, Invernadero; Estrategia, Cocina, Bodega, Música; Tenis, Entrada, Jardín y Cabaña. Los cuatro personajes usan los spritesheets RGBA proporcionados, con animación por dirección, frame quieto y pies anclados al suelo.

La matriz de conexiones es independiente de las imágenes. Las rutas usan puertas y arcos; la fachada solo se atraviesa por B3↔B4 y los exteriores se conectan lateralmente. Un arrastre es una previsualización: al soltar, se comprueban el destino, la ruta y las restricciones de filas/columnas. Si se acepta, la animación sigue los portales. Un rechazo conserva el aviso rojo y la posición anterior. El control de dos toques sigue disponible.

Los fondos se muestran completos, con proporción original y sin interpolación. No se han retocado ni recortado los PNG aprobados. Los personajes son una capa independiente y ocupan el 34% de la altura de una habitación. Las etiquetas de coordenadas y nombres se superponen para conservar la lectura de las pistas.

**Conservación:** los cuatro JSON de casos, `Engine.kt` y `InvestigationStore.kt` son idénticos a la versión 0.3. El nuevo mapa se añade como un recurso independiente y se asocia al caso 004 al cargarlo. El caso 001 conserva su escena y su despacho D2; el caso 004 ya tenía su despacho en B1. Los otros doce sospechosos mantienen los gráficos anteriores porque no se han proporcionado sprites para ellos.

- [Informe de continuidad visual](docs/CONTINUIDAD_VISUAL.md): las 24 costuras, la entrada de fachada y las limitaciones del arte.
- [Captura nativa del caso 004](docs/art/CASO004.png).
- `core/src/main/resources/maps/mansion.json`: nombres, rutas de imagen, conexiones y puntos de entrada/salida.
- `app/src/main/assets/pixelart/mansion/`: fondos y manifiesto originales.
- `app/src/main/assets/pixelart/characters/`: cuatro spritesheets originales.
- `docs/art/SHA256_ORIGINALES.json`: integridad de los 20 PNG.
- `python scripts/audit_art.py`: auditoría reproducible, requiere Pillow; no modifica los PNG.

## Mapa y arrastre · tercera entrega

El caso 001 incorpora una escena configurable de doce habitaciones (tres filas de cuatro) y una franja inferior con cuatro zonas exteriores: tenis, entrada, jardín y fuente provisional. Continúa siendo un tablero lógico 4×4. Los otros casos conservan su cuadrícula.

Mantén pulsado un personaje del panel o uno ya colocado y arrástralo a otra habitación. El personaje sigue al dedo, el destino se resalta y los bordes de pantalla desplazan el contenido para llegar a las habitaciones fuera de vista. Al soltar se comprueba la regla de filas y columnas: un destino inválido parpadea en rojo y conserva la posición anterior. Soltar fuera del tablero o cancelar el gesto no mueve la ficha. El movimiento se guarda únicamente al aceptar el destino. Se conserva el control alternativo nombre → casilla.

Las cuatro zonas exteriores comparten la fila 4: solo puede haber un personaje en todo el exterior. Las coordenadas siguen visibles en pequeño para interpretar las pistas. Las imágenes y sus nombres no afectan al motor lógico.

**Histórico de 0.3:** en esa entrega no se habían suministrado imágenes finales. Se muestran fondos de color y siluetas pixeladas provisionales. La app está preparada para cargar PNG locales sin suavizado, sin red y con fallback si faltan. Consulta [ARTE_PIXELART.md](ARTE_PIXELART.md) para tamaños, plano, carpetas, nombres y un prompt para el chat de generación.

- `app/.../ui/RoomBoard.kt`: mapa con capas de fondo, personajes, etiquetas y feedback.
- `app/.../ui/DragController.kt`: gesto de pulsación larga, coordenadas globales y destino de la suelta.
- `app/.../ui/PixelArt.kt`: carga de PNG y siluetas temporales.
- `case001.json > scene`: 16 destinos visuales, nombres y rutas de imagen.

## Novedades de la segunda entrega

- **Colocación en dos toques:** toca el nombre y después la casilla. El nombre queda seleccionado para poder moverlo con otro toque. Los nombres están antes del tablero y una instrucción indica a quién estás colocando. No requiere confirmar el movimiento.
- **Rechazo visible:** una casilla inválida emite dos destellos rojos; un mensaje explica el conflicto. El tablero no cambia. Cada intento vuelve a activar la animación, incluso en la misma casilla. No se rechazan posiciones por contradecir una pista durante la partida: las pistas solo se evalúan al comprobar.
- **Selección de casos:** todos están disponibles desde el menú, con dificultad, estado Pendiente/En curso/Resuelto, fichas colocadas y ayudas usadas. Jugar continúa el último caso. Desde un resultado puedes abrir el siguiente.
- **Progreso independiente:** cada investigación conserva posiciones, sospechoso seleccionado, pistas marcadas como leídas, ayudas vistas y estado resuelto. Reiniciar afecta solo al caso activo y mantiene su distintivo de Resuelto si ya se completó.
- **Pistas revisadas:** marca las que ya has leído para organizar la deducción. Es una anotación personal, nunca una indicación automática de si se cumplen.
- **Tres ayudas opcionales por caso:** primero orientan hacia pistas relevantes, después deducen una fila y finalmente una columna de un solo personaje. Solo aparecen al pulsar Pedir ayuda y su uso se guarda. Cada deducción se verifica por búsqueda exhaustiva usando únicamente las pistas que cita.

Para retirar una ficha, selecciona su nombre y pulsa Retirar seleccionado. Reiniciar vacía el caso activo y restablece sus marcas de lectura y ayudas. Comprobar abre el resultado únicamente cuando la distribución completa satisface las reglas y las pistas.

## Casos

| Caso | Título | Escenario | Dificultad |
|---|---|---|---|
| 001 | La última cena | Mansión Valcárcel | Iniciación |
| 002 | El brindis interrumpido | Hotel Mirador | Intermedio |
| 003 | La función sin aplausos | Teatro del Albor | Difícil |
| 004 | El archivo de las sombras | Palacio de Niebla | Experto |

El caso original mantiene su contenido para conservar la primera entrega. Los tres nuevos no fijan filas ni columnas exactas: combinan relaciones, distancias y, en el experto, exclusiones. Las etiquetas de dificultad reflejan el diseño de las pistas y quedan abiertas a ajustar tras pruebas con jugadores.

## Arquitectura

- `core/.../data`: modelos serializables independientes de Android y Compose, y repositorio que comprueba la integridad y unicidad de cada caso.
- `core/.../domain`: reglas puras, transiciones inmutables del tablero, evaluación y solucionador por backtracking. Admite tableros 5×5 y 6×6.
- `core/src/main/resources/cases/case001.json` a `case004.json`: contenido, solución, dificultad y ayudas; ningún componente visual incrusta esos datos.
- `app/.../data`: guardado JSON local en SharedPreferences, con una entrada por caso. Lee el guardado anterior de `case001` sin borrar sus posiciones.
- `app/.../ui`: ViewModel MVVM con StateFlow, SavedStateHandle, pantallas Compose y feedback efímero para la animación de rechazo.

Las posiciones internas empiezan en cero; se presentan como columna A–D y fila 1–4. Las reglas incluyen fila/columna exacta, desplazamiento dirigido entre filas o columnas, orden relativo, distancia absoluta y exclusiones. El culpable se deduce del ocupante del despacho. La unicidad se comprueba por reglas, no comparando propuestas con la solución oficial.

Las rotaciones mantienen el ViewModel. SavedStateHandle recupera recreaciones y SharedPreferences conserva el progreso entre procesos. No hay datos personales ni cuentas.

## Verificación

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Las pruebas cubren el motor original, unicidad de todos los casos, nuevas reglas y ayudas demostrables, persistencia por caso, migración del guardado anterior, reinicio, ayudas limitadas, rechazo repetido de movimientos y recorridos de interfaz Compose con Robolectric. Informes en `core/build/reports/tests/test/`, `app/build/reports/tests/testDebugUnitTest/` y `app/build/reports/lint-results-debug.html`.

Consulta `VALIDACION.md` para los resultados concretos de esta entrega y `ARCHIVOS.md` para el inventario. La revisión manual en dispositivo debe incluir rotación, contraste de los destellos rojos, fuentes grandes y TalkBack.

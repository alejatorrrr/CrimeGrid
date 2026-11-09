# CrimeGrid 0.5 · validación

**BUILD SUCCESSFUL. 53 pruebas, cero fallos y cero errores.** Android Lint no presenta errores; mantiene cuatro avisos de versiones más recientes de dependencias. No se han añadido bibliotecas de ejecución. Pixelify Sans se empaqueta localmente con su licencia OFL.

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
python scripts/audit_art.py
python scripts/audit_continuity_05.py
```

## Pruebas

- Se ejecutan las 41 pruebas anteriores, incluidas unicidad, reglas, movimiento, persistencia, recursos RGBA, rutas y los recorridos de los casos anteriores.
- Dos pruebas nuevas de resolución demuestran que los pasos citan las pistas originales, que las reconstrucciones compatibles convergen a la solución única y que un tablero incorrecto no puede producir la explicación oficial.
- Tres pruebas nuevas de ViewModel verifican el ciclo Pendiente→Leída→Marcada→Pendiente, la recuperación de las marcas por caso, el reinicio y la independencia respecto a posiciones y victoria, y la deselección sin mover fichas.
- Una prueba de caché comprueba que los PNG y los anclajes de sprites se reutilizan por identidad.
- Seis recorridos nativos de 0.5 verifican el mapa cuadrado con 16 destinos adyacentes y sin texto de coordenadas/habitaciones; zoom sin movimiento; colocación con selección; arrastre desde el panel y dentro del zoom; rechazo por conflicto y cancelación fuera; cuaderno persistente; resolución con culpable, ubicación y explicación reales. Incluyen pantalla de 320×568 y bloqueo de toques hacia el mapa detrás del cuaderno.
- Capturas nativas Canvas/Skia de Robolectric API 28 en 393×852: `TABLERO.png`, `ZOOM.png`, `CUADERNO.png`, `RESOLUCION.png`. Las fichas del tablero de demostración son colocaciones de prueba, no una revelación de la solución.

## Conservación y migración

Se comparan con el commit 0.4 `7ba16f7` y permanecen idénticos los cuatro JSON de casos, `Engine.kt`, `Navigation.kt`, `InvestigationStore.kt` y `WalkingLayer.kt`. La carga y guardado de partidas mantiene las claves anteriores. `markedClues` es un campo opcional nuevo del estado, vacío por defecto: los guardados anteriores conservan posiciones y marcas leídas. No cambia la estructura de los casos JSON.

Los cuatro spritesheets y 16 fondos originales conservan sus hashes. Los sprites mantienen frames de 48×64, hojas de 192×256, cadencia de 120 ms, tarjetas de 24×36 dp, fantasma de arrastre de 48×64 dp y altura de personaje del 34 % de su habitación. El zoom aplica la cámara al conjunto, sin cambiar los sprites originales.

El atlas se crea una vez con los originales y seis parches pequeños. No hay un PNG gigante duplicado ni recortes de habitaciones; su dibujo único con muestreo de vecino más cercano evita separaciones por redondeos. Los destinos táctiles, actores y animaciones usan la misma cámara; la capa de caminar se recorta al viewport del mapa.

## APK

- Paquete: `com.crimegrid`.
- `versionCode`: 5; `versionName`: `0.5`.
- Salida: `app/build/outputs/apk/debug/app-debug.apk`.
- Entrega: `build/distributions/CrimeGrid-0.5-debug.apk`.
- SHA-256: `62e59ad4f0ae6793b65158bb48931dcd2acff51ceffe4188d0607fecae7b3478`.
- Firma debug: se verifica contra el APK 0.4 conservado. No se incluyen claves privadas en el proyecto.

## Problemas pendientes

No hay errores conocidos de pruebas o compilación. Las pruebas nativas no sustituyen una revisión física con distintos móviles, fuentes grandes y TalkBack.

La corrección artística es deliberadamente parcial: seis uniones reciben ajustes cromáticos acotados; dieciocho conservan sus píxeles originales. Persisten discrepancias geométricas de puertas, marcos, columnas y caminos, que no se han corregido destruyendo decoración. El jardín sigue sin un empalme geométrico perfecto. Véase `CONTINUIDAD.md`, con las 24 uniones y las excepciones previstas en el encargo. No se afirma que esas conexiones artísticas estén completamente resueltas.

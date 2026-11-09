# CrimeGrid 0.5

Juego Android nativo de deducción espacial: Kotlin, Jetpack Compose y MVVM con StateFlow. Cuatro casos locales; sin anuncios, compras, cuentas, permiso de Internet ni servicios externos durante el juego.

## Instalar y compilar

Requisitos: JDK 21, Android SDK 35 y Build Tools 35. Configura `ANDROID_HOME` o `sdk.dir` en `local.properties` (no versionado).

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Android 8.0 o posterior. `versionCode=5`, `versionName=0.5`. La entrega local conserva la firma debug de 0.4 y puede instalarse encima sin desinstalar. El APK generado por otro runner de GitHub puede tener una firma debug distinta.

## Caso 004 · El archivo de las sombras

La presentación se transforma en una investigación pixel art victoriana:

- Mansión cuadrada de 16 habitaciones, a todo el ancho útil, sin coordenadas, nombres, bordes ni separaciones dibujadas sobre el mapa. Las coordenadas se conservan en los datos y en las descripciones accesibles.
- Sin sospechoso seleccionado, un toque breve amplía una habitación y parte de sus vecinas. **Vista general** devuelve el mapa completo. El zoom no mueve fichas ni escribe posiciones.
- Con selección activa, tocar una habitación conserva la colocación. **Dejar selección** permite inspeccionar de nuevo. Mantener pulsado un personaje conserva el arrastre, con cancelación fuera del mapa y rechazo rojo de movimientos inválidos.
- Los sprites originales, su escala relativa, frames, direcciones y animaciones no cambian. El caminar confirmado sigue la matriz de puertas de 0.4. En zoom, sus capas usan la misma cámara y se recortan al viewport.
- Paneles de cuero/burdeos y bronce, botones de marco recto pixelado, iconos de lupa/pluma/documento y Pixelify Sans empaquetada con licencia OFL. No hay instrucciones contextuales permanentes; la ayuda está en el menú.
- **Cuaderno** abre el expediente y las pistas en pergamino desplazable. Cada pista cambia manualmente entre Pendiente, Leída y Marcada. Las marcas no verifican pistas ni modifican el motor. Se guardan por caso y el contador reúne las revisadas.
- El resultado muestra CASO RESUELTO, culpable y ubicación reales, la explicación JSON y pasos obtenidos al aplicar las pistas progresivamente al solucionador. Las cantidades de reconstrucciones y deducciones se calculan, no son narrativa inventada.

Los casos 001, 002 y 003 conservan sus pantallas de investigación y sus mecánicas. El menú compartido recibe la nueva apariencia sin cambiar la selección o el progreso de casos.

## Conservación del arte y continuidad

Los 20 PNG originales de habitaciones y personajes se conservan byte a byte. El atlas se compone en memoria con los 16 originales y seis parches pequeños. La caché de 16 MB evita decodificar repetidamente imágenes y anclajes. Un único dibujo de atlas con vecino más cercano evita líneas de redondeo entre tiles.

La edición artística es **parcial y conservadora**: seis costuras reciben correcciones cromáticas en bandas de ocho píxeles; se conserva el 99,21 % de los píxeles del mapa. Dieciocho uniones conservan sus originales. Persisten discontinuidades arquitectónicas y de caminos que no se han corregido de forma destructiva. No se sustituyen habitaciones, muebles o distribuciones. La conectividad lógica sigue separada del aspecto gráfico.

- [Informe de las 24 uniones y pendientes](docs/0.5/CONTINUIDAD.md).
- [Tablero completo](docs/0.5/TABLERO.png).
- [Habitación ampliada](docs/0.5/ZOOM.png).
- [Cuaderno](docs/0.5/CUADERNO.png).
- [Resolución](docs/0.5/RESOLUCION.png).

## Arquitectura

- `core/.../data`: modelos serializables y repositorio de casos.
- `core/.../domain`: `Engine` y `Navigation` originales; `Resolution` añade una explicación de solo lectura al terminar un caso 4×4.
- `core/src/main/resources/cases`: los cuatro JSON permanecen intactos, sin cambios de estructura, pistas o solución.
- `core/src/main/resources/maps/mansion.json`: matriz y portales independientes de las imágenes, sin cambios de 0.4.
- `app/.../data`: el mismo almacenamiento de progreso. `markedClues` es una extensión opcional del estado, vacía por defecto; los guardados de 0.4 conservan posiciones y marcas leídas.
- `app/.../ui`: pantallas anteriores conservadas, más `DetectiveScreens`, `MansionMap`, `Notebook`, `DetectiveTheme` y `PixelAssets` para la presentación nueva.
- `app/src/main/assets/pixelart/mansion/patches`: seis parches visuales y su manifiesto. No son datos de colisión.

El motor sigue admitiendo tableros 5×5 y 6×6. La explicación exhaustiva de resolución se limita expresamente a los casos 4×4 actuales para no generar trabajo ilimitado en el hilo de interfaz.

## Verificación y entrega

[Resultados de las 53 pruebas y compilación](docs/0.5/VALIDACION.md). Incluyen los 41 tests anteriores, cuaderno persistente, resolución derivada, caché, mapa sin texto/separaciones, zoom, arrastre ampliado, cancelación/rechazo y una pantalla pequeña de 320×568.

```sh
python scripts/audit_art.py
python scripts/audit_continuity_05.py
```

Estas auditorías opcionales requieren Pillow; no se usan en Android. Verifican originales, las 24 uniones, máscara de cambios y límites cromáticos. La propuesta generativa completa se descartó; `scripts/repair_seams_05.py <propuesta.png>` documenta el proceso conservador.

GitHub Actions ejecuta pruebas y genera el artefacto descargable **CrimeGrid-debug** en cada push o ejecución manual, sin secretos privados. Los informes están en **unit-test-results**. No se incluyen builds, cachés, SDK local ni claves en el repositorio.

Las versiones anteriores permanecen en el historial, sus APK de entrega se conservan y la documentación de 0.4 se archiva en `docs/0.4/`. Las capturas actuales provienen de renderizado nativo de Robolectric; queda revisión en dispositivos físicos, fuentes grandes y TalkBack.

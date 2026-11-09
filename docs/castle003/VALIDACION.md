# Validación · castillo 003 y visor

**BUILD SUCCESSFUL. 58 pruebas, cero fallos y cero errores.** Se ejecuta la batería anterior de 53 pruebas y se añaden una prueba lógica del 003 y cuatro recorridos nativos del castillo/visor. Android Lint mantiene cuatro avisos de versiones de dependencias y no presenta errores.

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
python scripts/audit_castle003.py
```

Las pruebas comprueban la igualdad píxel a píxel de los 16 recortes con el atlas, carga 384×384, mapa sin rótulos y sin separación, validación del manifiesto, pulsación prolongada sin selección, pellizco de dos dedos, desplazamiento, cierre sin modificar fichas, prioridad de arrastre sobre personaje, inspección de suelo ocupado, bloqueo con selección y colocación por toque. Se mantienen las pruebas del caso 004, cuaderno, zoom, cancelación, resolución y persistencia.

El recorrido antiguo de ayuda del 003 se adapta a su nueva pantalla (abrir el cuaderno), manteniendo todas las comprobaciones de ayuda y progreso. Pasa a renderizado nativo para validar sus sprites PNG reales en lugar de las antiguas siluetas simuladas. No se suprimen pruebas para ocultar fallos. Durante el desarrollo se corrigió el orden de lectura/consumo del movimiento en el reconocedor de gestos.

APK: `app/build/outputs/apk/debug/app-debug.apk`; paquete `com.crimegrid`, versionCode 6, versionName 0.5.1. SHA-256: `57030dd3c5c48735b7ebe6d29ea94ff8f8adc4a73fe6239f86bbf32939714403`.

La firma debug se contrasta con la entrega 0.5 para poder actualizar sin borrar la partida. Los datos lógicos del 003 y los casos 001, 002 y 004 se conservan; solo se adapta la narrativa del 003 al castillo. Los recursos de la mansión no se reemplazan. Véase `INTEGRACION.md` para las 24 uniones y los límites de verificación.

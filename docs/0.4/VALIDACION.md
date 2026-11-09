# Validación · CrimeGrid 0.4.0

Resultado local: **BUILD SUCCESSFUL**, 41 pruebas, cero fallos y cero errores. Android Lint: cero errores y cuatro avisos sobre versiones más recientes de dependencias. Firma APK v2 y alineación de 16 KB verificadas con las herramientas oficiales de Android.

```sh
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
python scripts/audit_art.py
```

## Comprobaciones

- 19 pruebas de motor/campaña/navegación: se mantienen las reglas, soluciones únicas, ayudas y cobertura original. La matriz es de 16×16, simétrica, con 21 conexiones bidireccionales; todos los nodos son alcanzables y los portales coinciden en coordenadas globales. No existen conexiones fuera del mapa ni cruces directos de las tres paredes de fachada cerradas.
- 8 pruebas originales de ViewModel/persistencia.
- 6 recorridos Compose originales, incluido arrastre y rechazo por fila/columna.
- 3 pruebas del controlador de arrastre.
- 3 pruebas de recursos aprobados: decodificación de los 16 fondos de 256×256, coordenadas y nombres exactos, cuatro exteriores en la fila 4, RGBA real en los cuatro spritesheets y transparencia/contenido en los 64 frames. Se verifican rutas de movimiento y conservación de la resolución de todos los casos.
- 2 recorridos nativos del caso 004: selección de sospechoso, render de los cuatro personajes y 16 habitaciones, y movimiento confirmado por puertas. Captura real de la vista dibujada con Canvas/Skia nativo de Robolectric en `docs/art/CASO004.png`.
- Auditoría independiente por Pillow: **20 PNG idénticos byte a byte** a los originales importados, dimensiones correctas, 64 frames con alfa, 24 uniones medidas. No hay frames exactamente duplicados.
- Comparación binaria con el commit base `4c419d6`: los cuatro JSON de casos, `Engine.kt` e `InvestigationStore.kt` permanecen idénticos. No se altera el formato del progreso guardado.

Las primeras comprobaciones detectaron un aviso de indentación de Lint y límites de Robolectric en modo gráfico simulado/PixelCopy. Se corrigió la indentación y se verificó la carga de píxeles y la captura mediante renderizado nativo; la ejecución final completa pasa sin fallos.

## Entrega

- APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Descarga de la sesión: `CrimeGrid-0.4-debug.apk`.
- Paquete `com.crimegrid`, versionCode 4, versionName 0.4.0, Android 8.0 o posterior.
- SHA-256 del APK: `56836f50b0fe31a0f5bbe430d075c04981e32a8d6d4ec9bc74fc4ecb7881c09e`.
- Repositorio de destino: https://github.com/alejatorrrr/CrimeGrid .
- El workflow de GitHub genera `CrimeGrid-debug` y los informes de pruebas al publicarse el proyecto.

## Pendientes de continuidad

El informe `docs/CONTINUIDAD_VISUAL.md` enumera las 24 uniones. Permanecen saltos de materiales, marcos, sombras y curvas de senderos; la puerta de la entrada exterior está pintada cerrada y no coincide exactamente con el umbral interior. No se han retocado los recursos aprobados. Los sprites tienen transparencia real, aunque el proceso de separación original puede dejar halos y variación de volumen. Las pruebas nativas no sustituyen una revisión en dispositivo físico de rutas largas, bordes, fuentes grandes y TalkBack.

## Primera ejecución de GitHub Actions

La ejecución inicial del commit de integración se detuvo antes de Gradle porque `android-actions/setup-android@v3` intentaba instalar el paquete retirado `tools`. Se fija el runner a Ubuntu 24.04 y se solicita explícitamente `platform-tools`. La compilación local del APK y las 41 pruebas no dependen de ese fallo de preparación remota.

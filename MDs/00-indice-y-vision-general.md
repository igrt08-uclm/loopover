# Loopover 4x4 engranado — Visión general del proyecto

> Documentación para la asignatura **Sistemas Inteligentes**.
> Generada a partir del análisis de `src/*.java`, `pom.xml` y la guía de modelado (imagen adjunta).
> **No se ha modificado ningún archivo del repositorio.**

## 1. ¿Qué problema resuelve el proyecto?

Se resuelve el puzzle **Loopover engranado 4x4** mediante **búsqueda en espacio de estados**:

- Tablero toroidal de 4x4 = 16 casillas **sin hueco** (los bordes opuestos están conectados).
- Cada acción desplaza **una fila** y, a continuación, **la columna de origen** en el mismo sentido (movimiento *encadenado/engranado*).
- Objetivo: llegar desde un estado arbitrario al estado ordenado `00 01 02 … 15`.

Los documentos de esta carpeta:

| Archivo | Contenido |
|---|---|
| `00-indice-y-vision-general.md` | Este documento: mapa del proyecto, reparto en 3 tareas, flujo de ejecución |
| `01-modelado-del-problema.md` | Formalización como problema de búsqueda (estado, operadores, objetivo, coste, tamaño del espacio) |
| `02-tarea1-estado-y-acciones.md` | Detalle de `Estado`, `Sucesor`: bitboard, codificación de acciones, desplazamientos |
| `03-interfaz-de-linea-de-comandos-y-construccion.md` | `LoopoverApp`, `ComandoVerificar`, `ComandoBusqueda`, `pom.xml` |
| `04-tarea2-motor-de-busqueda.md` | `Nodo`, `Frontera`, `Visitados`, `Busqueda`: estrategias no informadas e informadas |
| `05-tarea3-heuristicas-y-pbd.md` | `Heuristicas`, `PBD`, `HeuristicasPBD`, SMA* (A* acotada) |
| `06-observaciones-sobre-el-codigo-actual.md` | Hallazgos del análisis (errores y mejoras posibles), **sin aplicar** |

> Nota de método: las Tareas 2 y 3 están como *stubs* (`TODO`). Lo que se explica de ellas se **infiere de las firmas, nombres y comentarios** y de la teoría estándar de la asignatura; se indica expresamente cuando algo es una interpretación.

## 2. Mapa de archivos y estado de implementación

| Archivo | Rol | Tarea | Estado |
|---|---|---|---|
| `Estado.java` | Estado (bitboard de 64 bits), acciones, sucesores | 1 | Implementado |
| `Sucesor.java` | Tupla (acción, estado, coste) | 1 | Implementado |
| `ComandoVerificar.java` | Subcomando `verify` | 1 | Implementado |
| `LoopoverApp.java` | Punto de entrada (picocli) | — | Implementado (mensaje "Hola, mundo" provisional) |
| `ComandoBusqueda.java` | Subcomando `solve` (opciones ya definidas) | 2/3 | CLI hecho; depende de `Busqueda` y heurísticas |
| `Nodo.java` | Nodo del árbol de búsqueda | 2 | *Stub* |
| `Frontera.java` | Frontera (heap min-max) | 2 (+3) | *Stub* (`eliminarPeorHojaNoRaiz` es de la Tarea 3) |
| `Visitados.java` | Tabla de estados visitados | 2 | *Stub* |
| `Busqueda.java` | Motor y estrategias | 2 | *Stub* |
| `Heuristicas.java` | Manhattan toroidal, admisible, permutaciones | 3 | *Stub* (devuelven 0) |
| `PBD.java` | Pattern database de 8 piezas | 3 | *Stub* |
| `HeuristicasPBD.java` | Heurísticas sobre PBD (pares, impares, máx, suma) | 3 | *Stub* |
| `pom.xml` | Maven: Java 17, picocli, fastutil, guava, shade | — | Hecho |
| `dependency-reduced-pom.xml` | Generado por `maven-shade-plugin` | — | Artefacto de compilación (no editar) |

## 3. Las tres partes del proyecto

1. **Tarea 1 — Modelado (hecha).** Representación del estado, operadores y generación de sucesores. Se puede probar con `verify`.
2. **Tarea 2 — Motor de búsqueda.** Nodos, frontera, tabla de visitados y estrategias: profundidad, anchura, coste uniforme, voraz y A\*. Sin heurística real (`h = 0`).
3. **Tarea 3 — Heurísticas y memoria acotada.** Manhattan toroidal y variantes, pattern databases de 8 piezas, y **A\* acotada (SMA\*)** con eliminación del peor nodo hoja.

## 4. Flujo de ejecución previsto de `solve`

```
CLI (picocli)
  └─ ComandoBusqueda.call()
       ├─ new Estado(cadena32)            ← valida la entrada
       ├─ resolverHeuristica(opción)      ← Heuristicas::… | HeuristicasPBD
       ├─ new Busqueda(estado, estrategia, profMax, maxNodos, h, maxVisitados)
       └─ Busqueda.buscar()
            ├─ Frontera  (orden por f, g o h según estrategia)
            ├─ Visitados (poda de estados repetidos)
            ├─ Estado.sucesores() → 32 hijos por expansión
            └─ devuelve Nodo solución → caminoComoTexto()
```

## 5. Glosario rápido

| Término | Significado |
|---|---|
| Bitboard | Tablero comprimido en un `long`: 16 casillas × 4 bits |
| Acción `fc±` | Fila `f`, columna `c`, sentido `+` (derecha/abajo) o `-` (izquierda/arriba) |
| `g(n)` | Coste acumulado desde la raíz hasta `n` |
| `h(n)` | Estimación del coste restante hasta el objetivo |
| `f(n)` | Valor de evaluación (`g`, `h` o `g+h` según estrategia) |
| Admisible | `h(n) ≤ h*(n)`: nunca sobreestima |
| PBD | Base de datos de patrones (*Pattern DataBase*, `PBD` en el código) |
| SMA\* | A\* con memoria acotada (*Simplified Memory-bounded A\**) |

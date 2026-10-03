# 03 · Interfaz de línea de comandos y construcción

## 1. Estructura de comandos (picocli)

```
loopover                      ← LoopoverApp (comando raíz)
 ├─ verify                    ← ComandoVerificar
 └─ solve                     ← ComandoBusqueda
```

`LoopoverApp.main` crea el `CommandLine`, ejecuta y hace `System.exit(codigo)`. Sin subcomando imprime un saludo provisional (`Hola, mundo desde Loopover!`).

## 2. `verify` (`ComandoVerificar`)

| Opción | Descripción |
|---|---|
| `-s` (obligatoria) | Estado, 32 dígitos |
| `-a` | Lista de acciones separadas por comas, p. ej. `21+,03-` |

- Sin `-a`: imprime los 32 sucesores (`Sucesor.toString()`).
- Con `-a`: aplica las acciones en orden y muestra el estado final.
- Errores de formato (estado o acción) → mensaje en `stderr` y código de salida 1.

## 3. `solve` (`ComandoBusqueda`)

| Opción | Por defecto | Descripción |
|---|---|---|
| `-s` | — (obligatoria) | Estado inicial |
| `-e`, `--estrategia` | `A_ESTRELLA` | `PROFUNDIDAD`, `ANCHURA`, `COSTO_UNIFORME`, `VORAZ`, `A_ESTRELLA`, `A_ESTRELLA_ACOTADA` |
| `-p`, `--profundidad` | 1000 | Profundidad máxima |
| `-c`, `--capacidad` | — | Máx. nodos del árbol en memoria (obligatorio con `A_ESTRELLA_ACOTADA`; si no se da, se pasa 10 000 000) |
| `-h`, `--heuristica` | `MANHATTAN` | Heurística para A\* (ver tabla) |
| `-m`, `--max-visitados` | 0 (ilimitado) | Aborta al superar ese nº de estados visitados |
| `-v`, `--verbose` | — | Estadísticas: profundidad, nodos expandidos, estados visitados, tiempo |

Heurísticas seleccionables:

| Valor | Origen | Nota |
|---|---|---|
| `MANHATTAN` | `Heuristicas.heuristicaManhattanToroidal` | Por defecto |
| `CERO` | lambda `e -> 0` | A\* se comporta como búsqueda en anchura/coste uniforme |
| `MANHATTAN_ADMISIBLE` | `Heuristicas.heuristicaManhattanAdmisible` | |
| `PERMUTACIONES` | `Heuristicas.heuristicaPermutaciones` | |
| `PARES_8`, `IMPARES_8` | `HeuristicasPBD` | PBD de 8 fichas pares / impares |
| `PBD_8` | `HeuristicasPBD` | Máximo de ambas (admisible) |
| `PBD_8_SUMA` | `HeuristicasPBD` | Suma de ambas (**no** admisible; no garantiza óptimo) |

### Flujo de `call()`

1. Construye `Estado` (si falla → "Error en el estado").
2. Si ya está resuelto → imprime `(ya resuelto)`.
3. Valida `A_ESTRELLA_ACOTADA` ⇒ requiere `-c`.
4. Resuelve la heurística y crea `Busqueda(estado, estrategia, profMax, capacidad, h, maxVisitados)`.
5. `buscar()`; si devuelve `null`, distingue entre "límite de visitados alcanzado" y "sin solución dentro de la profundidad".
6. Imprime `solucion.caminoComoTexto()` y, con `-v`, las estadísticas.

## 4. Construcción (Maven)

- **Java 17**, `sourceDirectory = src` (los `.java` están directamente en `src/`, sin paquetes).
- Dependencias:
  - `picocli 4.7.7` — CLI.
  - `fastutil 8.5.16` — colecciones primitivas (el `Nodo` ya importa `ObjectList`; previsible su uso en `Visitados` para mapas `long → float`).
  - `guava 33.7.1-jre` — utilidades (p. ej. un heap min-max en `Frontera`; ver documento 04).
- `maven-shade-plugin 3.6.1`: genera un *fat jar* `target/loopover.jar` con `Main-Class = LoopoverApp` (propiedad `main.class`).
- `dependency-reduced-pom.xml` lo genera el propio shade plugin; no se edita a mano.

Comandos habituales:

```
mvn clean package
java -jar target/loopover.jar verify -s 00010203040506070809101112131415
java -jar target/loopover.jar solve  -s <32 dígitos> -e A_ESTRELLA -h MANHATTAN -v
```

## 5. Recursos esperados (Tarea 3)

`HeuristicasPBD` declara `/pdb8.dat` y `/pdb8_impares.dat` como recursos del classpath (`PBD.desdeRecurso`). Estos ficheros binarios tendrían que estar en un directorio de recursos empaquetado en el jar; actualmente `pom.xml` solo define `sourceDirectory`, por lo que habrá que configurar `src/main/resources` o un `<resources>` adecuado cuando se genere la PBD.

# 04 · Tarea 2 — Motor de búsqueda

Archivos: `Nodo.java`, `Frontera.java`, `Visitados.java`, `Busqueda.java` (hoy son *stubs* con `TODO: Tarea 2`).

> **Aviso:** lo que sigue es el **diseño previsto**, deducido de las firmas, los nombres de métodos, los comentarios y la teoría de la asignatura. Los detalles internos (estructuras de datos concretas, criterios de desempate) se fijarán al implementar.

## 1. Esquema general (búsqueda en grafo)

```
frontera ← { Nodo(estadoInicial) }
mientras frontera no vacía:
    n ← frontera.extraerMejor()
    si n.estado es objetivo → devolver n
    si n.profundidad < profundidadMaxima:
        para cada sucesor s de n.estado.sucesores():
            hijo ← Nodo(n, s.accion, s.costo)
            si visitados.registrarSiMejor(hijo.estado.bitboard, hijo.costoAcumulado):
                hijo.fijarHeuristica(h(hijo.estado))      // si procede
                frontera.insertar(hijo)
devolver null   // sin solución en el límite, o límite de visitados alcanzado
```

La única diferencia entre estrategias es **el orden en que la frontera entrega los nodos**.

## 2. Estrategias de `Busqueda.Estrategia`

| Estrategia | Orden de extracción | Completa | Óptima | Notas |
|---|---|---|---|---|
| `PROFUNDIDAD` | Último en entrar (LIFO) | Solo con límite de profundidad y control de repetidos | No | Usa `profundidadMaxima`; con b = 32 explora ramas enormes |
| `ANCHURA` | Primero en entrar (FIFO) | Sí | Sí (coste = nº de acciones) | Memoria O(32^d): inviable más allá de profundidades pequeñas |
| `COSTO_UNIFORME` | Menor `g` | Sí | Sí | Con costes todos 1.0 equivale a anchura |
| `VORAZ` | Menor `h` | No (en general) | No | Rápida, pero puede dar caminos largos |
| `A_ESTRELLA` | Menor `f = g + h` | Sí | Sí si `h` es admisible (y consistente con control de repetidos) | `h` viene del parámetro `ToIntFunction<Estado>` |
| `A_ESTRELLA_ACOTADA` | Menor `f`, con tope de nodos en memoria | Sí si cabe el camino solución | Sí con `h` admisible | SMA\*; ver documento 05 |

Las estrategias no informadas (Tarea 2) funcionan sin heurística real: `Heuristicas` devuelve 0 deliberadamente (comentario del código).

## 3. Clases del motor

### 3.1 `Nodo`
Nodo del árbol; implementa `Comparable<Nodo>` para que la frontera lo ordene.

| Elemento | Interpretación |
|---|---|
| `Nodo(Estado)` | Raíz |
| `Nodo(padre, accion)` / `Nodo(padre, accion, costoAccion)` | Hijo: aplica la acción al estado del padre; coste de acción por defecto presumiblemente 1 |
| `estado()`, `padre()`, `accion()` | Datos del nodo y enlace al padre |
| `profundidad()`, `costoAcumulado()` | `profundidad = padre+1`; `g = padre.g + costoAccion` (`float`) |
| `fijarHeuristica(h)` / `valor()` / `fijarValor(v)` | Guardan `h` y el valor de evaluación `f` (tipo `int`) |
| `esRaiz()` | Sin padre |
| `camino()` / `caminoComoTexto()` | Reconstruye la lista de nodos raíz→solución (`ObjectList<Nodo>` de fastutil) y la secuencia de acciones en texto |
| `equals`/`hashCode` | Previsiblemente basados en el estado |
| `restarHijo`, `reexpandir`, `marcarExpandido`, `completado`, `agotado`, `respaldarMinimo` | Maquinaria de **SMA\*** (ver 4) |

Observación de tipos: el coste `g` es `float` pero `valor()`/`fijarValor` y la heurística son `int`. Con costes unitarios no hay pérdida, pero conviene decidir cómo se combina `g + h` al implementarlo.

### 3.2 `Frontera`
"Heap min-max" (comentario del código): una **cola de prioridad de doble extremo**.

| Método | Función |
|---|---|
| `Frontera(maxNodos)` | Capacidad máxima |
| `insertar`, `extraerMejor` | Extremo mínimo (mejor `f`): la expansión normal |
| `contiene(nodo)` | Detección de duplicados en frontera |
| `tamano()`, `vacia()` | Estado |
| `eliminarPeorHojaNoRaiz()` | **Tarea 3**: extremo máximo (peor `f`) para SMA\* |

La razón de un min-max heap es poder extraer el mejor nodo **y** descartar el peor en O(log n) cuando se llena la memoria. (Guava incluye `MinMaxPriorityQueue`, que encaja con este rol, y es la dependencia declarada en `pom.xml`; la implementación concreta queda a elección.)

### 3.3 `Visitados`
`registrarSiMejor(long estado, float valor)` + `tamano()`.

- Mapa **estado (bitboard `long`) → mejor `g` conocido**.
- Devuelve `true` si el estado es nuevo **o** se alcanza con menor coste que antes (hay que reinsertarlo); `false` si ya se llegó igual o mejor → poda.
- Con fastutil (`Long2FloatOpenHashMap`) se evita el *boxing* y se ahorra memoria; es crítico, porque el número de estados visitados crece muy rápido.

### 3.4 `Busqueda`
Constructores progresivos (cada uno añade un parámetro):

1. `(estadoInicial, estrategia, profundidadMaxima)`
2. `+ maxNodosArbol` (para SMA\*)
3. `+ heuristica` (`ToIntFunction<Estado>`)
4. `+ maxVisitados` (0 = ilimitado)

Métodos: `buscar()` (devuelve el `Nodo` solución o `null`), `nodosExpandidos()`, `estadosVisitados()`, `tiempoMs()`, `limiteVisitadosAlcanzado()`.

`maxVisitados` es una válvula de seguridad: al alcanzarlo, `buscar()` devuelve `null` y la CLI informa de que se abortó por memoria, no por falta de solución.

## 4. SMA\* y los métodos de `Nodo` (interpretación)

En SMA\* (A\* con memoria acotada), cuando el árbol alcanza `maxNodosArbol`:

1. Se **elimina la peor hoja** (mayor `f`) → `Frontera.eliminarPeorHojaNoRaiz()`.
2. Su valor `f` se **respalda en el padre** (`respaldarMinimo`) para recordar el mejor coste alcanzable por esa rama olvidada; el padre pierde un hijo (`restarHijo`) y puede volver a ser hoja candidata.
3. Un nodo cuyos hijos están todos en memoria está **completado** (`completado`) y su `f` pasa a ser el mínimo de los hijos.
4. Un nodo sin más sucesores por generar está **agotado** (`agotado`); `marcarExpandido` y `reexpandir` controlan la regeneración de hijos olvidados cuando esa rama vuelve a ser la más prometedora.

Esta correspondencia es una lectura razonable de los nombres, no una especificación del código actual.

## 5. Complejidad y límites prácticos (b = 32)

| Profundidad d | Nodos 32^d aprox. |
|---|---|
| 3 | 3,3 × 10⁴ |
| 4 | 1,0 × 10⁶ |
| 5 | 3,4 × 10⁷ |
| 6 | 1,1 × 10⁹ |

Por eso `-m` (límite de visitados) y `-c` (capacidad) existen en la CLI, y por eso las heurísticas informadas de la Tarea 3 son el centro del proyecto.

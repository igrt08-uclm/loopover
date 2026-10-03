# 05 · Tarea 3 — Heurísticas, PBD y A\* acotada

Archivos: `Heuristicas.java`, `PBD.java`, `HeuristicasPBD.java` y, de `Frontera`, el método `eliminarPeorHojaNoRaiz()`. Hoy son *stubs*; `Heuristicas` devuelve 0 para que la Tarea 2 funcione sin heurística.

> Como en el documento 04, el contenido describe el **diseño previsto** inferido de nombres y firmas; se señala lo que es interpretación.

## 1. Repaso teórico

- **Admisible:** `h(n) ≤ h*(n)` para todo `n` (nunca sobreestima el coste real restante). A\* con `h` admisible devuelve soluciones óptimas.
- **Consistente:** `h(n) ≤ c(n,n') + h(n')`. Implica admisible y permite no reabrir nodos.
- **Dominancia:** si `h₂(n) ≥ h₁(n)` para todo `n` (ambas admisibles), `h₂` expande menos nodos.
- Si `h₁` y `h₂` son admisibles, `max(h₁, h₂)` también lo es y domina a ambas.

## 2. Heurísticas geométricas (`Heuristicas`)

| Método | Idea |
|---|---|
| `heuristicaManhattanToroidal` | Suma, para cada ficha, de su distancia al destino en el toro |
| `heuristicaManhattanAdmisible` | Variante corregida para que no sobreestime |
| `heuristicaPermutaciones` | Heurística basada en la estructura de la permutación (nombre del método; criterio no definido en el código) |

### 2.1 Distancia toroidal
Para una ficha en `(f,c)` con destino `(F,C)`:

```
dFila = |f - F|;   dFila = min(dFila, 4 - dFila)
dCol  = |c - C|;   dCol  = min(dCol,  4 - dCol)
distancia = dFila + dCol
```

El `min(d, 4-d)` refleja que, en un toro, se puede ir "por el otro lado". Con tamaño 4, la distancia por eje va de 0 a 2.

### 2.2 ¿Por qué la suma Manhattan no es admisible aquí?
Una acción realiza **8 desplazamientos unitarios** (4 de la fila + 4 de la columna; la ficha de la intersección se mueve dos veces) y afecta a 7 casillas distintas (documento 01, apartado 4.3). Cada desplazamiento unitario reduce la suma como mucho en 1, así que una sola acción puede reducir la suma Manhattan hasta en 8, mientras que el coste de la acción es 1. Por tanto `Σ distancias` **puede sobreestimar** el coste real, y A\* con ella pierde la garantía de optimalidad.

Una corrección estándar (**interpretación**, a validar al implementar): dividir por 8 y redondear hacia arriba, `⌈Σ distancias / 8⌉`, que sí es admisible aunque poco informada. Otras opciones: tomar el máximo por filas/columnas, o apoyarse en PBD.

## 3. Pattern Databases (`PBD`)

Una PBD almacena el coste óptimo de resolver **solo un subconjunto de fichas** (el *patrón*), ignorando las demás. Es un problema relajado, por lo que el valor consultado es una cota inferior admisible del problema completo.

### 3.1 API de `PBD`

| Elemento | Función |
|---|---|
| `TAMANNO = 32 432 400` | Número de entradas de la tabla |
| `PBD(nombreFichero)` / `PBD(nombreFichero, minimo)` | Carga desde disco |
| `desdeRecurso(nombreRecurso, minimo)` | Carga desde el classpath (`/pdb8.dat`, `/pdb8_impares.dat`) |
| `valor(long tablero)` / `valor(Estado)` | Consulta: hash/índice del patrón → coste |
| `minimo()` | Valor `minimo` con el que se cargó |

El parámetro `minimo` no está documentado; su semántica exacta (por ejemplo, un suelo para el valor devuelto o un desplazamiento de almacenamiento) se fijará en la Tarea 3.

### 3.2 Tamaño de la tabla
`32 432 400 = 15 · 14 · 13 · 12 · 11 · 10 · 9 = 16·15·…·9 / 16`. Es decir, los arreglos ordenados de 8 fichas en 16 casillas (`16P8 = 518 918 400`) divididos entre 16. El código no explica el divisor; podría deberse a alguna normalización o simetría. **Hay que confirmarlo con el enunciado de la Tarea 3** antes de documentarlo como hecho. Si se almacena 1 byte por entrada, el fichero ocupa unos 32 MB.

### 3.3 Cómo se construiría (hipótesis de diseño)
1. Elegir las 8 fichas del patrón (las **pares** `0,2,4,…,14` y las **impares** `1,3,…,15`).
2. Recorrer en **anchura hacia atrás desde el objetivo**, registrando para cada colocación de esas 8 fichas el menor número de acciones.
3. Importante: como las acciones no son sus propias inversas (documento 01, apartado 4.1), la expansión "hacia atrás" debe aplicar los **operadores inversos** (`C(c,∓)` y luego `R(f,∓)`), no las 32 acciones directas.
4. Guardar en `pdb8.dat` / `pdb8_impares.dat` y empaquetar como recurso.

## 4. Heurísticas PBD (`HeuristicasPBD`)

| `Tipo` | Valor | Admisible |
|---|---|---|
| `PARES_8` | Consulta la PBD de las fichas pares | Sí |
| `IMPARES_8` | Consulta la PBD de las fichas impares | Sí |
| `PBD_8` | `max(pares, impares)` | Sí (y domina a ambas) |
| `PBD_8_SUMA` | `pares + impares` | **No** |

La **suma** no es admisible porque una misma acción mueve fichas de ambos grupos a la vez: el coste de esa acción se contaría dos veces. Es una *heurística no admisible* que suele expandir menos nodos pero no garantiza solución óptima; por eso la CLI la etiqueta como "admisible falso". Sumar solo sería válido con PBD **aditivas** (costes que cuentan únicamente las acciones que mueven fichas del patrón), algo que este diseño no hace.

`HeuristicasPBD`: el constructor carga ambos recursos (`IOException` si faltan); `funcion(Tipo)` devuelve un `ToIntFunction<Estado>` utilizable en `Busqueda`; y `valorPares/valorImpares/valorMaximo/valorSuma` son las consultas individuales.

## 5. A\* acotada (SMA\*) — Tarea 3

- Se activa con `-e A_ESTRELLA_ACOTADA -c <maxNodos>`.
- Cuando la frontera/árbol llega al máximo, `Frontera.eliminarPeorHojaNoRaiz()` retira la hoja de peor `f` (nunca la raíz) y se respalda su valor en el padre (ver documento 04, apartado 4).
- Permite resolver instancias que A\* estándar no podría por memoria, a costa de re-expandir nodos olvidados. Es completo si el camino solución cabe en la memoria asignada.

## 6. Qué heurística usar (guía de decisión)

| Objetivo | Opción recomendada |
|---|---|
| Solución **óptima** garantizada | `PBD_8` (o `MANHATTAN_ADMISIBLE`) con A\* |
| Rapidez, óptimo no imprescindible | `PBD_8_SUMA` o `MANHATTAN` |
| Comparar el efecto de la heurística | `CERO` frente a las demás, con `-v` (nodos expandidos, tiempo) |
| Poca memoria | `A_ESTRELLA_ACOTADA` con `-c` |

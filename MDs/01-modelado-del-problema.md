# 01 · Modelado del problema como búsqueda en espacio de estados

## 1. Descripción del puzzle (según la guía de la imagen)

- **Naturaleza toroidal 4x4:** matriz de 16 casillas sin espacios vacíos; el borde derecho se conecta con el izquierdo y el inferior con el superior.
- **Representación lineal:** cadena de **32 caracteres**, 2 dígitos por casilla (`00`–`15`), recorriendo las casillas por filas.
  Estado resuelto: `00010203040506070809101112131415`.
- **Identificación de celdas:** coordenadas `fc` desde `00` (arriba izquierda) hasta `33` (abajo derecha).
- **Codificación de acción:** `<fila><columna><dirección>`. Ejemplos de la guía: `02+` = fila 0, columna 2, derecha; `21-` = fila 2, columna 1, izquierda.
- **Mecánica engranada (dos pasos):**
  1. **Paso 1:** toda la **fila** indicada se desplaza circularmente (`+` derecha, `-` izquierda).
  2. **Paso 2:** la **columna original** (la de la acción) se desplaza en el sentido acoplado: **baja** si fue `+`, **sube** si fue `-`.

| Dirección inicial (fila) | Efecto secuencial (columna original) |
|---|---|
| `+` (derecha) | Desplazamiento hacia **abajo** |
| `-` (izquierda) | Desplazamiento hacia **arriba** |

## 2. Formalización (los 5 componentes clásicos)

| Componente | En el proyecto |
|---|---|
| **Estado** | Permutación de las 16 fichas sobre las 16 casillas. Código: `Estado` (bitboard `long`) |
| **Estado inicial** | Cualquiera que entre por `-s` (32 dígitos) |
| **Operadores / acciones** | 32 acciones: 4 filas × 4 columnas × 2 signos. Código: `Estado.ACCIONES[32]` |
| **Modelo de transición** | `Estado.aplicar(accion)` = desplazar fila, luego columna, ambas en el mismo sentido |
| **Test objetivo** | `Estado.esResuelto()`: bitboard `== 0xFEDCBA98_76543210L` |
| **Coste de camino** | 1 por acción (`Sucesor.costo = 1.0f`) → el coste del camino = número de acciones |

## 3. Tamaño del espacio de estados

- Configuraciones posibles (sin restricciones): 16! = 20 922 789 888 000 ≈ **2,09 × 10¹³**.
- Factor de ramificación: **b = 32** (constante, sin restricciones por estado).
- Un árbol sin poda crece como 32^d: a profundidad 5 ya hay ≈ 33,5 millones de nodos. Por eso son imprescindibles la **tabla de visitados** y las **heurísticas**.
- *Pendiente de analizar:* qué subconjunto de permutaciones es alcanzable con estas 32 acciones (paridad, invariantes) y el diámetro del grafo. El código no lo asume; conviene comprobarlo antes de afirmar que "todo estado tiene solución".

## 4. Propiedades del grafo de estados

### 4.1 Las acciones no son, en general, sus propias inversas

Sea `R(f,±)` el desplazamiento de fila y `C(c,±)` el de columna. La acción `fc+` es `C(c,+) ∘ R(f,+)` (primero fila, luego columna). Su deshacer exacto sería `R(f,-) ∘ C(c,-)` (primero columna hacia arriba, luego fila a la izquierda), mientras que la acción `fc-` es `C(c,-) ∘ R(f,-)` (primero fila, luego columna). **El orden es distinto**, y como la fila `f` y la columna `c` comparten la casilla `(f,c)`, las operaciones no conmutan.

Consecuencias:

- `fc-` **no** deshace `fc+` en general. El grafo es dirigido y no está garantizado que la inversa de una acción sea otra acción del conjunto.
- La **detección de repetidos** (`Visitados`) es esencial: no se puede podar simplemente "no aplicar la acción inversa del padre".
- Para construir una **PBD hacia atrás desde el objetivo** (Tarea 3) hay que usar los operadores inversos, no las 32 acciones tal cual (ver documento 05).

### 4.2 Ejemplo trabajado: `00+` desde el estado resuelto

Estado resuelto:

```
00 01 02 03
04 05 06 07
08 09 10 11
12 13 14 15
```

Paso 1, fila 0 a la derecha → `03 00 01 02`:

```
03 00 01 02
04 05 06 07
08 09 10 11
12 13 14 15
```

Paso 2, columna 0 hacia abajo (la columna contiene ahora `03,04,08,12` → `12,03,04,08`):

```
12 00 01 02
03 05 06 07
04 09 10 11
08 13 14 15
```

Cadena resultante: `12000102030506070409101108131415`.
Se puede comprobar con:

```
java -jar target/loopover.jar verify -s 00010203040506070809101112131415 -a 00+
```

### 4.3 Cuántas fichas mueve una acción

Una acción mueve 4 fichas de la fila + 4 de la columna, pero la casilla `(f,c)` pertenece a ambas: la ficha que ocupa esa casilla se desplaza dos veces (primero en horizontal, luego en vertical). Resultado: se alteran **7 casillas distintas** y hay **8 desplazamientos unitarios** en total. Este dato es la clave para razonar sobre la **admisibilidad** de la heurística Manhattan (documento 05).

## 5. Relación con los contenidos de Sistemas Inteligentes

| Concepto de teoría | Dónde aparece |
|---|---|
| Formulación de problemas de búsqueda | Este documento / `Estado` |
| Búsqueda no informada (profundidad, anchura, coste uniforme) | `Busqueda` (Tarea 2) |
| Búsqueda informada (voraz, A\*) | `Busqueda` + `Heuristicas` (Tareas 2–3) |
| Admisibilidad, consistencia, dominancia | Heurísticas y PBD (Tarea 3) |
| Búsqueda con memoria acotada (SMA\*) | `A_ESTRELLA_ACOTADA`, `Frontera`, `Nodo` (Tarea 3) |
| Control de estados repetidos | `Visitados` |

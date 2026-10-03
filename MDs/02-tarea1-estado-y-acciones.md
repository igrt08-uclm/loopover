# 02 · Tarea 1 — `Estado`, `Sucesor` y las acciones

Archivos: `Estado.java`, `Sucesor.java`.

## 1. Representación: bitboard de 64 bits

Cada casilla ocupa **4 bits** (un *nibble*, valores 0–15), y 16 casillas × 4 bits = 64 bits = un `long`.

- Casilla `i` (fila `i/4`, columna `i%4`) → bits `[4i, 4i+3]`.
- La casilla (0,0) está en los bits **menos** significativos.
- Estado resuelto: `0xFEDCBA98_76543210L` (la ficha `i` en la casilla `i`, de modo que el nibble 0 vale 0 y el nibble 15 vale `F`).

```
bits 63..60  59..56 ...   7..4    3..0
   casilla15 casilla14 ... casilla1 casilla0
      F         E     ...    1        0
```

Ventajas: copiar un estado es copiar 8 bytes, comparar es `==`, hashear es `Long.hashCode`, y mover una fila o columna son unas pocas operaciones de máscara y desplazamiento (O(1)). Además el `long` sirve directamente como clave de `Visitados` y como índice para PBD.

Constantes relevantes:

| Constante | Valor | Uso |
|---|---|---|
| `LADO` | 4 | Dimensión |
| `NUM_CASILLAS` | 16 | |
| `BITS_POR_CASILLA` | 4 | |
| `MASCARA_FICHA` | `0xF` | Aislar un nibble |
| `MASCARA_FILA_TABLERO` | `0xFFFF` | Una fila = 16 bits |
| `MASCARA_COLUMNA_TABLERO` | `0x000F000F000F000F` | Nibble de la columna 0 en cada una de las 4 filas |
| `NUM_ACCIONES` | 32 | |

> Detalle de diseño: el bitboard se guarda en un `long[1]` (campo `bitboard`), y hay un `TODO` preguntando si dejarlo como `long`. Un `long` simple `final` es más barato (sin objeto array ni indirección) y garantiza la inmutabilidad real; el array solo aporta mutabilidad que la clase no usa. Es una decisión estilística/de rendimiento, no de corrección.

## 2. Constructores

| Constructor | Entrada | Comportamiento |
|---|---|---|
| `Estado(long)` | Bitboard ya validado | Asignación directa |
| `Estado(String)` | 32 dígitos | Valida longitud y que sean dígitos; parsea 16 pares; compone el bitboard |
| `Estado(int[])` | 16 fichas | Pensado para validar longitud y delegar en `construirBitboard` |

Ver el documento 06 para los problemas detectados en las validaciones.

## 3. Codificación de acciones (5 bits)

```
bit 4      bits 3-2    bits 1-0
signo      columna     fila
(1 = '+')  (0..3)      (0..3)
```

Ejemplo: `21-` → fila 2 (`10`), columna 1 (`01`), signo `-` (0) → `0 01 10` = `00110` = 6.

`ACCIONES[]` se rellena en un bloque `static`: para la casilla `p = f*4 + c`:

- `ACCIONES[p]` = código con signo `+`
- `ACCIONES[16 + p]` = código con signo `-`

Utilidades de texto:

- `accionComoTexto(int)` → `"fc+"` / `"fc-"`.
- `accionDesde(String)` → valida longitud 3, fila y columna en `0..3`, signo `+`/`-`.

## 4. `aplicar(accion)` — el movimiento engranado

```java
f = accion & 0b00011;                 // fila
c = (accion & 0b01100) >>> 2;         // columna
positivo = (accion & 0b10000) != 0;   // signo
return new Estado(desplazarColumna(desplazarFila(bitboard, f, positivo), c, positivo));
```

Se aplica **primero la fila y después la columna**, y la columna se desplaza sobre el tablero **ya modificado** por la fila (tal como indica la guía: paso 1 fila, paso 2 columna original).

### 4.1 `desplazarFila`

- Máscara de la fila: `0xFFFF << (fila*16)`; la extrae con `bitboard & mascara`.
- **Derecha (`+`)**: cada ficha pasa a la columna siguiente (`<< 4`); la de la columna 3 vuelve a la 0 (`>>> 12`).
- **Izquierda (`-`)**: `>>> 4`, y la de la columna 0 pasa a la 3 (`<< 12`, con máscara de tope).
- Se recompone con `(bitboard & ~mascaraFila) | rotada`.

### 4.2 `desplazarColumna`

- Los 4 nibbles de una columna están separados 16 bits (una fila).
- **Abajo (`+`)**: `<< 16`; la de la fila 3 vuelve a la 0 (`>>> 48`).
- **Arriba (`-`)**: `>>> 16`; la de la fila 0 pasa a la 3 (`<< 48`, con máscara superior `0xF000… >>> (12 - 4*columna)`).

Ambas operaciones son O(1) y no modifican el estado original (se devuelve uno nuevo).

## 5. `sucesores()`

Recorre `ACCIONES[0..31]` y construye `new Sucesor(accion, aplicar(accion), 1.0f)`. Siempre devuelve **32** sucesores (algunos pueden coincidir en estado resultante; no se filtran).

`Sucesor` es una tupla inmutable `(accion, estado, costo)`; su `toString()` produce `(01+,<32 dígitos>,1.0)`.

## 6. Resto de la API de `Estado`

| Método | Descripción |
|---|---|
| `bitboard()` | Devuelve el `long` (clave para `Visitados` y PBD) |
| `ficha(casilla)` / `ficha(fila, col)` | `(bitboard >>> (4*casilla)) & 0xF`, con comprobación de rango |
| `esResuelto()` | Test objetivo |
| `equals`/`hashCode` | Por bitboard |
| `toString()` | 32 dígitos, 2 por casilla |

## 7. Cómo probar la Tarea 1

```
# Imprime los 32 sucesores del estado resuelto
java -jar target/loopover.jar verify -s 00010203040506070809101112131415

# Aplica una lista de acciones consecutivas
java -jar target/loopover.jar verify -s 00010203040506070809101112131415 -a 00+,21-
```

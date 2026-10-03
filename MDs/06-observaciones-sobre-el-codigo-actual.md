# 06 · Observaciones sobre el código actual

Hallazgos del análisis de `src/*.java`. **No se ha modificado ningún archivo**; son notas para que decidas si corregirlos. Están ordenadas por impacto.

## 1. Posibles errores

### 1.1 `Estado(int[])` descarta el bitboard construido
```java
public Estado(int[] fichas) {
    ...
    construirBitboard(fichas);   // el valor devuelto se ignora
}
```
`construirBitboard` es `static` y devuelve un `long`, pero el constructor no lo asigna a `this.bitboard[0]`. El estado queda en 0. Habría que hacer `this.bitboard[0] = construirBitboard(fichas);`.

### 1.2 Detección de fichas duplicadas incorrecta en `construirBitboard`
```java
} else if ((usedToken & (1 << fichas[i])) == 1) {
```
`usedToken & (1 << k)` vale `2^k`, no 1, salvo para `k = 0`. Solo se detecta duplicada la ficha 0. La condición correcta es `!= 0`.

### 1.3 `Estado(String)` no valida rango ni duplicados
Cada par de dígitos puede valer 0–99, pero un nibble solo admite 0–15. Un par como `16` desborda y activa bits de la casilla vecina; tampoco se detectan repetidos. Como `solve`/`verify` aceptan la cadena del usuario, un estado inválido puede generar resultados absurdos sin error. Podría reutilizar la validación de `construirBitboard` (una vez corregida).

### 1.4 Conflicto de la opción `-h` en `ComandoBusqueda`
`mixinStandardHelpOptions = true` registra `-h` y `--help`, y la opción de heurística también declara `-h`. Picocli debería rechazar la duplicidad al construir el modelo de comandos, lo que previsiblemente afectaría a toda la aplicación al arrancar. Conviene comprobarlo ejecutando el jar. Soluciones habituales: usar otra letra (`-H`) para la heurística, o quitar el mixin y definir la ayuda con otra opción.

### 1.5 `equals` lanza excepción con objetos que no son `Estado`
```java
} else {
    throw new IllegalArgumentException("Obj must be of Estado type");
}
```
El contrato de `equals` exige devolver `false` (también para `null` o tipos distintos). Lanzar una excepción puede romper colecciones (`HashMap`, `contains`…) cuando se comparen con otros tipos o con `null`.

## 2. Aspectos de diseño o robustez

- **Campo `long[] bitboard`:** el `TODO` pregunta si dejarlo como `long`. Un `long` simple `final` es preferible: inmutabilidad real, menos memoria por objeto y sin indirección. Nada en la clase necesita mutar el array (salvo el constructor `String`, que podría acumular en una variable local).
- **Mensajes de error en idiomas distintos:** `Estado` mezcla inglés (constructores) y español (`comprobarCasilla`, `validarAccion`). Es solo una cuestión de coherencia.
- **Bucles con `char`/`short` como contador** (`for (char i = 0; …)`): funcionan, pero `int` es más claro y evita conversiones.
- **`sucesores()` crea 32 `Estado` y 32 `Sucesor` por expansión:** correcto para la Tarea 1; en la Tarea 2 puede ser un cuello de botella de asignaciones. Una alternativa futura es trabajar con `long` y crear `Estado` solo cuando haga falta.

## 3. Comportamiento actual de la CLI (por los *stubs*)

- `solve` instancia `Busqueda`, cuyo constructor lanza `UnsupportedOperationException`. `ComandoBusqueda` solo captura `IllegalArgumentException` e `IOException`, así que hoy `solve` termina con una traza de excepción no controlada. Es esperable hasta completar la Tarea 2; se podría capturar `UnsupportedOperationException` mientras tanto.
- Igual ocurre con `PARES_8`, `IMPARES_8`, `PBD_8` y `PBD_8_SUMA`: `new HeuristicasPBD()` lanza `UnsupportedOperationException` hasta la Tarea 3.
- `LoopoverApp` sin subcomando imprime "Hola, mundo": es un resto de plantilla.
- Los recursos `pdb8.dat` y `pdb8_impares.dat` aún no aparecen declarados en el `pom.xml` (ver documento 03, apartado 5).

## 4. Qué está bien resuelto

- La aritmética de bits de `desplazarFila` y `desplazarColumna` es coherente con la codificación y las máscaras (incluida `mascaraSuperior` para el desplazamiento hacia arriba).
- `aplicar` respeta la regla engranada: fila primero, columna después sobre el tablero ya movido.
- La codificación de acciones en 5 bits y `ACCIONES[]` son consistentes con `accionComoTexto`/`accionDesde`.
- Separación clara por tareas, con `Heuristicas` devolviendo 0 para no bloquear la Tarea 2.

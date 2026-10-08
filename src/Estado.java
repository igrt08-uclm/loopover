import java.util.ArrayList;
import java.util.List;

/**
 * Estado del puzzle Loopover 4x4 representado como bitboard en un long sin
 * signo, junto con las acciones que lo transforman en otro estado.
 *
 * Cada casilla ocupa 4 bits: la casilla i (fila i/4, columna i%4) ocupa los
 * bits [i*4, i*4+3], quedando la casilla (0,0) en los bits mas bajos. El
 * estado resuelto vale 0xFEDCBA98_76543210L.
 *
 * Una accion es un movimiento encadenado codificado en 5 bits: los bits 0-1
 * son la fila, los bits 2-3 la columna y el bit 4 vale 1 si el signo es '+'.
 * Con '+' la fila se desplaza a la derecha y la columna hacia abajo; con '-',
 * hacia la izquierda y hacia arriba.
 *
 * TODO Tarea 1: implementar todos los metodos marcados con TODO.
 */
public final class Estado {

    public static final int LADO = 4;
    public static final int NUM_CASILLAS = LADO * LADO;
    public static final int BITS_POR_CASILLA = 4;
    public static final long MASCARA_FICHA = 15L;

    public static final int NUM_ACCIONES = 32;

    // El estado resuelto: ficha i en casilla i → nibble i = i
    private static final long BITBOARD_RESUELTO = 0xFEDCBA98_76543210L;

    // Bits de la codificacion de accion
    private static final int MASCARA_FILA_ACCION    = 0b00011;
    private static final int MASCARA_COLUMNA_ACCION = 0b01100;
    private static final int BIT_SIGNO_ACCION       = 0b10000;
    private static final int MASCARA_CODIGO_ACCION  = 0b11111;

    // Mascaras para operar filas y columnas del tablero
    private static final int  BITS_POR_FILA_TABLERO  = LADO * BITS_POR_CASILLA; // 16
    private static final long MASCARA_FILA_TABLERO   = 0xFFFFL;
    // Selecciona el nibble de columna 0 en cada una de las 4 filas
    private static final long MASCARA_COLUMNA_TABLERO = 0x000F000F000F000FL;

    /**
     * Tabla de 32 codigos de accion. Las primeras 16 entradas tienen signo '+',
     * las 16 siguientes signo '-'. Ya esta inicializada; no modificar.
     *
     * Formato de cada codigo (5 bits):
     *   bit  4  = signo (1 = '+')
     *   bits 3-2 = columna (0..3)
     *   bits 1-0 = fila    (0..3)
     */
    public static final int[] ACCIONES = new int[NUM_ACCIONES];

    static {
        for (int f = 0; f < LADO; f++) {
            for (int c = 0; c < LADO; c++) {
                int codigoBase = f | (c << 2);
                int posicion   = f * LADO + c;
                ACCIONES[posicion]               = codigoBase | BIT_SIGNO_ACCION; // signo '+'
                ACCIONES[NUM_CASILLAS + posicion] = codigoBase;                   // signo '-'
            }
        }
    }

    // TODO: Preguntar si quitar long y dejar como long a secas (array no) o dejarlo así como está.
    private long bitboard;

    // -------------------------------------------------------------------------
    // Constructores — TODO Tarea 1
    // -------------------------------------------------------------------------

    /** Construye el estado directamente desde el bitboard (ya validado). */
    public Estado(long bitboard) {
        this.bitboard = bitboard;
    }

    /**
     * Construye el estado desde una cadena de 32 digitos decimales donde cada
     * par representa la ficha de una casilla (casilla 0 = par 0..1, etc.).
     * Ejemplo: "00010203040506070809101112131415" es el estado resuelto.
     */
    public Estado(String representacion) {
        // TODO: validar longitud, parsear 16 pares de digitos, llamar a construirBitboard
        if (representacion.length() != 32) {
            throw new IllegalArgumentException("Representation must be of length 32. 2 digits per position.");
        } else {
            int[] fichas = new int[NUM_CASILLAS];
            for (short i = 0; i < 2*NUM_CASILLAS; i += 2) {
                char d = representacion.charAt(i);
                char u = representacion.charAt(i+1);

                if (!esDigito(u) || !esDigito(d)) {
                    throw new IllegalArgumentException("All characters in the representation must be digits.");
                } else {
                    fichas[i >> 1] = (d - '0') * 10 + (u - '0');
                    
                    if (fichas[i >> 1] < 0 || fichas[i >> 1] >= NUM_CASILLAS) {
                        throw new IllegalArgumentException(String.format("Token numbers must be in [0, %d]", NUM_CASILLAS-1));
                    }

                    this.bitboard |= ((long) fichas[i >> 1]) << ((i >> 1) * BITS_POR_CASILLA);
                }
            }
        }
    }

    /**
     * Construye el estado desde un array de 16 fichas donde fichas[i] es la
     * ficha en la casilla i.
     */
    public Estado(int[] fichas) {
        // TODO: validar longitud, llamar a construirBitboard
        if (fichas.length != 16) {
            throw new IllegalArgumentException("The array of tokens must be of length 16.");
        } else {
            this.bitboard = construirBitboard(fichas);
        }
    }

    // -------------------------------------------------------------------------
    // Consultas — TODO Tarea 1
    // -------------------------------------------------------------------------

    /** Devuelve el bitboard interno (necesario para la tabla de visitados). */
    public long bitboard() {
        // TODO: return bitboard
        return bitboard;
    }

    /**
     * Devuelve la ficha (0..15) que se encuentra en la casilla i.
     * Casilla i = fila i/4, columna i%4.
     * Operacion: (bitboard >>> (i * BITS_POR_CASILLA)) & MASCARA_FICHA
     */
    public int ficha(int casilla) {
        // TODO
        comprobarCasilla(casilla);
        return (int) ((bitboard >>> (casilla * BITS_POR_CASILLA)) & MASCARA_FICHA);
    }

    /** Devuelve la ficha en la posicion (fila, columna). */
    public int ficha(int fila, int columna) {
        // TODO: delegar a ficha(fila * LADO + columna)
        return ficha(fila * LADO + columna);
    }

    /** Devuelve true si el bitboard es igual al estado resuelto. */
    public boolean esResuelto() {
        // TODO
        return bitboard == BITBOARD_RESUELTO;
    }   

    // -------------------------------------------------------------------------
    // Sucesores — TODO Tarea 1
    // -------------------------------------------------------------------------

    /**
     * Devuelve la lista de 32 sucesores: uno por cada accion en ACCIONES[].
     * Cada sucesor tiene: la accion aplicada, el estado resultante y costo 1.0f.
     */
    public List<Sucesor> sucesores() {
        // TODO: iterar ACCIONES[], crear new Sucesor(accion, aplicar(accion), 1.0f)
        List<Sucesor> succ = new ArrayList<>();
        for (char i = 0; i < NUM_ACCIONES; i++) {
            succ.add(new Sucesor(ACCIONES[i], aplicar(ACCIONES[i]), 1.0f));
        }

        return succ;
    }

    // -------------------------------------------------------------------------
    // Aplicar acciones — TODO Tarea 1
    // -------------------------------------------------------------------------

    /**
     * Aplica la accion y devuelve el nuevo estado sin modificar este.
     * 1. Extraer fila y columna del codigo (bits 0-1 y 2-3).
     * 2. Extraer signo del bit 4.
     * 3. desplazarFila(bitboard, fila, positivo)
     * 4. desplazarColumna(resultado_anterior, columna, positivo)
     */
    public Estado aplicar(int accion) {
        // TODO
        validarAccion(accion);
        int f = accion & MASCARA_FILA_ACCION, c = (accion & MASCARA_COLUMNA_ACCION) >>> 2;
        boolean positivo = (accion & BIT_SIGNO_ACCION) != 0;

        return new Estado(desplazarColumna(desplazarFila(this.bitboard, f, positivo), c, positivo));
    }

    // -------------------------------------------------------------------------
    // Utilidades de acciones — TODO Tarea 1
    // -------------------------------------------------------------------------

    /**
     * Convierte el codigo de accion en texto legible, por ejemplo "01+" o "33-".
     * Formato: digito_fila + digito_columna + signo.
     */
    public static String accionComoTexto(int accion) {
        return (accion & MASCARA_FILA_ACCION) + "" + ((accion & MASCARA_COLUMNA_ACCION) >>> 2) + "" + ((accion & BIT_SIGNO_ACCION) != 0 ? '+' : '-');
    }

    /**
     * Parsea una accion en formato texto ("01+", "33-", etc.) y devuelve su codigo.
     * Validar: longitud = 3, primer caracter = digito fila 0-3,
     *          segundo = digito columna 0-3, tercero = '+' o '-'.
     */
    public static int accionDesde(String representacion) {
        // TODO
        int accion = 0;

        if (representacion.length() != 3) {
            throw new IllegalArgumentException("Representation of an action must have length 3.");
        } else {
            char f = representacion.charAt(0), c = representacion.charAt(1), s = representacion.charAt(2);

            if (!esDigito(f) || !esDigito(c) || (s != '+' && s != '-') || c < '0' || c > '3' || f < '0' || f > '3') {
                throw new IllegalArgumentException("Invalid characters used in action. Format is fc+ or fc- where f and c are digits from 0 to 3.");
            }

            accion = (f - '0') + ((c - '0') << 2) + ((s == '+' ? 1 : 0) << 4);
        }

        return accion;
    }

    // -------------------------------------------------------------------------
    // equals, hashCode, toString — TODO Tarea 1
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        // TODO: comparar bitboard con instanceof Estado
        boolean isEqual = false;
        if (obj instanceof Estado e) {
            isEqual = e.bitboard() == this.bitboard;
        } else {
            throw new IllegalArgumentException("Obj must be of Estado type");
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(bitboard);
    }

    /**
     * Representacion de 32 digitos: cada casilla como dos digitos decimales,
     * empezando por la casilla 0.
     * Ejemplo del estado resuelto: "00010203040506070809101112131415"
     */
    @Override
    public String toString() {
        // TODO: StringBuilder, para cada casilla i → ficha(i) con cero inicial si < 10
        StringBuilder sb = new StringBuilder();
        for (char i = 0; i < NUM_CASILLAS; i++) {
            int f = ficha(i);
            if (f < 10) {
                sb.append("0");
                sb.append(f);
            } else {
                sb.append(f);
            }
        }

        return sb.toString();
    }

    // =========================================================================
    // Metodos privados de ayuda — TODO Tarea 1
    // =========================================================================

    /**
     * Construye el bitboard a partir del array de fichas.
     * Para cada casilla i: resultado |= ((long) fichas[i]) << (i * BITS_POR_CASILLA)
     * Validar: sin duplicados, cada ficha en [0, NUM_CASILLAS).
     */
    private static long construirBitboard(int[] fichas) {
        // TODO
        long bitboard = 0;
        int usedToken = 0;
        for (char i = 0; i < 16; i++) {
            if (fichas[i] < 0 || fichas[i] >= NUM_CASILLAS) {
                throw new IllegalArgumentException(String.format("Token numbers must be in [0, %d]", NUM_CASILLAS-1));
            } else if ((usedToken & (1 << fichas[i])) != 0) {
                throw new IllegalArgumentException("No repeated tokens are allowed");
            } else {
                bitboard |= ((long) fichas[i]) << (i * BITS_POR_CASILLA);
                usedToken |= 1 << fichas[i];
            }

        }

        return bitboard;
    }

    /**
     * Desplaza la fila indicada del bitboard una posicion a la derecha (positivo=true)
     * o a la izquierda (positivo=false), con retorno circular.
     *
     * La fila ocupa 16 bits a partir del bit (fila * 16).
     * Mascara de la fila:    MASCARA_FILA_TABLERO << (fila * 16)
     * Mascara del nibble de retorno: MASCARA_FICHA << (fila * 16)
     *
     * Desplazamiento derecha (positivo):
     *   rotada = ((extraida << 4) & mascaraFila) | ((extraida >>> 12) & mascaraRetorno)
     *
     * Desplazamiento izquierda (negativo):
     *   mascaraTope = mascaraRetorno << 12
     *   rotada = ((extraida >>> 4) & mascaraFila) | ((extraida << 12) & mascaraTope)
     */
    private static long desplazarFila(long bitboard, int fila, boolean positivo) {
        // TODO
        long rotada;
        long mascaraFila = MASCARA_FILA_TABLERO << (fila * BITS_POR_FILA_TABLERO);
        long  mascaraRetorno = MASCARA_FICHA << (fila * BITS_POR_FILA_TABLERO);
        long extraida = bitboard & mascaraFila;

        if (positivo) {
            rotada = ((extraida << 4) & mascaraFila) | ((extraida >>> 12) & mascaraRetorno);
        } else {
            long mascaraTope = mascaraRetorno << 12;
            rotada = ((extraida >>> 4) & mascaraFila) | ((extraida << 12) & mascaraTope);
        }

        rotada = (bitboard & ~mascaraFila) | rotada;

        return rotada;
    }

    /**
     * Desplaza la columna indicada del bitboard una posicion hacia abajo (positivo=true)
     * o hacia arriba (positivo=false), con retorno circular.
     *
     * La columna ocupa 4 nibbles separados 16 bits entre si.
     * Mascara de la columna: MASCARA_COLUMNA_TABLERO << (columna * BITS_POR_CASILLA)
     * Mascara del nibble de retorno: MASCARA_FICHA << (columna * BITS_POR_CASILLA)
     *
     * Desplazamiento abajo (positivo):
     *   rotada = ((extraida << 16) & mascaraColumna) | ((extraida >>> 48) & mascaraRetorno)
     *
     * Desplazamiento arriba (negativo):
     *   mascaraSuperior = 0xF000000000000000L >>> (12 - columna * 4)
     *   rotada = ((extraida >>> 16) & mascaraColumna) | ((extraida << 48) & mascaraSuperior)
     */
    private static long desplazarColumna(long bitboard, int columna, boolean positivo) {
        // TODO
        long rotada;
        long mascaraColumna = MASCARA_COLUMNA_TABLERO << (columna * BITS_POR_CASILLA);
        long mascaraRetorno = MASCARA_FICHA << (columna * BITS_POR_CASILLA);
        long extraida = bitboard & mascaraColumna;

        if (positivo) {
            rotada = ((extraida << 16) & mascaraColumna) | ((extraida >>> 48) & mascaraRetorno);
        } else {
            long mascaraSuperior = 0xF000000000000000L >>> (12 - columna * 4);
            rotada = ((extraida >>> 16) & mascaraColumna) | ((extraida << 48) & mascaraSuperior);
        }

        rotada = (bitboard & ~mascaraColumna) | rotada;

        return rotada;
    }

    private static void comprobarCasilla(int casilla) {
        if (casilla < 0 || casilla >= NUM_CASILLAS) {
            throw new IllegalArgumentException("Square out of range " + casilla);
        }
    }

    private static void validarAccion(int accion) {
        if (accion < 0 || accion > MASCARA_CODIGO_ACCION) {
            throw new IllegalArgumentException(
                    "Action must be between 0 and " + MASCARA_CODIGO_ACCION + ": " + accion);
        }
    }

    private static boolean esDigito(char caracter) {
        return caracter >= '0' && caracter <= '9';
    }
}

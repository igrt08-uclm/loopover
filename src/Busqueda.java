import java.util.function.LongToIntFunction;

/**
 * Busqueda en espacio de estados con estrategias configurables y poda por
 * estados visitados.
 *
 * El mapa de visitados almacena el mejor valor visto para cada bitboard.
 * Al extraer un nodo de la frontera se poda si su estado ya esta registrado
 * con un valor igual o mejor; en caso contrario se registra y se expanden
 * todos sus sucesores.
 *
 * TODO Tarea 2: implementar buscar() y asignarValor() para estrategias
 *              no informadas (PROFUNDIDAD, ANCHURA, COSTO_UNIFORME).
 * TODO Tarea 3: completar asignarValor() para VORAZ, A_ESTRELLA, A_ESTRELLA_ACOTADA
 *              e implementar hacerSitio() para SMA*.
 */
public final class Busqueda {

    public enum Estrategia {
        PROFUNDIDAD,
        ANCHURA,
        COSTO_UNIFORME,
        VORAZ,
        A_ESTRELLA,
        A_ESTRELLA_ACOTADA
    }

    private static final int MAX_NODOS_POR_DEFECTO = 10_000_000;

    private final Estado estadoInicial;
    private final Estrategia estrategia;
    private final int profundidadMaxima;
    private final int maxNodosArbol;
    private final LongToIntFunction heuristica;
    private final long maxVisitados;

    private int nodosEnArbol;
    private int nodosExpandidos;
    private int estadosVisitados;
    private long tiempoMs;
    private boolean limiteVisitadosAlcanzado;

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima) {
        this(estadoInicial, estrategia, profundidadMaxima, MAX_NODOS_POR_DEFECTO,
                Heuristicas::heuristicaManhattanToroidal, 0L);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol) {
        this(estadoInicial, estrategia, profundidadMaxima, maxNodosArbol,
                Heuristicas::heuristicaManhattanToroidal);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol, LongToIntFunction heuristica) {
        this(estadoInicial, estrategia, profundidadMaxima, maxNodosArbol, heuristica, 0L);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol, LongToIntFunction heuristica, long maxVisitados) {
        if (estadoInicial == null) throw new IllegalArgumentException("El estado inicial no puede ser nulo");
        if (estrategia == null)    throw new IllegalArgumentException("La estrategia no puede ser nula");
        if (profundidadMaxima < 0) throw new IllegalArgumentException("La profundidad maxima debe ser >= 0: " + profundidadMaxima);
        if (estrategia == Estrategia.A_ESTRELLA_ACOTADA && maxNodosArbol < 1)
            throw new IllegalArgumentException("A* acotada requiere maximo de nodos del arbol >= 1");
        if (maxVisitados < 0) throw new IllegalArgumentException("El limite de visitados debe ser >= 0");
        this.estadoInicial    = estadoInicial;
        this.estrategia       = estrategia;
        this.profundidadMaxima = profundidadMaxima;
        this.maxNodosArbol    = maxNodosArbol;
        this.heuristica       = heuristica;
        this.maxVisitados     = maxVisitados;
    }

    /**
     * Ejecuta la busqueda y devuelve el nodo solucion, o null si no se encontro.
     */
    public Nodo buscar() {
        long inicio = System.currentTimeMillis();
        nodosEnArbol = 0;
        nodosExpandidos = 0;
        estadosVisitados = 0;
        limiteVisitadosAlcanzado = false;

        Frontera frontera = new Frontera(maxNodosArbol);
        Visitados visitados = new Visitados();
        Nodo raiz = new Nodo(estadoInicial);
        asignarValor(raiz);

        try {
            visitados.registrarSiMejor(raiz.estado().bitboard(), raiz.costoAcumulado());
            estadosVisitados = visitados.tamano();
            frontera.insertar(raiz);
            nodosEnArbol = frontera.tamano();

            while (!frontera.vacia()) {
                Nodo actual = frontera.extraerMejor();
                nodosEnArbol = frontera.tamano();

                if (actual == null) {
                    break;
                }
                if (actual.estado().esResuelto()) {
                    return actual;
                }
                if (actual.profundidad() >= profundidadMaxima) {
                    continue;
                }

                nodosExpandidos++;
                for (Sucesor sucesor : actual.estado().sucesores()) {
                    int indiceAccion = indiceDeAccion(sucesor.accion());
                    Nodo hijo = new Nodo(actual, indiceAccion, sucesor.costo());

                    if (!visitados.registrarSiMejor(
                            hijo.estado().bitboard(), hijo.costoAcumulado())) {
                        continue;
                    }

                    estadosVisitados = visitados.tamano();
                    if (maxVisitados > 0 && estadosVisitados > maxVisitados) {
                        limiteVisitadosAlcanzado = true;
                        return null;
                    }

                    asignarValor(hijo);
                    frontera.insertar(hijo);
                    nodosEnArbol = frontera.tamano();
                }
            }

            return null;
        } finally {
            tiempoMs = System.currentTimeMillis() - inicio;
        }
    }

    private static int indiceDeAccion(int accion) {
        for (int i = 0; i < Estado.NUM_ACCIONES; i++) {
            if (Estado.ACCIONES[i] == accion) {
                return i;
            }
        }
        throw new IllegalArgumentException("Accion desconocida: " + accion);
    }

    public int nodosExpandidos() { return nodosExpandidos; }
    public int estadosVisitados() { return estadosVisitados; }
    public long tiempoMs() { return tiempoMs; }
    public boolean limiteVisitadosAlcanzado() { return limiteVisitadosAlcanzado; }

    /**
     * Asigna el valor de ordenamiento del nodo segun la estrategia.
     */
    private void asignarValor(Nodo nodo) {
        switch (estrategia) {
            case PROFUNDIDAD -> nodo.fijarValor(-nodo.profundidad());
            case ANCHURA        -> nodo.fijarValor(nodo.profundidad());
            case COSTO_UNIFORME -> nodo.fijarValor(nodo.costoAcumulado());
            case VORAZ, A_ESTRELLA, A_ESTRELLA_ACOTADA -> throw new UnsupportedOperationException("TODO: Tarea 3 - estrategia informada");
            default -> {}
        }
    }

    /**
     * Para SMA*: libera memoria expulsando una hoja descartable y devuelve true
     * si lo consiguio, false si no habia ninguna.
     *
     * TODO: implementar en Tarea 3.
     */
    private boolean hacerSitio(Frontera frontera) {
        throw new UnsupportedOperationException("TODO: Tarea 3 - hacerSitio()");
    }
}

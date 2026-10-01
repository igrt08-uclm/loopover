import java.util.function.ToIntFunction;

/** Motor de busqueda. TODO: implementar en Tarea 2. */
public final class Busqueda {

    public enum Estrategia {
        PROFUNDIDAD,
        ANCHURA,
        COSTO_UNIFORME,
        VORAZ,
        A_ESTRELLA,
        A_ESTRELLA_ACOTADA
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima) {
        throw new UnsupportedOperationException("TODO: Tarea 2");
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol) {
        throw new UnsupportedOperationException("TODO: Tarea 2");
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol, ToIntFunction<Estado> heuristica) {
        throw new UnsupportedOperationException("TODO: Tarea 2");
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
                    int maxNodosArbol, ToIntFunction<Estado> heuristica,
                    long maxVisitados) {
        throw new UnsupportedOperationException("TODO: Tarea 2");
    }

    public Nodo buscar() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public int nodosExpandidos() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public int estadosVisitados() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public long tiempoMs() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public boolean limiteVisitadosAlcanzado() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
}

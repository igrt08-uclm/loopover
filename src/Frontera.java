
import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Frontera de la busqueda, implementada como un heap minimax usando
 * MinMaxPriorityQueue de Guava.
 *
 * La cola minimax consulta el minimo y el maximo en O(1) (peekFirst/peekLast)
 * e inserta y extrae en O(log n) (add/pollFirst/pollLast).
 *
 * TODO Tarea 2: implementar todos los metodos marcados con TODO.
 * TODO Tarea 3: implementar eliminarPeorHojaNoRaiz() para SMA*.
 */

public final class Frontera {

    private final int maxNodos;
    private final MinMaxPriorityQueue<Nodo> monticulo;

    /**
     * Inicializar el MinMaxPriorityQueue acotando la reserva inicial.
     * Patron: MinMaxPriorityQueue
     *             .expectedSize(Math.min(maxNodos, 65536))
     *             .create()
     *
     * Ojo: expectedSize(n) NO es solo una pista, reserva de golpe un Object[n].
     * La busqueda usa 10 000 000 como limite cuando no se da -c, asi que pasarle
     * maxNodos tal cual reservaria ~40 MB antes de expandir un solo nodo. El tope
     * no limita la poblacion: el heap crece solo si hace falta.
     */
    public Frontera(int maxNodos) {
        if (maxNodos < 1) throw new IllegalArgumentException("maxNodos debe ser >= 1: " + maxNodos);
        this.maxNodos = maxNodos;
        this.monticulo = MinMaxPriorityQueue
                .expectedSize(Math.min(maxNodos, 65536))
                .create();
    }

    /** Numero de nodos actualmente en la frontera. */
    public int tamano() {
        return monticulo.size();
    }

    public boolean vacia() {
        return monticulo.isEmpty();
    }

    public boolean contiene(Nodo nodo) {
        // Se compara por identidad (n == nodo) recorriendo la cola: hay que
        // saber si esa referencia concreta esta dentro, no si hay otro nodo con
        // el mismo estado.
        return monticulo.contains(nodo);
    }

    public void insertar(Nodo nodo) {
        monticulo.add(nodo);
    }

    /** Extrae y devuelve el nodo de menor valor (el mas prometedor). */
    public Nodo extraerMejor() {
        return vacia() ? null : monticulo.pollFirst();
    }

    /**
     * Para SMA*: itera la cola buscando el nodo de mayor valor que no sea la raiz
     * y que no tenga hijos vivos (es decir, que sea una hoja de verdad). Lo elimina
     * de la cola y lo devuelve, o devuelve null si solo quedan la raiz y nodos
     * internos.
     *
     * El filtro por hijos vivos es lo que hace que la expulsion no destruya una
     * rama: un nodo con descendencia en el arbol sigue siendo un nodo interno,
     * aunque su valor sea el mas alto, y perderlo dejaria subarboles huerfanos.
     *
     * TODO: implementar en Tarea 3.
     */
    public Nodo eliminarPeorHojaNoRaiz() {
        throw new UnsupportedOperationException("TODO: Tarea 3");
    }
}

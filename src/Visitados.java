
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/**
 * Tabla de estados visitados que almacena el mejor valor (menor costo) obtenido
 * para cada estado visto. Evita re-expandir nodos con un costo igual o peor.
 *
 * Usa Long2IntOpenHashMap de fastutil: tabla hash primitiva sin boxing de
 * Long/Integer, mucho mas eficiente que HashMap<Long, Integer> para millones de estados.
 *
 * TODO Tarea 2: implementar todos los metodos.
 */
public final class Visitados {

    // TODO: declarar el campo del mapa
    private final Long2IntOpenHashMap mapa;

    public Visitados() {
        mapa = new Long2IntOpenHashMap();
    }

    /**
     * Intenta registrar un estado con un valor dado.
     * Devuelve true y actualiza el mapa si el estado es nuevo O si valor < valorAnterior.
     * Devuelve false si el mapa ya contiene el estado con un valor igual o mejor.
     *
     * Patron:
     *   int anterior = mapa.getOrDefault(estado, Integer.MAX_VALUE);
     *   if (Math.abs(valor) < anterior) { mapa.put(estado, Math.abs(valor)); return true; }
     *   return false;
     */
    public boolean registrarSiMejor(long estado, int valor) {
        if (Math.abs(valor) < mapa.getOrDefault(estado, Integer.MAX_VALUE)) {
            mapa.put(estado, Math.abs(valor));
            return true;
        }

        return false;
    }

    public int tamano() {
        return mapa.size();
    }
}

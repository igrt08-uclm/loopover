import it.unimi.dsi.fastutil.objects.ObjectList;

/** Nodo del arbol de busqueda. TODO: implementar en Tarea 2. */
public final class Nodo implements Comparable<Nodo> {

    public Nodo(Estado estado) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public Nodo(Nodo padre, int accion) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public Nodo(Nodo padre, int accion, float costoAccion) { throw new UnsupportedOperationException("TODO: Tarea 2"); }

    public Estado estado() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public Nodo padre() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public int accion() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public int profundidad() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public float costoAcumulado() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void fijarHeuristica(int h) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public int valor() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void fijarValor(int v) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void restarHijo() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void reexpandir() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void marcarExpandido() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public boolean completado() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public boolean agotado() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public void respaldarMinimo(int v) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public boolean esRaiz() { throw new UnsupportedOperationException("TODO: Tarea 2"); }

    @Override
    public int compareTo(Nodo otro) { throw new UnsupportedOperationException("TODO: Tarea 2"); }

    public ObjectList<Nodo> camino() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    public String caminoComoTexto() { throw new UnsupportedOperationException("TODO: Tarea 2"); }

    @Override public boolean equals(Object o) { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    @Override public int hashCode() { throw new UnsupportedOperationException("TODO: Tarea 2"); }
    @Override public String toString() { return "Nodo[stub]"; }
}

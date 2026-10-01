/**
 * Tupla inmutable que representa un nodo sucesor en el espacio de estados:
 * la accion que se aplico, el estado resultante y el costo de esa transicion.
 */
public final class Sucesor {

    private final int accion;
    private final Estado estado;
    private final float costo;

    public Sucesor(int accion, Estado estado, float costo) {
        this.accion = accion;
        this.estado = estado;
        this.costo = costo;
    }

    public int accion() {
        return accion;
    }

    public Estado estado() {
        return estado;
    }

    public float costo() {
        return costo;
    }

    @Override
    public String toString() {
        return "(" + Estado.accionComoTexto(accion) + "," + estado + "," + costo + ')';
    }
}

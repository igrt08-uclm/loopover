import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Nodo del arbol de busqueda sobre el espacio de estados del puzzle Loopover.
 *
 * Cada nodo contiene un identificador unico creciente asignado al crearse, el
 * estado alcanzado, una referencia al nodo padre, el indice de la accion
 * aplicada al padre para generar este nodo, la profundidad (acciones desde la
 * raiz), el costo acumulado del camino y el valor asignado por la estrategia de
 * busqueda.
 *
 * La accion de un nodo se identifica por su indice 0..31 en Estado.ACCIONES,
 * no por su codigo de 5 bits: el indice 0 es "00+", el 1 es "01+", el 4 es
 * "10+", el 16 es "00-" y el 31 es "33-". El codigo de la accion i-ésima se
 * obtiene leyendo Estado.ACCIONES[i].
 *
 * La raiz se crea a partir de un estado sin padre ni accion. Un nodo hijo se
 * crea a partir de su padre, el indice de una accion y el costo de esa accion.
 *
 * TODO Tarea 2: implementar todos los metodos marcados con TODO.
 */
public final class Nodo implements Comparable<Nodo> {

    private static final int SIN_ACCION = -1;

    // El contador y el identificador son long: un int se desbordaria a los 2^31
    // nodos y los ids negativos alterarian el desempate de compareTo, que asume
    // que un nodo creado despues tiene un id mayor.
    private static long siguienteId = 0L;

    private final long id;
    private final long bitboard;      // el estado se guarda como bitboard, no como objeto
    private final Nodo padre;
    private final int indiceAccion;   // indice 0..31 de la accion en Estado.ACCIONES
    private final int profundidad;
    private final int costoAcumulado;
    private int heuristica;
    private int valor;
    // Para SMA* (Tarea 3):
    private int hijosPendientes;  // mascara de 32 bits, uno por accion; bit i a 1 =
                                  //   hijo pendiente de generar o de reconstruir
    private int hijosVivos;       // cuantos de esos 32 hijos estan ahora en el arbol
    private boolean expandido;

    /**
     * Constructor de la raiz: nodo sin padre ni accion.
     * - id = siguienteId++
     * - profundidad = 0, costoAcumulado = 0
     * - hijosPendientes = los 32 bits a 1, hijosVivos = 0, expandido = false
     */
    public Nodo(Estado estado) {
        if (estado == null) throw new IllegalArgumentException("El estado no puede ser nulo");
        id = siguienteId++;
        bitboard = estado.bitboard();
        padre = null;
        indiceAccion = SIN_ACCION;
        profundidad = 0;
        costoAcumulado = 0;
        hijosPendientes = 0xffffffff;
        hijosVivos = 0;
        expandido = false;
    }

    /**
     * Constructor de nodo hijo con costo de accion 1 (costo por defecto).
     * Delega en Nodo(padre, indiceAccion, 1).
     */
    public Nodo(Nodo padre, int indiceAccion) {
        this(padre, indiceAccion, 1);
    }

    /**
     * Constructor de nodo hijo con costo de accion explicito.
     * - validar el indice (0..31)
     * - estado = padre.estado.aplicar(Estado.ACCIONES[indiceAccion])
     * - profundidad = padre.profundidad + 1
     * - costoAcumulado = padre.costoAcumulado + costoAccion
     * - hijosPendientes = los 32 bits a 1, hijosVivos = 0, expandido = false
     */
    public Nodo(Nodo padre, int indiceAccion, int costoAccion) {
        if (padre == null) throw new IllegalArgumentException("El padre no puede ser nulo");
        id = siguienteId++;
        this.padre = padre;
        validarIndice(indiceAccion);
        this.indiceAccion = indiceAccion;
        bitboard = padre.estado().aplicar(Estado.ACCIONES[indiceAccion]).bitboard();
        profundidad = padre.profundidad + 1;
        costoAcumulado = padre.costoAcumulado + costoAccion;
        hijosPendientes = 0xffffffff;
        hijosVivos = 0;
        expandido = false;
    }

    public Estado estado() {
        return new Estado(bitboard);
    }

    public Nodo padre() {
        return padre;
    }

    /** Indice 0..31 de la accion aplicada al padre en Estado.ACCIONES. */
    public int accion() {
        return indiceAccion;
    }

    public int profundidad() {
        return profundidad;
    }

    public int costoAcumulado() {
        return costoAcumulado;
    }

    public void fijarHeuristica(int heuristica) { this.heuristica = heuristica; }
    public int valor() { return valor; }
    public void fijarValor(int valor) { this.valor = valor; }

    // Gestion de hijos para SMA* — TODO: Tarea 3
    public void marcarHijoGenerado(int indiceAccion) { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public void marcarHijoPodado(int indiceAccion) { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public int mascaraHijosPendientes() { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public boolean tieneHijosPendientes() { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public boolean tieneHijosVivos() { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public void marcarExpandido(){ throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public boolean necesitaReexpansion() { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public boolean agotado()    { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public void respaldarMinimo(int valor) { throw new UnsupportedOperationException("TODO: Tarea 3"); }

    public boolean esRaiz() {
        return padre == null;
    }

    /**
     * Orden por valor ascendente; en caso de empate, por id ascendente (FIFO).
     * Integer.compare(this.valor, otro.valor); si == 0 → Long.compare(id, otro.id)
     * (el id es long para no desbordar en busquedas largas)
     */
    @Override
    public int compareTo(Nodo otro) {
        int res;
        return ((res = Integer.compare(this.valor, otro.valor)) != 0 ? res : Long.compare(id, otro.id));
    }

    /**
     * Recorre la cadena de padres desde este nodo hasta la raiz y devuelve
     * la lista en orden raiz → este nodo.
     */
    public ObjectList<Nodo> camino() {
        ObjectList<Nodo> path = new ObjectArrayList<>();

        for (Nodo n = this; n != null; n = n.padre) {
            path.add(n);
        }
        for (int i = 0, j = path.size() - 1; i < j; i++, j--) {
            Nodo tmp = path.get(i);
            path.set(i, path.get(j));
            path.set(j, tmp);
        }

        return path;
    }

    public String caminoComoTexto() {
        StringBuilder camino = new StringBuilder();

        for (Nodo n : camino()) {
            if (camino.length() > 0) {
                camino.append('\n');
            }
            camino.append(n.toString());
        }

        return camino.toString();
    }

    /**
     * Dos nodos son iguales si coinciden en valor e identificador, la misma
     * condicion que la de compareTo() para valer 0. El guion detalla el contrato.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Nodo otro)) {
            return false;
        }
        return id == otro.id && valor == otro.valor;
    }

    /** Coherente con equals(). */
    @Override
    public int hashCode() {
        int resultado = Long.hashCode(id);
        resultado = 31 * resultado + Integer.hashCode(valor);
        return resultado;
    }

    /**
     * Representacion textual del nodo, con el formato
     * "[id,padre,accion,estado,profundidad,coste,heuristica,valor]"; en la raiz,
     * el padre y la accion se sustituyen por "raiz" y "NoAccion".
     *
     * Se entrega ya implementado: el corrector compara la salida del programa
     * con la del jar de referencia, asi que este formato no puede variar (en los
     * hijos se ve el texto de la accion, no su indice).
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(id);
        if (padre == null) {
            sb.append(",raiz,NoAccion");
        } else {
            sb.append(",").append(padre.id);
            sb.append(",").append(Estado.accionComoTexto(Estado.ACCIONES[indiceAccion]));
        }
        sb.append(",").append(new Estado(bitboard))
                .append(",").append(profundidad)
                .append(",").append(costoAcumulado)
                .append(",").append(heuristica)
                .append(",").append(valor)
                .append("]");
        return sb.toString();
    }

    /**
     * Comprueba que el indice de accion esta en 0..Estado.NUM_ACCIONES - 1.
     */
    private static void validarIndice(int indiceAccion) {
        if (indiceAccion < 0 || indiceAccion >= Estado.NUM_ACCIONES) {
            throw new IllegalArgumentException("Indice de accion no válido");
        }
    }
}

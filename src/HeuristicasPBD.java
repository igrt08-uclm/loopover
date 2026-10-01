import java.io.IOException;
import java.util.function.ToIntFunction;

/** Heuristicas basadas en PBD de 8 piezas. TODO: implementar en Tarea 3. */
public final class HeuristicasPBD {

    public static final String RECURSO_PARES   = "/pdb8.dat";
    public static final String RECURSO_IMPARES = "/pdb8_impares.dat";

    public enum Tipo { PARES_8, IMPARES_8, PBD_8, PBD_8_SUMA }

    public HeuristicasPBD() throws IOException {
        throw new UnsupportedOperationException("TODO: Tarea 3");
    }

    public ToIntFunction<Estado> funcion(Tipo tipo) { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public int valorPares(Estado estado)  { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public int valorImpares(Estado estado){ throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public int valorMaximo(Estado estado) { throw new UnsupportedOperationException("TODO: Tarea 3"); }
    public int valorSuma(Estado estado)   { throw new UnsupportedOperationException("TODO: Tarea 3"); }
}

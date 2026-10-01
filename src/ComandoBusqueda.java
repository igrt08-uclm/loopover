import java.util.concurrent.Callable;
import java.util.function.ToIntFunction;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "solve",
        description = "Resuelve un estado del puzzle Loopover mediante búsqueda y muestra la secuencia de acciones.",
        mixinStandardHelpOptions = true
)
public class ComandoBusqueda implements Callable<Integer> {

    @Option(names = "-s", required = true, description = "Estado como secuencia de 32 digitos.")
    private String estado;

    @Option(names = {"-e", "--estrategia"},
            description = "Estrategia de búsqueda: PROFUNDIDAD, ANCHURA, COSTO_UNIFORME, VORAZ, A_ESTRELLA o A_ESTRELLA_ACOTADA. Por defecto A_ESTRELLA.")
    private Busqueda.Estrategia estrategia = Busqueda.Estrategia.A_ESTRELLA;

    @Option(names = {"-p", "--profundidad"}, description = "Profundidad máxima de búsqueda. Por defecto 1000.")
    private int profundidadMaxima = 1000;

    @Option(names = {"-c", "--capacidad"}, description = "Numero maximo de nodos del arbol en memoria (SMA*). Obligatorio para A_ESTRELLA_ACOTADA.")
    private Integer capacidad;
    @Option(names = {"-h", "--heuristica"},
            description = "Heurística para A_ESTRELLA: MANHATTAN (toroidal, por defecto), CERO, PARES_8, IMPARES_8, PBD_8 (maximo de pares e impares), PBD_8_SUMA (admisible falso, no garantiza óptimo), MANHATTAN_ADMISIBLE o PERMUTACIONES. Con CERO la búsqueda explora en anchura; usa -m o -c para limitarla.")
    private Heuristica elegida = Heuristica.MANHATTAN;

    @Option(names = {"-m", "--max-visitados"},
            description = "Límite de estados visitados para abortar la búsqueda antes de quedarse sin memoria. 0 = ilimitado (por defecto).")
    private long maxVisitados = 0L;

    @Option(names = {"-v", "--verbose"}, description = "Muestra estadísticas de la búsqueda.")
    private boolean verbose;

    private enum Heuristica {
        MANHATTAN,
        CERO,
        PARES_8,
        IMPARES_8,
        PBD_8,
        PBD_8_SUMA,
        MANHATTAN_ADMISIBLE,
        PERMUTACIONES
    }

    @Override
    public Integer call() {
        Estado actual;
        try {
            actual = new Estado(estado);
        } catch (IllegalArgumentException e) {
            System.err.println("Error en el estado: " + e.getMessage());
            return 1;
        }
        if (actual.esResuelto()) {
            System.out.println("(ya resuelto)");
            return CommandLine.ExitCode.OK;
        }

        Busqueda busqueda;
        try {
            if (estrategia == Busqueda.Estrategia.A_ESTRELLA_ACOTADA && capacidad == null) {
                throw new IllegalArgumentException(
                        "A* acotada requiere indicar el maximo de nodos del arbol con -c");
            }
            ToIntFunction<Estado> heuristica = resolverHeuristica(elegida);
            busqueda = new Busqueda(actual, estrategia, profundidadMaxima,
                    capacidad != null ? capacidad : 10_000_000, heuristica, maxVisitados);
        } catch (IllegalArgumentException | java.io.IOException e) {
            System.err.println("Error de configuración: " + e.getMessage());
            return 1;
        }

        Nodo solucion = busqueda.buscar();
        if (solucion == null) {
            if (busqueda.limiteVisitadosAlcanzado()) {
                System.err.println("Búsqueda abortada: se alcanzó el límite de "
                        + maxVisitados + " estados visitados. "
                        + "El espacio de estados 4x4 es enorme; reduce el problema, "
                        + "limita los nodos del arbol con -c o sube -m.");
            } else {
                System.err.println("No se encontró solución dentro del límite de profundidad "
                        + profundidadMaxima + ".");
            }
            return 1;
        }

        System.out.println(solucion.caminoComoTexto());

        if (verbose) {
            System.out.println("profundidad=" + solucion.profundidad()
                    + ", nodosExpandidos=" + busqueda.nodosExpandidos()
                    + ", estadosVisitados=" + busqueda.estadosVisitados()
                    + ", tiempo=" + busqueda.tiempoMs() + "ms");
        }
        return CommandLine.ExitCode.OK;
    }

    private static ToIntFunction<Estado> resolverHeuristica(Heuristica elegida)
            throws java.io.IOException {
        switch (elegida) {
            case MANHATTAN:
                return Heuristicas::heuristicaManhattanToroidal;
            case CERO:
                return estado -> 0;
            case MANHATTAN_ADMISIBLE:
                return Heuristicas::heuristicaManhattanAdmisible;
            case PERMUTACIONES:
                return Heuristicas::heuristicaPermutaciones;
            case PARES_8:
            case IMPARES_8:
            case PBD_8:
            case PBD_8_SUMA:
                return new HeuristicasPBD().funcion(
                        switch (elegida) {
                            case PARES_8 -> HeuristicasPBD.Tipo.PARES_8;
                            case IMPARES_8 -> HeuristicasPBD.Tipo.IMPARES_8;
                            case PBD_8_SUMA -> HeuristicasPBD.Tipo.PBD_8_SUMA;
                            default -> HeuristicasPBD.Tipo.PBD_8;
                        });
            default:
                throw new IllegalArgumentException("Heuristica no soportada: " + elegida);
        }
    }
}

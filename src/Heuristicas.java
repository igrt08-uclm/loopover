/** Heuristicas basadas en geometria toroidal. TODO: implementar en Tarea 3. */
public final class Heuristicas {

    private Heuristicas() {}

    // Devuelve 0 para que las estrategias no informadas (Tarea 2) funcionen sin
    // heuristica. La busqueda llama a las versiones que reciben el bitboard.
    public static int heuristicaManhattanToroidal(Estado estado) { return heuristicaManhattanToroidal(estado.bitboard()); }
    public static int heuristicaManhattanAdmisible(Estado estado) { return heuristicaManhattanAdmisible(estado.bitboard()); }
    public static int heuristicaPermutaciones(Estado estado) { return heuristicaPermutaciones(estado.bitboard()); }

    public static int heuristicaManhattanToroidal(long bitboard) { return 0; }
    public static int heuristicaManhattanAdmisible(long bitboard) { return 0; }
    public static int heuristicaPermutaciones(long bitboard) { return 0; }
}

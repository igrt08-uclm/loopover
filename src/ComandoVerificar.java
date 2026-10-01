import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "verify",
        description = "Verifica un estado y aplica opcionalmente una lista de acciones consecutivas.",
        mixinStandardHelpOptions = true
)
public class ComandoVerificar implements Callable<Integer> {

    @Option(names = "-s", required = true, description = "Estado como secuencia de 32 digitos.")
    private String estado;

    @Option(names = "-a", description = "Lista de acciones separadas por comas, por ejemplo: 21+,03-")
    private String acciones;

    @Override
    public Integer call() {
        Estado inicial;
        try {
            inicial = new Estado(estado);
        } catch (IllegalArgumentException e) {
            System.err.println("Error en el estado: " + e.getMessage());
            return 1;
        }
        if (acciones != null) {
            Estado actual = inicial;
            for (String parte : acciones.split(",")) {
                String limpia = parte.trim();
                int accion;
                try {
                    accion = Estado.accionDesde(limpia);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error en la accion '" + limpia + "': " + e.getMessage());
                    return 1;
                }
                actual = actual.aplicar(accion);
            }
            System.out.println(actual);
            return CommandLine.ExitCode.OK;
        }
        for (Sucesor s : inicial.sucesores()) {
            System.out.println(s);
        }
        return CommandLine.ExitCode.OK;
    }
}

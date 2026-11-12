import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "loopover",
        description = "Resuelve el puzzle Loopover mediante búsqueda en espacio de estado con movimientos encadenados.",
        mixinStandardHelpOptions = true,
        version = "loopover 0.1.0",
        subcommands = {ComandoVerificar.class, ComandoBusqueda.class}
)
public class LoopoverApp implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "Muestra información detallada durante la búsqueda.")
    private boolean verbose;

    @Override
    public Integer call() {
        System.out.println("Hola, mundo desde Loopover!");
        if (verbose) {
            System.out.println("Modo verbose activado.");
        }
        return CommandLine.ExitCode.OK;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LoopoverApp()).execute(args);
        System.exit(exitCode);
    }
}

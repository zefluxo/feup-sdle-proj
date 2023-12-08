package sdle.client;

import lombok.SneakyThrows;
import picocli.CommandLine;
import sdle.client.command.*;

@CommandLine.Command(
        name = "SDLE Project",
        description = "Shopping Lists on the Cloud",
        subcommands = {
                ShoppListCreateCommand.class,
                ShoppListGetCommand.class,
                ShopListGetAllLocalCommand.class,
                ShoppListIncItemCommand.class,
                ShoppListDecItemCommand.class,
        }

)
public class ClientRestCommad implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClientRestCommad()).execute(args);
        System.exit(exitCode);
    }

    @Override
    @SneakyThrows
    public void run() {
        new LocalStorage().synchronise();
    }
}

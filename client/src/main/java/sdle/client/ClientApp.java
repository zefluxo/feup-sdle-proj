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
                ShoppListDeleteCommand.class,
                ShoppListDeleteItemCommand.class
        }

)
public class ClientApp implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClientApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    @SneakyThrows
    public void run() {
        //
    }
}

package sdle.client.command;

import picocli.CommandLine;
import sdle.client.LocalStorage;

public class BaseShoppListCommand {
    @CommandLine.Parameters(index = "0", description = "hash id of list", defaultValue = "")
    String hashId;
    @CommandLine.Parameters(index = "1", description = "item name", defaultValue = "")
    String name;
    @CommandLine.Parameters(index = "2", description = "item quantity", defaultValue = "0")
    String quantity;

    LocalStorage localStorage = new LocalStorage();
}

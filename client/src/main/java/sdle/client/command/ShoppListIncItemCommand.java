package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "incItem"
)
public class ShoppListIncItemCommand extends BaseShoppListCommand implements Runnable {

    @CommandLine.Option(names = {"-id", "--hashId"})
    String hashId;
    @CommandLine.Option(names = {"-n", "--itemName"})
    String name;
    @CommandLine.Option(names = {"-q", "--itemQuantity"})
    String quantity;

    @Override
    public void run() {
        System.out.printf("Inc item to a shopping list: (hashId=%s, name=%s, quantity=%s%n", hashId, name, quantity);
        localStorage.incItem(hashId, name, quantity);
    }
}

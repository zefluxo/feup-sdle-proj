package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "incItem"
)
public class ShoppListIncItemCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.printf("Inc item to a shopping list: (hashId=%s, name=%s, quantity=%s%n", hashId, name, quantity);
        localStorage.incItem(hashId, name, quantity);
    }
}

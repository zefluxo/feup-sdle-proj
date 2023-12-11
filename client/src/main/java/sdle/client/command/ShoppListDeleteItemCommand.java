package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "deleteItem"
)
public class ShoppListDeleteItemCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.printf("Delete item from a shopping list: (hashId=%s, name=%s%n", hashId, name);
        localStorage.deleteItem(hashId, name);
    }
}

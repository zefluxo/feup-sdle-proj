package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "decItem"
)
public class ShoppListDecItemCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.printf("Dec item to a shopping list: (hashId=%s, name=%s, quantity=%s%n", hashId, name, quantity);
        localStorage.decItem(hashId, name, quantity);
    }
}

package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "createList"
)
public class ShoppListCreateCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Creating a new shopping list");
        String hashId = localStorage.newShoppList();
        System.out.printf("New list hash: %s%n", hashId);
    }
}

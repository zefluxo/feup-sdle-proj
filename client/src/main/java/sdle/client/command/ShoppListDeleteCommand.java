package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "deleteList"
)
public class ShoppListDeleteCommand extends BaseShoppListCommand implements Runnable {
    @CommandLine.Option(names = {"-id", "--hashId"})
    String hashId;

    @Override
    public void run() {
        System.out.println("Delete a shopping list");
        localStorage.deleteShoppList(hashId);
    }
}

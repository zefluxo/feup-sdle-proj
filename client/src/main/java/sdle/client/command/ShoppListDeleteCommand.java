package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "deleteList"
)
public class ShoppListDeleteCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Delete a shopping list");
        localStorage.deleteShoppList(hashId);
    }
}

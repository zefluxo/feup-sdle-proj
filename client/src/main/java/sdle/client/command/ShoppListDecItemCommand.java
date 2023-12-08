package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "decItem"
)
public class ShoppListDecItemCommand extends BaseShoppListCommand implements Runnable {

    @CommandLine.Option(names = {"-id", "--hashId"})
    String hashId;
    @CommandLine.Option(names = {"-n", "--itemName"})
    String name;
    @CommandLine.Option(names = {"-q", "--itemQuantity"})
    String quantity;

    @Override
    public void run() {
        System.out.printf("Inc item to a shopping list: (hashId=%s, name=%s, quantity=%s%n", hashId, name, quantity);
        localStorage.getLocalShoppLists().get(hashId).dec(name, Integer.valueOf(quantity));
        localStorage.writeOnDisk(hashId);
        boolean success = restAdapter.decItem(hashId, name, quantity);
        //if (!success) System.out.printf("Hash ID from new list: %s%n", shoppList.getMap());

    }
}

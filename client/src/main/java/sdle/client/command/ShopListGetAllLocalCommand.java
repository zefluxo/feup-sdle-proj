package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(
        name = "all"
)
public class ShopListGetAllLocalCommand extends BaseShoppListCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Listing all local shopping lists");
        localStorage.getLocalShoppLists().forEach((hashId, shoppList) ->
                System.out.printf("List: %s %s%n", hashId, shoppList.getMap()));

    }
}

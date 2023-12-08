package sdle.client.command;

import picocli.CommandLine;
import sdle.client.CloudRestAdapter;
import sdle.crdt.implementations.ORMap;

@CommandLine.Command(
        name = "new"
)
public class ShoppListCreateCommand extends BaseShoppListCommand implements Runnable {

    CloudRestAdapter restAdapter = new CloudRestAdapter();

    @Override
    public void run() {
        System.out.println("Creating a new shopping list");
        String hashId = restAdapter.newShoppList();
        System.out.printf("New list hash: %s%n", hashId);
        localStorage.synchroniseShoppList(new ORMap(), hashId);
    }
}

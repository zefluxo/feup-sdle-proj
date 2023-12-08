package sdle.client.command;

import picocli.CommandLine;
import sdle.crdt.implementations.ORMap;

@CommandLine.Command(name = "getList")
public class ShoppListGetCommand extends BaseShoppListCommand implements Runnable {

    @CommandLine.Option(names = {"-id", "--hashId"})
    String hashId;

    @Override
    public void run() {
        System.out.printf("Retrieving a shopping list from cloud: %s%n", hashId);
        ORMap shoppList = localStorage.getShoppList(hashId);
        localStorage.synchroniseShoppList(shoppList, hashId);
        System.out.printf("List: %s %s%n", hashId, shoppList.getMap());
    }
}

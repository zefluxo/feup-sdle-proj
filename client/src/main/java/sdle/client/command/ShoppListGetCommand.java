package sdle.client.command;

import picocli.CommandLine;

@CommandLine.Command(name = "getList")
public class ShoppListGetCommand extends BaseShoppListCommand implements Runnable {

    @Override
    public void run() {
        System.out.printf("Retrieving a shopping list from cloud: %s%n", hashId);
        localStorage.getShoppList(hashId);
    }
}

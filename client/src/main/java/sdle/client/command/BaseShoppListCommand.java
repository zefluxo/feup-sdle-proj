package sdle.client.command;

import sdle.client.CloudRestAdapter;
import sdle.client.LocalStorage;

public class BaseShoppListCommand {
    CloudRestAdapter restAdapter = new CloudRestAdapter();
    LocalStorage localStorage = new LocalStorage();
}

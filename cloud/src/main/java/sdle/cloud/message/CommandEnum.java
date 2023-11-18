package sdle.cloud.message;

public enum CommandEnum {


    // Messages of shopping list operation

    /**
     * "putList" (no arqs) -> create a new list and return "listHashId"
     */
    PUT_LIST("putList", CommandType.SHOPP_LIST),

    /**
     * "getList <listHashId>" -> return the list (json format)
     */
    GET_LIST("getList", CommandType.SHOPP_LIST),

    /**
     * "deleteList <listHashId>" -> delete the list and return "OK"
     */
    DELETE_LIST("deleteList", CommandType.SHOPP_LIST),

    /**
     * "getItem <listHashId> <itemId>" -> return item of the list (json format)
     */
    GET_ITEM("getItem", CommandType.SHOPP_LIST),
    /**
     * "putItem <listHashId> <item> (json)" -> add a item on the list and return "OK"
     */
    PUT_ITEM("putItem", CommandType.SHOPP_LIST),
    /**
     * "deleteItem <listHashId> <itemId>" -> delete  item off the list
     */
    DELETE_ITEM("deleteItem", CommandType.SHOPP_LIST),

    // cluster internal messages
    // enviada para um bootstrap node na inicializacao de cada node
    CLUSTER_JOIN("cluster_join", CommandType.MEMBERSHIP),
    // enviada para um bootstrap node quando um node vai ser desligado (desligamento "normal")
    CLUSTER_LEAVE("cluster_leave", CommandType.MEMBERSHIP),

    // enviada pelo bootstrap para todos os nos do cluster, apos receber um join ou leave (ou quando um node for "expulso" do cluster por estar irresponsivo)
    CLUSTER_UPDATE("cluster_update", CommandType.MEMBERSHIP),

    CMD_NOT_RECOGNIZED("cmd not recognized", CommandType.OTHER);
    private final String cmd;
    private final CommandType cmdType;

    CommandEnum(String cmd, CommandType cmdType) {
        this.cmd = cmd;
        this.cmdType = cmdType;
    }

    public static CommandEnum getMessage(String cmd) {
        for (CommandEnum messageEnum : values()) {
            if (messageEnum.cmd.equals(cmd)) {
                return messageEnum;
            }
        }
        return CMD_NOT_RECOGNIZED;
    }

    public String cmd() {
        return cmd;
    }

    public CommandType cmdType() {
        return cmdType;
    }
}

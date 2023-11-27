package sdle.cloud.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sdle.cloud.processor.BaseProcessor;
import sdle.cloud.processor.NotImplementedProcessor;
import sdle.cloud.processor.NotRecognizedProcessor;
import sdle.cloud.processor.cloud.*;
import sdle.cloud.processor.shopplist.GetListProcessor;
import sdle.cloud.processor.shopplist.PutItemProcessor;
import sdle.cloud.processor.shopplist.PutListProcessor;
import sdle.cloud.processor.shopplist.ReplicateListProcessor;

@AllArgsConstructor
@Getter
public enum CommandEnum {


    // Messages of shopping list operation

    /**
     * "putList" (no arqs) -> create a new list and return "listHashId"
     */
    PUT_LIST("putList", CommandType.SHOPP_LIST, new PutListProcessor()),

    /**
     * "replicateList <listHashId> <list>" -> replicate a list and return "listHashId"
     */
    REPLICATE_LIST("replicateList", CommandType.SHOPP_LIST, new ReplicateListProcessor()),

    /**
     * "getList <listHashId>" -> return the list (json format)
     */
    GET_LIST("getList", CommandType.SHOPP_LIST, new GetListProcessor()),

    /**
     * "deleteList <listHashId>" -> delete the list and return "OK"
     */
    DELETE_LIST("deleteList", CommandType.SHOPP_LIST, new NotImplementedProcessor()),

    /**
     * "putItem <listHashId> <item> (json)" -> add a item on the list and return "OK"
     */
    PUT_ITEM("putItem", CommandType.SHOPP_LIST, new PutItemProcessor()),

    /**
     * "getItem <listHashId> <itemId>" -> return item of the list (json format)
     */
    GET_ITEM("getItem", CommandType.SHOPP_LIST, new NotImplementedProcessor()),

    /**
     * "deleteItem <listHashId> <itemId>" -> delete  item off the list
     */
    DELETE_ITEM("deleteItem", CommandType.SHOPP_LIST, new NotImplementedProcessor()),

    // cluster internal messages
    // enviada para um bootstrap node na inicializacao de cada node
    CLUSTER_JOINING("cluster_joining", CommandType.MEMBERSHIP, new ClusterJoiningProcessor()),

    // enviada para um bootstrap node quando um node vai ser desligado (desligamento "normal")
    CLUSTER_LEAVING("cluster_leaving", CommandType.MEMBERSHIP, new ClusterLeavingProcessor()),

    // enviada pelo bootstrap para todos os nos do cluster, apos receber um join ou leave (ou quando um node for "expulso" do cluster por estar irresponsivo)
    CLUSTER_UPDATE("cluster_update", CommandType.MEMBERSHIP, new ClusterUpdateProcessor()),

    // enviada para um bootstrap node quando um node vai ser desligado (desligamento "normal")
    CLUSTER_LEAVE("cluster_leave", CommandType.MEMBERSHIP, new ClusterLeaveProcessor()),


    CLUSTER_HEARTBEAT("cluster_heartbeat", CommandType.MEMBERSHIP, new ClusterHeartBeatProcessor()),

    CMD_NOT_RECOGNIZED("cmd not recognized", CommandType.OTHER, new NotRecognizedProcessor());

    private final String cmd;
    private final CommandType cmdType;
    private final BaseProcessor processor;

    public static CommandEnum getCommand(String cmd) {
        for (CommandEnum messageEnum : values()) {
            if (messageEnum.cmd.equals(cmd)) {
                return messageEnum;
            }
        }
        return CMD_NOT_RECOGNIZED;
    }

}

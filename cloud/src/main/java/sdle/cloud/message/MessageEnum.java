package sdle.cloud.message;

public enum MessageEnum {


    // Messages of shopping list operation
    GET_ITEM("getItem", MessageType.SHOPP_LIST),
    PUT_ITEM("putItem", MessageType.SHOPP_LIST),
    DELETE_ITEM("deleteItem", MessageType.SHOPP_LIST),
    GET_LIST("getList", MessageType.SHOPP_LIST),
    PUT_LIST("putList", MessageType.SHOPP_LIST),
    DELETE_LIST("deleteList", MessageType.SHOPP_LIST),

    // cluster internal messages
    CLUSTER_JOIN("cluster_join", MessageType.MEMBERSHIP),
    CLUSTER_JOIN_RESPONSE("cluster_join_response", MessageType.MEMBERSHIP),
    CLUSTER_LEAVE("cluster_leave", MessageType.MEMBERSHIP),
    CLUSTER_LEAVE_RESPONSE("cluster_leave_response", MessageType.MEMBERSHIP),

    MSG_NOT_RECOGNIZED("msg not reconized", MessageType.OTHER);
    private final String msg;
    private final MessageType msgType;

    MessageEnum(String msg, MessageType msgType) {
        this.msg = msg;
        this.msgType = msgType;
    }

    public static MessageEnum getMessage(String msg) {
        for (MessageEnum messageEnum : values()) {
            if (messageEnum.msg.equals(msg)) {
                return messageEnum;
            }
        }
        return MSG_NOT_RECOGNIZED;
    }

    public String msg() {
        return msg;
    }

    public MessageType msgType() {
        return msgType;
    }
}

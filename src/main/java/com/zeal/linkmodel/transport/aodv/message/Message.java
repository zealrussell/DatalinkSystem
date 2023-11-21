package com.zeal.linkmodel.transport.aodv.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 17:41
 */
@Data
public abstract class Message {
    private final byte type;
    private int preHop;
    private int destination;

    public Message(byte type, int destination) {
        this.type = type;
        this.destination = destination;
    }
    public abstract byte[] toMessage();
}

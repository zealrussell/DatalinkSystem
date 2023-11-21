package com.zeal.linkmodel.transport.aodv.message;

import lombok.ToString;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 18:23
 */
public class RREP_ACK extends Message{

    public RREP_ACK(int actualATDestination) {
        super(Type.RREP_ACK, actualATDestination);
    }
    @Override
    public byte[] toMessage() {
        return new byte[]{getType()};
    }

    @Override
    public String toString() {
        return "RREP ACK";
    }
}

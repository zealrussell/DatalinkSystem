package com.zeal.linkmodel.transport.aodv.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 18:18
 */
@Getter
@Setter
@ToString
public class RERR extends Message{
    // private byte destinationCount;
    // 不可达的ip地址
    private int unreachableDestinationAddress;
    // 不可达的节点序列号
    private int unreachableDestinationSequenceNumber;
    private LinkedList<Integer> additionalAddresses;
    private LinkedList<Byte> additionalSequenceNumber;

    public RERR(int actualATDestination, int unreachableDestinationAddress,
                int unreachableDestinationSequenceNumber) {
        super(Type.RERR, actualATDestination);

        this.unreachableDestinationAddress = unreachableDestinationAddress;
        this.unreachableDestinationSequenceNumber = unreachableDestinationSequenceNumber;
    }

    @Override
    public byte[] toMessage() {
        int offset = 3;
        byte[] message = new byte[offset + additionalAddresses.size() + additionalSequenceNumber.size()];

        message[0] = getType();
        message[1] = (byte) unreachableDestinationAddress;
        message[2] = (byte) unreachableDestinationSequenceNumber;

        for (int i = 0; offset + i + 1 < message.length; i = i + 2) {
            message[offset + i] = additionalAddresses.get(i).byteValue();
            message[offset + i + 1] = additionalSequenceNumber.get(i);
        }
        return message;
    }
}

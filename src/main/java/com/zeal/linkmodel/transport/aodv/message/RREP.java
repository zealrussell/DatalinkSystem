package com.zeal.linkmodel.transport.aodv.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 17:55
 */
@Getter
@Setter
@ToString
public class RREP extends Message{
    private byte hopCount;
    private int originAddress;
    private int destinationAddress;
    private byte destinationSequenceNumber;
    private byte lifetime;
    public RREP(int actualATDestination, byte hopCount, int originAddress, int destinationAddress, byte destinationSequenceNumber, byte lifetime) {
        super(Type.RREP, 0);
        this.hopCount = hopCount;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.lifetime = lifetime;
    }
    public void increaseHopCount() {
        hopCount++;
    }
    @Override
    public byte[] toMessage() {
        return new byte[]{getType(), hopCount, (byte) originAddress, (byte) destinationAddress, destinationSequenceNumber, lifetime};
    }
}

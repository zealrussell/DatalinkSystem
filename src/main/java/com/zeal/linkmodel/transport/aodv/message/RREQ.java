package com.zeal.linkmodel.transport.aodv.message;

import lombok.*;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 17:46
 */
@Getter
@Setter
public class RREQ extends Message{
    private byte hopCount;
    private int originAddress;
    private byte originSequenceNumber;
    private int destinationAddress;
    private byte destinationSequenceNumber;
    private byte rreqID;

    public RREQ( byte hopCount, byte rreqID, int originAddress, byte originSequenceNumber, int destinationAddress, byte destinationSequenceNumber) {
        super(Type.RREQ, 0);
        this.hopCount = hopCount;
        this.originAddress = originAddress;
        this.originSequenceNumber = originSequenceNumber;
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.rreqID = rreqID;
    }
    public void increaseHopCount() {
        hopCount++;
    }
    @Override
    public byte[] toMessage() {
        return new byte[]{this.getType(), hopCount, rreqID, (byte) originAddress, originSequenceNumber, (byte) destinationAddress, destinationSequenceNumber};
    }
}

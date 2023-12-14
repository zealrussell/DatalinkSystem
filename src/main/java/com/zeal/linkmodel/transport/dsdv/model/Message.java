package com.zeal.linkmodel.transport.dsdv.model;

import lombok.Data;

import java.io.Serializable;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 19:40
 */
@Data
public abstract class Message implements Serializable {
    protected int srcAddress;
    protected int destAddress;
    protected int type;
    public Message(){
        this.srcAddress = 0;
        this.destAddress = 0;
        this.type = 0;
    }
    public Message(int srcAddress, int destAddress, int type) {
        this.srcAddress = srcAddress;
        this.destAddress = destAddress;
        this.type = type;
    }
}

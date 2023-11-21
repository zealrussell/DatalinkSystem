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
    private int destAddress;
    private int type;
    public Message(){
        this.destAddress = 0;
        this.type = 0;
    }
    public Message(int destAddress, int type) {
        this.destAddress = destAddress;
        this.type = type;
    }
    public abstract byte[] toBytes();
}
package com.zeal.linkmodel.transport.dsdv.model;

import lombok.Data;

import java.util.HashMap;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/13 10:00
 */
@Data
public class RouteMessage extends Message{
    HashMap<Integer, DsdvRoute> routeHashMap;

    public RouteMessage(int destAddress, HashMap<Integer, DsdvRoute> routeHashMap) {
        super(destAddress, Type.Route);
        this.routeHashMap = routeHashMap;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}

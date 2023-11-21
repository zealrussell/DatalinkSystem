package com.zeal.linkmodel.transport.aodv;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 18:24
 */
@Getter
@Setter
@ToString
public class Route {
    // 目的地址
    private int destinationAddress;
    private byte destinationSequenceNumber;
    // 路由表状态
    private boolean validRoute;
    // 跳数
    private byte hopCount;
    // 下一跳地址
    private int nextHop;
    private LinkedList<Integer> precursorsList = new LinkedList<>();
    private int precursor = 0;
    private int lifetimeUnsigned;
    // 路由表计时
    private long lifetime;

    public Route(int destinationAddress, byte destinationSequenceNumber, byte hopCount, int nextHop) {
        this.destinationAddress = destinationAddress;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.validRoute = true;
        this.hopCount = hopCount;
        this.nextHop = nextHop;
        this.lifetime = System.currentTimeMillis();
    }

    public boolean getValidRoute() {
        if (lifetime < 0)
            this.validRoute = false;
        return validRoute;
    }

    public byte getLifetime() {
        long diff = (System.currentTimeMillis() - lifetime);
        if (diff > Constants.ROUTE_LIFETIME_IN_MILLIS)
            return 0;
        return (byte) (180 - diff / 1000);
    }

    public int getLifetimeUnsigned() {
        lifetimeUnsigned = Byte.toUnsignedInt(getLifetime());
        return lifetimeUnsigned;
    }

}

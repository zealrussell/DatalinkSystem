package com.zeal.linkmodel.transport.dsdv;

import com.zeal.linkmodel.packet.UserMessage;
import com.zeal.linkmodel.transport.dsdv.model.DsdvRoute;
import com.zeal.linkmodel.transport.dsdv.model.RouteMessage;

import java.util.HashMap;
import java.util.Objects;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 15:18
 */
public class DsdvHelper {
    /**
     * 查找路由表中的下一跳
     *
     * @param routeHashMap 路由表
     * @param targetAddress 目标地址
     * @return 下一跳地址
     */
    public static int findIndex(HashMap<Integer, DsdvRoute> routeHashMap, int targetAddress) {
        for (Integer key : routeHashMap.keySet()) {
            DsdvRoute route = routeHashMap.get(key);
            if (route.getDestAddress() == targetAddress) {
                return key;
            }
        }
        return -1;
    }

    /**
     * 查找路由表中的下一跳
     *
     * @param routeHashMap 路由表
     * @param targetAddress 目标地址
     * @return 下一跳地址
     */
    public static int findNextHop(HashMap<Integer, DsdvRoute> routeHashMap, int targetAddress) {
        int index = findIndex(routeHashMap, targetAddress);
        if (index != -1) {
            return routeHashMap.get(index).getNextHop();
        }
        return -1;

    }

    public static DsdvRoute findRouteByDesName(HashMap<Integer, DsdvRoute> routeHashMap, String des) {
        for (Integer key : routeHashMap.keySet()) {
            DsdvRoute route = routeHashMap.get(key);
            if (Objects.equals(route.getDestName(), des)) {
                return routeHashMap.get(key);
            }
        }
        return null;
    }

    public static DsdvRoute findRouteByDes(HashMap<Integer, DsdvRoute> routeHashMap, int des) {
        int index = findIndex(routeHashMap, des);
        if (index != -1) {
            return routeHashMap.get(index);
        }
        return null;
    }

    public static void printRouteTable(HashMap<Integer, DsdvRoute> routingTable) {
        System.out.println("路由表：");
        System.out.println("序号\t目的节点\t目的地址\t下一跳节点\t下一跳地址\t跳数\t序列号\t时间");

        synchronized (routingTable) {
            routingTable.forEach((k, v) -> {
                System.out.println(k + "\t" + v.getDestName() + "\t" + v.getDestAddress() + "\t" + v.getNextName() + "\t\t" + v.getNextHop()
                        + "\t\t" + v.getHopCount() + "\t" + v.getSeqNumber() + "\t" + v.getDate());
            });
        }
    }
    public RouteMessage parseRouteMessage(byte[] bytes) {
        return null;
    }
    public UserMessage parseUserMessage(byte[] bytes) {
        return null;
    }

}

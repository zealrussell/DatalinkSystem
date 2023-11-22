package com.zeal.linkmodel.transport.dsdv.service;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 11:24
 */

import com.zeal.linkmodel.transport.TransportUtil;
import com.zeal.linkmodel.transport.dsdv.Constants;
import com.zeal.linkmodel.transport.dsdv.Signal;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import com.zeal.linkmodel.transport.dsdv.model.DsdvRoute;

import com.zeal.linkmodel.transport.dsdv.model.RouteMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 广播服务
 */
@Slf4j
public class BroadcastService implements Runnable {


    private Thread broadcast;
    private TransportUtil transportUtil;
    // 路由表
    private HashMap<Integer, DsdvRoute> routingTable;
    // 邻居节点表
    private HashMap<Integer, DsdvNode> neighborTable;

    private boolean flag = true;

    public BroadcastService(HashMap<Integer, DsdvNode> neighborTable, HashMap<Integer, DsdvRoute> routingTable, TransportUtil transportUtil) {
        this.routingTable = routingTable;
        this.neighborTable = neighborTable;
        this.transportUtil = transportUtil;
    }

    @Override
    public void run() {
        while (flag) {
            try {
                DsdvRoute route = routingTable.get(1);
                // 1. 广播路由表前，更序列号
                if (route.getSeqNumber() % 2 == 0) {
                    route.setSeqNumber(route.getSeqNumber() + 2);
                    // routingTable.put(1, route);
                } else if(route.getSeqNumber() % 2 == 1) {
                    flag = false;
                }

                // 2. 消费信号量
                synchronized (Signal.getInstance()){
                    Signal.getInstance().sub();
                    if (Signal.getInstance().get()<=0)
                        flag = false;
                }

                // 当没有邻居节点时，不广播
                if (routingTable.size() <= 1) continue;

                // 3. 广播路由表
                InetAddress address = InetAddress.getByName("localhost");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                RouteMessage routeMessage = new RouteMessage(0,routingTable);
                oos.writeObject(routeMessage);
                oos.flush();
                byte[] buf = baos.toByteArray();

                for (Map.Entry<Integer, DsdvNode> entry : neighborTable.entrySet()) {
                    Integer k = entry.getKey();
                    DsdvNode v = entry.getValue();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, v.getPort());
                    // log.info("广播了路由表" + v.getPort());
                    transportUtil.sendPacket(packet);
                }
                log.info("广播了路由表");
                TimeUnit.MILLISECONDS.sleep(Constants.BROADCAST_PERIOD);
            } catch (IOException e) {
                log.error("广播失败" + e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void start(){
        log.info("广播服务启动");
        if (broadcast == null) {
            broadcast = new Thread(this);
            broadcast.start();
        }
    }
}

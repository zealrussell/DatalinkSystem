package com.zeal.linkmodel.transport.dsdv.service;

import com.zeal.linkmodel.application.LinkMessage;
import com.zeal.linkmodel.packet.UserMessage;
import com.zeal.linkmodel.transport.TransportUtil;
import com.zeal.linkmodel.transport.dsdv.Constants;
import com.zeal.linkmodel.transport.dsdv.DsdvHelper;
import com.zeal.linkmodel.transport.dsdv.Signal;
import com.zeal.linkmodel.transport.dsdv.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 15:03
 */
@Slf4j
public class ReceiveService implements Runnable {
    private static final int timeLimit = 10000;       //判断时间ms
    private static final int MaxInt = 100000;
    private DsdvNode ownNode;

    private Thread receive;
    private UserMessage receivedMessage;
    private HashMap<Integer, DsdvNode> neighborTable;

    private HashMap<Integer, DsdvRoute> routeTable;
    // 更新时间表, key为节点端口号，value为更新时间
    private HashMap<Integer, Date> updateTable = new HashMap<Integer, Date>();
    private TransportUtil transportUtil;

    public ReceiveService(DsdvNode node, HashMap<Integer, DsdvNode> neighborTable, UserMessage receivedMessage, HashMap<Integer, DsdvRoute> routeTable, TransportUtil transportUtil) {
        this.ownNode = node;
        this.neighborTable = neighborTable;
        this.receivedMessage = receivedMessage;
        this.routeTable = routeTable;
        this.transportUtil = transportUtil;
    }

    /**
     * 检测路由表中的坏节点
     */
    private boolean detectBrokenRoute() {
        AtomicBoolean isChanged = new AtomicBoolean(false);
        Date now = new Date();
        updateTable.forEach((k, v) -> {
            // 1. 如果更新时间超过，则认为该节点已经坏掉
            if (now.getTime() - v.getTime() > timeLimit) {
                synchronized (routeTable) {
                    routeTable.forEach((k1, v1) -> {
                        // 2. 如果路由表中的目的节点或下一跳节点是过期节点
                        if (v1.getDestAddress() == k || v1.getNextHop() == k) {
                            // 则将该节点的跳数设置为最大值
                            v1.setHopCount(MaxInt);
                            // 序列号设为奇数
                            if (v1.getSeqNumber() % 2 == 0) {
                                v1.setSeqNumber(v1.getSeqNumber() + 1);
                            }
                            isChanged.set(true);
                        }
                    });
                }

            }
        });
        return isChanged.get();
    }


    /**
     * 根据路由信息添加路由表
     * @param senderInfo 发送者信息
     * @param route 本表没有的路由信息
     */
    private void addTableByRoute(DsdvRoute senderInfo, DsdvRoute route) {
        DsdvRoute newRoute = new DsdvRoute();
        newRoute.setDestAddress(route.getDestAddress());
        newRoute.setDestName(route.getDestName());
        newRoute.setNextHop(senderInfo.getNextHop());
        newRoute.setNextName(senderInfo.getNextName());
        newRoute.setHopCount(senderInfo.getHopCount() + route.getHopCount());
        newRoute.setDate(new Date());
        routeTable.put(routeTable.size() + 1, newRoute);

        // 如果只有一跳距离，则添加到邻居节点表
        if (route.getNextName().equals(ownNode.getName()) && route.getHopCount() == 1) {
            log.info("新表{}", route);
            neighborTable.put(neighborTable.size() + 1, new DsdvNode(senderInfo.getDestName(), senderInfo.getDestAddress()));
            log.info("{}添加了新邻居节点: {}", ownNode.getName(), senderInfo.getDestName());
        }
        log.info("{}添加了新路由", ownNode.getName());
    }

    /**
     * 更新路由表时间
     * @param sender 发送者
     */
    private void updateRouteTime(DsdvRoute sender) {
        // 1. 如果路由表中有这个节点，则更新时间、序列号
        int index = DsdvHelper.findIndex(routeTable, sender.getDestAddress());
        if (index != -1) {
            DsdvRoute route = routeTable.get(index);
            synchronized (route) {
                route.setSeqNumber(sender.getSeqNumber());
                routeTable.put(index, route);
                updateTable.put(sender.getDestAddress(), new Date());
            }
        }
        log.debug("{}更新路由表时间", ownNode.getName());
    }


    /**
     * 更新路由表
     *  如果路由表中没有该节点，则添加
     *  如果路由表中有该节点，则比较序列号
     *  如果序列号大于路由表中的序列号，则更新路由表
     *  如果序列号小于路由表中的序列号，则丢弃该路由表
     *  如果序列号等于路由表中的序列号，则选择跳数小的路由表
     * @param newTable 新的路由表
     */
    private boolean updateRoutingTable(HashMap<Integer, DsdvRoute> newTable) {
        log.debug("{} 更新路由表", ownNode.getName());
        AtomicBoolean isChanged = new AtomicBoolean(false);
        DsdvRoute sender = newTable.get(1);



        // 1. 更新路由表时间
        updateRouteTime(sender);

        // 2. 更新路由表
        DsdvRoute senderItem;  // 发送者在本节点的路由表中的信息
        int indexx = DsdvHelper.findIndex(routeTable, sender.getDestAddress());
        if (indexx != -1) {
           senderItem = routeTable.get(indexx);
        } else {
            senderItem = sender;
        }

        newTable.forEach((k, revItem) -> {
            // 2.1. 如果是第一个节点，则不更新
            if (k == 1) return;
            AtomicBoolean isExist = new AtomicBoolean(false);
            for (Map.Entry<Integer, DsdvRoute> entry : routeTable.entrySet()) {
                Integer k1 = entry.getKey();
                DsdvRoute ownItem = entry.getValue();
                // 1. 如果新表中的节点在路由表中存在
                if (ownItem.getDestAddress() == revItem.getDestAddress()) {
                    isExist.set(true);
                    // 2. 如果序列号大于路由表中的序列号，则更新路由表
                    if (ownItem.getSeqNumber() < revItem.getSeqNumber()) {
                        synchronized (routeTable) {
                            isChanged.set(true);
                            ownItem.setSeqNumber(revItem.getSeqNumber());
                            ownItem.setNextHop(senderItem.getDestAddress());
                            ownItem.setNextName(senderItem.getDestName());
                            ownItem.setHopCount(senderItem.getHopCount() + revItem.getHopCount());
                        }
                        break;
                        // 3. 如果序列号等于路由表中的序列号并且跳数小于路由表中的跳数，则更新路由表
                    } else if (ownItem.getSeqNumber() == revItem.getSeqNumber() && senderItem.getHopCount() + revItem.getHopCount() < ownItem.getHopCount()) {
                        synchronized (routeTable) {
                            isChanged.set(true);
                            ownItem.setNextName(senderItem.getDestName());
                            ownItem.setNextHop(senderItem.getDestAddress());
                            ownItem.setHopCount(senderItem.getHopCount() + revItem.getHopCount());
                        }
                        break;
                    }
                }
            }

            // 3. 不存在则添加
            if (!isExist.get()) {
                isChanged.set(true);
                synchronized (routeTable) {
                    addTableByRoute(senderItem, revItem);
                }
            }

        });
        return isChanged.get();
    }

    /**
     * 处理路由消息
     * @param receivePacket 接收到的数据包
     * @throws IOException IO异常
     * @throws ClassNotFoundException 类找不到异常
     */
    private void handleRouteMessage(Message receivePacket) throws IOException, ClassNotFoundException {
        // 1. 拆包
        HashMap<Integer, DsdvRoute> receivedRouteTable = ((RouteMessage)receivePacket).getRouteHashMap();
        // 2. 更新路由表
        boolean isChanged = updateRoutingTable(receivedRouteTable);
        // 3. 检测路由表中的坏节点
        isChanged |= detectBrokenRoute();
        // 4. 立即通知广播线程立即广播路由表
        if (isChanged) {
            synchronized (Signal.getInstance()) {
                Signal.getInstance().set();
            }
        }
    }
    /**
     * 转发用户消息
     * @param message 用户消息
     * @param desAddress 目的地址
     */
    private void forwardUserMessage(UserMessage message, int desAddress) {
        int index = DsdvHelper.findIndex(routeTable, desAddress);
        if (index == -1) {
            log.info("{} 没有找到下一跳节点", ownNode.getName());
            return;
        }
        message.getPreRoute().add(String.valueOf(ownNode.getPort()));
        try {
            transportUtil.send(message, routeTable.get(index).getNextHop());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUserMessage(Message receivePacket) {
        // 1. 拆包
        UserMessage message = (UserMessage) receivePacket;
        if (message.getDestAddress() == ownNode.getPort()) {
            message.setData(LinkMessage.decode(message.getData()));
            message.getPreRoute().add(String.valueOf(ownNode.getPort()));
            log.info("{} 收到战术消息：{}", ownNode.getName(), message.getData());
            log.info("消息路径为：{}", receivedMessage.getPreRoute());
            // 2. 交给上层处理
            synchronized (receivedMessage) {
                receivedMessage.copy(message);
            }
        } else {
            // 3. 转发
            log.info("{} 转发战术消息", ownNode.getName());
            forwardUserMessage(message, message.getDestAddress());
        }

    }
    private void handle(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream baos2 = new ByteArrayInputStream(receivePacket.getData());
        ObjectInputStream oos2 = new ObjectInputStream(baos2);
        Message receivedMessage = (Message) oos2.readObject();
        // 1. 判断是路由消息还是用户消息
        if (receivedMessage.getType() == Type.Data) {
            log.info("{} 收到用户消息", ownNode.getName());
            handleUserMessage(receivedMessage);
        } else if (receivedMessage.getType() == Type.Route) {
            log.debug("{} 收到路由消息", ownNode.getName());
            handleRouteMessage(receivedMessage);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                transportUtil.receive(receivePacket);
                handle(receivePacket);
                TimeUnit.MILLISECONDS.sleep(Constants.RECEIVE_PERIOD);
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                log.error("{} 接收失败, {}", ownNode.getName(),e.getMessage());
            }
        }
    }
    public void start() {
        log.info("{} 接收服务启动", ownNode.getName());
        if (receive == null) {
            receive = new Thread(this, "receive");
            receive.start();
        }
    }

}

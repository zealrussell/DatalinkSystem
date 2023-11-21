package com.zeal.linkmodel.transport.dsdv;

import com.zeal.linkmodel.packet.UserMessage;
import com.zeal.linkmodel.transport.TransportUtil;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import com.zeal.linkmodel.transport.dsdv.model.DsdvRoute;
import com.zeal.linkmodel.transport.dsdv.service.BroadcastService;
import com.zeal.linkmodel.transport.dsdv.service.ReceiveService;
import com.zeal.linkmodel.transport.dsdv.service.SendService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.TypeReference;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 14:34
 */
@Slf4j
public class DsdvController {

    private BroadcastService broadcastService;
    private ReceiveService receiveService;
    private SendService sendService;

    private DsdvNode ownNode;
    private TransportUtil transportUtil;

    // 路由表，key为目的节点序号，value为路由信息
    // 第一个节点为本节点
    private HashMap<Integer, DsdvRoute> routingTable;
    private HashMap<Integer, DsdvNode> neighbors;
    private BlockingQueue<UserMessage> messageQueue;

    private  UserMessage receivedMessage;
    private static String messageData = "hello world";

    public DsdvController(DsdvNode node, HashMap<Integer, DsdvNode> neighbors) throws SocketException, UnknownHostException {
        this.ownNode = node;
        this.routingTable = new HashMap<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.neighbors = neighbors;
        init();
    }

    private void init() throws SocketException, UnknownHostException {
        initTable();
        transportUtil = new TransportUtil(ownNode.getPort());
        broadcastService = new BroadcastService(neighbors, routingTable, transportUtil);
        receiveService = new ReceiveService(ownNode, neighbors, messageQueue, routingTable, transportUtil);
        sendService = new SendService(messageQueue, routingTable, transportUtil);
    }

    /**
     * 根据邻居节点表初始化路由表
     */
    public void initTable() {
        DsdvRoute ownRoute = new DsdvRoute();
        ownRoute.setDestName(ownNode.getName());
        ownRoute.setDestAddress(ownNode.getPort());
        ownRoute.setHopCount(0);

        routingTable.put(1, ownRoute);

        if (neighbors == null) {
            return;
        }

        neighbors.forEach((k,v)->{
            DsdvRoute route = new DsdvRoute();
            route.setDestAddress(v.getPort());
            route.setDestName(v.getName());
            route.setNextHop(v.getPort());
            route.setNextName(v.getName());
            route.setSeqNumber(0);
            route.setHopCount(1);
            routingTable.put(routingTable.size() + 1, route);
        });

    }

    /**
     * 生成战术消息
     */
    public void makeMessage(String data) {
        messageData = data;
    }

    /**
     * 发送消息
     * @param destAddress 目的节点序号
     */
    public void send(String destAddress)  {
        DsdvRoute destNode = DsdvHelper.findRouteByDesName(routingTable, destAddress);
        // 1. 如果没有目的节点的路由，则不发送
        if (destNode == null) {
            System.out.println("没有找到目的节点");
            return;
        } else {
            try {
                UserMessage message = new UserMessage(destNode.getDestAddress(), messageData);
                message.getPreRoute().add(String.valueOf(transportUtil.getPort()));
                messageQueue.put(message);
            } catch (InterruptedException e) {
                log.error("{} 发送消息失败: {}", ownNode.getName(), e.getMessage());
            }
        }
    }

    /**
     * 获取路由表
     *
     * @return 路由表
     */
    public HashMap<Integer, DsdvRoute> getRouteTable() {
        return this.routingTable;
    }

    /**
     * 打印路由表
     */
    public void printRouteTable() {
        DsdvHelper.printRouteTable(routingTable);
    }

    public List<Integer> getRoute(int destAddress) {
        return null;
    }
    public void start() {
        broadcastService.start();
        receiveService.start();
        sendService.start();
    }

    public List<String> getMessageRoute() {
        if (receivedMessage != null) {
            return receivedMessage.getPreRoute();
        }
        return null;
    }
    public void close() {
        transportUtil.close();
    }

}

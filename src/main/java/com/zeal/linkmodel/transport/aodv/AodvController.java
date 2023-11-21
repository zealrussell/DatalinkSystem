package com.zeal.linkmodel.transport.aodv;

import com.zeal.linkmodel.transport.aodv.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/6 11:40
 */
@Slf4j
public class AodvController extends Thread {

    private DatagramSocket socket;
    private int port;
    private int node_id;
    private byte sequenceNumber = 0;
    private int seq_no;
    private byte rreqID;

    // 邻居节点
    private Set<Integer> neighbors;

    private HashMap<Integer, Integer> messageRequests = new HashMap<>();

    // 路由表：端口号-》表
    private HashMap<Integer, Route> routingTable = new HashMap<>();
    // 序列号表
    private HashMap<Integer, Byte> sequenceTable = new HashMap<>();
    private BlockingQueue<Message> messagesQueue = new ArrayBlockingQueue<>(30);
    // 应用层消息队列
    private BlockingQueue<Message> userMessageQueue = new ArrayBlockingQueue<>(100);

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isWaitingforRREP = new AtomicBoolean(false);

    public AodvController() {

    }
    public boolean init() {
        if (!isRunning.get()) {
            isRunning.set(true);
        }
        return true;
    }

    /**
     * 更新节点的sequenceNumber
     */
    private void getNextSequenceNumber() {
        if (sequenceNumber == Constants.MAX_SEQUENCE_NUMBER) {
            sequenceNumber = 0;
        } else {
            sequenceNumber++;
        }
    }
    private void saveSequenceNumber(int address, byte sequenceNumber) {
        sequenceTable.put(address, sequenceNumber);
    }
    private void deleteRoute(int address) {
        routingTable.remove(address);
    }
    private void restartRouteTimer(int port) {

    }
    /**
     * 更新路由表
     */
    private void updateTable() {
        routingTable.forEach((key, value) -> {
            value.getLifetime();
        });
    }

    /**
     * 根据收到的RREQ消息添加路由表
     * @param message rreq消息
     */
    private void addRouteTable(RREP message) {
        Route route = new Route(message.getDestinationAddress(), message.getDestinationSequenceNumber(), message.getHopCount(), message.getPreHop());
        this.routingTable.put(message.getOriginAddress(), route);
    }
    /**
     * 检查序号是否是最新的
     * @param sequenceNumberToCheck 待检查的序号
     * @param currentSeqNumber 当前节点保存的最大序号
     * @return 结果
     */
    private boolean isSeqNew(byte sequenceNumberToCheck, byte currentSeqNumber) {
        return ((byte) (sequenceNumberToCheck - currentSeqNumber)) > 0;
    }

    // -------------------------------------------------------------------------------------------------------------------
    /**
     * 将生成的message发送到目的地
     * @param destinationAddress 目的节点
     * @param message 消息
     */
    private void send(int destinationAddress, Message message) {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", destinationAddress);
        DatagramPacket data = new DatagramPacket(message.toMessage(), 10, address);
        try {
            socket.send(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param message 消息
     */
    private void broadcast(Message message) {
        updateTable();
        neighbors.forEach((address) -> {
            send(address, message);
        });
    }
    /**
     * 转发到所有可用的路由表去
     * @param message 消息
     */
    private void forward(int nextAddress, Message message) {
        message.setPreHop(port);
        send(nextAddress, message);
    }


    private void sendUserMessage(Message message) {
        int destAddress = message.getDestination();
        // 1. 如果路由表有目的地址
        if (routingTable.containsKey(destAddress)) {
            Route route = routingTable.get(destAddress);
            // 如果过期了，则广播rreq消息
            if (!route.getValidRoute()) {
               // sendRouteRequest();
            } else {
                send(route.getNextHop(), message);

                log.info("User Message sent");
            }
        } else {
        // 2. 没有，则发送rreq
            RREQ rreq = new RREQ((byte) 0, (byte) 0, port, sequenceNumber, destAddress, sequenceNumber);
            sendRouteRequest(rreq);
        //
        }
    }
    /**
     * 处理应用层的消息
     */
    private void handleUserMessage(Message message) {
        log.debug("Get message from ");
        // 1. 如果自己是目的节点，就交由上层处理
        if (message.getDestination() == port) {
            log.debug(message.toString());
        } else {
            // 2. 否则转发消息
            Route route = routingTable.get(message.getDestination());

            send(route.getDestinationAddress(), message);
        }
    }
    /**
     * 向所有邻居节点发送 Hello 消息
     */
    private void sendHelloMessage() {

    }

    private void handleHelloMessage() {
        log.debug("Get Hello message");

    }
    /**
     * 生成RREQ消息
     * @param rreq rreq消息
     */
    private void sendRouteRequest(RREQ rreq) {
        updateTable();
        if(rreq.getOriginAddress() == port) {
            sequenceNumber++;
            rreqID++;
        } else {
            rreq.setHopCount((byte) (rreq.getHopCount() + (byte)1));
        }
    }

    /**
     * 处理RREQ消息
     * @param rreq 路由请求
     */
    private void handleRouteRequest(RREQ rreq) {
        // 1. 如果自己是源节点，忽略
        if (rreq.getOriginAddress() == port)
            return;

        // 2. 检查序列号,如果不是最新的RREQ消息则舍弃
        Route route = routingTable.get(rreq.getOriginAddress());
        if (route != null) {
            // 获取目的节点的序列号
            byte currentSequenceNumber = route.getDestinationSequenceNumber();
            if (rreq.getOriginSequenceNumber() < currentSequenceNumber) {
                return;
            } else {
                getNextSequenceNumber();
            }
        }

        // 3. 如果目的地址是本节点，则发送RREP
        if (rreq.getDestinationAddress() == port) {
            saveSequenceNumber(rreq.getOriginAddress(), rreq.getOriginSequenceNumber());
            log.debug("Get RREQ message" + rreq.toString());

            RREP rrep = new RREP(rreq.getPreHop(), rreq.getHopCount(), rreq.getOriginAddress(), rreq.getDestinationAddress(), sequenceNumber, (byte) 180);
            sendRouteReply(rrep);
            // 刷新路由
            return;
        } else if (routingTable.containsKey(rreq.getDestinationAddress())) {
        // 4. 如果路由表中有目的地址，则转发RREQ
                Route desRoute = routingTable.get(rreq.getDestinationAddress());
                if (desRoute.getDestinationSequenceNumber() >= rreq.getDestinationSequenceNumber()) {
                    // send();
                }

        } else {
            // 5. 否则广播RREQ
            rreq.increaseHopCount();
            rreq.setPreHop(port);
            broadcast(rreq);
        }

    }

    private void sendRouteReply(RREP rrep){

    }

    /**
     * 转发RREP报文
     * @param rrep rrep报文
     * @param nextAddress 下一跳地址
     */
    private void forwardRREP(RREP rrep, int nextAddress) {
        RREP tmp = new RREP(port, rrep.getHopCount(), rrep.getOriginAddress(), rrep.getDestinationAddress(), rrep.getDestinationSequenceNumber(), rrep.getLifetime());
        send(nextAddress, tmp);
    }

    /**
     * 处理RREP报文
     * @param rrep rrep报文
     */
    private void handleRouteReply(RREP rrep) {
        log.debug("Get RREP message" + rrep.toString());
        // 1. 如果自己是目的节点，则更新路由表
        if (rrep.getOriginAddress() == port) {
            sequenceTable.put(rrep.getDestinationAddress(), rrep.getDestinationSequenceNumber());
            addRouteTable(rrep);
            isWaitingforRREP.set(false);
        } else {
        // 2. 如果不是目的节点，则转发RREP
            Route route = routingTable.get(rrep.getDestinationAddress());
            // 2.1 如果路由表中有目的地址，直接转发
            if(!ObjectUtils.isEmpty(route) ) {
                rrep.increaseHopCount();
                forwardRREP(rrep, route.getNextHop());
                addRouteTable(rrep);
                // 刷新路由计时器
            } else {
                return;
            }

        }
    }

    /**
     * 生成RERR报文
     * @param brokenAddress 不可达的地址
     */
    private void sendRouteError(int brokenAddress) {
        if (routingTable.containsKey(brokenAddress)) {
            Route route = routingTable.get(brokenAddress);
            if (route.getValidRoute()) {
                RERR rerr = new RERR(port, route.getDestinationAddress(), route.getDestinationSequenceNumber());
                broadcast(rerr);
            }
        }
    }

    /**
     * 处理RERR报文
     * @param rerr rerr报文
     */
    private void handleRouteError(RERR rerr) {
        int dest = rerr.getUnreachableDestinationAddress();
        // 1. 如果本节点是不可达的节点
        if (rerr.getUnreachableDestinationAddress() == port) {
            log.info("Get RERR message" + rerr.toString());
            return;
        }
        // 2. 当路由表中有破损地址
        if (routingTable.containsKey(dest)) {
            Route route = routingTable.get(dest);
            // 2.1
            if (route.getValidRoute() && route.getNextHop() == rerr.getPreHop()) {
                route.setValidRoute(false);
                routingTable.put(dest, route);
                //TODO:: 转发rerr消息
                broadcast(rerr);
            }

        } else {
            log.info("No route to broken address");
        }
    }
    private void removeBrokenRoute(int brokenAddress) {
        routingTable.remove(brokenAddress);
    }

    /**
     *
     * @param message
     */
    private void route(Message message) {
        Route route = routingTable.get(message.getDestination());
        if (!ObjectUtils.isEmpty(route) && route.getValidRoute()) {

        }
    }




    // 处理逻辑，
    private void handler (Message message){
        switch (message.getType()) {
            case Type.RREQ:
                handleRouteRequest((RREQ) message);
                break;
            case Type.RREP:
                handleRouteReply((RREP) message);
                break;
            case Type.RERR:
                handleRouteError((RERR) message);
                break;
            case Type.SEND_TEXT_REQUEST:
                handleUserMessage(message);
                break;
            case Type.SEND_TEXT_REQUEST_ACK:
                break;
            case Type.SEND_HOP_ACK:
                break;
            default:
                break;
        }
    }
}

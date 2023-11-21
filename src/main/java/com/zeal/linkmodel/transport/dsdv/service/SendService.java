package com.zeal.linkmodel.transport.dsdv.service;

import com.zeal.linkmodel.packet.UserMessage;
import com.zeal.linkmodel.transport.TransportUtil;
import com.zeal.linkmodel.transport.dsdv.DsdvHelper;
import com.zeal.linkmodel.transport.dsdv.model.DsdvRoute;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 19:53
 */
@Slf4j
public class SendService implements Runnable {
    private HashMap<Integer, DsdvRoute> routingTable;
    private TransportUtil transportUtil;
    private Thread send;
    private BlockingQueue<UserMessage> messageQueue;

    public SendService(BlockingQueue<UserMessage> messageQueue, HashMap<Integer, DsdvRoute> routingTable, TransportUtil transportUtil) {
        this.messageQueue = messageQueue;
        this.routingTable = routingTable;
        this.transportUtil = transportUtil;
    }

    @Override
    public void run() {

        while (true) {
            try {
                // 1. 从消息队列中取出消息
                if (messageQueue.isEmpty()) continue;
                UserMessage message = messageQueue.take();
                if (message.getPreRoute().isEmpty()) {
                    message.getPreRoute().add(String.valueOf(transportUtil.getPort()));
                }
                // 2. 根据目的节点序号，查找路由表，找到下一跳节点，如果有路由，则发送
                int nextHop = DsdvHelper.findNextHop(routingTable, message.getDestAddress());
                if (nextHop != -1) {
                    transportUtil.send(message, nextHop);
                }
                // 3. 否则，通知更新路由表，然后将消息放入消息队列
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException | IOException e) {
                log.error("发送消息失败" + e.getMessage());
                throw new RuntimeException(e);
            }

        }
    }

    public void start() {
        log.info("发送服务启动");
        if (send == null) {
            send = new Thread(this);
            send.start();
        }
    }
}

package com.zeal.linkmodel;

import com.zeal.linkmodel.transport.dsdv.DsdvController;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import com.zeal.linkmodel.utils.PortUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/6 11:37
 */
@Slf4j
@Data
@AllArgsConstructor
public class LinkModel {

    private DsdvNode node;

    private DsdvController controller;

    // 邻接节点表
    private HashMap<Integer, DsdvNode> neighborTable;

    private BlockingQueue<String> messageQueue;

    public LinkModel(String name, int port, HashMap<Integer,DsdvNode> neighborTable) throws SocketException, UnknownHostException {
        node = new DsdvNode(name, port);
        controller = new DsdvController(node, neighborTable);
    }

    public void init () {
        // controller.printRouteTable();
        controller.start();
    }

    // 生成战术消息
    public void makeMessage(String data) {
        controller.makeMessage(data);
    }

    /**
     * 发送消息ddddc
     * @param desAddress 目的节点地址
     */
    public void sendMessage(String desAddress) {
        controller.send(desAddress);
    }

    /**
     * 获取消息路由路径
     * @return 消息路由路径
     */
    public List<String> getMessageRoute() {
        return controller.getMessageRoute();
    }

    public void printRoute() {
        controller.printRouteTable();
    }

    public void close() {
        controller.close();
    }
}

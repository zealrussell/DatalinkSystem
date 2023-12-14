package com.zeal.datalinksystem.controller;

import com.zeal.datalinksystem.common.ResultCommon;
import com.zeal.linkmodel.LinkModel;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/3 9:29
 */
@Slf4j
@RestController
public class NodeController {
    private static HashMap<String, DsdvNode> nodeMap;
    private static HashMap<String, LinkModel> modelMap;
    private HashMap<String, Integer> nameMap;
    private static int NODE_ID = 1;
    static {
        nodeMap = new HashMap<>();
        modelMap = new HashMap<>();
    }

    @GetMapping("/add/{name}/{neighbor}")
    public ResultCommon addNode(@PathVariable("name") String name, @PathVariable("neighbor") List<String> neighbor) {
        log.info("Add node {} !!!", name);
        List<String> neighborList = new ArrayList<>();
        if (modelMap.containsKey(name) ) {
            return ResultCommon.error("node already exists!!!");
        }

        DsdvNode node = new DsdvNode(name, 10010 + NODE_ID);
        //LinkModel linkModel = new LinkModel(node);
        //modelMap.put(name, linkModel);
        nodeMap.put(name, node);

        return ResultCommon.success("add success!!!");
    }

    /**
     * 删除节点
     * @param name 节点名称
     */
    @GetMapping("/delete/{name}")
    public ResultCommon deleteNode(@PathVariable("name") String name) {
        log.info("Delete node {}!!!", name);

        modelMap.get(name).close();
        modelMap.remove(name);
        nodeMap.remove(name);
        return ResultCommon.success("delete success!!!");
    }


    /**
     * 生成消息
     * @param name 源节点
     * @param data 消息内容
     */
    @GetMapping("/make/{name}/{type}/{data}")
    public ResultCommon makeMessage(@PathVariable("name") String name, @PathVariable int type, @PathVariable("data") String data) {
        modelMap.get(name).makeMessage(type, data);
        return ResultCommon.success("make success!!!");
    }

    /**
     * 发送消息，返回发送的路径
     * @param res 源节点
     * @param des 目的节点
     */
    @GetMapping("/send/{res}/{des}")
    public ResultCommon sendMessage(@PathVariable("res")String res, @PathVariable("des")String des) {
        log.info("Send a message!!!");
        ResultCommon resultCommon = new ResultCommon();
        List<String> routeList = new ArrayList<>();
        modelMap.get(res).sendMessage(des);

        while (routeList == null || routeList.isEmpty()) {
            routeList = modelMap.get(des).getMessageRoute();
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        resultCommon.setCode(200);
        resultCommon.setMsg("send success!!!");
        resultCommon.setData(routeList);

        return resultCommon;
    }


    @GetMapping("/test")
    public void test() {
        try {
            HashMap<Integer, DsdvNode> neighbor1 = new HashMap<>();
            neighbor1.put(1, new DsdvNode("node2", 10012));
            LinkModel linkModel = new LinkModel("node1", 10011, neighbor1);

            HashMap<Integer, DsdvNode> neighbor2 = new HashMap<>();
            neighbor2.put(1, new DsdvNode("node1", 10011));
            neighbor2.put(2, new DsdvNode("node3", 10013));
            LinkModel linkModel2 = new LinkModel("node2", 10012, neighbor2);

            HashMap<Integer, DsdvNode> neighbor3 = new HashMap<>();
            neighbor3.put(1, new DsdvNode("node2", 10012));
            LinkModel linkModel3 = new LinkModel("node3", 10013, neighbor3);

            linkModel.init();
            linkModel2.init();
            linkModel3.init();

            linkModel.printRoute();
            TimeUnit.SECONDS.sleep(10);
            linkModel.printRoute();
            linkModel.makeMessage(6, "Hello3, I am node1");
            linkModel.sendMessage("node3");
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static {
        try {
            DsdvNode node1 = new DsdvNode("node1", 10011);
            DsdvNode node2 = new DsdvNode("node2", 10012);
            DsdvNode node3 = new DsdvNode("node3", 10013);
            DsdvNode node4 = new DsdvNode("node4", 10014);
            DsdvNode node5 = new DsdvNode("node5", 10015);
            nodeMap.put("node1", node1);
            nodeMap.put("node2", node2);
            nodeMap.put("node3", node3);
            nodeMap.put("node4", node4);
            nodeMap.put("node5", node5);

            HashMap<Integer, DsdvNode> neighbor1 = new HashMap<>();
            HashMap<Integer, DsdvNode> neighbor2 = new HashMap<>();
            HashMap<Integer, DsdvNode> neighbor3 = new HashMap<>();
            HashMap<Integer, DsdvNode> neighbor4 = new HashMap<>();
            HashMap<Integer, DsdvNode> neighbor5 = new HashMap<>();

            neighbor1.put(1, node2);
            LinkModel linkModel1 = new LinkModel(node1.getName(), node1.getPort(), neighbor1);

            neighbor2.put(1, node1);
            neighbor2.put(2, node3);
            LinkModel linkModel2 = new LinkModel(node2.getName(), node2.getPort(), neighbor2);

            neighbor3.put(1, node2);
            neighbor3.put(2, node4);
            LinkModel linkModel3 = new LinkModel(node3.getName(), node3.getPort(), neighbor3);

            neighbor4.put(1, node3);
            neighbor4.put(2, node5);
            LinkModel linkModel4 = new LinkModel(node4.getName(), node4.getPort(), neighbor4);

            neighbor5.put(1, node4);
            LinkModel linkModel5 = new LinkModel(node5.getName(), node5.getPort(), neighbor5);

            modelMap.put("node1", linkModel1);
            modelMap.put("node2", linkModel2);
            modelMap.put("node3", linkModel3);
            modelMap.put("node4", linkModel4);
            modelMap.put("node5", linkModel5);
            for (LinkModel model : modelMap.values()) {
                model.init();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/addDefault")
    public ResultCommon add() throws SocketException, UnknownHostException, InterruptedException {
        if (nodeMap.containsKey("node6")) {
            return ResultCommon.error("node already exists!!!");
        }

        // 1. 添加节点1
        DsdvNode node6 = new DsdvNode("node6", 10016);
        HashMap<Integer, DsdvNode> neighbor6 = new HashMap<>();
        neighbor6.put(1, nodeMap.get("node1"));
        LinkModel linkModel6 = new LinkModel(node6.getName(), node6.getPort(), neighbor6);
        nodeMap.put("node6", node6);
        modelMap.put("node6", linkModel6);
        linkModel6.init();
        TimeUnit.SECONDS.sleep(1);
        linkModel6.printRoute();
        return ResultCommon.success("add success!!!");
    }

    @GetMapping("/deleteDefault")
    public ResultCommon delete() {
        if (!nodeMap.containsKey("node1")) {
            return ResultCommon.error("node not exists!!!");
        }
        LinkModel linkModel = modelMap.get("node6");
        linkModel.close();
        linkModel = null;

        nodeMap.remove("node6");
        modelMap.remove("node6");
        return ResultCommon.success("delete success!!!");
    }

}

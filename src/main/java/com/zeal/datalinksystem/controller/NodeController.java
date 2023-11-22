package com.zeal.datalinksystem.controller;

import com.zeal.datalinksystem.common.ResultCommon;
import com.zeal.linkmodel.LinkModel;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

        DsdvNode node = new DsdvNode(name, 10000 + NODE_ID);
        // LinkModel linkModel = new LinkModel(node);
        // modelMap.put(name, linkModel);
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
    @GetMapping("/make/{name}/{data}")
    public ResultCommon makeMessage(@PathVariable("name") String name, @PathVariable("data") String data) {
        modelMap.get(name).makeMessage(data);
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
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
            routeList = modelMap.get(res).getMessageRoute();
        } catch (InterruptedException e) {
            return ResultCommon.error(e.getMessage());
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
            neighbor1.put(1, new DsdvNode("node2", 10002));
            LinkModel linkModel = new LinkModel("node1", 10001, neighbor1);

            HashMap<Integer, DsdvNode> neighbor2 = new HashMap<>();
            neighbor2.put(1, new DsdvNode("node1", 10001));
            neighbor2.put(2, new DsdvNode("node3", 10003));
            LinkModel linkModel2 = new LinkModel("node2", 10002, neighbor2);

            HashMap<Integer, DsdvNode> neighbor3 = new HashMap<>();
            neighbor3.put(1, new DsdvNode("node2", 10002));
            LinkModel linkModel3 = new LinkModel("node3", 10003, neighbor3);

            linkModel.init();
            linkModel2.init();
            linkModel3.init();
            linkModel.printRoute();
            TimeUnit.SECONDS.sleep(10);
            linkModel.printRoute();
            linkModel.makeMessage("Hello3, I am node1");
            linkModel.sendMessage("node3");
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

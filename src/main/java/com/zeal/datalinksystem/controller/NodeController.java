package com.zeal.datalinksystem.controller;

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
    }
    @GetMapping("/add")
    public String addNode() {
        log.info("Add a new node!!!");
        String name = "";
        List<String> neighborList = new ArrayList<>();
        DsdvNode node = new DsdvNode("node" + NODE_ID, 10000 + NODE_ID);

        return "";
    }

    @GetMapping("/delete/{id}")
    public String deleNode(@PathVariable("id") int id) {
        log.info("Delete a node!!!");
        nodeMap.remove(id);
        return "delete success";
    }


    @GetMapping("/make/{name}/{data}")
    public void makeMessage(@PathVariable("name") String name, @PathVariable("data") String data) {
        modelMap.get(name).makeMessage(data);
    }

    /**
     * 发送消息，返回发送的路径
     * @param res 源节点
     * @param des 目的节点
     */
    @GetMapping("/send/{res}/{des}")
    public List<String> sendMessage(@PathVariable("res")String res, @PathVariable("des")String des) {
        log.info("Send a message!!!");
        List<String> routeList = new ArrayList<>();
        modelMap.get(res).sendMessage(des);
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
            routeList = modelMap.get(res).getMessageRoute();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return routeList;
    }

    @GetMapping("/update")
    public void updateRoute() {

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
            //linkModel2.printRoute();
            //linkModel3.printRoute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

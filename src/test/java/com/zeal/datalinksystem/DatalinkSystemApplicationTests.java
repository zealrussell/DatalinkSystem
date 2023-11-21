package com.zeal.datalinksystem;

import com.zeal.linkmodel.LinkModel;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class DatalinkSystemApplicationTests {

    @Test
    void contextLoads() throws SocketException, UnknownHostException {

        HashMap<Integer, DsdvNode> neighbor2 = new HashMap<>();
        neighbor2.put(1, new DsdvNode("node1", 10001));
        neighbor2.put(2, new DsdvNode("node3", 10003));
        DsdvNode node = neighbor2.get(2);
        node.setPort(10002);
        System.out.println(neighbor2);
    }


}

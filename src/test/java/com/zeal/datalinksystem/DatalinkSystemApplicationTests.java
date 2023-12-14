package com.zeal.datalinksystem;

import com.zeal.linkmodel.LinkModel;
import com.zeal.linkmodel.packet.UserMessage;
import com.zeal.linkmodel.transport.dsdv.model.DsdvNode;
import com.zeal.linkmodel.utils.AesUtil;
import com.zeal.linkmodel.utils.CrcUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class DatalinkSystemApplicationTests {

    @Test
    void contextLoads() throws SocketException, UnknownHostException {
    }

    @Test
    void crcTest() {
        String data = "hello world";
        String msg = CrcUtil.encode(data);
        System.out.println("msg:" + msg);
        System.out.println(CrcUtil.decode(msg));
    }

    @Test
    void aesTest() {
        String data = "hello world";
        String msg = AesUtil.encode(data);
        System.out.println("msg:" + msg);
        System.out.println(AesUtil.decode(msg));

    }
    @Test
    void userMessageTest() {
        UserMessage userMessage = new UserMessage(8090,8091,"011110111101111000000000001110000011001000111101110000000011100110010011000010001010010101100100011011000001010000000000011000011011110111000101100000100000011010111000110000110011110100011101111010100110000101001101010111010101001011010011000001011011000110011011011110111010100101101000");
        System.out.println(userMessage.toString());
    }

    @Test
    void messageQueueTest() {

        UserMessage userMessage6 = new UserMessage(8090,8091,"测试消息6", 6);
        UserMessage userMessage5 = new UserMessage(8090,8091,"测试消息5", 5);
        UserMessage userMessage1 = new UserMessage(8090,8091,"测试消息1", 1);
        UserMessage userMessage2 = new UserMessage(8090,8091,"测试消息2", 2);
        UserMessage userMessage4 = new UserMessage(8090,8091,"测试消息4", 4);
        UserMessage userMessage21 = new UserMessage(8090,8091,"测试消息2.1", 2);
        UserMessage userMessage3 = new UserMessage(8090,8091,"测试消息3", 3);


        BlockingQueue<UserMessage> messageQueue = new LinkedBlockingQueue<>(20);

        messageQueue.add(userMessage2);
        messageQueue.add(userMessage5);
        messageQueue.add(userMessage21);
        messageQueue.add(userMessage4);
        messageQueue.add(userMessage3);


        System.out.println(messageQueue.toString());
    }

    @Test
    void nodeTest() {
        try {
            HashMap<Integer, DsdvNode> neighbor1 = new HashMap<>();
            neighbor1.put(1, new DsdvNode("node2", 10012));
            LinkModel linkModel1 = new LinkModel("node1", 10011, neighbor1);

            HashMap<Integer, DsdvNode> neighbor2 = new HashMap<>();
            neighbor2.put(1, new DsdvNode("node1", 10011));
            neighbor2.put(2, new DsdvNode("node3", 10013));
            LinkModel linkModel2 = new LinkModel("node2", 10012, neighbor2);

            HashMap<Integer, DsdvNode> neighbor3 = new HashMap<>();
            neighbor3.put(1, new DsdvNode("node2", 10012));
            LinkModel linkModel3 = new LinkModel("node3", 10013, neighbor3);

            linkModel1.init();
            linkModel2.init();
            linkModel3.init();
            linkModel1.printRoute();
            linkModel1.makeMessage(6, "Hello3, I am node1");
            linkModel1.sendMessage("node3");
            TimeUnit.SECONDS.sleep(5);
            linkModel1.printRoute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

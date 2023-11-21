package com.zeal.linkmodel.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import java.util.Base64;


/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/6 12:34
 */
@Slf4j
public class PortUtil {

    public static void send(DatagramSocket socket, String msg, int desAddress) {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", desAddress);
        DatagramPacket data = new DatagramPacket(msg.getBytes(), msg.length(), address);
        try {
            socket.send(data);
            log.info("Send data: " + msg);
        } catch (IOException e) {
            System.out.println("Send error!!");
            log.error("Port send error");
        }
    }

    public static void receive(DatagramSocket socket) {
        try {
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", socket.getLocalPort());
            byte[] dataIn = new byte[1024];
            DatagramPacket inputData = new DatagramPacket(dataIn, 1024);
            while(true) {
                socket.receive(inputData);
                String data = Base64.getEncoder().encodeToString(inputData.getData());
                log.info("Receive data: " + new String(dataIn,0 ,inputData.getLength()));
            }
        } catch (IOException e) {
            log.error("receive error");
        }
    }
}

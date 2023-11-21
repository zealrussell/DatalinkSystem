package com.zeal.linkmodel.transport;

import com.zeal.linkmodel.transport.dsdv.DsdvController;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/7 22:06
 */
@Slf4j
public class TransportUtil {
    private DatagramSocket socket;
    private static InetAddress address = null;

    public TransportUtil() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }
    public TransportUtil(int port) throws UnknownHostException, SocketException {
        this.socket = new DatagramSocket(port);
        address = InetAddress.getByName("localhost");
    }

    public int getPort() {
        return socket.getLocalPort();
    }
    public void sendPacket(DatagramPacket packet) throws IOException {
        // log.info("send packet: {}", packet);
        socket.send(packet);
    }
    public void send(Object message, int destination) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        byte[] buf = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destination);
        socket.send(packet);
    }

    public void receive(DatagramPacket packet) throws IOException {
        socket.receive(packet);
    }

    public void close() {
        this.socket.close();
    }


}
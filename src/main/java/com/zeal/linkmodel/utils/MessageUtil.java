package com.zeal.linkmodel.utils;

import com.zeal.linkmodel.transport.aodv.message.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/8 15:48
 */
public class MessageUtil {

    /**
     * 将从udp收到的消息解码成为Message类型
     * @return 消息
     */
    private static Message decode(byte[] bytes) {
        if (bytes.length < 10)
            return null;
        // Copy LR,ADDR,NUMB_BYTES,
        byte[] atPacketBytes = Arrays.copyOfRange(bytes, 0, 10);
        // Get ADDR
        String at = new String(atPacketBytes, StandardCharsets.US_ASCII);
        String[] atPacket = at.split(",");
        int prevHop =  Integer.parseInt(atPacket[1]);

        // Copy AODV Message without Lora header
        byte[] data = Arrays.copyOfRange(bytes, 11, bytes.length);

        switch ((int) data[0]) {
//            case (Type.RREQ): {
//                if (data.length != 8)
//                    return null;
//                RREQ rreq = new RREQ(data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
//                rreq.setPreHop(prevHop);
//                return rreq;
//            }
            case (Type.RREP): {
                if (data.length != 6)
                    return null;
                RREP rrep = new RREP(0, data[1], data[2], data[3], data[4], data[5]);
                rrep.setPreHop(prevHop);
                return rrep;
            }
            case (Type.RERR): {
                if (data.length < 6)
                    return null;
                LinkedList<Byte> additionalAddresses = new LinkedList<>();
                LinkedList<Byte> additionalSequenceNumber = new LinkedList<>();
                // (Additional Addr),(Additional Seq),(Additional Addr), (Additional Seq), ....
                //  4               , 5             , 6                , 7               , ....
                int offset = 4;
                for (int i = 0; i < (data.length - offset); i = i + 2) {
                    additionalAddresses.add(data[offset + i]);
                    additionalSequenceNumber.add(data[offset + i + 1]);
                }
                RERR rrer = new RERR(0, data[1], data[2]);
                rrer.setPreHop(prevHop);
                return rrer;
            }
//            case (Type.RREP_ACK): {
//                if (data.length != 1)
//                    return null;
//                RREP_ACK rrepAck = new RREP_ACK("");
//                rrepAck.setPreHop(prevHop);
//                return rrepAck;
//            }
//            case (Type.SEND_TEXT_REQUEST): {
//                if (data.length < 5 || data.length > 34)
//                    return null;
//                byte[] payloadBytes = Arrays.copyOfRange(data, 4, data.length);
//                String payload = new String(payloadBytes, StandardCharsets.US_ASCII);
//                SEND_TEXT_REQUEST sendTextRequest = new SEND_TEXT_REQUEST("", data[1], data[2], data[3], payload);
//                sendTextRequest.setPrevHop(prevHop);
//                return sendTextRequest;
//            }
//            case (Type.SEND_HOP_ACK): {
//                if (data.length != 2)
//                    return null;
//                SEND_HOP_ACK sendHopAck = new SEND_HOP_ACK("", data[1]);
//                sendHopAck.setPrevHop(prevHop);
//                return sendHopAck;
//            }
//            case (Type.SEND_TEXT_REQUEST_ACK): {
//                if (data.length != 4)
//                    return null;
//                SEND_TEXT_REQUEST_ACK sendTextRequestAck = new SEND_TEXT_REQUEST_ACK("", data[1], data[2], data[3]);
//                sendTextRequestAck.setPrevHop(prevHop);
//                return sendTextRequestAck;
//            }
        }
        return null;
    }
}

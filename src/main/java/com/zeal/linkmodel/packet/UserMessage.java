package com.zeal.linkmodel.packet;

import com.zeal.linkmodel.transport.dsdv.model.Message;
import com.zeal.linkmodel.transport.dsdv.model.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 21:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserMessage extends Message {
    private List<String> preRoute;
    private int priority;
    private String data;

    public UserMessage(int srcAddress, int destAddress, String data) {
        super(srcAddress, destAddress, Type.Data);
        preRoute = new ArrayList<>();
        priority = 5;
        this.data = data;
    }
    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

}

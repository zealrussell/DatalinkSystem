package com.zeal.linkmodel.packet;

import com.zeal.linkmodel.transport.dsdv.model.Message;
import com.zeal.linkmodel.transport.dsdv.model.Type;
import lombok.*;

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
@ToString(callSuper = true)
public class UserMessage extends Message {
    private List<String> preRoute;
    private int priority;
    private String data;

    public UserMessage(int srcAddress, int destAddress, String data) {
        super(srcAddress, destAddress, Type.Data);
        preRoute = new ArrayList<>();
        this.priority = 6;
        this.data = data;
    }
    public UserMessage(int srcAddress, int destAddress, String data, int priority) {
        super(srcAddress, destAddress, Type.Data);
        preRoute = new ArrayList<>();
        this.priority = priority;
        this.data = data;
    }

    public UserMessage() {

    }

    public void copy(UserMessage message) {
        this.srcAddress = message.getSrcAddress();
        this.destAddress = message.getDestAddress();
        this.type = message.getType();
        this.preRoute = message.getPreRoute();
        this.priority = message.getPriority();
        this.data = message.getData();

    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

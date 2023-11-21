package com.zeal.linkmodel.transport.dsdv.model;

import lombok.Data;

import java.io.Serializable;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 11:23
 */
@Data
public class DsdvNode implements Serializable {
    private String name;
    private int port;
    private int distance;

    public DsdvNode(String name, int port) {
        this.name = name;
        this.port = port;

    }
}

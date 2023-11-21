package com.zeal.linkmodel.transport.dsdv.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/12 11:09
 */
@Data
public class DsdvRoute implements Serializable {
    private int destAddress;
    private String destName;
    private int nextHop;
    private String nextName;
    private int seqNumber = 0;
    private int hopCount = 0;
    private Date date;
}

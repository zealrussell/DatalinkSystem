package com.zeal.linkmodel.application;

import com.zeal.linkmodel.utils.AesUtil;
import com.zeal.linkmodel.utils.CrcUtil;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/14 11:46
 */
public class LinkMessage {

    public static String encode(String message) {
        // 1. aes
        String aesMessage = AesUtil.encode(message);
        // 2. crc
        String crcMessage = CrcUtil.encode(aesMessage);
        // 3. rs
        return crcMessage;
    }

    /**
     * 解码
     * @param message
     * @return
     */
    public static String decode(String message) {
        String crcMessage = CrcUtil.decode(message);
        String aesMessage = AesUtil.decode(crcMessage);
        return aesMessage;
    }

}

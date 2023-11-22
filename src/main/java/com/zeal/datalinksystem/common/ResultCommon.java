package com.zeal.datalinksystem.common;

import lombok.Data;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/11/20 20:04
 */
@Data
public class ResultCommon {
    private int code;
    private String msg;
    private Object data;

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 500;
    public ResultCommon() {
    }

    public ResultCommon(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultCommon(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResultCommon success() {
        return new ResultCommon(SUCCESS_CODE, "success");
    }

    public static ResultCommon success(Object data) {
        return new ResultCommon(SUCCESS_CODE, "success", data);
    }

    public static ResultCommon error(String message) {
        return new ResultCommon(ERROR_CODE, "error");
    }

}

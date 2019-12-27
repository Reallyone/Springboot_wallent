package com.really.spring_wallent.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * ajax请求统一返回对象
 *
 * @param <T>
 */
@Data
public class JsonResult<T> implements Serializable {

    private String message;
    private boolean success;
    private T data;

    public JsonResult(String message, boolean success, T data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public JsonResult(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public final static JsonResult SUCCESS = new JsonResult<>(null, true, null);
    public final static JsonResult FAIL = new JsonResult<>(null, false, null);


}

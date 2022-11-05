package com.ling.flowable;

public class RespBean {

    private String code;
    private String message;
    private Object data;

    public RespBean() {
    }

    public RespBean(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public static RespBean ok(String message, Object data){
        return new RespBean(message, data);
    }

    @Override
    public String toString() {
        return "RespBean{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

package com.mugiranezaJ.bBoss.backend.payload.response;


import org.springframework.http.HttpStatus;

public class ResponseBody {

    private String message;
    private Object data;
    private int statusCode;

    public ResponseBody( int statusCode, String message, Object data) {
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Object getData(){
        return data;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(String data){
        this.data = data;
    }
    public void setStatusCode(int httpStatus){
        this.statusCode = httpStatus;
    }
}

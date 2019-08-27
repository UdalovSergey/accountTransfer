package com.github.udalovsergey.bank.controller;

public class ResponseBody {

    private final String responseBody;
    private final int statusCode;

    public ResponseBody(String responseBody, int statusCode) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }

    public ResponseBody(int statusCode) {
        this(null, statusCode);
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

}

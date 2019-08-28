package com.github.udalovsergey.bank.controller;

import com.github.udalovsergey.bank.account.exception.AccountNotFoundException;
import com.github.udalovsergey.bank.transaction.exception.TransactionProcessingException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BankErrorHandler extends ErrorHandler {


    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");

        String jsonResponse = new JSONObject(new ErrorMessage(exception.getMessage())).toString();
        if (exception instanceof AccountNotFoundException) {
            writeJsonResponse(response, jsonResponse, HttpStatus.NOT_FOUND_404);
            return;
        } else if (exception instanceof TransactionProcessingException) {
            writeJsonResponse(response, jsonResponse, HttpStatus.FORBIDDEN_403);
            return;
        }
        writeJsonResponse(response, jsonResponse, HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private void writeJsonResponse(HttpServletResponse response, String jsonString, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(jsonString);
        out.flush();
    }

    public static final class ErrorMessage {
        private final String message;

        private ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }


}

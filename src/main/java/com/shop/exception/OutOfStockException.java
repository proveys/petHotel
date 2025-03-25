package com.shop.exception;
 // 예외처리 직접 만듬
public class OutOfStockException extends RuntimeException{
    public OutOfStockException(String message) {super(message);}
}

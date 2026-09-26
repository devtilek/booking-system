package com.nailstudio.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Запись с ID " + id + " не найдена");
    }
}

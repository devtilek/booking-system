package com.nailstudio.exception;

public class SlotNotFoundException extends RuntimeException {
    public SlotNotFoundException(Long id) {
        super("Слот с ID " + id + " не найден");
    }
}

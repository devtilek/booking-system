package com.nailstudio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingRequest(
        @NotBlank(message = "Укажите имя")
        @Size(max = 100, message = "Имя слишком длинное")
        String name,

        @NotBlank(message = "Укажите телефон")
        @Size(max = 30, message = "Телефон слишком длинный")
        String phone,

        @Size(max = 150, message = "Название услуги слишком длинное")
        String service,

        @Size(max = 1000, message = "Комментарий слишком длинный")
        String comment
) {
}

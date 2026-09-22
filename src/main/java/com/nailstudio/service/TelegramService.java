package com.nailstudio.service;

import com.nailstudio.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.format.DateTimeFormatter;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.admin.chat.ids}")
    private String adminChatIds;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void notifyNewBooking(Booking booking) {
        if (botToken == null || botToken.isBlank()
                || adminChatIds == null || adminChatIds.isBlank()) {
            log.warn("Telegram не настроен, уведомление не отправлено");
            return;
        }

        String text = "🔔 *Новая запись!*\n\n"
                + "👤 " + safe(booking.getClientName()) + "\n"
                + "📞 " + safe(booking.getPhone()) + "\n"
                + "💅 " + safe(booking.getService()) + "\n"
                + "📅 " + booking.getSlot().getDateTime().format(FMT) + "\n"
                + (booking.getComment() != null && !booking.getComment().isBlank()
                ? "💬 " + booking.getComment() : "");

        for (String chatId : adminChatIds.split(",")) {
            sendMessage(chatId.trim(), text);
        }
    }

    @Async
    public void notifyBookingCancelled(Booking booking) {
        if (botToken == null || botToken.isBlank()
                || adminChatIds == null || adminChatIds.isBlank()) return;

        String text = "❌ *Запись отменена*\n\n"
                + "👤 " + safe(booking.getClientName()) + "\n"
                + "📞 " + safe(booking.getPhone()) + "\n"
                + "📅 " + booking.getSlot().getDateTime().format(FMT);

        for (String chatId : adminChatIds.split(",")) {
            sendMessage(chatId.trim(), text);
        }
    }

    private String safe(String s) { return s == null ? "—" : s; }

    private void sendMessage(String chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", text);
            body.put("parse_mode", "Markdown");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> request =
                    new org.springframework.http.HttpEntity<>(body, headers);

            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.error("Ошибка отправки в Telegram: {}", e.getMessage());
        }
    }
}
package com.nailstudio.service;

import com.nailstudio.model.*;
import com.nailstudio.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final TimeSlotRepository slotRepo;
    private final BookingRepository bookingRepo;
    private final WorkRepository workRepo;
    private final PriceItemRepository priceRepo;
    private final TelegramService telegram;

    public BookingService(TimeSlotRepository slotRepo,
                          BookingRepository bookingRepo,
                          WorkRepository workRepo,
                          PriceItemRepository priceRepo,
                          TelegramService telegram) {
        this.slotRepo = slotRepo;
        this.bookingRepo = bookingRepo;
        this.workRepo = workRepo;
        this.priceRepo = priceRepo;
        this.telegram = telegram;
    }

    // ==== SLOTS ====
    public List<TimeSlot> getFreeSlots() {
        return slotRepo.findByBookedFalseAndDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());
    }

    public List<TimeSlot> getAllSlots() {
        return slotRepo.findAllByOrderByDateTimeAsc();
    }

    @Transactional
    public TimeSlot addSlot(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("Дата и время обязательны");
        }
        if (!dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя создать слот в прошлом");
        }
        if (slotRepo.existsByDateTime(dateTime)) {
            throw new IllegalArgumentException("Слот на это время уже существует");
        }

        TimeSlot slot = new TimeSlot();
        slot.setDateTime(dateTime);
        return slotRepo.save(slot);
    }

    @Transactional
    public void deleteSlot(Long id) {
        TimeSlot slot = slotRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Слот не найден"));

        if (slot.isBooked()) {
            throw new IllegalStateException(
                    "Нельзя удалить занятый слот. Сначала отмените запись."
            );
        }

        slotRepo.delete(slot);
    }

    // ==== BOOKINGS ====
    @Transactional
    public Booking book(Long slotId, String name, String phone, String service, String comment) {
        validateClientData(name, phone, comment);

        TimeSlot slot = slotRepo.findByIdForUpdate(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Слот не найден"));

        if (!slot.getDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Нельзя записаться на прошедшее время");
        }

        if (slot.isBooked()) {
            throw new IllegalStateException("Это время уже занято");
        }

        String normalizedService = normalizeService(service);

        slot.setBooked(true);

        Booking booking = new Booking();
        booking.setClientName(name.trim());
        booking.setPhone(phone.trim());
        booking.setService(normalizedService);
        booking.setComment(normalizeComment(comment));
        booking.setSlot(slot);

        Booking saved = bookingRepo.save(booking);

        telegram.notifyNewBooking(saved);
        return saved;
    }

    private void validateClientData(String name, String phone, String comment) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Укажите имя");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Укажите телефон");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Имя слишком длинное");
        }
        if (phone.trim().length() > 30) {
            throw new IllegalArgumentException("Телефон слишком длинный");
        }
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Комментарий слишком длинный");
        }
    }

    private String normalizeService(String service) {
        if (service == null || service.isBlank()) {
            return null;
        }

        String requestedService = service.trim();

        return priceRepo.findByNameIgnoreCase(requestedService)
                .map(PriceItem::getName)
                .orElseThrow(() -> new IllegalArgumentException("Выбранная услуга не найдена"));
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalized = comment.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public Page<Booking> getBookings(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return bookingRepo.findAllByOrderByCreatedAtDesc(
                PageRequest.of(safePage, safeSize)
        );
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = getBookingForUpdate(bookingId);

        requireStatus(booking, BookingStatus.PENDING, "подтвердить");

        if (!booking.getSlot().getDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Нельзя подтвердить запись на прошедшее время");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Transactional
    public void completeBooking(Long bookingId) {
        Booking booking = getBookingForUpdate(bookingId);

        requireStatus(booking, BookingStatus.CONFIRMED, "завершить");
        booking.setStatus(BookingStatus.COMPLETED);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingForUpdate(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Запись уже отменена");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Нельзя отменить завершённую запись");
        }

        TimeSlot slot = booking.getSlot();
        slot.setBooked(false);
        booking.setStatus(BookingStatus.CANCELLED);

        telegram.notifyBookingCancelled(booking);
    }

    private Booking getBookingForUpdate(Long bookingId) {
        return bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));
    }

    private void requireStatus(Booking booking, BookingStatus expected, String action) {
        if (booking.getStatus() != expected) {
            throw new IllegalStateException(
                    "Нельзя " + action + " запись со статусом " + booking.getStatus()
            );
        }
    }

    // ==== WORKS ====
    public List<Work> getWorks() {
        return workRepo.findAll();
    }

    public void addWork(Work work) {
        workRepo.save(work);
    }

    public void deleteWork(Long id) {
        workRepo.deleteById(id);
    }

    // ==== PRICES ====
    public List<PriceItem> getPrices() {
        return priceRepo.findAll();
    }

    public void addPrice(PriceItem price) {
        priceRepo.save(price);
    }

    public void deletePrice(Long id) {
        priceRepo.deleteById(id);
    }
}

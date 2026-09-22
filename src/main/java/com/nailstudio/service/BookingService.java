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

    public TimeSlot addSlot(LocalDateTime dt) {
        TimeSlot s = new TimeSlot();
        s.setDateTime(dt);
        return slotRepo.save(s);
    }

    @Transactional
    public void deleteSlot(Long id) {
        TimeSlot slot = slotRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Слот не найден"));
        if (slot.isBooked()) {
            throw new RuntimeException("Нельзя удалить занятый слот. Сначала отмените запись.");
        }
        slotRepo.deleteById(id);
    }

    // ==== BOOKINGS ====
    @Transactional
    public Booking book(Long slotId, String name, String phone, String service, String comment) {
        if (name == null || name.isBlank()) throw new RuntimeException("Укажите имя");
        if (phone == null || phone.isBlank()) throw new RuntimeException("Укажите телефон");
        name = name.trim();
        phone = phone.trim();
        if (name.length() > 100) throw new RuntimeException("Имя слишком длинное");
        if (phone.length() > 30) throw new RuntimeException("Телефон слишком длинный");

        TimeSlot slot = slotRepo.findByIdForUpdate(slotId)
                .orElseThrow(() -> new RuntimeException("Слот не найден"));

        if (slot.isBooked()) {
            throw new RuntimeException("Это время уже занято");
        }

        slot.setBooked(true);
        slotRepo.save(slot);

        Booking b = new Booking();
        b.setClientName(name);
        b.setPhone(phone);
        b.setService(service);
        b.setComment(comment);
        b.setSlot(slot);
        Booking saved = bookingRepo.save(b);

        telegram.notifyNewBooking(saved);
        return saved;
    }

    public Page<Booking> getBookings(int page, int size) {
        return bookingRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        TimeSlot slot = b.getSlot();
        slot.setBooked(false);
        slotRepo.save(slot);

        bookingRepo.delete(b);
        telegram.notifyBookingCancelled(b);
    }

    // ==== WORKS ====
    public List<Work> getWorks() { return workRepo.findAll(); }
    public void addWork(Work w) { workRepo.save(w); }
    public void deleteWork(Long id) { workRepo.deleteById(id); }

    // ==== PRICES ====
    public List<PriceItem> getPrices() { return priceRepo.findAll(); }
    public void addPrice(PriceItem p) { priceRepo.save(p); }
    public void deletePrice(Long id) { priceRepo.deleteById(id); }
}
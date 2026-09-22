package com.nailstudio.controller;

import com.nailstudio.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final BookingService service;

    public HomeController(BookingService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("slots", service.getFreeSlots());
        model.addAttribute("works", service.getWorks());
        model.addAttribute("prices", service.getPrices());
        return "index";
    }

    @PostMapping("/book")
    public String book(@RequestParam Long slotId,
                       @RequestParam String name,
                       @RequestParam String phone,
                       @RequestParam(name = "service", required = false) String selectedService,
                       @RequestParam(required = false) String comment,
                       RedirectAttributes ra) {
        try {
            service.book(slotId, name, phone, selectedService, comment);
            ra.addFlashAttribute("success", "Вы успешно записаны! Мы свяжемся с вами для подтверждения.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
package com.nailstudio.controller;

import com.nailstudio.model.PriceItem;
import com.nailstudio.model.Work;
import com.nailstudio.service.BookingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookingService service;

    @Value("${upload.dir}")
    private String uploadDir;

    public AdminController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public String panel(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("slots", service.getAllSlots());
        model.addAttribute("works", service.getWorks());
        model.addAttribute("prices", service.getPrices());
        model.addAttribute("bookings", service.getBookings(page, 20));
        return "admin";
    }

    // ==== SLOTS ====
    @PostMapping("/slot/add")
    public String addSlot(@RequestParam String date, @RequestParam String time,
                          RedirectAttributes ra) {
        try {
            LocalDateTime dt = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
            service.addSlot(dt);
            ra.addFlashAttribute("msg", "Слот добавлен: " + dt);
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/slot/delete/{id}")
    public String deleteSlot(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.deleteSlot(id);
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==== BOOKINGS ====
    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cancelBooking(id);
            ra.addFlashAttribute("msg", "Запись отменена");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==== WORKS ====
    @PostMapping("/work/add")
    public String addWork(@RequestParam String title,
                          @RequestParam MultipartFile file,
                          RedirectAttributes ra) {
        try {
            if (file.isEmpty()) throw new RuntimeException("Файл пустой");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Можно загрузить только изображение");
            }

            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String fname = UUID.randomUUID() + ".jpg";
            Path target = dir.resolve(fname);

            BufferedImage src = ImageIO.read(file.getInputStream());
            if (src == null) throw new RuntimeException("Не удалось прочитать изображение");

            // Сжимаем до 1200px по большей стороне
            BufferedImage out = resize(src, 1200);
            ImageIO.write(out, "jpg", target.toFile());

            Work w = new Work();
            w.setTitle(title);
            w.setImageUrl("/uploads/" + fname);
            service.addWork(w);
            ra.addFlashAttribute("msg", "Фото загружено");
        } catch (Exception e) {
            ra.addFlashAttribute("err", "Ошибка загрузки: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/work/delete/{id}")
    public String deleteWork(@PathVariable Long id) {
        service.deleteWork(id);
        return "redirect:/admin";
    }

    // ==== PRICES ====
    @PostMapping("/price/add")
    public String addPrice(@RequestParam String name,
                           @RequestParam Integer price,
                           @RequestParam(required = false) String description) {
        PriceItem p = new PriceItem();
        p.setName(name);
        p.setPrice(price);
        p.setDescription(description);
        service.addPrice(p);
        return "redirect:/admin";
    }

    @PostMapping("/price/delete/{id}")
    public String deletePrice(@PathVariable Long id) {
        service.deletePrice(id);
        return "redirect:/admin";
    }

    // ==== UTIL ====
    private BufferedImage resize(BufferedImage src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) {
            // всё равно конвертируем в RGB (для JPEG)
            BufferedImage rgb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return rgb;
        }

        double ratio = (double) maxSize / Math.max(w, h);
        int nw = (int) (w * ratio);
        int nh = (int) (h * ratio);

        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }
}
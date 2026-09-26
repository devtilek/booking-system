package com.nailstudio.controller;

import com.nailstudio.model.PriceItem;
import com.nailstudio.model.Work;
import com.nailstudio.service.BookingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

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
    @PostMapping("/booking/confirm/{id}")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.confirmBooking(id);
            ra.addFlashAttribute("msg", "Запись подтверждена");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/booking/complete/{id}")
    public String completeBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.completeBooking(id);
            ra.addFlashAttribute("msg", "Запись завершена");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin";
    }

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

    @PostMapping("/work/add")
    public String addWork(@RequestParam(required = false) String title,
                          @RequestParam MultipartFile file,
                          RedirectAttributes ra) {
        try {
            if (file.isEmpty()) throw new RuntimeException("Файл пустой");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Можно загрузить только изображение");
            }

            // Сжимаем до 1200px
            BufferedImage src = ImageIO.read(file.getInputStream());
            if (src == null) throw new RuntimeException("Не удалось прочитать изображение");
            BufferedImage out = resize(src, 1200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(out, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            String fname = UUID.randomUUID() + ".jpg";
            String uploadUrl = supabaseUrl + "/storage/v1/object/"
                    + supabaseBucket + "/" + fname;

            WebClient webClient = WebClient.builder()
                    .defaultHeader("Authorization", "Bearer " + supabaseKey)
                    .defaultHeader("apikey", supabaseKey)   // ← ДОБАВИТЬ ЭТО
                    .build();

            webClient.post()
                    .uri(uploadUrl)
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .header("x-upsert", "true")
                    .bodyValue(imageBytes)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(String.class);
                        } else {
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> reactor.core.publisher.Mono.error(
                                            new RuntimeException("Supabase "
                                                    + response.statusCode()
                                                    + ": " + body)));
                        }
                    })
                    .block();

            String publicUrl = supabaseUrl + "/storage/v1/object/public/"
                    + supabaseBucket + "/" + fname;

            Work w = new Work();
            w.setTitle(title == null || title.isBlank() ? "" : title);
            w.setImageUrl(publicUrl);
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

    private BufferedImage resize(BufferedImage src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) {
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
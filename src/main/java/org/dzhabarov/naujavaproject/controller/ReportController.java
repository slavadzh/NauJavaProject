package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.dto.ReportDTO;
import org.dzhabarov.naujavaproject.entity.Report;
import org.dzhabarov.naujavaproject.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/create")
    public String createReport(Model model) {

        Long id = reportService.createReport();
        reportService.generateReport(id);

        return "redirect:/reports/" + id;
    }

    @GetMapping("/{id}")
    public String getReport(@PathVariable Long id, Model model) {

        Report report = reportService.getReport(id);

        if (report.getStatus().equals("CREATED")) {
            model.addAttribute("status", "Отчет формируется...");
            return "report-status";
        }

        if (report.getStatus().equals("ERROR")) {
            model.addAttribute("status", "Ошибка при формировании");
            return "report-status";
        }

        try {
            ReportDTO dto = reportService.generateReport(id).join();
            model.addAttribute("report", dto);
            return "report";

        } catch (Exception e) {
            model.addAttribute("status", "Ошибка");
            return "report-status";
        }
    }
}

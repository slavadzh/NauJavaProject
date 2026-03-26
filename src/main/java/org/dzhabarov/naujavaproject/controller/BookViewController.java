package org.dzhabarov.naujavaproject.controller;

import lombok.RequiredArgsConstructor;
import org.dzhabarov.naujavaproject.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BookViewController {

    private final BookService bookService;

    @GetMapping("/books")
    public String getBooks(Model model) {
        model.addAttribute(
                "books",
                bookService.findAll()
        );
        return "books";
    }
}
package org.dzhabarov.naujavaproject.console;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandProcessor {

    private final BookService bookService;

    @Autowired
    public CommandProcessor(BookService bookService) {
        this.bookService = bookService;
    }

    public void processCommand(String input) {

        String[] cmd = input.split(" ");

        switch (cmd[0]) {

            case "create" -> {
                bookService.createBook(
                        Long.valueOf(cmd[1]),
                        cmd[2],
                        cmd[3]
                );
                System.out.println("Книга добавлена");
            }

            case "find" -> {
                Book book = bookService.findById(Long.valueOf(cmd[1]));

                if (book != null) {
                    System.out.println(
                            book.getId() + " "
                                    + book.getTitle() + " "
                                    + book.getAuthor()
                    );
                } else {
                    System.out.println("Книга не найдена");
                }
            }

            case "delete" -> {
                bookService.deleteById(Long.valueOf(cmd[1]));
                System.out.println("Книга удалена");
            }

            case "update" -> {
                bookService.updateBook(
                        Long.valueOf(cmd[1]),
                        cmd[2],
                        cmd[3]
                );
                System.out.println("Книга обновлена");
            }

            default -> System.out.println("Неизвестная команда");
        }
    }
}

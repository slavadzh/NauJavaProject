package org.dzhabarov.naujavaproject.controller;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(BookRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        RestAssuredMockMvc.basePath = "/api/books";
    }

    @Test
    void testFindByTitle_success() {
        BookDTO book1 = new BookDTO();
        book1.setTitle("Java Programming");
        book1.setAuthorsNames(List.of("Dzhabarov"));

        BookDTO book2 = new BookDTO();
        book2.setTitle("Effective Java");
        book2.setAuthorsNames(List.of("Joshua Bloch"));

        List<BookDTO> mockBooks = List.of(book1, book2);

        when(bookService.findByTitleContaining("Java")).thenReturn(mockBooks);

        given()
                .queryParam("title", "Java")
                .when()
                .get("/search/title")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("title", hasItems("Java Programming", "Effective Java"))
                .body("authorsNames[0]", hasItem("Dzhabarov"));
    }

    @Test
    void testFindByTitle_notFound() {
        when(bookService.findByTitleContaining("NonExistingBookTitle"))
                .thenReturn(Collections.emptyList());

        given()
                .queryParam("title", "NonExistingBookTitle")
                .when()
                .get("/search/title")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testFindByTitle_emptyTitle() {
        given()
                .queryParam("title", "")
                .when()
                .get("/search/title")
                .then()
                .statusCode(200);
    }

    @Test
    void testFindByAuthor_success() {
        BookDTO book1 = new BookDTO();
        book1.setTitle("Java");
        book1.setAuthorsNames(List.of("Dzhabarov"));

        BookDTO book2 = new BookDTO();
        book2.setTitle("Spring Boot");
        book2.setAuthorsNames(List.of("Dzhabarov"));

        List<BookDTO> mockBooks = List.of(book1, book2);

        when(bookService.findByAuthor("Dzhabarov")).thenReturn(mockBooks);

        given()
                .queryParam("name", "Dzhabarov")
                .when()
                .get("/search/author")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("authorsNames.flatten()", everyItem(equalTo("Dzhabarov")));
    }

    @Test
    void testFindByAuthor_notFound() {
        when(bookService.findByAuthor("Unknown Author"))
                .thenReturn(Collections.emptyList());

        given()
                .queryParam("name", "Unknown Author")
                .when()
                .get("/search/author")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testFindByAuthor_emptyName() {
        given()
                .queryParam("name", "")
                .when()
                .get("/search/author")
                .then()
                .statusCode(200);
    }
}
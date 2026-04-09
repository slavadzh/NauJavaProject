package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dto.ReportDTO;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Report;
import org.dzhabarov.naujavaproject.entity.ReportStatus;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.ReportRepository;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Long createReport() {
        Report report = new Report();
        report.setStatus(ReportStatus.CREATED.name());
        return reportRepository.save(report).getId();
    }

    public Report getReport(Long id) {
        return reportRepository.findById(id).orElseThrow();
    }


    @Async
    public CompletableFuture<ReportDTO> generateReport(Long reportId) {

        Report report = getReport(reportId);

        long totalStart = System.currentTimeMillis();

        try {
            final long[] usersTime = new long[1];
            final long[] usersCount = new long[1];

            Thread t1 = new Thread(() -> {
                long start = System.currentTimeMillis();
                usersCount[0] = userRepository.count();
                usersTime[0] = System.currentTimeMillis() - start;
            });

            final long[] booksTime = new long[1];
            final List<Book>[] books = new List[1];

            Thread t2 = new Thread(() -> {
                long start = System.currentTimeMillis();
                books[0] = bookRepository.findAll();
                booksTime[0] = System.currentTimeMillis() - start;
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            long totalTime = System.currentTimeMillis() - totalStart;

            report.setStatus(ReportStatus.COMPLETED.name());
            reportRepository.save(report);

            return CompletableFuture.completedFuture(
                    new ReportDTO(
                            usersCount[0],
                            books[0],
                            usersTime[0],
                            booksTime[0],
                            totalTime
                    )
            );

        } catch (Exception e) {
            report.setStatus(ReportStatus.ERROR.name());
            reportRepository.save(report);

            return CompletableFuture.failedFuture(e);
        }
    }
}
package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}

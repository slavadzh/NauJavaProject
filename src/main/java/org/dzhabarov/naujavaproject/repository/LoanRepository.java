package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

}

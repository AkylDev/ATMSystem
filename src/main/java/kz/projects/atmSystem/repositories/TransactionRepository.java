package kz.projects.atmSystem.repositories;

import kz.projects.atmSystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByUserAccountNumber(String accountNumber);
}

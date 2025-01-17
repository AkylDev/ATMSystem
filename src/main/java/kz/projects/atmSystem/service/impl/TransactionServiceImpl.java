package kz.projects.atmSystem.service.impl;

import jakarta.transaction.Transactional;
import kz.projects.atmSystem.dto.TransactionDTO;
import kz.projects.atmSystem.dto.TransactionRequest;
import kz.projects.atmSystem.mapper.TransactionMapper;
import kz.projects.atmSystem.model.Transaction;
import kz.projects.atmSystem.model.TransactionType;
import kz.projects.atmSystem.model.User;
import kz.projects.atmSystem.repositories.TransactionRepository;
import kz.projects.atmSystem.service.TransactionService;
import kz.projects.atmSystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final UserService userService;

  private final TransactionRepository transactionRepository;

  @Override
  @Transactional
  public void deposit(Double amount) {
    if (amount < 0){
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    User currentUser = userService.getCurrentSessionUser();
    currentUser.setBalance(userService.getCurrentUserBalance() + amount);
    userService.saveUser(currentUser);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionType.DEPOSIT);
    transaction.setAmount(amount);
    transaction.setDate(LocalDateTime.now());
    transaction.setUser(currentUser);
    transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public void withdrawAmount(Double amount) {
    if (amount < 0){
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    User currentUser = userService.getCurrentSessionUser();
    double balanceLeft = currentUser.getBalance() - amount;

    if (balanceLeft < 0){
      throw new IllegalArgumentException("Not enough balance in your account!");
    }

    currentUser.setBalance(balanceLeft);
    userService.saveUser(currentUser);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionType.WITHDRAWAL);
    transaction.setAmount(amount);
    transaction.setDate(LocalDateTime.now());
    transaction.setUser(currentUser);
    transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public void transferAmount(TransactionRequest request) {
    User currentUser = userService.getCurrentSessionUser();

    User checkAccount = userService.getUser(request.account());
    if(checkAccount==null){
      throw new IllegalArgumentException("Account doesn't exist");
    }

    if (request.amount() < 0){
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    if (!Objects.equals(currentUser.getAccountNumber(), request.account())){
      User userToTransfer = userService.getUser(request.account());

      double balanceLeft = currentUser.getBalance() - request.amount();

      if (balanceLeft < 0){
        throw new IllegalArgumentException("Not enough balance in your account!");
      }

      currentUser.setBalance(balanceLeft);
      userService.saveUser(currentUser);

      Double userToTransferAmount = userToTransfer.getBalance();
      userToTransfer.setBalance(userToTransferAmount + request.amount());
      userService.saveUser(userToTransfer);

      Transaction transaction = new Transaction();
      transaction.setType(TransactionType.TRANSFER);
      transaction.setAmount(request.amount());
      transaction.setDate(LocalDateTime.now());
      transaction.setUser(currentUser);
      transactionRepository.save(transaction);
    }
    else {
      throw new IllegalArgumentException("Cannot transfer to the same account");
    }
  }

  @Override
  public List<TransactionDTO> getTransactions() {
    List<Transaction> transactions = transactionRepository.findAll();
    return transactions.stream()
            .map(TransactionMapper::toDto)
            .collect(Collectors.toList());
  }
}

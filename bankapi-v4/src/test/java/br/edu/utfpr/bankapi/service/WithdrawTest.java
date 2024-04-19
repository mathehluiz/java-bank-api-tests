package br.edu.utfpr.bankapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import br.edu.utfpr.bankapi.dto.WithdrawDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import br.edu.utfpr.bankapi.repository.TransactionRepository;
import br.edu.utfpr.bankapi.validations.AvailableAccountValidation;
import br.edu.utfpr.bankapi.validations.AvailableBalanceValidation;

@SpringBootTest
public class WithdrawTest {

    @Autowired
    private TransactionService service;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private AvailableAccountValidation availableAccountValidation;

    @MockBean
    private AvailableBalanceValidation availableBalanceValidation;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    /**
     * Testa se é possível realizar um saque.
     * 
     * @throws NotFoundException
     */
    @Test
    void deveriaRealizarSaque() throws NotFoundException {
        // ### ARRANGE ###
        double initialBalance = 1200.50;

        long sourceAccountNumber = 67890;

        long amount = 200;

        WithdrawDTO withdrawDTO = new WithdrawDTO(sourceAccountNumber, 200);

        Account sourceAccount = new Account("ClienteTeste1", 67890, initialBalance, 0);

        Transaction transaction = new Transaction(sourceAccount, sourceAccount, amount,
                TransactionType.WITHDRAW);

        BDDMockito.willDoNothing().given(availableBalanceValidation).validate(transaction);

        BDDMockito.given(availableAccountValidation.validate(withdrawDTO.sourceAccountNumber()))
                .willReturn(sourceAccount);

        service.withdraw(withdrawDTO);

        BDDMockito.then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        Assertions.assertEquals(sourceAccount, savedTransaction.getSourceAccount());
        Assertions.assertEquals(withdrawDTO.amount(), savedTransaction.getAmount());
        Assertions.assertEquals(TransactionType.WITHDRAW,
                savedTransaction.getType());
        Assertions.assertEquals(initialBalance - withdrawDTO.amount(),
                savedTransaction.getSourceAccount().getBalance());
    }
}
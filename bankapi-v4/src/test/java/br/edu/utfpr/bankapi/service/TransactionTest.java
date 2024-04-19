package br.edu.utfpr.bankapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.utfpr.bankapi.dto.TransferDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import br.edu.utfpr.bankapi.repository.TransactionRepository;
import br.edu.utfpr.bankapi.validations.AvailableAccountValidation;
import br.edu.utfpr.bankapi.validations.AvailableBalanceValidation;

@ExtendWith(MockitoExtension.class)
public class TransactionTest {

    @InjectMocks
    private TransactionService service;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AvailableAccountValidation availableAccountValidation;

    @Mock
    private AvailableBalanceValidation availableBalanceValidation;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    TransferDTO transferDTO;

    Account sourceAccount;

    Account receiverAccount;

    @Mock
    Transaction transaction;

    @Test
    void deveriaTransferirSaldo() throws NotFoundException {
        double sourceInitialBalance = 1200.50;
        double receiverInitialBalance = 800.50;

        long sourceAccountNumber = 12345;
        long receiverAccountNumber = 67890;
        double amount = 300.00;

        transferDTO = new TransferDTO(sourceAccountNumber, receiverAccountNumber, amount);
        sourceAccount = new Account("Teste1", sourceAccountNumber, sourceInitialBalance, 0);
        receiverAccount = new Account("Teste1", receiverAccountNumber, receiverInitialBalance, 0);

        BDDMockito.given(availableAccountValidation.validate(transferDTO.sourceAccountNumber()))
                .willReturn(sourceAccount);
        BDDMockito.given(availableAccountValidation.validate(transferDTO.receiverAccountNumber()))
                .willReturn(receiverAccount);

        service.transfer(transferDTO);

        BDDMockito.then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        assertEquals(sourceAccount, savedTransaction.getSourceAccount());
        assertEquals(receiverAccount, savedTransaction.getReceiverAccount());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(sourceInitialBalance - amount, savedTransaction.getSourceAccount().getBalance());
        assertEquals(receiverInitialBalance + amount, savedTransaction.getReceiverAccount().getBalance());
        assertEquals(TransactionType.TRANSFER, savedTransaction.getType());
    }
}

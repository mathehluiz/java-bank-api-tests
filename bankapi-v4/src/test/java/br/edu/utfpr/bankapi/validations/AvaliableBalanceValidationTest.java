package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.exception.WithoutBalanceException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AvaliableBalanceValidationTest {

    @Autowired
    AvailableBalanceValidation availableBalanceValidation;


    @Test
    void deveriaDispararErroQuandoAContaEnull() {
        var transaction = new Transaction(null, null, 10, TransactionType.TRANSFER);
        Assertions.assertThrows(NullPointerException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoQuandoTemSaldoSuficiente() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 0);
        var transaction = new Transaction(sourceAccount, null, 500, TransactionType.TRANSFER);
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaValidarComLimiteESaldo() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 500);
        var transaction = new Transaction(sourceAccount, null, 1200, TransactionType.TRANSFER);
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoComSaldoExato() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 0);
        var transaction = new Transaction(sourceAccount, null, 800, TransactionType.TRANSFER);
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoComSaldoELimiteExato() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 500);
        var transaction = new Transaction(sourceAccount, null, 1500, TransactionType.TRANSFER);
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaDispararExcecaoQuandoNaoTemLimiteSuficiente() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 0);
        var transaction = new Transaction(sourceAccount, null, 1500, TransactionType.TRANSFER);
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaDispararExecaoQuandoBalancoMaior() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 0);
        var transaction = new Transaction(sourceAccount, null, 1001, TransactionType.TRANSFER);
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaDispararExecaoQuandoBalancoMaiorQueLimite() {
        var sourceAccount = new Account("ClientTeste1", 99988, 800, 500);
        var transaction = new Transaction(sourceAccount, null, 1501, TransactionType.TRANSFER);
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }
}

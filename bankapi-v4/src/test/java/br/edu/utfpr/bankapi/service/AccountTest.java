package br.edu.utfpr.bankapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import br.edu.utfpr.bankapi.dto.AccountDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
public class AccountTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Test
    void deveriaAcharContaPorNumero() {
        // ARRANGE
        long accountNumber = 123456789L;
        Account account = new Account("Cliente teste1", accountNumber, 2000.0, 500.0);
        given(accountRepository.getByNumber(accountNumber)).willReturn(Optional.of(account));

        // ACT
        Optional<Account> result = accountService.getByNumber(accountNumber);

        // ASSERT
        assertTrue(result.isPresent());
        BDDMockito.then(accountRepository).should().getByNumber(accountNumber);
        assertEquals(account.getNumber(), result.get().getNumber()); 
    }

       @Test
    void deveriaLancarExcecaoQuandoNaoEncontraConta() {
        // ARRANGE
        long accountId = 1L;
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // ACT / ASSERT
        assertThrows(NotFoundException.class,
                () -> accountService.update(accountId, new AccountDTO("ClientTeste1", 123456789L, 100.0, 500.0)));
        then(accountRepository).should(never()).save(any(Account.class)); 
    }

        @Test
    void deveriaSalvarContaComSaldoInicial() {
        // ARRANGE
        AccountDTO accountDTO = new AccountDTO("Cliente teste1", 123456789L, 0, 200.0);
        BDDMockito.given(accountRepository.save(any(Account.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Account savedAccount = accountService.save(accountDTO);

        // ASSERT
        assertEquals(0, savedAccount.getBalance());
        Account captured = accountCaptor.getValue();
        assertEquals(accountDTO.name(), captured.getName());
        BDDMockito.then(accountRepository).should().save(accountCaptor.capture());
        assertEquals(500.0, captured.getSpecialLimit());
    }

    @Test
    void deveriaRetornarTodasContas() {
        // ARRANGE
        Account account = new Account("Cliente teste1", 123456789L, 1500.0, 200.0);
        BDDMockito.given(accountRepository.findAll()).willReturn(Arrays.asList(account));

        // ACT
        List<Account> results = accountService.getAll();

        // ASSERT
        BDDMockito.then(accountRepository).should().findAll();
        assertEquals(1, results.size());
        assertFalse(results.isEmpty());
    }

    @Test
    void deveriaAtualizarConta() throws NotFoundException {
        // ARRANGE
        long accountId = 1L;
        Account existingAccount = new Account("Cliente teste1", 123456789L, 1000.0, 300.0);
        AccountDTO updateDTO = new AccountDTO("TesteConta", 123456789L, 1500.0, 700.0);
        given(accountRepository.findById(accountId)).willReturn(Optional.of(existingAccount));
        given(accountRepository.save(any(Account.class))).willAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setNumber(updateDTO.number());
            a.setSpecialLimit(updateDTO.specialLimit());
            a.setName(updateDTO.name());
            a.setBalance(updateDTO.balance());
            return a;
        });

        // ACT
        Account updatedAccount = accountService.update(accountId, updateDTO);

        // ASSERT
        then(accountRepository).should().save(accountCaptor.capture());
        assertEquals("Conta atualizada", captured.getName());
        assertEquals(1500.0, captured.getBalance());
        assertEquals(700.0, captured.getSpecialLimit());
        Account captured = accountCaptor.getValue();
    }
}
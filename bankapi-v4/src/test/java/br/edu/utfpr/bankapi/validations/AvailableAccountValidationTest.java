package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.model.Account;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestEntityManager
public class AvailableAccountValidationTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    AvailableAccountValidation availableAccountValidation;

    @Test
    void deveriaDispararErroQuandoAContaNaoEEncontrada() {
        // Arrange
        long number = 9832;

        // Act + Assert
        Assertions.assertThrows(Exception.class, () -> availableAccountValidation.validate(number));
    }

    @Test
    @Transactional
    void deveriaSerValidoQuandoAContaExiste() throws Exception {
        // Arrange
        var account = new Account("ClientTeste", 123231, 500, 0);
        entityManager.persist(account);

        // Act
        var response = availableAccountValidation.validate(account.getNumber());

        // Assert
        Assertions.assertEquals(account, response);
    }
}

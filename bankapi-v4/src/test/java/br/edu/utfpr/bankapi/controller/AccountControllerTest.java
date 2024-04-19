package br.edu.utfpr.bankapi.controller;

import br.edu.utfpr.bankapi.model.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.edu.utfpr.bankapi.dto.AccountDTO;
import br.edu.utfpr.bankapi.service.AccountService;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
class AccountControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestEntityManager entityManager;

    
    // GET /account
    @Test
    void deveriaRetornarTodasContas() throws Exception {
        var account1 = new Account("ClienteTeste1", 88733, 200, 0);
        var account2 = new Account("ClienteTeste2", 99999, 1000, 0);
        entityManager.persist(account1);
        entityManager.persist(account2);

        mvc.perform(MockMvcRequestBuilders.get("/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].number").value(account1.getNumber()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].balance").value(account1.getBalance()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].name").value(account1.getName()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].number").value(account2.getNumber()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].balance").value(account2.getBalance()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].name").value(account2.getName()));
    }

    @Test
    void deveriaRetornarStatus200QuandoNaotemConta() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$").isEmpty());
    }

    // GET /account/{number}
    @Test
    void deveriaRetornar404ParaContaNaoEncontrada() throws Exception {
        long accountNumber = 11111;


        var res = mvc.perform(MockMvcRequestBuilders.get("/account/" + accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();


        Assertions.assertEquals(404, res.getStatus());
        Assertions.assertEquals("", res.getContentAsString());
    }

    @Test
    void deveriaRetornarStatus200ParaValidaConta() throws Exception {
        long accountNumber = 88733;
        double balance = 1000;
        var account = new Account("ClienteTeste1", accountNumber, balance, 0);
        entityManager.persist(account);

        mvc.perform(MockMvcRequestBuilders.get("/account/" + accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("number").value(accountNumber))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("balance").value(balance))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("name").value(account.getName()));
    }


    // POST /account
    @Test
    void deveriaRetornarStatus400paraInvalida() throws Exception {
        var json = "{}";

        mvc.perform(MockMvcRequestBuilders.post("/account")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isBadRequest());
    }

    @Test
    void deveriaRetornarStatus201ParaValida() throws Exception {
        String name = "ClienteTeste1";
        long number = 88733;
        var json = """
                {
                    "name": "ClienteTeste1",
                    "number": 88733,
                    "balance": 500,
                    "specialLimit": 0
                }
                """;

        mvc.perform(MockMvcRequestBuilders.post("/account")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isCreated())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("number").value(number))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("name").value(name))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("specialLimit").value(0))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("balanceWithLimit").value(0));
    }

    // PUT /account/{id}

    @Test
    void deveriaRetornarStatus404QuandoNaoEncontraContaParaAtualizar() throws Exception {
        long accountId = 1;
        var json = """
                {
                    "name": "ClienteTeste1",
                    "number": 88733,
                    "balance": 500,
                    "specialLimit": 0
                }
                """;

        mvc.perform(MockMvcRequestBuilders.put("/account/" + accountId)
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .content().contentType("text/plain;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers
                        .content().string("Not found"));
    }

    @Test
    void deveriaRetornarStatus400ForInValidaUpdateRequest() throws Exception {
        long accountId = 1;
        var json = "{}";


        var res = mvc.perform(MockMvcRequestBuilders.put("/account/" + accountId)
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();


        Assertions.assertEquals(400, res.getStatus());
    }

}

package com.onrender.themba.cashcardapi;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.util.Arrays;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
//import org.springframework.data.web.JsonPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {
    @Autowired
    private JacksonTester<CashCard> json;
    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    @BeforeEach
    void setUp(){
        cashCards = Arrays.array(
                new CashCard(99l, 123.45, "Jakaza"),
                new CashCard(100L, 1.00, "Jakaza"),
                new CashCard(101L, 150.00, "Jakaza")
        );
    }

    @Test
    void cashCardSerializationTest()
            throws IOException {
        CashCard cashCard = cashCards[0];
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected = """
                {
                "id": 99,
                "amount": 123.45, 
                "owner": "Jakaza"
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, 123.45, "Jakaza"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);

    }
    @Test
    void allCashCardSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }
    @Test
    void allCashCardDeserializationTest() throws IOException {
        String expected = """
                [
                    {"id":  99, "amount": 123.45, "owner":  "Jakaza"},
                    {"id":  100, "amount": 1.00, "owner":  "Jakaza"},
                    {"id":  101, "amount": 150.00, "owner":  "Jakaza"}
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
        DocumentContext documentContext = JsonPath.parse(expected);
//        JSONArray read = documentContext.read("[*]");
    }
}

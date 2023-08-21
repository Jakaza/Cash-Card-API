package com.onrender.themba.cashcardapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/sql/schema.sql", "/sql/data.sql"})
class CashCardApiApplicationTests {

	@Autowired
	TestRestTemplate testRestTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved(){
		ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/cashcards/99", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());

		Number id = documentContext.read("$.id");
		double amount = documentContext.read("$.amount");

		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId(){
		ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/cashcards/000", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).isBlank();
	}

	@Test
	void shouldCreateANewCashCard(){
		CashCard newCashCard = new CashCard(null, 1.00);
		ResponseEntity<Void> newResponseEntity = testRestTemplate.postForEntity(
				"/cashcards",
				newCashCard,
				Void.class
		);
		assertThat(newResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		URI location = newResponseEntity.getHeaders().getLocation();
		ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(location, String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		Number id = documentContext.read("$.id");
		double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(1.00);
		assertThat(id).isNotNull();
	}

}

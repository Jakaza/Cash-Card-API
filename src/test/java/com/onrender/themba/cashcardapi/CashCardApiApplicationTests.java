package com.onrender.themba.cashcardapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql({"/sql/schema.sql", "/sql/data.sql"})
class CashCardApiApplicationTests {
	@Autowired
	TestRestTemplate testRestTemplate;
	@Test
	void contextLoads() {
	}
	@Test
	void shouldReturnACashCardWhenDataIsSaved(){
		ResponseEntity<String> responseEntity = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());

		Number id = documentContext.read("$.id");
		double amount = documentContext.read("$.amount");

		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.45);
	}
	@Test
	void shouldNotReturnACashCardWithAnUnknownId(){
		ResponseEntity<String> responseEntity = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity("/cashcards/000", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).isBlank();
	}
	@Test
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00, "Jakaza");
		ResponseEntity<Void> createResponse = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}
	@Test
	void shouldReturnListOfCashCardSaved(){
		ResponseEntity<String> responseEntity = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity("/cashcards?page=0&size=3&sort=amount,asc", String.class);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		JSONArray data = documentContext.read("[*]");
		JSONArray ids = documentContext.read("$..id");
		double min = documentContext.read("$.[0].amount");
		double max = documentContext.read("$.[2].amount");

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(ids).containsExactlyInAnyOrder(102,100,99);
		assertThat(data.size()).isEqualTo(3);
		assertThat(min).isEqualTo(2.00);
		assertThat(max).isEqualTo(150);
	}
	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(200);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("Jakaza", "jka123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(4);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(2.00, 123.45, 150.00, 200.00);
	}
}

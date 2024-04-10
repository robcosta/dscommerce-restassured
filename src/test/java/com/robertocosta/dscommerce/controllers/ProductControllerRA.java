package com.robertocosta.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.robertocosta.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class ProductControllerRA {


	private Long existingProductId, nonExistingProductId;
	private String clientToken, adminToken, invalidToken;
	private String clientUsername, clientPassword, adminUsername, adminPassword;


	private Map<String, Object> postProductInstance;
	List<Map<String, Object>> categories;


	@BeforeEach
	public void setUp() throws Exception{
		baseURI = "http://localhost:8080";

		postProductInstance = new HashMap<>();
		postProductInstance.put("name", "Meu novo produto");
		postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore");
		postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		postProductInstance.put("price", 50.0);

		categories = new ArrayList<>();

		Map<String, Object> category1 = new HashMap<>();
		category1.put("id", 2);

		Map<String, Object> category2 = new HashMap<>();
		category2.put("id", 3);

		categories.add(category1);
		categories.add(category2);

		postProductInstance.put("categories", categories);

		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		adminUsername = "alex@gmail.com";
		adminPassword = "123456";

		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "xpto"; //simulates wrong passwordd
	}

	@Test
	public void findByShouldReturnProductEhwnIdExists() {
		existingProductId = 2L;

		given()
			.get("/products/{id}", existingProductId)
		.then()
			.statusCode(200).assertThat()
			.body("id", is(2))
			.body("name", equalTo("Smart TV"))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
			.body("price", is(2190.0F))
			.body("categories.id", hasItems(2, 3))
			.body("categories.name", hasItems("Eletrônicos", "Computadores"));
	}

	@Test
	public void findByShouldReturnNotFoundWhenIdDoesNotExists() {
		nonExistingProductId = 1000L;

		given()
			.get("/products/{id}", nonExistingProductId)
		.then().statusCode(404).assertThat();
	}

	@Test
	public void findAllShouldReturnPageProductsWhenNameProductIsEmpty() {

		given()
			.get("/products?page=0")
		.then()
			.statusCode(200)
			.body("content.name", hasItems("Macbook Pro","PC Gamer Tera"))
			.body("pageable.pageSize", is(20));
	}

	@Test
	public void findAllShouldReturnPageProductsWhenNameProductNotEmpty() {

		String productName = "Macbook";

		given()
			.param("name",productName)
			.get("/products?page=0")
		.then()
			.statusCode(200)
			.body("content.id[0]",is(3))
			.body("content.name", hasItem("Macbook Pro"))
			.body("content.price[0]", is(1250.0F))
			.body("totalPages", is(1))
			.body("totalElements", is(1));
	}

	@Test
	public void findAllShouldReturnPageProductsWhenPriceGreaterThen2000() {

		given()
			.get("/products")
		.then()
			.statusCode(200)
			.body("content.findAll {it.price > 2000}.name", hasItems("Smart TV","PC Gamer Hera") );
	}

	@Test
	public void insertShouldReturnProductCreatedWhenAdminLogged() {

		JSONObject newProduct = new JSONObject(postProductInstance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(201)
			.body("name", equalTo("Meu novo produto"))
			.body("price", is(50.0F))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
			.body("categories.id", hasItems(2,3));
	}
	
	@Test
	public void insertShouldReturnUnporocessableEntityWhenAdminLoggedAndNameInvalid() {
		
		postProductInstance.put("name", "Ro");//campo name com menos de três letras
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
		.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnUnporocessableEntityWhenAdminLoggedAndDescriptionInvalid() {
		
		postProductInstance.put("description", "Lorem ip");//campo description com menos de dez letras
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
		.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnUnporocessableEntityWhenAdminLoggedAndPriceNegative() {
		
		postProductInstance.put("price", -50.0);
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
		.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnUnporocessableEntityWhenAdminLoggedAndPriceZero() {
		
		postProductInstance.put("price", 0.0);
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
		.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnUnporocessableEntityWhenAdminLoggedAndWithoutCategory() {
		
		categories.clear();
		postProductInstance.put("categories", categories);
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
		.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() {

		JSONObject newProduct = new JSONObject(postProductInstance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() {

		JSONObject newProduct = new JSONObject(postProductInstance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(401);
	}
	
		
}
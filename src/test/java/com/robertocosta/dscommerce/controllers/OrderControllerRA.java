package com.robertocosta.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.robertocosta.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {


	private Integer existsOrderId, nonExistOrderId;
	private String clientToken, adminToken, invalidToken;
	private String clientUsername, clientPassword, adminUsername, adminPassword;

	@BeforeEach
	public void setUp() throws Exception{
		baseURI = "http://localhost:8080";	
					
		nonExistOrderId = 100;

		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		adminUsername = "alex@gmail.com";
		adminPassword = "123456";

		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "xpto"; //simulates wrong password
	}

	@Test
	public void findByIdShouldReturnAnyOrderWhenAdminLoggedAndIdExists() {
		existsOrderId = 2;
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + adminToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", existsOrderId)
	.then()
		.statusCode(200)
		.body("id", is(existsOrderId))
		.body("client.name", equalTo("Alex Green"))
		.body("items.name", hasItems("Macbook Pro"))
		.body("total", is(1250.0F));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenAnyAdminLoggedAndIdDoesNotExists() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + adminToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", nonExistOrderId)
	.then()
		.statusCode(404)
		.body("error", equalTo("Recurso não encontrado."))
		.body("path", equalTo("/orders/" + nonExistOrderId));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenAnyClientLoggedAndIdDoesNotExists() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", nonExistOrderId)
	.then()
		.statusCode(404)
		.body("error", equalTo("Recurso não encontrado."))
		.body("path", equalTo("/orders/" + nonExistOrderId));
	}
	
	@Test
	public void findByIdShouldReturnOrderWhenClientLoggedAndOrderIsYours() {
		existsOrderId = 1;
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", existsOrderId)
	.then()
		.statusCode(200)
		.body("id", is(existsOrderId))
		.body("client.name", equalTo("Maria Brown"))
		.body("items.name", hasItems("The Lord of the Rings","Macbook Pro"))
		.body("total", is(1431.0F));
	}
	
	@Test
	public void findByIdShouldReturnForbidenWhenClientLoggedAndOrderIsNotYours() {
		existsOrderId = 2;
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", existsOrderId)
	.then()
		.statusCode(403)
		.body("error", equalTo("Access denied. Should be self or admins"))
		.body("path", equalTo("/orders/" + existsOrderId));
	}
	
	@Test
	public void findByIdShouldReturnUnauthorizedWhenInvalidToken() {
		existsOrderId = 1;
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + invalidToken)
		.contentType(ContentType.JSON)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", existsOrderId)
	.then()
		.statusCode(401);
	}
}
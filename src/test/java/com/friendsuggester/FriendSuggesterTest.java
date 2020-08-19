package com.friendsuggester;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.friendsuggester.verticles.FriendSuggesterVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class FriendSuggesterTest {

	private Vertx vertx;
	private static final int PORT = 8000;
	private static final String HOST = "localhost";

	@Before
	public void before(TestContext context) {
		vertx = Vertx.vertx();
		JsonObject config = new JsonObject();
		config.put("http.port", 8000);
		config.put("http.host", HOST);
		config.put("service", "redis");
		config.put("service.host", "127.0.0.1");
		config.put("service.port", 6380);
		final DeploymentOptions options = new DeploymentOptions().setConfig(config);

		FriendSuggesterVerticle verticle = new FriendSuggesterVerticle();

		vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());
	}

	@After
	public void after(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testCreateUser(TestContext context) throws Exception {
		HttpClient client = vertx.createHttpClient();
		Async async = context.async();
		JsonObject user = createBody("TA", "Test", "User1", "test@user1");
		client.post(PORT, HOST, "/create", response -> {
			context.assertEquals(201, response.statusCode());
			client.close();
			async.complete();
		}).putHeader("content-type", "application/json").end(Json.encodePrettily(user));
	}

	@Test
	public void testAddFriend(TestContext context) throws Exception {
		HttpClient client = vertx.createHttpClient();
		Async async = context.async();
		JsonObject userB = createBody("TB", "Test", "User1", "test@user1");
		JsonObject userC = createBody("TC", "Test", "User2", "test@user2");
		String addFriendURI = "/add/TB/TC";
		client.post(PORT, HOST, "/create", res -> {
			client.close();
			async.complete();
		}).putHeader("content-type", "application/json").end(Json.encodePrettily(userB));
		client.post(PORT, HOST, "/create", res -> {
			client.close();
			async.complete();
		}).putHeader("content-type", "application/json").end(Json.encodePrettily(userC));
		client.post(PORT, HOST, addFriendURI, response -> {
			context.assertEquals(202, response.statusCode());
			client.close();
			async.complete();
		}).putHeader("content-type", "application/json").end();
	}

	private JsonObject createBody(String userName, String firstName, String lastName, String email) {
		JsonObject obj = new JsonObject();
		obj.put("username", userName);
		obj.put("firstName", firstName);
		obj.put("lastName", lastName);
		obj.put("email", email);
		return obj;
	}

}

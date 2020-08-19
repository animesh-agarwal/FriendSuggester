package com.friendsuggester.verticles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.friendsuggester.constants.RouteConstants;
import com.friendsuggester.exceptions.ErrorConstants;
import com.friendsuggester.models.User;
import com.friendsuggester.service.FriendSuggesterService;
import com.friendsuggester.service.ServiceFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class FriendSuggesterVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(FriendSuggesterVerticle.class);

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8000;
	private static final String DEFAULT_SERVICE = "redis";

	private HttpServer httpServer = null;

	private FriendSuggesterService service;

	@Override
	public void start(Future<Void> future) throws Exception {
		initializeService();
		Router router = initRouter();
		
		router.post(RouteConstants.CREATE_USER).handler(this::createUser);
		router.post(RouteConstants.ADD_FRIEND).handler(this::addFriend);
		router.get(RouteConstants.GET_FRIEND_REQUESTS).handler(this::getFriendRequests);
		router.get(RouteConstants.GET_FRIENDS).handler(this::getAllFriends);

		String host = config().getString("http.host", DEFAULT_HOST);
		int port = config().getInteger("http.port", DEFAULT_PORT);
		httpServer = vertx.createHttpServer();
		logger.info("Friend Suggester Server is running on host " + host + " and on port " + port);
		httpServer.requestHandler(router::accept).listen(port, host, result -> {
			if (result.succeeded()) {
				future.complete();
			} else {
				future.fail(result.cause());
			}
		});
	}

	/**
	 * Initializes the service and checks whether the persistence layer is running 
	 */
	private void initializeService() {
		String serviceType = config().getString("service", DEFAULT_SERVICE);
		service = ServiceFactory.get(serviceType, vertx, config());
		service.initializeData().setHandler(res -> {
			if (res.failed()) {
				logger.error("Could not connect to " + serviceType);
				res.cause().printStackTrace();
			} else {
				logger.info("Successfully connected to " + serviceType);
			}
		});
	}

	private Router initRouter() {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(
				CorsHandler.create("*").allowedHeaders(getAllowedHeaders()).allowedMethods(getAllowedMethods()));
		return router;
	}

	private Set<String> getAllowedHeaders() {
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		return allowHeaders;
	}

	private Set<HttpMethod> getAllowedMethods() {
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);
		return allowMethods;
	}

	private void createUser(RoutingContext context) {
		try {
			JsonObject obj = context.getBodyAsJson();
			User user = Json.mapper.convertValue(obj, User.class);
			logger.info("Received request for creating user: " + user.getUserName());
			service.createUser(user).setHandler(res -> {
				if (res.succeeded()) {
					sendResponse(context, 201, user);
				} else {
					sendResponse(context, 400, failedResponse(res.cause().getMessage()));
				}
			});
		} catch (DecodeException e) {
			sendResponse(context, 400);
		}

	}

	private void addFriend(RoutingContext context) {
		String userA = context.request().getParam("userA");
		String userB = context.request().getParam("userB");
		logger.info(userA + " requested to be friends with " + userB);
		service.addFriend(userA, userB).setHandler(res -> {
			if (res.succeeded() && res.result()) {
				sendResponse(context, 202, successResponse());
			} else {
				sendResponse(context, 400, failedResponse(res.cause().getMessage()));
			}
		});
	}

	private void getFriendRequests(RoutingContext context) {
		String userA = context.request().getParam("userA");
		logger.info("Getting pending friend requests for "+ userA);
		if (null == userA) {
			sendResponse(context, 400, failedResponse(String.format(ErrorConstants.INVALID_PARAM, "userA")));
			return;
		}
		service.getPendingRequests(userA).setHandler(res -> {
			if (res.succeeded()) {
				List<String> friends = res.result();
				if (null == friends || friends.isEmpty()) {
					sendResponse(context, 404,
							failedResponse(String.format(ErrorConstants.NO_PENDING_REQUESTS, userA)));
				} else {
					sendResponse(context, 200, successResponse("friend_requests", friends));
				}
			} else {
				sendResponse(context, 400, failedResponse(res.cause().getMessage()));
			}
		});
	}

	private void getAllFriends(RoutingContext context) {
		String userA = context.request().getParam("userA");
		logger.info("Getting all friends for "+ userA);
		if (null == userA) {
			sendResponse(context, 400, failedResponse(String.format(ErrorConstants.INVALID_PARAM, "userA")));
		}
		service.getAllFriends(userA).setHandler(res -> {
			if (res.succeeded()) {
				List<String> friends = res.result();
				if (null == friends || friends.isEmpty()) {
					sendResponse(context, 404, failedResponse(String.format(ErrorConstants.NO_FRIENDS, userA)));
				} else {
					sendResponse(context, 200, successResponse("friends", friends));
				}
			} else {
				sendResponse(context, 400, failedResponse(res.cause().getMessage()));
			}
		});
	}

	private JsonObject failedResponse(String message) {
		JsonObject resp = new JsonObject();
		resp.put("status", "failed");
		resp.put("reason", message);
		return resp;
	}

	private JsonObject successResponse() {
		JsonObject resp = new JsonObject();
		resp.put("status", "success");
		return resp;
	}

	private JsonObject successResponse(String key, Object details) {
		JsonObject resp = new JsonObject();
		resp.put(key, details);
		return resp;
	}

	private void sendResponse(RoutingContext context, int statusCode, Object details) {
		context.response().setStatusCode(statusCode).putHeader("content-type", "application/json")
				.end(Json.encodePrettily(details));
	}

	private void sendResponse(RoutingContext context, int statusCode) {
		context.response().setStatusCode(statusCode).end();
	}

}

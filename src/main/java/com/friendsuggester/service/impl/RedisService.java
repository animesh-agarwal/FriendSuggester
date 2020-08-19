package com.friendsuggester.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.friendsuggester.exceptions.ErrorConstants;
import com.friendsuggester.exceptions.FriendSuggesterExceptions;
import com.friendsuggester.lua.LuaScript;
import com.friendsuggester.models.User;
import com.friendsuggester.service.FriendSuggesterService;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * Used to interact with the Redis Cluster
 *
 */
public class RedisService implements FriendSuggesterService {

	private final RedisClient redisClient;
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
	private static final String FRIENDS_PREFIX = "friends:";
	private static final String PENDING_FRIENDS_PREFIX = "pendingfriends:";

	private static final String DEFAULT_REDIS_HOST = "127.0.0.1";
	private static final int DEFAULT_REDIS_PORT = 6379;

	public RedisService(Vertx vertx, JsonObject config) {
		String host = config.getString("service.host", DEFAULT_REDIS_HOST);
		int port = config.getInteger("service.port", DEFAULT_REDIS_PORT);
		RedisOptions redisOptions = new RedisOptions().setHost(host).setPort(port);
		logger.info("Connecting to Redis on " + host + ":" + port);
		redisClient = RedisClient.create(vertx, redisOptions);
	}

	@Override
	public Future<Boolean> initializeData() {
		Future<Boolean> result = Future.future();
		redisClient.get("mykey", res -> {
			if (res.succeeded()) {
				result.complete(true);
			} else {
				result.fail(res.cause());
			}
		});
		return result;
	}

	@Override
	public Future<Boolean> createUser(User user) {
		Future<Boolean> result = Future.future();
		String uKey = user.getUserName();
		List<String> keys = new ArrayList<String>(Arrays.asList(uKey));
		List<String> args = new ArrayList<String>(Arrays.asList("username", uKey, "firstName", user.getFirstName(),
				"lastName", user.getLastName(), "email", user.getEmail()));
		redisClient.eval(LuaScript.CREATE_USER_SCRIPT, keys, args, res -> {
			if (res.succeeded()) {
				JsonArray array = res.result();
				long val = (Long) array.getValue(0);
				if (val == 1) {
					result.complete(true);
				} else if (val == -1) {
					result.fail(new FriendSuggesterExceptions(String.format(ErrorConstants.USER_ALREADY_EXIST, uKey)));
				}
			} else {
				result.fail(res.cause());
			}
		});
		return result;
	}

	@Override
	public Future<Boolean> addFriend(String userA, String userB) {
		Future<Boolean> result = Future.future();
		List<String> keys = new ArrayList<String>();
		keys.add(PENDING_FRIENDS_PREFIX + userA);
		keys.add(PENDING_FRIENDS_PREFIX + userB);
		keys.add(FRIENDS_PREFIX + userA);
		keys.add(FRIENDS_PREFIX + userB);
		List<String> args = new ArrayList<String>(Arrays.asList(userA, userB));

		redisClient.eval(LuaScript.ADD_FRIEND_SCRIPT, keys, args, res -> {
			if (res.succeeded()) {
				JsonArray array = res.result();
				long val = (Long) array.getValue(0);
				if (val == 1) {
					result.complete(true);
				} else if (val == -1) {
					result.fail(new FriendSuggesterExceptions(
							String.format(ErrorConstants.USERS_ARE_ALREADY_FRIENDS, userA, userB)));
				} else {
					result.fail(new FriendSuggesterExceptions(
							String.format(ErrorConstants.REQUEST_ALREADY_SENT, userA, userB)));
				}
			} else {
				result.fail(res.cause());
			}
		});

		return result;
	}

	@Override
	public Future<List<String>> getPendingRequests(String user) {
		return getList(user, PENDING_FRIENDS_PREFIX);
	}

	@Override
	public Future<List<String>> getAllFriends(String user) {
		return getList(user, FRIENDS_PREFIX);
	}

	@Override
	public Future<List<String>> getSuggestions(String user) {
		Future<List<String>> result = Future.future();
		List<String> keys = new ArrayList<String>(Arrays.asList(FRIENDS_PREFIX + user));
		redisClient.eval(LuaScript.GET_SUGGESTED_FRIENDS, keys, new ArrayList<String>(), res -> {
			if (res.succeeded()) {
				result.complete(res.result().stream().map(r -> (String) r).collect(Collectors.toList()));

			} else {
				result.fail(res.cause());
			}
		});
		return result;
	}

	private Future<List<String>> getList(String user, String key) {
		Future<List<String>> result = Future.future();
		redisClient.smembers(key + user, res -> {
			if (res.succeeded()) {
				result.complete(res.result().stream().map(r -> (String) r).collect(Collectors.toList()));
			} else {
				result.fail(res.cause());
			}
		});
		return result;
	}

}

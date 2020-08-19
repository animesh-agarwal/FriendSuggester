package com.friendsuggester.service;

import com.friendsuggester.service.impl.RedisService;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory class to return the appropriate service based on the configuration
 *
 */
public class ServiceFactory {

	private ServiceFactory() {

	}

	public static FriendSuggesterService get(String serviceType, Vertx vertx, JsonObject config) {
		switch (serviceType) {
		case "redis":
			return new RedisService(vertx, config);
		}
		return null;
	}
}

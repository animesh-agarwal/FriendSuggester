package com.friendsuggester.service;

import java.util.List;

import com.friendsuggester.models.User;

import io.vertx.core.Future;

public interface FriendSuggesterService {

	/**
	 * Initializes the data in the persistence layer
	 */
	public Future<Boolean> initializeData();

	/**
	 * @param user - User that needs to be created
	 * Adds a user in the system
	 */
	public Future<Boolean> createUser(User user);

	/**
	 * @param userA
	 * @param userB
	 * Adds userA to the pending friend requests for userB.
	 * If userB has already requested for userA, both of them become friends
	 * and they don't have pending friend requests on each other
	 */
	public Future<Boolean> addFriend(String userA, String userB);

	/**
	 * @param user
	 * Gets the pending friend requests for the user 
	 */
	public Future<List<String>> getPendingRequests(String user);

	/**
	 * @param user
	 * Gets all the friends for the user 
	 */
	public Future<List<String>> getAllFriends(String user);
	
	/**
	 * @param user
	 * Gets all the suggested friends for the user 
	 */
	public Future<List<String>> getSuggestions(String user);

}

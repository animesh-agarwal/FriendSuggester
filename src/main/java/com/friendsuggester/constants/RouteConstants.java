package com.friendsuggester.constants;

/**
 * Constants file to keep all the routes  
 *
 */
public class RouteConstants {

	private RouteConstants() {

	}

	public static final String ADD_FRIEND = "/add/:userA/:userB";
	public static final String GET_FRIEND_REQUESTS = "/friendRequests/:userA";
	public static final String CREATE_USER = "/create";
	public static final String GET_FRIENDS = "/friends/:userA";
}

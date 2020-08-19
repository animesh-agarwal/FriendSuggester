package com.friendsuggester.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class for the user entity. Builder pattern is used to create the User object
 *
 */
public class User {

	@JsonProperty("username")
	private String userName;
	private String firstName;
	private String lastName;
	private String email;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}

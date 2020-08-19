package com.friendsuggester.models;

/**
 * POJO class for the user entity. Builder pattern is used to create the User object
 *
 */
public class User {

	public static class UserBuilder {
		private String username;
		private String firstName;
		private String lastName;
		private String email;

		public UserBuilder(String userName) {
			this.username = userName;
		}

		public UserBuilder withFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public UserBuilder withLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public UserBuilder withEmail(String email) {
			this.email = email;
			return this;
		}

		public User build() {
			User user = new User();
			user.username = this.username;
			user.email = this.email;
			user.firstName = this.firstName;
			user.lastName = this.lastName;
			return user;
		}

	}

	private String username;
	private String firstName;
	private String lastName;
	private String email;

	public String getUserName() {
		return username;
	}

	public void setUserName(String userName) {
		this.username = userName;
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

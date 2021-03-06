/*
 * Copyright 2011-2012 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.test.model;

import java.io.Serializable;

/**
 * User
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String role;
	
	private String isLogin;

	public User() {
	}
	
	public User(String name, String role, String isLogin) {
		this.name = name;
		this.role = role;
		this.isLogin = isLogin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isOwned(Book book) {
		return true;
	}

	public String getIsLogin() {
		return isLogin;
	}

	public void setIsLogin(String isLogin) {
		this.isLogin = isLogin;
	}

}
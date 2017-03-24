package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;

public class DataBase {
	
	private DataBase() {
	}
	
    private static Map<String, User> users = Maps.newConcurrentMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
    
    public static User makeTestUser() {
    	User uesr1 = new User("a", "a", "a", "a@a");
    	addUser(uesr1);
    	
    	User uesr2 = new User("b", "b", "b", "b@b");
    	addUser(uesr2);
    	System.out.println("CREATE NEW USER: "+ uesr1.toString());
		return null;
    }
}

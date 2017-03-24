package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import db.DataBase;
import model.User;
import util.HttpRequestUtils.Pair;

public class HttpRequestUtilsTest {
    @Test
    public void parseQueryString() {
        String queryString = "userId=javajigi";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));

        queryString = "userId=javajigi&password=password2";
        parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is("password2"));
    }

    @Test
    public void parseQueryString_null() {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(null);
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString("");
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString(" ");
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    public void parseQueryString_invalid() {
        String queryString = "userId=javajigi&password";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));
    }

    @Test
    public void parseCookies() {
        String cookies = "logined=true; JSessionId=1234";
        Map<String, String> parameters = HttpRequestUtils.parseCookies(cookies);
        assertThat(parameters.get("logined"), is("true"));
        assertThat(parameters.get("JSessionId"), is("1234"));
        assertThat(parameters.get("session"), is(nullValue()));
    }

    @Test
    public void getKeyValue() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId=javajigi", "=");
        assertThat(pair, is(new Pair("userId", "javajigi")));
    }

    @Test
    public void getKeyValue_invalid() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId", "=");
        assertThat(pair, is(nullValue()));
    }

    @Test
    public void parseHeader() throws Exception {
        String header = "Content-Length: 59";
        Pair pair = HttpRequestUtils.parseHeader(header);
        assertThat(pair, is(new Pair("Content-Length", "59")));
    }
    
    @Test
    public void parseQueryString_invalid2() {
        String queryString = "userId=id&password=pass&name=name&email=a%40a";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("id"));
        assertThat(parameters.get("password"), is("pass"));
        assertThat(parameters.get("name"), is("name"));
        assertThat(parameters.get("email"), is("a%40a"));
        
    }
    
    @Test
    public void createUser() {
    	String queryString = "userId=id&password=pass&name=name&email=a%40a";
    	Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
    	assertThat(parameters.get("userId"), is("id"));
    	assertThat(parameters.get("password"), is("pass"));
    	assertThat(parameters.get("name"), is("name"));
    	assertThat(parameters.get("email"), is("a%40a"));
    	
    	System.out.println(new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"), parameters.get("email")));
    }
    
    @Test
    public void cookieParser() {
    	String header = "logined=true";
    	Map<String, String> cookieMap; 
    	cookieMap = HttpRequestUtils.parseCookies(header);
    	System.out.println(cookieMap);
    	assertThat(cookieMap.get("logined"), is("true"));
    }
    
    @Test
    public void dbTest() {
    	User user = new User("a", "a", "a", "a@a");
    	DataBase.addUser(user);
    	User findUser = DataBase.findUserById("a");
    	assertThat(findUser.getPassword(), is("a"));
    }
   
}

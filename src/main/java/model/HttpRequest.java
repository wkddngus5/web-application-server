package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;
import util.HttpRequestUtils.Pair;
import webserver.RequestHandler;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	String method;
	String path;
	HashMap<String, String> header = new HashMap<>();
	Map<String, String> parameter = new HashMap<>();
	byte[] body;
	Map<String, String> cookieMap = new HashMap<>();

	public HttpRequest(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String requestLine = br.readLine();
			String[] values = requestLine.split(" |\\?");
			method = values[0];
			path = values[1];
			log.debug(values[2]);

			parameter = HttpRequestUtils.parseQueryString(values[2]);

			if (requestLine == null) {
				return;
			}

			String line = br.readLine();
			Pair headerPair;

			while (!line.equals("")) {
				headerPair = HttpRequestUtils.parseHeader(line);
//				log.debug(headerPair.toString());
				header.put(headerPair.getKey(), headerPair.getValue());

				line = br.readLine();
			}

			if (getMethod().equals("post")) {
				String requestBody = IOUtils.readData(br, Integer.parseInt(getHeader("Content-Length")));
				log.debug("requestBody: {}", requestBody);
				parameter = HttpRequestUtils.parseQueryString(requestBody);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public byte[] getBody() {
		return body;
	}

	public Map<String, String> getCookieMap() {
		return cookieMap;
	}

	public String getHeader(String key) {
		return header.get(key);
	}

	public String getParameter(String key) {
		return parameter.get(key);
	}

	public User getUser() {
		return new User(getParameter("userId"), getParameter("password"), getParameter("name"), getParameter("email"));
	}

}

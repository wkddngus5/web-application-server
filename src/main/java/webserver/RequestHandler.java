package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.HttpRequest;
import model.User;
import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			HttpRequest request = new HttpRequest(in);
			byte[] body = null;

			DataOutputStream dos = new DataOutputStream(out);
			String url = request.getPath();
			Map<String, String> cookieMap = request.getCookieMap();
			
			if (request.getMethod().equals("GET")) {
				if (url.equals("/")) {
					url = "/index.html";
				}
				if (url.equals("/user/list")) {
					if (cookieMap.get("logined").equals("true")) {
						body = DataBase.findAll().toString().getBytes();
					} else {
						try {
							dos.writeBytes("HTTP/1.1 302 Found \r\n");
							dos.writeBytes("Location: /user/login.html\r\n");
							dos.writeBytes("Set-Cookie: logined=false\r\n");
							dos.writeBytes("\r\n");
						} catch (IOException e) {
							log.error(e.getMessage());
						}
					}
				} else {
					log.debug("url: {}", url);
					body = Files.readAllBytes(new File("./webapp" + url).toPath());					
				}
			}

			if (request.getMethod().equals("POST")) {

				if (url.equals("/user/create")) {
					User user = request.getUser();
					DataBase.addUser(user);

					log.debug("CREATE USER: {}", user.toString());
					response302Header(dos, body.length);
					responseBody(dos, body);
					return;
				}

				if (url.equals("/user/login")) {
					User loginUser = null;
					try {
						loginUser = DataBase.findUserById(request.getParameter("userId"));
					} catch (NullPointerException e) {
					}
					if(loginUser == null || !loginUser.checkPassword(request.getParameter("password"))) {
						try {
							dos.writeBytes("HTTP/1.1 302 Found \r\n");
							dos.writeBytes("Location: /user/login_failed.html\r\n");
							dos.writeBytes("Set-Cookie: logined=false\r\n");
							dos.writeBytes("\r\n");
						} catch (IOException e) {
							log.error(e.getMessage());
						}
					}
					try {
						dos.writeBytes("HTTP/1.1 302 Found \r\n");
						dos.writeBytes("Location: /index.html\r\n");
						dos.writeBytes("Set-Cookie: logined=true\r\n");
						dos.writeBytes("\r\n");
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					log.debug("login!!!");
				}
			}
			if (request.getPath().contains("css")) {
				response200HeaderCss(dos, body.length);
				responseBody(dos, body);
				return;
			}
			response200Header(dos, body.length);
			responseBody(dos, body);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200HeaderCss(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
package handler;

import java.util.HashMap;

import httpd.Session;

public class SessionManager {
	private HashMap<String, Session> sessions;
	
	public SessionManager() {
		sessions = new HashMap<>();
	}
	
	
	public String createNewSession() {
		// 随机生成一个唯一的会话id
		String sessId = "";
		
		sessions.put(sessId, new Session());
		
		return sessId;
	}
	
	public Session getSessionById(String sessId) {
		Session session = sessions.get(sessId);
		if (session == null) {
			session = new Session();
			sessions.put(sessId, session);
		}
		
		return session;
	}
}

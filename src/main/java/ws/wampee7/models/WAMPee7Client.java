package ws.wampee7.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.websocket.Session;
import org.codehaus.jackson.JsonNode;


public class WAMPee7Client {
	//static ALogger log = Logger.of(WAMPlayClient.class);
	final Set<String> topics;
	final Map<String, String> prefixes;
	final String ID;
	final Session out;
	JsonNode lastSent;

	public WAMPee7Client(Session out) {
		this.out = out;
		topics = new HashSet<String>();
		prefixes = new HashMap<String, String>();
		ID = UUID.randomUUID().toString();
	}

	public void send(JsonNode response) {
		// Just for testing.
		if (out == null) {
			lastSent = response;
			return;
		}

		try {
			out.getBasicRemote().sendText(response.toString());
		} catch (Exception e) {
			//log.error("Cannot send, client dead!");
                    System.out.println("Cannot send, client dead!");
		}
	}

	public void setPrefix(String prefix, String URI) {
		prefixes.put(prefix, URI);
	}

	private String convertCURIEToURI(String curie) {
		// TODO
		// if (prefixes.containsKey(prefix)){
		// return prefixes.get(prefix);
		// }
		return curie;
	}

	public void subscribe(String topic) {
		topics.add(convertCURIEToURI(topic));
	}

	public boolean isSubscribed(String topic) {
            System.out.println("Topic: " + convertCURIEToURI(topic));
            for (int i = 0; i < topics.size(); i++){
                System.out.println(i + " " + topics.toArray()[i].toString());
            }
		return topics.contains(convertCURIEToURI(topic));
	}

	public void unsubscribe(String topic) {
		topics.remove(convertCURIEToURI(topic));
	}

	public String getSessionID() {
		return out.getId();
	}

	public void kill() throws IOException {
		if (out != null) {
			out.close();
		}
	}

	public JsonNode lastMessage() {
		return lastSent;
	}
}

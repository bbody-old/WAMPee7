package ws.wampee7.controllers.messageHandlers;

import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import ws.wampee7.models.WAMPee7Client;
import ws.wampee7.models.messages.Welcome;


public class ConnectHandler implements MessageHandler {
	@Override
	public void process(WAMPee7Client client, JsonNode message) {
		List<Object> welcome = new Welcome(client.getSessionID()).toList();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.convertValue(welcome, JsonNode.class);
                client.send(node);
	}
}
package ws.wampee7.controllers.messageHandlers;


import org.codehaus.jackson.JsonNode;
import ws.wampee7.models.WAMPee7Client;


public class UnsubscribeHandler implements MessageHandler {

	@Override
	public void process(WAMPee7Client client, JsonNode message) {
		String topic = message.get(1).getTextValue();
		client.unsubscribe(topic);
	}

}

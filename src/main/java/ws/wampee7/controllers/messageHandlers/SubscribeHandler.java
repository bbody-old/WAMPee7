package ws.wampee7.controllers.messageHandlers;

import org.codehaus.jackson.JsonNode;
import ws.wampee7.callbacks.PubSubCallback;
import ws.wampee7.controllers.WAMPee7Server;
import ws.wampee7.models.PubSub;
import ws.wampee7.models.WAMPee7Client;


public class SubscribeHandler implements MessageHandler {
	//static ALogger log = Logger.of(SubscribeHandler.class);

	@Override
	public void process(WAMPee7Client senderClient, JsonNode message) {
		String topic = message.get(1).getTextValue();
		PubSubCallback cb = PubSub.getPubSubCallback(topic);
                System.out.println(senderClient.getSessionID() + " is trying to subscribe to " + topic);
		if (cb == null) {
			//log.error("Topic not found: " + topic);
                    String error = "Topic not found: " + topic;
                    System.out.println(error);
			return;
		}

		boolean sucessful = cb.runSubCallback(senderClient.getSessionID());

		if (!sucessful) {
			//log.debug("Callback for " + topic + " canceled.");
                        String error = "Callback for " + topic + " canceled.";
                    System.out.println(error);
			return;
		}

		if (WAMPee7Server.isTopic(topic)) {
                    
                    
			senderClient.subscribe(topic);
			return;
		}
		//log.error("Client tried to subscribe to nonexistant topic: " + topic);
                String error = "Client tried to subscribe to nonexistant topic: " + topic;
                    System.out.println(error);
	}

}

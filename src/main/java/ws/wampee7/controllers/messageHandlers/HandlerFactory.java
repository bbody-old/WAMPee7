package ws.wampee7.controllers.messageHandlers;

import org.codehaus.jackson.JsonNode;

import ws.wampee7.models.messages.MessageType;


public class HandlerFactory {
	//static ALogger log = Logger.of(HandlerFactory.class);

	public static MessageHandler get(JsonNode request) throws IllegalArgumentException{
                if (request == null) {
                    
			return MessageType.CONNECT.getHandler();
		}
                
		if (!request.isArray() || request.get(0).getIntValue() == -1) {
                    System.out.println("Not valid WAMP request");
			throw new IllegalArgumentException("Not valid WAMP request: " + request.toString());
		}

		try {
			MessageType type = MessageType.getType(request.get(0).getIntValue());
			return type.getHandler();
		} catch (EnumConstantNotPresentException e) {
			String error = "Message not implemented! " + request.toString();
			//log.error(error);
                        System.out.println(error);
			throw new IllegalArgumentException(error);
		}
	}
}

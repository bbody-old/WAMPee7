package ws.wampee7.models.messages;

import java.util.ArrayList;
import java.util.List;

import ws.wampee7.controllers.WAMPee7Server;



public class Welcome extends Message{
	final String clientID;
	
	public Welcome(String clientID) {
		this.clientID = clientID;
	}
	
	@Override
	public MessageType getType() {
		return MessageType.WELCOME;
	}

	@Override
	public List<Object> toList() {
		List<Object> res = new ArrayList<Object>();
		res.add(getType().getTypeCode());
		res.add(this.clientID);
		res.add(WAMPee7Server.PROTOCOL_VERSION);
		res.add(WAMPee7Server.VERSION);
		return res;
	}

}

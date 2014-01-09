package ws.wampee7.controllers.messageHandlers;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonNode;


import ws.wampee7.callbacks.RPCCallback;
import ws.wampee7.models.RPC;
import ws.wampee7.models.WAMPee7Client;
import ws.wampee7.models.messages.CallError;
import ws.wampee7.models.messages.CallResult;


public class RPCHandler implements MessageHandler{

	@Override
	public void process(WAMPee7Client client, JsonNode message) {
            
		String callID = message.get(1).getTextValue();
		String procURI = message.get(2).getTextValue();

		List<JsonNode> args = new ArrayList<JsonNode>();

		for (int i = 3; i < message.size(); i++) {
			args.add(message.get(i));
		}

		RPCCallback cb = RPC.getCallback(procURI);
                
		if (cb == null) {
			client.send(printList(new CallError(callID, procURI, "404", "RPC method not found!").toList()));
			return;
		}

		try {
			JsonNode response = cb.call(client.getSessionID(), args.toArray(new JsonNode[args.size()]));
			client.send(new CallResult(callID, response).toJson());
		} catch (IllegalArgumentException e) {
			CallError resp;
			if (e.getMessage() == null) {
				resp = new CallError(callID, procURI, "400");
			} else {
				resp = new CallError(callID, procURI, "400", e.getMessage());
			}
			client.send(resp.toJson());
		} catch (Throwable e) {
			CallError resp = new CallError(callID, procURI, "500", e.toString());
			client.send(resp.toJson());
		}
	}

        private String printList(List<Object> ls){
            String s = "[";
            s += ls.get(0).toString() + ", ";
            s += "\"" + ls.get(1).toString() + "\", ";
            s += ls.get(2).toString() + ", ";
            s += "\"" + ls.get(3).toString() + "\"]";
            return s;
        }

}

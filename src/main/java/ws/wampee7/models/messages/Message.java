package ws.wampee7.models.messages;

import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;



public abstract class Message {
	public abstract MessageType getType();
	public abstract List<Object> toList();
	public String toString() {
		return this.toList().toString();
	}
	public JsonNode toJson(){
            ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(toList());
	}
}

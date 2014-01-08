package ws.wampee7.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


import ws.wampee7.annotations.onRPC;
import ws.wampee7.callbacks.RPCCallback;
import ws.wampee7.controllers.WAMPee7Contoller;

public class RPC {

    //static ALogger log = Logger.of(RPC.class.getSimpleName());
    static ConcurrentMap<String, RPCCallback> procURIs = new ConcurrentHashMap<String, RPCCallback>();

    public static void addController(String prefix, final WAMPee7Contoller controller) {
        for (final Method method : controller.getClass().getMethods()) {
            if (method.isAnnotationPresent(onRPC.class)) {
                String procURI = prefix
                        + method.getAnnotation(onRPC.class).value();
                procURIs.put(procURI, new RPCCallback() {
                    @Override
                    public JsonNode call(String sessionID, JsonNode[] args) throws Throwable {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            if (args.length == 0) {
                                System.out.println("No RPC arguments!");
                                Object o = method.invoke(controller, sessionID);
                                
                                JsonNode rootNode = mapper.readTree("{\"value\" : \"" + o.toString() + "\"}");

                                /**
                                 * * read value from key "name" **
                                 */
                                return rootNode.path("value");
                            } else {
                                for (int i = 0; i < args.length; i++){
                                    System.out.println((i+1) +":"+ args[i].toString());
                                }
                                Object o = method.invoke(controller, sessionID, args);
                                if (o != null){
                                    System.out.println("Answer: " + o.toString());
                                } else {
                                    System.out.println("Answer: null");
                                }
                                JsonNode rootNode = mapper.readTree("{\"value\" : \"" + o.toString() + "\"}");

                                /**
                                 * * read value from key "name" **
                                 */
                                return rootNode.path("value");
                            }
                        } catch (InvocationTargetException e) {
                            System.out.println("='(");
                            throw e.getCause();
                        }
                    }
                });
            }
        }

    }

    public static void addCallback(String procURI, RPCCallback cb) {
        procURIs.put(procURI, cb);
    }

    public static RPCCallback getCallback(String procURI) {
        return procURIs.get(procURI);
    }

    public static void reset() {
        procURIs.clear();
    }
}
package ws.wampee7.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import ws.wampee7.annotations.URIPrefix;
import ws.wampee7.callbacks.PubSubCallback;
import ws.wampee7.controllers.messageHandlers.HandlerFactory;
import ws.wampee7.controllers.messageHandlers.MessageHandler;
import ws.wampee7.controllers.messageHandlers.PublishHandler;
import ws.wampee7.models.PubSub;
import ws.wampee7.models.RPC;
import ws.wampee7.models.WAMPee7Client;

@ServerEndpoint("/wamp")
public class WAMPee7Server {

    public static String VERSION = "WAMPlay/0.1.6";
    public static int PROTOCOL_VERSION = 1;
    public static WAMPee7Client lastClient;
    private static boolean controllersBound = false;
    
    static Map<String, WAMPee7Client> clients = Collections
            .unmodifiableMap(new HashMap<String, WAMPee7Client>());

    @OnOpen
    public void onOpen(Session sClient) {

        
        // Get WAMPify the Session
        final WAMPee7Client client = new WAMPee7Client(sClient);
        WAMPee7Server.addClient(client);
        handleRequest(client, null);
        lastClient = client;
    }

    @OnMessage
    public void onMessage(Session client, String message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(message);

        WAMPee7Client cc = getClient(client.getId());
        if (cc != null) {
            handleRequest(cc, actualObj);
        }
    }

    @OnClose
    public void onClose(Session client) {
        WAMPee7Client cc = getClient(client.getId());
        if (cc != null) {
            WAMPee7Server.removeClient(cc);
        }
    }

    /**
     * Sends a raw WAMP message to the correct controller. Method is public for
     * easier testing. Do not use in your application.
     *
     * @param Raw WAMP JSON request.
     * @param Originating client.
     */
    public static void handleRequest(WAMPee7Client client, JsonNode request) {
        try {
            MessageHandler handler = HandlerFactory.get(request);

            handler.process(client, request);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void addClient(WAMPee7Client client) {
        Map<String, WAMPee7Client> clientsNew = new HashMap<String, WAMPee7Client>();
        clientsNew.putAll(clients);
        clientsNew.put(client.getSessionID(), client);
        clients = Collections.unmodifiableMap(clientsNew);
        //log.debug("WAMPClient: " + client.getSessionID() + " connected.");
        System.out.println("WAMPClient: " + client.getSessionID() + " connected.");
    }

    private static synchronized void removeClient(WAMPee7Client client) {
        Map<String, WAMPee7Client> clientsNew = new HashMap<String, WAMPee7Client>();
        clientsNew.putAll(clients);
        clientsNew.remove(client.getSessionID());
        clients = Collections.unmodifiableMap(clientsNew);
        //log.debug("WAMPClient: " + client.getSessionID() + " disconnected.");
        System.out.println("WAMPClient: " + client.getSessionID() + " disconnected.");
    }

    /**
     * Gets a connected client with a ID. Can be used to send arbitrary JSON to
     * a specific client.
     *
     * @param sessionID Client's session ID as a string.
     * @return Connected WAMP client. Returns null if there is no client.
     */
    public static WAMPee7Client getClient(String clientID) {
        return clients.get(clientID);
    }

    /**
     * Gets a copy of the map of all the currently connected clients. This map
     * is immutable.
     *
     * @return A map of the currently connected WAMP clients.
     */
    public static Map<String, WAMPee7Client> getClients() {
        return clients;
    }

    /**
     * Add a PubSub topic and callbacks for clients to interact with. Topics
     * must be specifically added or clients could kill the server by filling it
     * up with useless topics. Adding topics through a controller's
     *
     * @onPublish or
     * @onSubscribe annotation is the preferred method.
     *
     * @param topicURI
     * @param pubSubCallback
     */
    public static void addTopic(String topicURI, PubSubCallback pubSubCallback) {
        PubSub.addTopic(topicURI, pubSubCallback);
    }

    /**
     * Remove a topic from the server. Clients will no longer be able to publish
     * or subscribe to this topic.
     *
     * @param topicURI
     */
    public static void removeTopic(String topicURI) {
        PubSub.removeTopic(topicURI);
    }

    /**
     * Add a PubSub topic with no callbacks for clients to interact with. Topics
     * must be specifically added or clients could kill the server by filling it
     * up with useless topics. Adding topics through a controller's
     *
     * @onPublish or
     * @onSubscribe annotation is the preferred method.
     *
     * @param topicURI
     */
    public static void addTopic(String topicURI) {
        PubSub.addTopic(topicURI);
    }

    /**
     * Checks if topicURI is a valid (subscribable) topic.
     *
     * @param topicURI
     * @return if the URI points to a valid topic
     */
    public static boolean isTopic(String topicURI) {
        return PubSub.getPubSubCallback(topicURI) == null ? false : true;
    }

    /**
     * Registers a controller for RPC and/or PubSub. Only one onPublish or
     * onSubscribe annotation is needed to add a topic.
     *
     * @param controller
     */
    public static void addController(WAMPee7Contoller controller) {
        String prefix = "";
        if (controller.getClass().isAnnotationPresent(URIPrefix.class)) {
            prefix = controller.getClass().getAnnotation(URIPrefix.class)
                    .value();
        }

        PubSub.addController(prefix, controller);
        RPC.addController(prefix, controller);
    }

    /**
     * Resets WAMPlayServer's state. Removes all controllers, topics, and
     * clients.
     */
    public static synchronized void reset() {
        clients = Collections
                .unmodifiableMap(new HashMap<String, WAMPee7Client>());
        PubSub.reset();
        RPC.reset();
    }

    /**
     * Publish an event to all clients with sessionIDs not in the exclude
     * collection.
     *
     * @param topicURI
     * @param event
     * @param exclude , Collection of sessionIDs to exclude.
     */
    public static void publishExclude(String topicURI, JsonNode event,
            Collection<String> exclude) {
        PublishHandler.publish(topicURI, event, exclude, null);
    }

    /**
     * Publish an event to all clients with sessionIDs in the eligible
     * collection.
     *
     * @param topicURI
     * @param event
     * @param exclude , Collection of eligible sessionIDs.
     */
    public static void publishEligible(String topicURI, JsonNode event,
            Collection<String> eligible) {
        PublishHandler.publish(topicURI, event, null, eligible);
    }

    /**
     * Publish an event to all clients.
     *
     * @param topicURI
     * @param event
     */
    public static void publish(String topicURI, JsonNode event) {
        PublishHandler.publish(topicURI, event, null, null);
    }
}
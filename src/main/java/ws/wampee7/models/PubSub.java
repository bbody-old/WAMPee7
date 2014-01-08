package ws.wampee7.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.jackson.JsonNode;

import ws.wampee7.annotations.onPublish;
import ws.wampee7.annotations.onSubscribe;
import ws.wampee7.callbacks.PubCallback;
import ws.wampee7.callbacks.PubSubCallback;
import ws.wampee7.callbacks.SubCallback;
import ws.wampee7.controllers.WAMPee7Contoller;


public class PubSub {
	//static ALogger log = Logger.of(PubSub.class.getSimpleName());
	static ConcurrentMap<String, PubSubCallback> topics = new ConcurrentHashMap<String, PubSubCallback>();

	public static void addController(String prefix, final WAMPee7Contoller controller) {

		for (final Method method : controller.getClass().getMethods()) {
			if (method.isAnnotationPresent(onPublish.class)) {
				String topic = prefix + method.getAnnotation(onPublish.class).value();
				PubCallback cb = new PubCallback() {

					@Override
					protected JsonNode onPublish(String sessionID, JsonNode eventJson) {
						try {
                                                    
                                                    JsonNode send = (JsonNode) method.invoke(controller, sessionID, eventJson);
                                                    System.out.println("Publish: " + send.toString());
							return send;
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							//log.error(controller.getClass().getSimpleName() + " " + method.getName() + " has incorrect arguments!");
                                                    String error = controller.getClass().getSimpleName() + " " + method.getName() + " has incorrect arguments!";
                                                    System.out.println(error);
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
				};
				addTopicCallback(topic, cb);
			}

			if (method.isAnnotationPresent(onSubscribe.class)) {
				String topic = prefix + method.getAnnotation(onSubscribe.class).value();
				SubCallback cb = new SubCallback() {

					@Override
					protected boolean onSubscribe(String sessionID) {
						try {
                                                    boolean result = (boolean) Boolean.valueOf(method.invoke(controller, sessionID).toString());
							return  result;
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							//log.error(controller.getClass().getSimpleName() + " " + method.getName() + " has incorrect arguments!");
                                                        String error = controller.getClass().getSimpleName() + " " + method.getName() + " has incorrect arguments!";
                                                    System.out.println(error);
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return false;
					}
				};
				addTopicCallback(topic, cb);
			}
		}
	}

	private static void addTopicCallback(String topic, PubCallback cb) {
		createOrGet(topic).setPubCallback(cb);
	}

	private static void addTopicCallback(String topic, SubCallback cb) {
		createOrGet(topic).setSubCallback(cb);
	}

	private static PubSubCallback createOrGet(String topic) {
		PubSubCallback pub = topics.get(topic);
		if (pub == null) {
			pub =  new PubSubCallback();
			topics.put(topic, pub);
		}
		return pub;
	}

	public static void addTopic(String topic) {
		// Just add a topic with no callback functions.
		addTopic(topic, new PubSubCallback());
	}

	public static void addTopic(String topic, PubSubCallback pubSubCallback) {
		topics.put(topic, pubSubCallback);

	}

	public static void removeTopic(String topic) {
		topics.remove(topic);

	}

	public static PubSubCallback getPubSubCallback(String topic) {
		return topics.get(topic);
	}

	public static void reset() {
		topics.clear();
	}

}

package ws.wampee7.callbacks;


public abstract class SubCallback {
	protected abstract boolean onSubscribe(String sessionID);
}

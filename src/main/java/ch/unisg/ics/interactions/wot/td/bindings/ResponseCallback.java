package ch.unisg.ics.interactions.wot.td.bindings;

/**
 * Callback called whenever a Thing asynchronously notifies the Consumer (during e.g. a {@code observeProperty} or
 * {@code subscribeEvent} operation).
 */
public interface ResponseCallback {

  void onResponse(Response response);

}

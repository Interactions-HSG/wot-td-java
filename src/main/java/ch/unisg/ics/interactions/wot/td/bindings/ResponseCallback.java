package ch.unisg.ics.interactions.wot.td.bindings;

public interface ResponseCallback {

  /**
   * Called whenever a Thing asynchronously notifies the Consumer (during e.g. a {@code observeProperty} or
   * {@code subscribeEvent} operation).
   */
  void onResponse(Response response);

  /**
   * Called if connection to the Thing is lost, after initial request was sent.
   * Note that if the Thing is in erroneous state but does send messages,
   * these messages will be passed to consumer via {@link ResponseCallback#onResponse(Response)}
   * (with error status).
   */
  void onError();

}

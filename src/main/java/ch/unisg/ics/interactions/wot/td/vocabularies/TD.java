package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class TD {
  public static final String PREFIX = "https://www.w3.org/2019/wot/td#";

  /* Classes */
  public static final String Thing = PREFIX + "Thing";
  public static final String ActionAffordance = PREFIX + "ActionAffordance";
  public static final String PropertyAffordance = PREFIX + "PropertyAffordance";
  public static final String EventAffordance = PREFIX + "EventAffordance";

  /* Object properties */
  public static final String baseURI = PREFIX + "baseURI";
  public static final String name = PREFIX + "name";

  public static final String hasInteractionAffordance = PREFIX + "hasInteractionAffordance";
  public static final String hasActionAffordance = PREFIX + "hasActionAffordance";
  public static final String hasPropertyAffordance = PREFIX + "hasPropertyAffordance";
  public static final String hasEventAffordance = PREFIX + "hasEventAffordance";

  public static final String hasSecurityConfiguration = PREFIX + "hasSecurityConfiguration";

  public static final String isObservable = PREFIX + "isObservable";

  public static final String hasInputSchema = PREFIX + "hasInputSchema";
  public static final String hasOutputSchema = PREFIX + "hasOutputSchema";

  public static final String hasSubscriptionSchema = PREFIX + "hasSubscriptionSchema";
  public static final String hasNotificationSchema = PREFIX + "hasNotificationSchema";
  public static final String hasCancellationSchema = PREFIX + "hasCancellationSchema";

  public static final String hasForm = PREFIX + "hasForm";

  public static final String hasUriTemplateSchema = PREFIX + "hasUriTemplateSchema";

  /* Named individuals */
  public static final String readProperty = PREFIX + "readProperty";
  public static final String writeProperty = PREFIX + "writeProperty";
  public static final String invokeAction = PREFIX + "invokeAction";
  public static final String observeProperty = PREFIX + "observeProperty";
  public static final String unobserveProperty = PREFIX + "unobserveProperty";
  public static final String subscribeEvent = PREFIX + "subscribeEvent";
  public static final String unsubscribeEvent = PREFIX + "unsubscribeEvent";

  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }

  private TD() {
  }
}

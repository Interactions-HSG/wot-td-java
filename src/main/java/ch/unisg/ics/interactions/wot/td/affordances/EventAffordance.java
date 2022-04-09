package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class EventAffordance extends InteractionAffordance {
  private final Optional<DataSchema> subscription;
  private final Optional<DataSchema> notification;
  private final Optional<DataSchema> cancellation;

  private EventAffordance(String name, Optional<String> title, List<String> types,
                          List<Form> forms, Optional<Map<String,DataSchema>> uriVariables,
                          Optional<DataSchema> subscription, Optional<DataSchema> notification,
                          Optional<DataSchema> cancellation) {
    super(name, title, types, forms, uriVariables);
    this.subscription = subscription;
    this.notification = notification;
    this.cancellation = cancellation;
  }

  public Optional<DataSchema> getSubscriptionSchema() {
    return subscription;
  }

  public Optional<DataSchema> getNotificationSchema() { return notification; }

  public Optional<DataSchema> getCancellationSchema() {
    return cancellation;
  }

  public static class Builder
    extends InteractionAffordance.Builder<EventAffordance, EventAffordance.Builder> {

    private Optional<DataSchema> subscription;
    private Optional<DataSchema> notification;
    private Optional<DataSchema> cancellation;

    public Builder(String name, List<Form> forms) {
      super(name, forms);

      for (Form form : this.forms) {
        if (form.getOperationTypes().isEmpty()) {
          form.addOperationType(TD.subscribeEvent);
          form.addOperationType(TD.unsubscribeEvent);
        }
      }

      this.subscription = Optional.empty();
      this.notification = Optional.empty();
      this.cancellation = Optional.empty();
    }

    public Builder(String name, Form form) {
      this(name, new ArrayList<>(Collections.singletonList(form)));
    }

    public EventAffordance.Builder addSubscriptionSchema(DataSchema subscription) {
      this.subscription = Optional.of(subscription);
      return this;
    }

    public EventAffordance.Builder addNotificationSchema(DataSchema data) {
      this.notification = Optional.of(data);
      return this;
    }

    public EventAffordance.Builder addCancellationSchema(DataSchema cancellation) {
      this.cancellation = Optional.of(cancellation);
      return this;
    }

    @Override
    public EventAffordance build() {
      return new EventAffordance(name, title, types, forms, uriVariables, subscription, notification, cancellation);
    }
  }
}

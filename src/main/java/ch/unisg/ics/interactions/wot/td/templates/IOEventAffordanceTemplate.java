package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.templates.InteractionAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class IOEventAffordanceTemplate extends InteractionAffordanceTemplate implements Template{
  private final Optional<DataSchema> subscription;
  private final Optional<DataSchema> notification;
  private final Optional<DataSchema> cancellation;

  private IOEventAffordanceTemplate(String name, Optional<String> title, List<String> types,
                          Optional<Map<String,DataSchema>> uriVariables,
                          Optional<DataSchema> subscription, Optional<DataSchema> notification,
                          Optional<DataSchema> cancellation) {
    super(name, title, types, uriVariables);
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

  @Override
  public boolean isTemplateOf(Object obj) {
    boolean b = false;
    if ( obj instanceof EventAffordance){
      EventAffordance event = (EventAffordance) obj;
      if ( this.title.equals(event.getTitle()) && getNotificationSchema().equals(event.getNotificationSchema()) &&
        getCancellationSchema().equals(event.getCancellationSchema()) && getSubscriptionSchema().equals(event.getSubscriptionSchema())){
        b = true;
      }
    }
    return b;
  }

  public static class Builder
    extends InteractionAffordanceTemplate.Builder<IOEventAffordanceTemplate, IOEventAffordanceTemplate.Builder> {

    private Optional<DataSchema> subscription;
    private Optional<DataSchema> notification;
    private Optional<DataSchema> cancellation;

    public Builder(String name) {
      super(name);

      this.subscription = Optional.empty();
      this.notification = Optional.empty();
      this.cancellation = Optional.empty();
    }


    public IOEventAffordanceTemplate.Builder addSubscriptionSchema(DataSchema subscription) {
      this.subscription = Optional.of(subscription);
      return this;
    }

    public IOEventAffordanceTemplate.Builder addNotificationSchema(DataSchema data) {
      this.notification = Optional.of(data);
      return this;
    }

    public IOEventAffordanceTemplate.Builder addCancellationSchema(DataSchema cancellation) {
      this.cancellation = Optional.of(cancellation);
      return this;
    }

    @Override
    public IOEventAffordanceTemplate build() {
      return new IOEventAffordanceTemplate(name, title, types, uriVariables, subscription, notification, cancellation);
    }
  }
}

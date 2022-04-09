package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventAffordanceTest {

  private EventAffordance testEvent;
  private Form form;

  @Before
  public void init() {
    form = new Form.Builder("http://example.org/event").build();
    testEvent = new EventAffordance.Builder("an_event", form).build();
  }

  @Test
  public void testOneForm() {
    List<Form> forms = testEvent.getForms();
    assertEquals(1, forms.size());
    assertEquals(form, forms.get(0));
  }

  @Test
  public void testMultipleForms() {
    Form form1 = new Form.Builder("http://example.org")
      .setMethodName("GET")
      .setContentType("application/json")
      .build();

    Form form2 = new Form.Builder("http://example.org")
      .setMethodName("POST")
      .setContentType("application/json")
      .build();

    Form form3 = new Form.Builder("http://example.org")
      .setMethodName("PUT")
      .setContentType("application/json")
      .build();

    List<Form> formList = new ArrayList<Form>(Arrays.asList(form1, form2, form3));

    EventAffordance event = new EventAffordance.Builder("name", formList).build();
    List<Form> forms = event.getForms();

    assertEquals(3, forms.size());
    assertEquals(form1, forms.get(0));
    assertEquals(form2, forms.get(1));
    assertEquals(form3, forms.get(2));
  }

  @Test
  public void testDefaultValues() {
    String subscribeEvent = TD.subscribeEvent;
    String unsubscribeEvent = TD.unsubscribeEvent;
    assertTrue(testEvent.hasFormWithOperationType(subscribeEvent));
    assertTrue(testEvent.hasFormWithOperationType(unsubscribeEvent));
  }

  @Test
  public void testFullOptionEvent() {
    Form form = new Form.Builder("http://example.org").setMethodName("GET").build();

    StringSchema subscriptionSchema = new StringSchema.Builder().build();
    IntegerSchema notificationSchema = new IntegerSchema.Builder().build();
    BooleanSchema cancellationSchema = new BooleanSchema.Builder().build();

    EventAffordance event = new EventAffordance.Builder("overheating", form)
      .addTitle("Overheating")
      .addSemanticType("ex:Overheating")
      .addSubscriptionSchema(subscriptionSchema)
      .addNotificationSchema(notificationSchema)
      .addCancellationSchema(cancellationSchema)
      .build();

    assertEquals("overheating", event.getName());
    assertEquals("Overheating", event.getTitle().get());
    assertEquals(1, event.getSemanticTypes().size());
    assertEquals("ex:Overheating", event.getSemanticTypes().get(0));

    assertTrue(event.getSubscriptionSchema().isPresent());
    assertTrue(event.getNotificationSchema().isPresent());
    assertTrue(event.getCancellationSchema().isPresent());

    assertEquals(subscriptionSchema, event.getSubscriptionSchema().get());
    assertEquals(notificationSchema, event.getNotificationSchema().get());
    assertEquals(cancellationSchema, event.getCancellationSchema().get());
  }
}

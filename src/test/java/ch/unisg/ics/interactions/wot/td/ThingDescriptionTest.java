package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class ThingDescriptionTest {

  private ThingDescription commonTd;

  @Before
  public void init() {
    PropertyAffordance prop0 = new PropertyAffordance.Builder("temp",
      new Form.Builder("http://example.org/prop0")
        .addOperationType(TD.readProperty)
        .build())
      .addSemanticType("ex:Temp")
      .addSemanticType("ex:Value")
      .build();

    PropertyAffordance prop1 = new PropertyAffordance.Builder("air-quality",
      new Form.Builder("http://example.org/prop1")
        .addOperationType(TD.readProperty)
        .build())
      .addSemanticType("ex:AirQuality")
      .addSemanticType("ex:Value")
      .build();

    ActionAffordance action0 = new ActionAffordance.Builder("setTemp",
      new Form.Builder("http://example.org/action0")
        .addOperationType(TD.invokeAction)
        .build())
      .addSemanticType("ex:SetTemp")
      .addSemanticType("ex:ModifyEnv")
      .build();

    ActionAffordance action1 = new ActionAffordance.Builder("openVentilator",
      new Form.Builder("http://example.org/action1")
        .addOperationType(TD.invokeAction)
        .build())
      .addSemanticType("ex:OpenVentilator")
      .addSemanticType("ex:ModifyEnv")
      .build();

    EventAffordance event0 = new EventAffordance.Builder("overheating",
      new Form.Builder("http://example.org/event0")
        .addOperationType(TD.subscribeEvent)
        .build())
      .addSemanticType("ex:Overheating")
      .addSemanticType("ex:Alarm")
      .build();

    EventAffordance event1 = new EventAffordance.Builder("smoke-alarm",
      new Form.Builder("http://example.org/event1")
        .addOperationType(TD.subscribeEvent)
        .build())
      .addSemanticType("ex:SmokeAlarm")
      .addSemanticType("ex:Alarm")
      .build();

    commonTd = new ThingDescription.Builder("A Thing")
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
      .addProperty(prop0)
      .addProperty(prop1)
      .addAction(action0)
      .addAction(action1)
      .addEvent(event0)
      .addEvent(event1)
      .build();
  }

  @Test
  public void testTitle() {
    ThingDescription td = new ThingDescription.Builder("My Thing").build();

    assertEquals("My Thing", td.getTitle());
  }

  @Test(expected = InvalidTDException.class)
  public void testTitleNull() {
    new ThingDescription.Builder(null).build();
  }

  @Test
  public void testURI() {
    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addThingURI("http://example.org/#thing")
      .build();

    assertEquals("http://example.org/#thing", td.getThingURI().get());
  }

  @Test
  public void testOneType() {
    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addSemanticType("http://w3id.org/eve#Artifact")
      .build();

    assertEquals(1, td.getSemanticTypes().size());
    assertTrue(td.getSemanticTypes().contains("http://w3id.org/eve#Artifact"));
  }

  @Test
  public void testMultipleTypes() {
    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addSemanticType(TD.Thing)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://iot-schema.org/eve#Light")
      .build();

    assertEquals(3, td.getSemanticTypes().size());
    assertTrue(td.getSemanticTypes().contains(TD.Thing));
    assertTrue(td.getSemanticTypes().contains("http://w3id.org/eve#Artifact"));
    assertTrue(td.getSemanticTypes().contains("http://iot-schema.org/eve#Light"));
  }

  @Test
  public void testBaseURI() {
    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addThingURI("http://example.org/#thing")
      .addBaseURI("http://example.org/")
      .build();

    assertEquals("http://example.org/", td.getBaseURI().get());
  }

  @Test
  public void testGetFirstSecuritySchemeByType() {
    ThingDescription td = new ThingDescription.Builder("Secured Thing")
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
      .addSecurityScheme("apikey_sc", new APIKeySecurityScheme.Builder().build())
      .build();

    Optional<SecurityScheme> scheme = td.getFirstSecuritySchemeByType(WoTSec.APIKeySecurityScheme);
    assertTrue(scheme.isPresent());
    assertTrue(SecurityScheme.APIKEY.equals(scheme.get().getSchemeName()));
  }

  @Test
  public void testGetPropertyByName() {
    Optional<PropertyAffordance> prop0 = commonTd.getPropertyByName("temp");
    assertTrue(prop0.isPresent());
    assertTrue(prop0.get().getSemanticTypes().contains("ex:Temp"));

    Optional<PropertyAffordance> prop1 = commonTd.getPropertyByName("air-quality");
    assertTrue(prop1.isPresent());
    assertTrue(prop1.get().getSemanticTypes().contains("ex:AirQuality"));
  }

  @Test
  public void testGetPropertiesByOperationType() {
    List<PropertyAffordance> readProps = commonTd.getPropertiesByOperationType(TD.readProperty);
    assertEquals(2, readProps.size());

    List<EventAffordance> writeProps = commonTd.getEventsByOperationType(TD.writeProperty);
    assertEquals(0, writeProps.size());
  }

  @Test
  public void testGetFirstPropertyBySemanticType() {
    Optional<PropertyAffordance> existingProp = commonTd.getFirstPropertyBySemanticType("ex:Value");
    assertTrue(existingProp.isPresent());
    assertTrue(existingProp.get().getSemanticTypes().contains("ex:Value"));

    Optional<PropertyAffordance> unknownProp = commonTd.getFirstPropertyBySemanticType("ex:NoValue");
    assertFalse(unknownProp.isPresent());
  }

  @Test
  public void testGetActionByName() {
    Optional<ActionAffordance> action0 = commonTd.getActionByName("setTemp");
    assertTrue(action0.isPresent());
    assertTrue(action0.get().getSemanticTypes().contains("ex:SetTemp"));

    Optional<ActionAffordance> action1 = commonTd.getActionByName("openVentilator");
    assertTrue(action1.isPresent());
    assertTrue(action1.get().getSemanticTypes().contains("ex:OpenVentilator"));
  }

  @Test
  public void testGetActionsByOperationType() {
    List<ActionAffordance> invokeActions = commonTd.getActionsByOperationType(TD.invokeAction);
    assertEquals(2, invokeActions.size());

    List<ActionAffordance> writeProps = commonTd.getActionsByOperationType(TD.writeProperty);
    assertEquals(0, writeProps.size());
  }

  @Test
  public void testGetFirstActionBySemanticType() {
    Optional<ActionAffordance> existingAction = commonTd.getFirstActionBySemanticType("ex:ModifyEnv");
    assertTrue(existingAction.isPresent());
    assertTrue(existingAction.get().getSemanticTypes().contains("ex:ModifyEnv"));

    Optional<ActionAffordance> unknownAction = commonTd.getFirstActionBySemanticType("ex:NoModifyEnv");
    assertFalse(unknownAction.isPresent());
  }

  @Test
  public void testGetEventByName() {
    Optional<EventAffordance> event0 = commonTd.getEventByName("overheating");
    assertTrue(event0.isPresent());
    assertTrue(event0.get().getSemanticTypes().contains("ex:Overheating"));

    Optional<EventAffordance> event1 = commonTd.getEventByName("smoke-alarm");
    assertTrue(event1.isPresent());
    assertTrue(event1.get().getSemanticTypes().contains("ex:SmokeAlarm"));

    Optional<EventAffordance> event2 = commonTd.getEventByName("unknown-event");
    assertFalse(event2.isPresent());
  }

  @Test
  public void testGetEventsByOperationType() {
    List<EventAffordance> subscribeEvents = commonTd.getEventsByOperationType(TD.subscribeEvent);
    assertEquals(2, subscribeEvents.size());

    List<EventAffordance> unsubscribeEvents = commonTd.getEventsByOperationType(TD.unsubscribeEvent);
    assertEquals(0, unsubscribeEvents.size());
  }

  @Test
  public void testGetFirstEventBySemanticType() {
    Optional<EventAffordance> existingEvent = commonTd.getFirstEventBySemanticType("ex:Alarm");
    assertTrue(existingEvent.isPresent());
    assertTrue(existingEvent.get().getSemanticTypes().contains("ex:Alarm"));

    Optional<EventAffordance> unknownEvent = commonTd.getFirstEventBySemanticType("ex:NoAlarm");
    assertFalse(unknownEvent.isPresent());
  }
}

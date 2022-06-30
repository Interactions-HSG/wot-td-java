package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.templates.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ThingDescriptionTemplateTest {

  private ThingDescriptionTemplate commonTDT;

  @Before
  public void init() {
    PropertyAffordanceTemplate prop0 = new PropertyAffordanceTemplate.Builder("temp")
      .addSemanticType("ex:Temp")
      .addSemanticType("ex:Value")
      .build();

    PropertyAffordanceTemplate prop1 = new PropertyAffordanceTemplate.Builder("air-quality")
      .addSemanticType("ex:AirQuality")
      .addSemanticType("ex:Value")
      .build();

    ActionAffordanceTemplate action0 = new ActionAffordanceTemplate.Builder("setTemp")
      .addSemanticType("ex:SetTemp")
      .addSemanticType("ex:ModifyEnv")
      .build();

    ActionAffordanceTemplate action1 = new ActionAffordanceTemplate.Builder("openVentilator")
      .addSemanticType("ex:OpenVentilator")
      .addSemanticType("ex:ModifyEnv")
      .build();

    EventAffordanceTemplate event0 = new EventAffordanceTemplate.Builder("overheating")
      .addSemanticType("ex:Overheating")
      .addSemanticType("ex:Alarm")
      .build();

    EventAffordanceTemplate event1 = new EventAffordanceTemplate.Builder("smoke-alarm")
      .addSemanticType("ex:SmokeAlarm")
      .addSemanticType("ex:Alarm")
      .build();

    commonTDT = new ThingDescriptionTemplate.Builder("A Thing")
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
      .addSecurityScheme(new NoSecurityScheme())
      .addSecurityScheme(new APIKeySecurityScheme())
      .build();

    Optional<SecurityScheme> scheme = td.getFirstSecuritySchemeByType(WoTSec.APIKeySecurityScheme);
    assertTrue(scheme.isPresent());
    assertEquals(WoTSec.APIKeySecurityScheme, scheme.get().getSchemeType());
  }

  @Test
  public void testGetPropertyByName() {
    Optional<PropertyAffordanceTemplate> prop0 = commonTDT.getPropertyByName("temp");
    assertTrue(prop0.isPresent());
    assertTrue(prop0.get().getSemanticTypes().contains("ex:Temp"));

    Optional<PropertyAffordanceTemplate> prop1 = commonTDT.getPropertyByName("air-quality");
    assertTrue(prop1.isPresent());
    assertTrue(prop1.get().getSemanticTypes().contains("ex:AirQuality"));
  }



  @Test
  public void testGetFirstPropertyBySemanticType() {
    Optional<PropertyAffordanceTemplate> existingProp = commonTDT.getFirstPropertyBySemanticType("ex:Value");
    assertTrue(existingProp.isPresent());
    assertTrue(existingProp.get().getSemanticTypes().contains("ex:Value"));

    Optional<PropertyAffordanceTemplate> unknownProp = commonTDT.getFirstPropertyBySemanticType("ex:NoValue");
    assertFalse(unknownProp.isPresent());
  }

  @Test
  public void testGetActionByName() {
    Optional<ActionAffordanceTemplate> action0 = commonTDT.getActionByName("setTemp");
    assertTrue(action0.isPresent());
    assertTrue(action0.get().getSemanticTypes().contains("ex:SetTemp"));

    Optional<ActionAffordanceTemplate> action1 = commonTDT.getActionByName("openVentilator");
    assertTrue(action1.isPresent());
    assertTrue(action1.get().getSemanticTypes().contains("ex:OpenVentilator"));
  }



  @Test
  public void testGetFirstActionBySemanticType() {
    Optional<ActionAffordanceTemplate> existingAction = commonTDT.getFirstActionBySemanticType("ex:ModifyEnv");
    assertTrue(existingAction.isPresent());
    assertTrue(existingAction.get().getSemanticTypes().contains("ex:ModifyEnv"));

    Optional<ActionAffordanceTemplate> unknownAction = commonTDT.getFirstActionBySemanticType("ex:NoModifyEnv");
    assertFalse(unknownAction.isPresent());
  }

  @Test
  public void testGetEventByName() {
    Optional<EventAffordanceTemplate> event0 = commonTDT.getEventByName("overheating");
    assertTrue(event0.isPresent());
    assertTrue(event0.get().getSemanticTypes().contains("ex:Overheating"));

    Optional<EventAffordanceTemplate> event1 = commonTDT.getEventByName("smoke-alarm");
    assertTrue(event1.isPresent());
    assertTrue(event1.get().getSemanticTypes().contains("ex:SmokeAlarm"));

    Optional<EventAffordanceTemplate> event2 = commonTDT.getEventByName("unknown-event");
    assertFalse(event2.isPresent());
  }


  @Test
  public void testGetFirstEventBySemanticType() {
    Optional<EventAffordanceTemplate> existingEvent = commonTDT.getFirstEventBySemanticType("ex:Alarm");
    assertTrue(existingEvent.isPresent());
    assertTrue(existingEvent.get().getSemanticTypes().contains("ex:Alarm"));

    Optional<EventAffordanceTemplate> unknownEvent = commonTDT.getFirstEventBySemanticType("ex:NoAlarm");
    assertFalse(unknownEvent.isPresent());
  }
}

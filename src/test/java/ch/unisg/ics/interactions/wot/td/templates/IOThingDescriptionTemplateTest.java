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

public class IOThingDescriptionTemplateTest {

  private IOThingDescriptionTemplate commonIOTDT;

  @Before
  public void init() {
    IOPropertyAffordanceTemplate prop0 = new IOPropertyAffordanceTemplate.Builder("temp")
      .addSemanticType("ex:Temp")
      .addSemanticType("ex:Value")
      .build();

    IOPropertyAffordanceTemplate prop1 = new IOPropertyAffordanceTemplate.Builder("air-quality")
      .addSemanticType("ex:AirQuality")
      .addSemanticType("ex:Value")
      .build();

    IOActionAffordanceTemplate action0 = new IOActionAffordanceTemplate.Builder("setTemp")
      .addSemanticType("ex:SetTemp")
      .addSemanticType("ex:ModifyEnv")
      .build();

    IOActionAffordanceTemplate action1 = new IOActionAffordanceTemplate.Builder("openVentilator")
      .addSemanticType("ex:OpenVentilator")
      .addSemanticType("ex:ModifyEnv")
      .build();

    IOEventAffordanceTemplate event0 = new IOEventAffordanceTemplate.Builder("overheating")
      .addSemanticType("ex:Overheating")
      .addSemanticType("ex:Alarm")
      .build();

    IOEventAffordanceTemplate event1 = new IOEventAffordanceTemplate.Builder("smoke-alarm")
      .addSemanticType("ex:SmokeAlarm")
      .addSemanticType("ex:Alarm")
      .build();

    commonIOTDT = new IOThingDescriptionTemplate.Builder("A Thing")
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
    Optional<IOPropertyAffordanceTemplate> prop0 = commonIOTDT.getPropertyByName("temp");
    assertTrue(prop0.isPresent());
    assertTrue(prop0.get().getSemanticTypes().contains("ex:Temp"));

    Optional<IOPropertyAffordanceTemplate> prop1 = commonIOTDT.getPropertyByName("air-quality");
    assertTrue(prop1.isPresent());
    assertTrue(prop1.get().getSemanticTypes().contains("ex:AirQuality"));
  }



  @Test
  public void testGetFirstPropertyBySemanticType() {
    Optional<IOPropertyAffordanceTemplate> existingProp = commonIOTDT.getFirstPropertyBySemanticType("ex:Value");
    assertTrue(existingProp.isPresent());
    assertTrue(existingProp.get().getSemanticTypes().contains("ex:Value"));

    Optional<IOPropertyAffordanceTemplate> unknownProp = commonIOTDT.getFirstPropertyBySemanticType("ex:NoValue");
    assertFalse(unknownProp.isPresent());
  }

  @Test
  public void testGetActionByName() {
    Optional<IOActionAffordanceTemplate> action0 = commonIOTDT.getActionByName("setTemp");
    assertTrue(action0.isPresent());
    assertTrue(action0.get().getSemanticTypes().contains("ex:SetTemp"));

    Optional<IOActionAffordanceTemplate> action1 = commonIOTDT.getActionByName("openVentilator");
    assertTrue(action1.isPresent());
    assertTrue(action1.get().getSemanticTypes().contains("ex:OpenVentilator"));
  }



  @Test
  public void testGetFirstActionBySemanticType() {
    Optional<IOActionAffordanceTemplate> existingAction = commonIOTDT.getFirstActionBySemanticType("ex:ModifyEnv");
    assertTrue(existingAction.isPresent());
    assertTrue(existingAction.get().getSemanticTypes().contains("ex:ModifyEnv"));

    Optional<IOActionAffordanceTemplate> unknownAction = commonIOTDT.getFirstActionBySemanticType("ex:NoModifyEnv");
    assertFalse(unknownAction.isPresent());
  }

  @Test
  public void testGetEventByName() {
    Optional<IOEventAffordanceTemplate> event0 = commonIOTDT.getEventByName("overheating");
    assertTrue(event0.isPresent());
    assertTrue(event0.get().getSemanticTypes().contains("ex:Overheating"));

    Optional<IOEventAffordanceTemplate> event1 = commonIOTDT.getEventByName("smoke-alarm");
    assertTrue(event1.isPresent());
    assertTrue(event1.get().getSemanticTypes().contains("ex:SmokeAlarm"));

    Optional<IOEventAffordanceTemplate> event2 = commonIOTDT.getEventByName("unknown-event");
    assertFalse(event2.isPresent());
  }


  @Test
  public void testGetFirstEventBySemanticType() {
    Optional<IOEventAffordanceTemplate> existingEvent = commonIOTDT.getFirstEventBySemanticType("ex:Alarm");
    assertTrue(existingEvent.isPresent());
    assertTrue(existingEvent.get().getSemanticTypes().contains("ex:Alarm"));

    Optional<IOEventAffordanceTemplate> unknownEvent = commonIOTDT.getFirstEventBySemanticType("ex:NoAlarm");
    assertFalse(unknownEvent.isPresent());
  }
}

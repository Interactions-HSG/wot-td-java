package ch.unisg.ics.interactions.wot.td;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class ThingDescriptionTest {
  
  @Test
  public void testTitle() {
    ThingDescription td = new ThingDescription.Builder("My Thing").build();
    
    assertEquals("My Thing", td.getTitle());
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
  
  public void testGetFirstSecuritySchemeByType() {
    ThingDescription td = new ThingDescription.Builder("Secured Thing")
        .addSecurityScheme(new NoSecurityScheme())
        .addSecurityScheme(new APIKeySecurityScheme())
        .build();
    
    Optional<SecurityScheme> scheme = td.getFirstSecuritySchemeByType(WoTSec.APIKeySecurityScheme);
    assertTrue(scheme.isPresent());
    assertEquals(WoTSec.APIKeySecurityScheme, scheme.get().getSchemaType());
  }
}

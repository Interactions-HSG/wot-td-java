package ch.unisg.ics.interactions.wot.td;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.vocabularies.TDVocab;

public class ThingDescriptionTest {
  
  @Test
  public void testTitle() {
    ThingDescription td = (new ThingDescription.Builder("My Thing")).build();
    
    assertEquals("My Thing", td.getTitle());
  }
  
  @Test
  public void testURI() {
    ThingDescription td = (new ThingDescription.Builder("My Thing"))
        .addURI("http://example.org/#thing")
        .build();
    
    assertEquals("http://example.org/#thing", td.getThingURI().get());
  }
  
  @Test
  public void testOneType() {
    ThingDescription td = (new ThingDescription.Builder("My Thing"))
        .addType("http://w3id.org/eve#Artifact")
        .build();
    
    assertEquals(1, td.getTypes().size());
    assertEquals("http://w3id.org/eve#Artifact", td.getTypes().get(0));
  }
  
  @Test
  public void testMultipleTypes() {
    ThingDescription td = (new ThingDescription.Builder("My Thing"))
        .addType(TDVocab.Thing.getIRIString())
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://iot-schema.org/eve#Light")
        .build();
    
    assertEquals(3, td.getTypes().size());
    assertEquals(TDVocab.Thing.getIRIString(), td.getTypes().get(0));
    assertEquals("http://w3id.org/eve#Artifact", td.getTypes().get(1));
    assertEquals("http://iot-schema.org/eve#Light", td.getTypes().get(2));
  }
  
  @Test
  public void testBaseURI() {
    ThingDescription td = (new ThingDescription.Builder("My Thing"))
        .addURI("http://example.org/#thing")
        .addBaseURI("http://example.org/")
        .build();
    
    assertEquals("http://example.org/", td.getBaseURI().get());
  }
}

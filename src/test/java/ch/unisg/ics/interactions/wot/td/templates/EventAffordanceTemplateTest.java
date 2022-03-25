package ch.unisg.ics.interactions.wot.td.templates;

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

public class EventAffordanceTemplateTest {



  @Test
  public void testFullOptionEvent() {

    EventAffordanceTemplate event = new EventAffordanceTemplate.Builder("overheating")
      .addTitle("Overheating")
      .addSemanticType("ex:Overheating")
      .build();

    assertEquals("overheating", event.getName());
    assertEquals("Overheating", event.getTitle().get());
    assertEquals(1, event.getSemanticTypes().size());
    assertEquals("ex:Overheating", event.getSemanticTypes().get(0));

  }
}

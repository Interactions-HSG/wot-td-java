package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IOActionAffordanceTemplateTest {





  @Test
  public void testFullOptionAction() {
    IOActionAffordanceTemplate action = new IOActionAffordanceTemplate.Builder("turn_on")
      .addTitle("Turn on")
      .addSemanticType("iot:TurnOn")
      // TODO: add schema as well
      //.addInputSchema(inputSchema)
      .build();

    assertEquals("turn_on", action.getName());
    assertEquals("Turn on", action.getTitle().get());
    assertEquals(1, action.getSemanticTypes().size());
    assertEquals("iot:TurnOn", action.getSemanticTypes().get(0));
  }
}

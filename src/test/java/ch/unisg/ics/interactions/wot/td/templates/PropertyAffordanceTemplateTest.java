package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyAffordanceTemplateTest {

  private PropertyAffordanceTemplate testProperty;

  @Before
  public void init() {

    testProperty = new PropertyAffordanceTemplate.Builder("my_property")
      .addTitle("My Property")
      .addSemanticType("sem_type")
      .addObserve()
      .build();
  }

  @Test
  public void testProperty() {
    assertEquals("My Property", testProperty.getTitle().get());
    assertTrue(testProperty.getSemanticTypes().contains("sem_type"));
    assertTrue(testProperty.isObservable());
  }


}

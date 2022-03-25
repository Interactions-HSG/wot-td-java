package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IOPropertyAffordanceTemplateTest {

  private IOPropertyAffordanceTemplate testProperty;

  @Before
  public void init() {
    DataSchema schema = new NumberSchema.Builder().build();

    testProperty = new IOPropertyAffordanceTemplate.Builder("my_property")
      .addTitle("My Property")
      .addDataSchema(schema)
      .addSemanticType("sem_type")
      .addObserve()
      .build();
  }

  @Test
  public void testProperty() {
    assertEquals("My Property", testProperty.getTitle().get());
    assertTrue(testProperty.getSemanticTypes().contains("sem_type"));
    assertEquals(DataSchema.NUMBER, testProperty.getDataSchema().getDatatype());
    assertTrue(testProperty.isObservable());
  }

}

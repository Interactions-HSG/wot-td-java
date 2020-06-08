package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class PropertyAffordanceTest {

  private PropertyAffordance testProperty;
  
  @Before
  public void init() {
    DataSchema schema = new NumberSchema.Builder().build();
    Form form = new Form.Builder("http://example.org/action1")
        .build();
    
    testProperty = new PropertyAffordance.Builder(schema, form)
        .addTitle("My Property")
        .addSemanticType("sem_type")
        .addObserve()
        .build();
  }
  
  @Test
  public void testProperty() {
    assertEquals("My Property", testProperty.getTitle().get());
    assertTrue(testProperty.getSemanticTypes().contains("sem_type"));
    assertEquals(DataSchema.NUMBER, testProperty.getDataSchema().getDatatype());
    assertEquals(1, testProperty.getForms().size());
    assertTrue(testProperty.isObservable());
  }
  
  @Test
  public void testDefaultOperationTypes() {
    assertTrue(testProperty.hasFormWithOperationType(TD.readProperty));
    assertTrue(testProperty.hasFormWithOperationType(TD.writeProperty));
    
    Form form = testProperty.getForms().get(0);
    assertEquals("GET", form.getMethodName(TD.readProperty).get());
    assertEquals("PUT", form.getMethodName(TD.writeProperty).get());
  }
}

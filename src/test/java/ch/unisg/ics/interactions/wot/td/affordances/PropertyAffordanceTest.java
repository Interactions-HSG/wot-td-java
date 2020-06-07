package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class PropertyAffordanceTest {

  @Test
  public void testProperty() {
    Set<String> operationTypes = Stream.of(TD.readProperty.stringValue(),
        TD.writeProperty.stringValue())
        .collect(Collectors.toCollection(HashSet::new));
    
    DataSchema schema = new NumberSchema.Builder().build();
    Form form = new Form("PUT", "http://example.org/action1", "application/json", operationTypes);
    
    PropertyAffordance property = new PropertyAffordance.Builder(schema, form)
        .addTitle("My Property")
        .addSemanticType("sem_type")
        .addObserve()
        .build();
    
    assertEquals("My Property", property.getTitle().get());
    assertTrue(property.getSemanticTypes().contains("sem_type"));
    assertEquals(DataSchema.NUMBER, property.getDataSchema().getDatatype());
    assertEquals(1, property.getForms().size());
    assertTrue(property.hasFormWithOperationType(TD.readProperty.stringValue()));
    assertTrue(property.hasFormWithOperationType(TD.writeProperty.stringValue()));
    assertTrue(property.isObservable());
  }
}

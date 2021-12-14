package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UriVariableTest {

  private Form form;
  private Optional<Map<String, DataSchema>> uriVariable;
  private InteractionAffordance affordance;

  @Before
  public void init() {
    form = new Form.Builder("http://example.org/action").build();
    Map<String, DataSchema> map = new HashMap<>();
    map.put("name", new StringSchema.Builder().build());
    uriVariable = Optional.of(map);
    affordance = new ActionAffordance.Builder("actionAffordance", form).build();
  }

  @Test
  public void createUriVariableAffordance() {
    assertTrue(uriVariable.isPresent());
    affordance = new ActionAffordance.Builder("actionAffordance", form)
      .addUriVariables(uriVariable.get())
      .build();
    assertEquals(uriVariable, affordance.getUriVariables());
  }

  @Test
  public void getUriVariable() {
    assertTrue(uriVariable.isPresent());
    affordance = new ActionAffordance.Builder("actionAffordance", form)
      .addUriVariables(uriVariable.get())
      .build();
    assertTrue(affordance.getUriVariables().isPresent());
    Map<String, DataSchema> uriVariablesMap = affordance.getUriVariables().get();
    assertEquals(DataSchema.STRING, uriVariablesMap.get("name").getDatatype());
  }

  @Test
  public void createManyUriVariables() {
    Map<String, DataSchema> map = new HashMap<>();
    map.put("name", new StringSchema.Builder().build());
    map.put("number", new NumberSchema.Builder().build());
    affordance = new ActionAffordance.Builder("actionAffordance", form)
      .addUriVariables(map)
      .build();
    assertTrue(affordance.getUriVariables().isPresent());
    assertEquals(DataSchema.STRING, affordance.getUriVariables().get().get("name").getDatatype());
    assertEquals(DataSchema.NUMBER, affordance.getUriVariables().get().get("number").getDatatype());
  }

  @Test(expected = IllegalArgumentException.class)
  public void impossibleUriVariable() {
    DataSchema name = new StringSchema.Builder().build();
    DataSchema objectVariable = new ObjectSchema.Builder()
      .addProperty("name", name)
      .build();
    Map<String, DataSchema> map = new HashMap<>();
    map.put("object", objectVariable);
    affordance = new ActionAffordance.Builder("actionAffordance", form)
      .addUriVariables(map)
      .build();
  }
}


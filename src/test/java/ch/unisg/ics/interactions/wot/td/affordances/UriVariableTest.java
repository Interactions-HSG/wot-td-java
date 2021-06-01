package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UriVariableTest {

  //private InteractionAffordance testInteraction;
  private Form form;
  private DataSchema uriVariable;
  private InteractionAffordance affordance;

  @Before
  public void init() {
    form = new Form.Builder("http://example.org/action").build();
    uriVariable=new StringSchema.Builder().build();
    affordance=new ActionAffordance.Builder(form).build();
  }

  @Test
  public void createUriVariableAffordance(){
    affordance=new ActionAffordance.Builder(form)
      .addUriVariable(uriVariable)
      .build();
  }

  @Test
  public void getUriVariable(){
    affordance=new ActionAffordance.Builder(form)
      .addUriVariable(uriVariable)
      .build();
    assertEquals(uriVariable,affordance.getUriVariables().get(0));
  }

  @Test
  public void createManyUriVariables(){
    DataSchema newUriVariable= new NumberSchema.Builder().build();
    List<DataSchema> variables=new ArrayList<>();
    variables.add(uriVariable);
    variables.add(newUriVariable);
    affordance=new ActionAffordance.Builder(form)
      .addUriVariables(variables)
      .build();
    assertEquals(newUriVariable,affordance.getUriVariables().get(1));
  }
}

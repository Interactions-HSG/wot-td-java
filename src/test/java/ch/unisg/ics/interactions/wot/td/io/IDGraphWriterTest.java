package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionInput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionOutput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

public class IDGraphWriterTest {

  private InteractionDescription commonID;

  @Before
  public void init() {
    Form form = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction)
      .setContentType("application/json")
      .setMethodName("POST")
      .build();

    DataSchema schema = new StringSchema.Builder().build();

    commonID = InteractionDescription.builder()
      .title("interaction-1")
      .input(new InteractionInput("input1", form, schema))
      .output(new InteractionOutput("output1", schema))
      .type(InteractionTypes.ACTION)
      .uri("http://example.org/log#interaction-1")
      .build();
  }

  @Test
  public void testWriteID() {
    String output = IDGraphWriter.write(commonID);
    System.out.println(output);
  }
}

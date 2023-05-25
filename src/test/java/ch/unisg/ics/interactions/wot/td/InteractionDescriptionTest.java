package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionInput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionOutput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InteractionDescriptionTest {

  @Test
  public void testTitle() {
    String title = "title of interaction description";
    InteractionDescription intd = InteractionDescription.builder().title(title).build();

    assertEquals(title, intd.getTitle());
  }

  @Test(expected = NullPointerException.class)
  public void testTitleNull() {
    InteractionDescription.builder().build();
  }

  @Test
  public void testURI() {
    InteractionDescription intd = InteractionDescription.builder()
      .title("interaction-1")
      .uri("http://example.org/#thing")
      .build();

    assertEquals("http://example.org/#thing", intd.getUri());
  }

  @Test
  public void testInput() {
    Form form = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction)
      .setContentType("application/json")
      .setMethodName("POST")
      .build();

    DataSchema schema = new DataSchema.Builder()
      .addSemanticType(DataSchema.STRING)
      .build();

    InteractionInput input = new InteractionInput("input1", form, schema);

    InteractionDescription intd = InteractionDescription.builder()
      .title("interaction-1")
      .input(input)
      .build();

    assertSame(input, intd.getInput());
  }

  @Test
  public void testOutput() {
    DataSchema schema = new DataSchema.Builder()
      .addSemanticType(DataSchema.STRING)
      .build();

    InteractionOutput output = new InteractionOutput("output1", schema);

    InteractionDescription intd = InteractionDescription.builder()
      .title("interaction-1")
      .output(output)
      .build();

    assertSame(output, intd.getOutput());
  }

  @Test
  public void testTypeAction() {
    InteractionDescription intd = InteractionDescription.builder()
      .title("interaction-1")
      .type(InteractionTypes.ACTION)
      .build();

    assertEquals(InteractionTypes.ACTION, intd.getType());
  }

  @Test
  public void testTypeEvent() {
    InteractionDescription intd = InteractionDescription.builder()
      .title("interaction-1")
      .type(InteractionTypes.EVENT)
      .build();

    assertEquals(InteractionTypes.EVENT, intd.getType());
  }
}

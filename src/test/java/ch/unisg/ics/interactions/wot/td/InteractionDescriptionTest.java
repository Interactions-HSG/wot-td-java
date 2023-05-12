package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionInput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionOutput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InteractionDescriptionTest {

  @Before
  public void init() {
    Form form = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction)
      .setContentType("application/json")
      .setMethodName("POST")
      .build();

    DataSchema schema = new DataSchema.Builder()
      .addSemanticType(DataSchema.STRING)
      .build();

    InteractionDescription commonID = InteractionDescription.builder()
      .title("interaction-1")
      .input(new InteractionInput("input1", form, schema))
      .output(new InteractionOutput("output1", schema))
      .type(InteractionTypes.ACTION)
      .uri("http://example.org/interaction")
      .build();
  }

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
    InteractionDescription td = InteractionDescription.builder()
      .title("interaction-1")
      .uri("http://example.org/#thing")
      .build();

    assertEquals("http://example.org/#thing", td.getUri());
  }
}

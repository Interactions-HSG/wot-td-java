package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionInput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionOutput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertTrue;

public class IDGraphWriterTest {
  private static final String ID_TITLE = "actionlog-1";
  private static final String PREFIXES =
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix log: <https://example.org/log#> .\n";

  private Form GET_FORM;

  private Form POST_FORM;
  private StringSchema STRING_SCHEMA;

  private static final String IO_BASE_IRI = "http://example.org/";

  @Before
  public void init() {
    this.POST_FORM = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction)
      .setContentType("application/json")
      .setMethodName("POST")
      .build();

    this.GET_FORM = new Form.Builder("http://example.org/action")
      .addOperationType(TD.readProperty)
      .setMethodName("GET")
      .build();

    this.STRING_SCHEMA  = new StringSchema.Builder().build();
  }

  @Test
  public void testWriteCompleteID() throws IOException {
    InteractionDescription completeIntD = InteractionDescription.builder()
      .title(ID_TITLE)
      .uri("http://example.org/log#actionlog-1")
      .form(POST_FORM)
      .input(new InteractionInput("input1", STRING_SCHEMA))
      .output(new InteractionOutput("output1", STRING_SCHEMA))
      .type(InteractionTypes.ACTION)
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#actionlog-1> a log:ActionExecution;\n" +
        "  log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#actionlog-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasInput [ a <https://www.w3.org/2019/wot/json-schema#StringSchema>;\n" +
        "      log:value \"input1\"\n" +
        "    ];\n" +
        "  log:hasOutput [ a <https://www.w3.org/2019/wot/json-schema#StringSchema>;\n" +
        "      log:value \"output1\"\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"POST\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#invokeAction>\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, completeIntD);
  }

  @Test
  public void testWriteIDWithoutInput() throws IOException {
    InteractionDescription intDWithoutInput = InteractionDescription.builder()
      .title(ID_TITLE)
      .type(InteractionTypes.EVENT)
      .form(GET_FORM)
      .output(new InteractionOutput("output1", STRING_SCHEMA))
      .uri("http://example.org/log#actionlog-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#actionlog-1> a log:Event;\n" +
        "  log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#actionlog-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasOutput [ a <https://www.w3.org/2019/wot/json-schema#StringSchema>;\n" +
        "      log:value \"output1\"\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"GET\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#readProperty>\n" +
        "    ] .", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithoutInput);
  }

  @Test
  public void testWriteIDWithoutOutput() throws IOException {
    InteractionDescription intDWithoutOutput = InteractionDescription.builder()
      .title(ID_TITLE)
      .input(new InteractionInput("input1", STRING_SCHEMA))
      .form(POST_FORM)
      .type(InteractionTypes.ACTION)
      .uri("http://example.org/log#interaction-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#interaction-1> a log:ActionExecution;\n" +
        "  log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#interaction-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasInput [ a <https://www.w3.org/2019/wot/json-schema#StringSchema>;\n" +
        "      log:value \"input1\"\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"POST\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#invokeAction>\n" +
        "    ] .", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithoutOutput);
  }

  private String getDateTimeWithoutSeconds() {
    OffsetDateTime dateTime = OffsetDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    return dateTime.format(formatter);
  }

  private void assertIsomorphicGraphs(String expectedID, InteractionDescription id) throws RDFParseException,
    RDFHandlerException, IOException {
    Model expectedModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, expectedID, IO_BASE_IRI);

    String description = new IDGraphWriter(id).write();

    System.out.println("Actual:");
    System.out.println(description);
    System.out.println("Expected:");
    System.out.println(expectedID);

    Model idModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);

    assertTrue(Models.isomorphic(expectedModel, idModel));
  }
}

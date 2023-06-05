package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionInput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionOutput;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
      "@prefix log: <https://example.org/log#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n";

  private Form GET_FORM;

  private Form POST_FORM;
  private StringSchema STRING_SCHEMA;
  private IntegerSchema INTEGER_SCHEMA;
  private ArraySchema ARRAY_SCHEMA;
  private ObjectSchema OBJECT_SCHEMA;
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
    this.INTEGER_SCHEMA = new IntegerSchema.Builder().build();
    this.ARRAY_SCHEMA = new ArraySchema.Builder().build();
    this.OBJECT_SCHEMA = new ObjectSchema.Builder().build();
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
        "  log:hasInput [ a log:Input;\n" +
        "      log:hasValue \"input1\";\n" +
        "      log:hasSchema [ a js:StringSchema\n" +
        "        ]\n" +
        "    ];\n" +
        "  log:hasOutput [ a log:Output;\n" +
        "      log:hasValue \"output1\";\n" +
        "      log:hasSchema [ a js:StringSchema\n" +
        "        ]\n" +
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
  public void testWriteIDWithStringOutput() throws IOException {
    InteractionDescription intDWithStringOutput = InteractionDescription.builder()
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
        "  log:hasOutput [ a log:Output;\n" +
        "      log:hasValue \"output1\";\n" +
        "      log:hasSchema [ a js:StringSchema\n" +
        "        ]\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"GET\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#readProperty>\n" +
        "    ] .", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithStringOutput);
  }

  @Test
  public void testWriteIDWithStringInput() throws IOException {
    InteractionDescription intDWithStringInput = InteractionDescription.builder()
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
        "  log:hasInput [ a log:Input;\n" +
        "      log:hasValue \"input1\";\n" +
        "      log:hasSchema [ a js:StringSchema\n" +
        "        ]\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"POST\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#invokeAction>\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithStringInput);
  }

  @Test
  public void testWriteIDWithIntegerInput() throws IOException {
    InteractionDescription intDWithIntegerInput = InteractionDescription.builder()
      .title(ID_TITLE)
      .input(new InteractionInput(1, INTEGER_SCHEMA))
      .uri("http://example.org/log#interaction-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#interaction-1> log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#interaction-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasInput [ a log:Input;\n" +
        "      log:hasValue \"1\"^^xsd:int;\n" +
        "      log:hasSchema [ a js:IntegerSchema\n" +
        "        ]\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithIntegerInput);
  }

  @Test
  public void testWriteIDWithIntegerOutput() throws IOException {
    InteractionDescription intDWithIntegerOutput = InteractionDescription.builder()
      .title(ID_TITLE)
      .output(new InteractionOutput(1, INTEGER_SCHEMA))
      .uri("http://example.org/log#interaction-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#interaction-1> log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#interaction-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasOutput [ a log:Output;\n" +
        "      log:hasValue \"1\"^^xsd:int;\n" +
        "      log:hasSchema [ a js:IntegerSchema\n" +
        "        ]\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithIntegerOutput);
  }

  @Test
  public void testWriteIDWithObjectInput() throws IOException {
    JsonObject body = new JsonObject();
    body.addProperty("currentTemperature", 3.0);
    body.addProperty("isActive", true);
    body.add("pastValues", new Gson().toJsonTree(new Integer[]{1, 2, 3}));

    InteractionDescription intDWithObjectInput = InteractionDescription.builder()
      .title(ID_TITLE)
      .input(new InteractionInput(body, OBJECT_SCHEMA))
      .uri("http://example.org/log#interaction-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#interaction-1> log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#interaction-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasInput [ a log:Input;\n" +
        "      log:hasValue \"{\\\"currentTemperature\\\":3.0,\\\"isActive\\\":true,\\\"pastValues\\\":[1,2,3]}\";\n" +
        "      log:hasSchema [ a js:ObjectSchema\n" +
        "        ]\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithObjectInput);
  }

  @Test
  public void testWriteIDWithObjectOutput() throws IOException {
    JsonObject body = new JsonObject();
    body.addProperty("currentTemperature", 3.0);
    body.addProperty("isActive", true);
    body.add("pastValues", new Gson().toJsonTree(new Integer[]{1, 2, 3}));

    InteractionDescription intDWithObjectInput = InteractionDescription.builder()
      .title(ID_TITLE)
      .output(new InteractionOutput(body, OBJECT_SCHEMA))
      .uri("http://example.org/log#interaction-1")
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#interaction-1> log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#interaction-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasOutput [ a log:Output;\n" +
        "      log:hasValue \"{\\\"currentTemperature\\\":3.0,\\\"isActive\\\":true,\\\"pastValues\\\":[1,2,3]}\";\n" +
        "      log:hasSchema [ a js:ObjectSchema\n" +
        "        ]\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithObjectInput);
  }

  @Test
  public void testWriteIDWithArrayInput() throws IOException {
    JsonArray body = new JsonArray();
    body.add("input1");
    body.add("input2");
    body.add("input3");

    InteractionDescription intDWithArrayInput = InteractionDescription.builder()
      .title(ID_TITLE)
      .uri("http://example.org/log#actionlog-1")
      .form(POST_FORM)
      .input(new InteractionInput(body, ARRAY_SCHEMA))
      .type(InteractionTypes.ACTION)
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#actionlog-1> a log:ActionExecution;\n" +
        "  log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#actionlog-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasInput [ a log:Input;\n" +
        "      log:hasValue \"[\\\"input1\\\",\\\"input2\\\",\\\"input3\\\"]\";\n" +
        "      log:hasSchema [ a js:ArraySchema\n" +
        "        ]\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"POST\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#invokeAction>\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithArrayInput);
  }

  @Test
  public void testWriteIDWithArrayOutput() throws IOException {
    JsonArray body = new JsonArray();
    body.add("output1");
    body.add("output2");
    body.add("output3");

    InteractionDescription intDWithArrayOutput = InteractionDescription.builder()
      .title(ID_TITLE)
      .uri("http://example.org/log#actionlog-1")
      .form(POST_FORM)
      .output(new InteractionOutput(body, ARRAY_SCHEMA))
      .type(InteractionTypes.ACTION)
      .build();

    String testID = PREFIXES + String.format(
      "\n<http://example.org/log#actionlog-1> a log:ActionExecution;\n" +
        "  log:title \"actionlog-1\";\n" +
        "  log:uri <http://example.org/log#actionlog-1>;\n" +
        "  log:created \"%s\";\n" +
        "  log:hasOutput [ a log:Output;\n" +
        "      log:hasValue \"[\\\"output1\\\",\\\"output2\\\",\\\"output3\\\"]\";\n" +
        "      log:hasSchema [ a js:ArraySchema\n" +
        "        ]\n" +
        "    ];\n" +
        "  log:hasForm [\n" +
        "      htv:methodName \"POST\";\n" +
        "      hctl:hasTarget <http://example.org/action>;\n" +
        "      hctl:forContentType \"application/json\";\n" +
        "      hctl:hasOperationType <https://www.w3.org/2019/wot/td#invokeAction>\n" +
        "    ] .\n", getDateTimeWithoutSeconds());

    assertIsomorphicGraphs(testID, intDWithArrayOutput);
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

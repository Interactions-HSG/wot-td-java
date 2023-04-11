package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.bindings.ProtocolBindings;
import ch.unisg.ics.interactions.wot.td.io.TDGraphReader;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import com.google.gson.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TDCoapOperationTest {
  private static final String PREFIX = "http://example.org/";

  static final ObjectSchema USER_SCHEMA = new ObjectSchema.Builder()
    .addSemanticType(PREFIX + "User")
    .addProperty("first_name", new StringSchema.Builder()
      .addSemanticType(PREFIX + "FirstName")
      .build())
    .addProperty("last_name", new StringSchema.Builder()
      .addSemanticType(PREFIX + "LastName")
      .build())
    .addRequiredProperties("last_name")
    .build();

  private static final Form FORM = new Form.Builder("coap://example.org/toggle")
    .setMethodName("PUT")
    .addOperationType(TD.invokeAction)
    .build();

  private static final String FORKLIFT_ROBOT_TD = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
    "@prefix cov: <http://www.example.org/coap-binding#> .\n" +
    "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
    "@prefix dct: <http://purl.org/dc/terms/> .\n" +
    "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
    "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
    "@prefix ex: <http://example.org/> .\n" +
    "\n" +
    "ex:forkliftRobot a td:Thing ; \n" +
    "    td:title \"forkliftRobot\" ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
    "    td:hasPropertyAffordance [\n" +
    "        a td:PropertyAffordance, js:BooleanSchema, ex:Status ; \n" +
    "        td:name \"status\" ;\n" +
    "        td:hasForm [\n" +
    "            hctl:hasTarget <coap://example.org/forkliftRobot/busy> ; \n" +
    "            cov:methodName \"PUT\" ; \n" +
    "            hctl:hasOperationType td:writeProperty ; \n" +
    "        ] ; \n" +
    "    ] ;\n" +
    "    td:hasActionAffordance [\n" +
    "        a td:ActionAffordance, ex:CarryFromTo ;\n" +
    "        td:name \"carry\" ;\n" +
    "        td:title \"carry\" ; \n" +
    "        td:hasForm [\n" +
    "            hctl:hasTarget <coap://example.org/forkliftRobot/carry> ; \n" +
    "            cov:methodName \"PUT\" ; \n" +
    "        ] ; \n" +
    "        td:hasInputSchema [ \n" +
    "            a js:ObjectSchema ;\n" +
    "            js:properties [ \n" +
    "                a js:ArraySchema, ex:SourcePosition ;\n" +
    "                js:propertyName \"sourcePosition\";\n" +
    "                js:minItems 3 ;\n" +
    "                js:maxItems 3 ;\n" +
    "                js:items [\n" +
    "                    a js:NumberSchema ;\n" +
    "                ] ;\n" +
    "            ] ;\n" +
    "            js:properties [\n" +
    "                a js:ArraySchema, ex:TargetPosition ;\n" +
    "                js:propertyName \"targetPosition\";\n" +
    "                js:minItems 3 ;\n" +
    "                js:maxItems 3 ;\n" +
    "                js:items [\n" +
    "                    a js:NumberSchema ;\n" +
    "                ] ;\n" +
    "            ] ;\n" +
    "            js:required \"sourcePosition\", \"targetPosition\" ;" +
    "        ] ; \n" +
    "    ] .\n";

  private ThingDescription td;

  @Before
  public void init() {
    td = TDGraphReader.readFromString(ThingDescription.TDFormat.RDF_TURTLE, FORKLIFT_ROBOT_TD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoDefaultMethodName() {
    new TDCoapOperation(new Form.Builder("coap://example.org/action")
      .addOperationType("http://example.org#observeProperty").build(),
      TD.invokeAction);
  }

  @Test
  public void testObserveOption() {
    TDCoapOperation coapRequest = new TDCoapOperation(new Form.Builder("coap://example.org/action")
      .setMethodName("GET")
      .addOperationType(TD.observeProperty)
      .addSubProtocol(COV.observe).build(),
      TD.observeProperty);

    assertTrue(coapRequest.getRequest().getOptions().hasObserve());
  }

  @Test
  public void testObserveWithDefaultBinding() {
    TDCoapOperation coapRequest = new TDCoapOperation(new Form.Builder("coap://example.org/action")
      .addOperationType(TD.observeProperty).build(),
      TD.observeProperty);

    assertEquals(coapRequest.getRequest().getCode(), CoAP.Code.valueOf("GET"));
    assertTrue(coapRequest.getRequest().getOptions().hasObserve());
  }

  @Test
  public void testToStringNoPayload() {
    TDCoapOperation request = new TDCoapOperation(new Form.Builder("coap://example.org/action")
      .setMethodName("POST")
      .addOperationType(TD.invokeAction).build(),
      TD.invokeAction);

    assertEquals("[TDCoapRequest] Method: POST, Target: coap://example.org/action, "
        + "{\"Uri-Host\":\"example.org\", \"Uri-Path\":\"action\", \"Content-Format\":\"application/json\"}"
      , request.toString());
  }

  @Test
  public void testNoDefaultBindingForOperationType() {
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      new TDCoapRequest(new Form.Builder("coap://example.org/action")
        .addOperationType(TD.invokeAction).build(),
        TD.readProperty);
    });

    String expectedMessage = "Unknown operation type: https://www.w3.org/2019/wot/td#readProperty";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testAsyncObserveRelationWithNoSubprotocol() {TDCoapRequest coapRequest = new TDCoapRequest(FORM, TD.invokeAction);
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      coapRequest.establishRelation(getEmptyTDCoAPHandler());
    });

    String expectedMessage = "No form for subprotocol: http://www.example" +
      ".org/coap-binding#observe for the given operation type";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSyncObserveRelationWithNoSubprotocol() throws IOException {
    TDCoapRequest coapRequest = new TDCoapRequest(FORM, TD.invokeAction);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      coapRequest.establishRelationAndWait(getEmptyTDCoAPHandler());
    });

    String expectedMessage = "No form for subprotocol: http://www.example" +
      ".org/coap-binding#observe for the given operation type";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testWriteProperty() throws UnsupportedOperationException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDCoapOperation r = new TDCoapOperation(form.get(), TD.writeProperty);
    r.setPayload(property.get().getDataSchema(), true);

    Request request = r.getRequest();

    assertEquals("PUT", request.getCode().name());

    JsonElement payload = JsonParser.parseString(request.getPayloadString());

    assertTrue(payload.isJsonPrimitive());
    assertTrue(payload.getAsBoolean());
  }

  @Test
  public void testInvokeAction() throws UnsupportedOperationException {
    Optional<ActionAffordance> action = td.getFirstActionBySemanticType(PREFIX + "CarryFromTo");
    assertTrue(action.isPresent());
    Optional<Form> form = action.get().getFirstForm();
    assertTrue(form.isPresent());

    Map<String, Object> payloadVariables = new HashMap<>();
    payloadVariables.put("sourcePosition", Arrays.asList(30, 50, 70));
    payloadVariables.put("targetPosition", Arrays.asList(30, 60, 70));

    TDCoapOperation r = new TDCoapOperation(form.get(), TD.invokeAction);
    r.setPayload(action.get().getInputSchema().get(), payloadVariables);
    Request request = r.getRequest();

    assertEquals("PUT", request.getCode().name());

    JsonObject payload = JsonParser.parseString(request.getPayloadString()).getAsJsonObject();

    JsonArray sourcePosition = payload.get("sourcePosition").getAsJsonArray();
    assertEquals(30, sourcePosition.get(0).getAsInt());
    assertEquals(50, sourcePosition.get(1).getAsInt());
    assertEquals(70, sourcePosition.get(2).getAsInt());

    JsonArray targetPosition = payload.get("targetPosition").getAsJsonArray();
    assertEquals(30, targetPosition.get(0).getAsInt());
    assertEquals(60, targetPosition.get(1).getAsInt());
    assertEquals(70, targetPosition.get(2).getAsInt());
  }

  @Test
  public void testNoPayload() {
    Request request = new TDCoapOperation(FORM, TD.invokeAction)
      .getRequest();
    assertNull(request.getPayload());
  }

  @Test
  public void testSimpleObjectPayload() throws JsonSyntaxException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
      .addProperty("first_name", new StringSchema.Builder().build())
      .addProperty("last_name", new StringSchema.Builder().build())
      .build();

    Map<String, Object> payloadVariables = new HashMap<>();
    payloadVariables.put("first_name", "Andrei");
    payloadVariables.put("last_name", "Ciortea");

    TDCoapOperation r = new TDCoapOperation(FORM, TD.invokeAction);
    r.setPayload(payloadSchema, payloadVariables);
    Request request = r.getRequest();

    assertUserSchemaPayload(request);
  }

  @Test
  public void testInvalidBooleanPayload() {
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      new TDCoapOperation(FORM, TD.invokeAction)
        .setPayload(new BooleanSchema.Builder().build(), "string");
    });

    String expectedMessage = "The payload's datatype does not match StringSchema " +
      "(payload datatype: boolean)";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testInvalidIntegerPayload() {
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      new TDCoapOperation(FORM, TD.invokeAction)
        .setPayload(new IntegerSchema.Builder().build(), 0.5);
    });

    String expectedMessage = "The payload's datatype does not match NumberSchema " +
      "(payload datatype: integer)";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testInvalidStringPayload() {
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      new TDCoapOperation(FORM, TD.invokeAction)
        .setPayload(new StringSchema.Builder().build(), true);
    });

    String expectedMessage = "The payload's datatype does not match BooleanSchema " +
      "(payload datatype: string)";
    assertTrue(ex.getMessage().contains(expectedMessage));
  }

  @Test
  public void testArrayPayload() throws UnsupportedOperationException {
    ArraySchema payloadSchema = new ArraySchema.Builder()
      .addItem(new NumberSchema.Builder().build())
      .build();

    List<Object> payloadVariables = new ArrayList<Object>();
    payloadVariables.add(1);
    payloadVariables.add(3);
    payloadVariables.add(5);

    TDCoapOperation r = new TDCoapOperation(FORM, TD.invokeAction);
    r.setPayload(payloadSchema, payloadVariables);
    Request request = r.getRequest();

    JsonArray payload = JsonParser.parseString(request.getPayloadString()).getAsJsonArray();
    assertEquals(3, payload.size());
    assertEquals(1, payload.get(0).getAsInt());
    assertEquals(3, payload.get(1).getAsInt());
    assertEquals(5, payload.get(2).getAsInt());
  }

  @Test
  public void testSemanticObjectWithOneArrayPayload() throws UnsupportedOperationException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
      .addProperty("speed", new NumberSchema.Builder()
        .addSemanticType(PREFIX + "Speed")
        .build())
      .addProperty("coordinates", new ArraySchema.Builder()
        .addSemanticType(PREFIX + "3DCoordinates")
        .addItem(new IntegerSchema.Builder().build())
        .build())
      .build();

    List<Object> coordinates = new ArrayList<>();
    coordinates.add(30);
    coordinates.add(50);
    coordinates.add(70);

    Map<String, Object> payloadVariables = new HashMap<>();
    payloadVariables.put("speed", 3.5);
    payloadVariables.put("coordinates", coordinates);

    TDCoapOperation r = new TDCoapOperation(FORM, TD.invokeAction);
    r.setPayload(payloadSchema, payloadVariables);
    Request request = r.getRequest();

    JsonObject payload = JsonParser.parseString(request.getPayloadString()).getAsJsonObject();
    assertEquals(3.5, payload.get("speed").getAsDouble(), 0.01);

    JsonArray coordinatesArray = payload.getAsJsonArray("coordinates");
    assertEquals(3, coordinatesArray.size());
    assertEquals(30, coordinatesArray.get(0).getAsInt());
    assertEquals(50, coordinatesArray.get(1).getAsInt());
    assertEquals(70, coordinatesArray.get(2).getAsInt());
  }

  @Test
  public void testPathVariable() {
    Form form = new Form.Builder("coap://example.org/{subscriptionId}")
      .setMethodName("PUT")
      .addOperationType(TD.invokeAction)
      .build();
    Map<String, DataSchema> uriVariables = new HashMap();
    uriVariables.put("subscriptionId", new StringSchema.Builder().build());
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("subscriptionId", "abc");
    TDCoapOperation op = (TDCoapOperation) ProtocolBindings.getBinding(form).bind(form, TD.invokeAction, uriVariables, parameters);
    assertEquals("coap://example.org/abc", op.getTarget());
  }

  private void assertUserSchemaPayload(Request request)
    throws UnsupportedOperationException {

    assertEquals(MediaTypeRegistry.APPLICATION_JSON, request.getOptions().getContentFormat());

    JsonObject payload = JsonParser.parseString(request.getPayloadString()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
  }
}

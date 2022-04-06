package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.graph.TDGraphReader;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.security.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.http.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

public class TDHttpRequestTest {
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

  private static final Form FORM = new Form.Builder(PREFIX + "toggle")
    .setMethodName("PUT")
    .addOperationType(TD.invokeAction)
    .build();

  private static final String FORKLIFT_ROBOT_TD = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
    "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
    "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
    "@prefix dct: <http://purl.org/dc/terms/> .\n" +
    "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
    "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
    "@prefix ex: <http://example.org/> .\n" +
    "\n" +
    "ex:forkliftRobot a td:Thing ; \n" +
    "    dct:title \"forkliftRobot\" ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme ;\n" +
    "        wotsec:in \"header\" ;\n" +
    "        wotsec:name \"X-API-Key\" ;\n" +
    "    ] ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:BasicSecurityScheme ;\n" +
    "        wotsec:in \"header\" ;\n" +
    "        wotsec:name \"Basic\" ;\n" +
    "    ] ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:DigestSecurityScheme ;\n" +
    "        wotsec:in \"header\" ;\n" +
    "        wotsec:name \"nonce\" ;\n" +
    "        wotsec:qop \"auth-int\" ;\n" +
    "    ] ;\n" +
    "    td:hasSecurityConfiguration [ a wotsec:BearerSecurityScheme ;\n" +
    "        wotsec:name \"Authorization\" ;\n" +
    "        wotsec:alg \"ES256\" ;\n" +
    "        wotsec:format \"jwt\" ;\n" +
    "    ] ;\n" +
    "    td:hasPropertyAffordance [\n" +
    "        a td:PropertyAffordance, js:BooleanSchema, ex:Status ; \n" +
    "        td:hasForm [\n" +
    "            hctl:hasTarget <http://example.org/forkliftRobot/busy> ; \n" +
    "        ] ; \n" +
    "    ] ;\n" +
    "    td:hasActionAffordance [\n" +
    "        a td:ActionAffordance, ex:CarryFromTo ;\n" +
    "        dct:title \"carry\" ; \n" +
    "        td:hasForm [\n" +
    "            hctl:hasTarget <http://example.org/forkliftRobot/carry> ; \n" +
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
    td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, FORKLIFT_ROBOT_TD);
  }

  @Test
  public void testToStringNullEntity() {
    TDHttpRequest request = new TDHttpRequest(new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction).build(),
      TD.invokeAction);

    assertEquals("[TDHttpRequest] Method: POST, Target: http://example.org/action, "
      + "Content-Type: application/json", request.toString());
  }

  @Test
  public void testWriteProperty() throws UnsupportedOperationException, IOException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    BasicClassicHttpRequest request = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true)
      .getRequest();

    assertEquals("PUT", request.getMethod());

    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    JsonElement payload = JsonParser.parseString(writer.toString());

    assertTrue(payload.isJsonPrimitive());
    assertTrue(payload.getAsBoolean());
  }

  @Test
  public void testInvokeAction() throws UnsupportedOperationException, IOException {
    Optional<ActionAffordance> action = td.getFirstActionBySemanticType(PREFIX + "CarryFromTo");
    assertTrue(action.isPresent());
    Optional<Form> form = action.get().getFirstForm();
    assertTrue(form.isPresent());

    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "SourcePosition", Arrays.asList(30, 50, 70));
    payloadVariables.put(PREFIX + "TargetPosition", Arrays.asList(30, 60, 70));

    BasicClassicHttpRequest request = new TDHttpRequest(form.get(), TD.invokeAction)
      .setObjectPayload((ObjectSchema) action.get().getInputSchema().get(), payloadVariables)
      .getRequest();

    assertEquals("POST", request.getMethod());

    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();

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
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
      .getRequest();
    assertNull(request.getEntity());
  }

  @Test
  public void testSimpleObjectPayload() throws ProtocolException, URISyntaxException,
    JsonSyntaxException, ParseException, IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
      .addProperty("first_name", new StringSchema.Builder().build())
      .addProperty("last_name", new StringSchema.Builder().build())
      .build();

    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put("first_name", "Andrei");
    payloadVariables.put("last_name", "Ciortea");

    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
      .setObjectPayload(payloadSchema, payloadVariables)
      .getRequest();

    assertUserSchemaPayload(request);
  }

  @Test
  public void testSimpleSemanticObjectPayload() throws ProtocolException, URISyntaxException,
    JsonSyntaxException, ParseException, IOException {
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "FirstName", "Andrei");
    payloadVariables.put(PREFIX + "LastName", "Ciortea");

    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
      .setObjectPayload(USER_SCHEMA, payloadVariables)
      .getRequest();

    assertEquals("PUT", request.getMethod());
    assertEquals(0, request.getUri().compareTo(URI.create(PREFIX + "toggle")));
    assertUserSchemaPayload(request);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidBooleanPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
      .setPrimitivePayload(new BooleanSchema.Builder().build(), "string")
      .getRequest();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidIntegerPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
      .setPrimitivePayload(new IntegerSchema.Builder().build(), 0.5)
      .getRequest();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidStringPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
      .setPrimitivePayload(new StringSchema.Builder().build(), true)
      .getRequest();
  }

  @Test
  public void testArrayPayload() throws UnsupportedOperationException, IOException {
    ArraySchema payloadSchema = new ArraySchema.Builder()
      .addItem(new NumberSchema.Builder().build())
      .build();

    List<Object> payloadVariables = new ArrayList<Object>();
    payloadVariables.add(1);
    payloadVariables.add(3);
    payloadVariables.add(5);

    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
      .setArrayPayload(payloadSchema, payloadVariables)
      .getRequest();

    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());

    JsonArray payload = JsonParser.parseString(writer.toString()).getAsJsonArray();
    assertEquals(3, payload.size());
    assertEquals(1, payload.get(0).getAsInt());
    assertEquals(3, payload.get(1).getAsInt());
    assertEquals(5, payload.get(2).getAsInt());
  }

  @Test
  public void testSemanticObjectWithOneArrayPayload() throws UnsupportedOperationException,
    IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
      .addProperty("speed", new NumberSchema.Builder()
        .addSemanticType(PREFIX + "Speed")
        .build())
      .addProperty("coordinates", new ArraySchema.Builder()
        .addSemanticType(PREFIX + "3DCoordinates")
        .addItem(new IntegerSchema.Builder().build())
        .build())
      .build();

    List<Object> coordinates = new ArrayList<Object>();
    coordinates.add(30);
    coordinates.add(50);
    coordinates.add(70);

    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "Speed", 3.5);
    payloadVariables.put(PREFIX + "3DCoordinates", coordinates);

    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
      .setObjectPayload(payloadSchema, payloadVariables)
      .getRequest();

    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());

    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();
    assertEquals(3.5, payload.get("speed").getAsDouble(), 0.01);

    JsonArray coordinatesArray = payload.getAsJsonArray("coordinates");
    assertEquals(3, coordinatesArray.size());
    assertEquals(30, coordinatesArray.get(0).getAsInt());
    assertEquals(50, coordinatesArray.get(1).getAsInt());
    assertEquals(70, coordinatesArray.get(2).getAsInt());
  }

  @Test
  public void testArrayOfSemanticObjectsPayload() {
    // TODO
  }

  @Test
  public void testSemanticObjectWithArrayOfSemanticObjectsPayload() {
    // TODO
  }

  @Test
  public void testValidateArrayPayload() {
    // TODO
  }

  @Test
  public void testAddHeader() throws ProtocolException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDHttpRequest tdRequest = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true);

    tdRequest.addHeader("headerName", "headerValue");

    BasicClassicHttpRequest request = tdRequest.getRequest();
    List<Header> headers = Arrays.asList(request.getHeaders());
    assertEquals(headers.stream().filter(header -> "headerName".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("headerName").getValue(), "headerValue");
  }

  @Test
  public void testSetAPIKey() throws ProtocolException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDHttpRequest tdRequest = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true);

    Optional<SecurityScheme> securityScheme =
      td.getFirstSecuritySchemeByType(WoTSec.APIKeySecurityScheme);
    assertTrue(securityScheme.isPresent());
    tdRequest.setAPIKey((APIKeySecurityScheme) securityScheme.get(), "api-key-value");

    BasicClassicHttpRequest request = tdRequest.getRequest();

    List<Header> headers = Arrays.asList(request.getHeaders());
    assertEquals(headers.stream().filter(header -> "X-API-Key".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("X-API-Key").getValue(), "api-key-value");
  }

  @Test
  public void testSetBasicAuth() throws ProtocolException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDHttpRequest tdRequest = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true);

    Optional<SecurityScheme> securityScheme =
      td.getFirstSecuritySchemeByType(WoTSec.BasicSecurityScheme);
    assertTrue(securityScheme.isPresent());
    tdRequest.setBasicAuth((BasicSecurityScheme) securityScheme.get(), "basic-value");

    BasicClassicHttpRequest request = tdRequest.getRequest();

    List<Header> headers = Arrays.asList(request.getHeaders());
    assertEquals(headers.stream().filter(header -> "Basic".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("Basic").getValue(), "basic-value");
  }

  @Test
  public void testSetDigestAuth() throws ProtocolException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDHttpRequest tdRequest = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true);

    Optional<SecurityScheme> securityScheme =
      td.getFirstSecuritySchemeByType(WoTSec.DigestSecurityScheme);
    assertTrue(securityScheme.isPresent());
    tdRequest.setDigestAuth((DigestSecurityScheme) securityScheme.get(), "nonce-value");

    BasicClassicHttpRequest request = tdRequest.getRequest();

    List<Header> headers = Arrays.asList(request.getHeaders());
    assertEquals(headers.stream().filter(header -> "nonce".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("nonce").getValue(), "nonce-value");

    assertEquals(headers.stream().filter(header -> "qop".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("qop").getValue(), "auth-int");
  }

  @Test
  public void testSetBearerAuth() throws ProtocolException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    TDHttpRequest tdRequest = new TDHttpRequest(form.get(), TD.writeProperty)
      .setPrimitivePayload(property.get().getDataSchema(), true);

    Optional<SecurityScheme> securityScheme =
      td.getFirstSecuritySchemeByType(WoTSec.BearerSecurityScheme);
    assertTrue(securityScheme.isPresent());
    tdRequest.setBearerAuth((BearerSecurityScheme) securityScheme.get(), "token-value");

    BasicClassicHttpRequest request = tdRequest.getRequest();

    List<Header> headers = Arrays.asList(request.getHeaders());
    assertEquals(headers.stream().filter(header -> "Authorization".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("Authorization").getValue(), "token-value");

    assertEquals(headers.stream().filter(header -> "alg".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("alg").getValue(), "ES256");

    assertEquals(headers.stream().filter(header -> "format".equals(header.getName())).count(),
      1);
    assertEquals(request.getHeader("format").getValue(), "jwt");
  }

  private void assertUserSchemaPayload(BasicClassicHttpRequest request)
    throws UnsupportedOperationException, IOException, ProtocolException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());

    assertEquals("application/json", request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());

    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
  }

}

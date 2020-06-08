package ch.unisg.ics.interactions.wot.td.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.TDGraphReader;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDHttpRequestTest {
  private static final String PREFIX = "http://example.org/";
  
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
    td = TDGraphReader.readFromString(FORKLIFT_ROBOT_TD);
  }
  
  @Test
  public void testToStringNullEntity() {
    TDHttpRequest request = new TDHttpRequest(new Form.Builder("http://example.org/action")
        .addOperationType(TD.invokeAction.stringValue()).build(), 
        TD.invokeAction.stringValue());
    
    assertEquals("[TDHttpRequest] Method: POST, Target: http://example.org/action, "
        + "Content-Type: application/json", request.toString());
  }
  
  @Test
  public void testReadProperty() throws UnsupportedOperationException, IOException {
    assertEquals(1, td.getProperties().size());
    Optional<PropertyAffordance> property = td.getFirstPropertyBySemanticType(PREFIX + "Status");
    assertTrue(property.isPresent());
    Optional<Form> form = property.get().getFirstFormForOperationType(TD.writeProperty.stringValue());
    assertTrue(form.isPresent());
    
    BasicClassicHttpRequest request = new TDHttpRequest(form.get(), TD.writeProperty.stringValue())
        .setPrimitivePayload(property.get().getDataSchema(), true)
        .getRequest();
    
    assertEquals("PUT", request.getMethod());
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    JsonElement payload = JsonParser.parseString(writer.toString());
    
    assertTrue(payload.isJsonPrimitive());
    assertEquals(true, payload.getAsBoolean());
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
    
    BasicClassicHttpRequest request = new TDHttpRequest(form.get(), TD.invokeAction.stringValue())
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
}

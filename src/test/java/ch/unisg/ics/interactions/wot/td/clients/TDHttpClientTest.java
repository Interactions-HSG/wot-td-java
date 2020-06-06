package ch.unisg.ics.interactions.wot.td.clients;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.utils.TDGraphReader;

public class TDHttpClientTest {
  
  private static final String TEST_TD = 
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix ex: <http://example.org/> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" + 
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" + 
      "    td:hasActionAffordance [\n" + 
      "        a td:ActionAffordance, ex:UpdateAccount ;\n" + 
      "        dct:title \"Update Account\" ;\n" + 
      "        td:hasForm [\n" + 
      "            htv:methodName \"PUT\" ;\n" + 
      "            hctl:hasTarget <http://example.org/account> ;\n" + 
      "            hctl:forContentType \"application/json\";\n" + 
      "            hctl:hasOperationType td:invokeAction;\n" + 
      "        ] ;\n" + 
      "        td:hasInputSchema [\n" + 
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:FirstName ;\n" +
      "                js:propertyName \"first_name\";\n" +
      "            ] ;\n" +
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:LastName ;\n" +
      "                js:propertyName \"last_name\";\n" +
      "            ] ;\n" +
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:Email ;\n" +
      "                js:propertyName \"email\";\n" +
      "            ] ;\n" +
      "            js:required \"email\" ;\n" +
      "        ] ;\n" + 
      "        td:hasOutputSchema [\n" + 
      "            a js:ObjectSchema ;\n" + 
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:FirstName ;\n" +
      "                js:propertyName \"first_name\";\n" +
      "            ] ;\n" +
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:LastName ;\n" +
      "                js:propertyName \"last_name\";\n" +
      "            ] ;\n" +
      "            js:properties [\n" + 
      "                a js:StringSchema, ex:Email ;\n" +
      "                js:propertyName \"email\";\n" +
      "            ] ;\n" +
      "        ]\n" + 
      "    ] ." ;

  private TDHttpClient client;
  
  @Before
  public void init() {
    ThingDescription td = TDGraphReader.readFromString(TEST_TD);
    client = new TDHttpClient(td);
  }
  
  @Test
  public void testInvokeSemanticActionKnownType() throws URISyntaxException, ProtocolException {
    String prefix = "http://example.org/";
    Map<String, Object> payloadVariables = getUserAccountPayloadVariables(prefix);
    
    SimpleHttpRequest request = (SimpleHttpRequest) client.requestActionBySemanticType(prefix 
        + "UpdateAccount", payloadVariables);
    
    assertEquals("PUT", request.getMethod());
    assertEquals(0, request.getUri().compareTo(URI.create("http://example.org/account")));
    assertEquals("application/json", request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
    
    JsonObject payload = JsonParser.parseString(request.getBodyText()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
    assertEquals("andrei.ciortea@unisg.ch", payload.get("email").getAsString());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvokeSemanticActionUnknownType() throws URISyntaxException, ProtocolException {
    String prefix = "http://iot-schema.org/";
    Map<String, Object> payloadVariables = getUserAccountPayloadVariables(prefix);
    
    client.invokeActionBySemanticType(prefix + "UpdateAccount", payloadVariables);
  }
  
  private Map<String, Object> getUserAccountPayloadVariables(String prefix) {
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(prefix + "FirstName", "Andrei");
    payloadVariables.put(prefix + "LastName", "Ciortea");
    payloadVariables.put(prefix + "Email", "andrei.ciortea@unisg.ch");
    
    return payloadVariables;
  }
  
}

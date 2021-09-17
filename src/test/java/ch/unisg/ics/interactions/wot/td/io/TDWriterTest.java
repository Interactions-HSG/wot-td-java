package ch.unisg.ics.interactions.wot.td.io;


import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TDWriterTest {

  private static final String THING_TITLE = "My Thing";
  private static final String PREFIXES =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix saref: <https://saref.etsi.org/core/> .\n" +
      "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";
  private static final String IO_BASE_IRI = "http://example.org/";

  ThingDescription td;

  @Before
  public void init(){
    this.td = new ThingDescription.Builder(THING_TITLE)
      .addSecurityScheme(new NoSecurityScheme())
      .build();
  }

  @Test
  //TODO change this as soon as you have the reader implemented
  public void testWriteJSON() {
    String jsonTD = "{\"@context\":\"https://www.w3.org/2019/wot/td/v1\",\"title\":\"My Thing\",\"securityDefinitions\":{\"nosec_sc\":{\"scheme\":\"nosec\"}},\"security\":[\"nosec_sc\"]}";
    Assert.assertEquals(jsonTD, TDWriter.write(td, RDFFormat.JSONLD));
  }

  @Test
  public void testWriteTurtle(){
   //TODO implement
  }
}

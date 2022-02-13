package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SecuritySchemeTest {

  @Test
  public void testNoSecurityScheme() {
    SecurityScheme scheme = SecurityScheme.getNoSecurityScheme();
    assertEquals(SecurityScheme.NOSEC, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.NoSecurityScheme));

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(0, conf.keySet().size());
  }

  @Test
  public void testAPIKeySecurityScheme() {
    APIKeySecurityScheme scheme = new APIKeySecurityScheme.Builder()
      .addToken(TokenLocation.HEADER, "tokenName")
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.APIKEY, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.APIKeySecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertTrue(scheme.getTokenName().isPresent());
    assertEquals("tokenName", scheme.getTokenName().get());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(2, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
    assertEquals("tokenName", conf.get(WoTSec.name));
  }

  @Test
  public void testAPIKeySecuritySchemeDefaultValues() {
    APIKeySecurityScheme scheme = new APIKeySecurityScheme.Builder()
      .build();

    assertEquals(SecurityScheme.APIKEY, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.APIKeySecurityScheme));

    assertEquals(TokenLocation.QUERY, scheme.getTokenLocation());
    assertFalse(scheme.getTokenName().isPresent());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(1, conf.keySet().size());
    assertEquals(TokenLocation.QUERY, conf.get(WoTSec.in));
  }

  @Test
  public void testBasicSecurityScheme() {
    BasicSecurityScheme scheme = new BasicSecurityScheme.Builder()
      .addToken(TokenLocation.BODY, "tokenName")
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.BASIC, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BasicSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertEquals(TokenLocation.BODY, scheme.getTokenLocation());
    assertTrue(scheme.getTokenName().isPresent());
    assertEquals("tokenName", scheme.getTokenName().get());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(2, conf.keySet().size());
    assertEquals(TokenLocation.BODY, conf.get(WoTSec.in));
    assertEquals("tokenName", conf.get(WoTSec.name));
  }

  @Test
  public void testBasicSecuritySchemeDefaultValues() {
    BasicSecurityScheme scheme = new BasicSecurityScheme.Builder()
      .build();

    assertEquals(SecurityScheme.BASIC, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BasicSecurityScheme));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertFalse(scheme.getTokenName().isPresent());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(1, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
  }
}

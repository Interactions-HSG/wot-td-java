package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.security.DigestSecurityScheme.QualityOfProtection;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  @Test
  public void testDigestSecurityScheme() {
    DigestSecurityScheme scheme = new DigestSecurityScheme.Builder()
      .addToken(TokenLocation.HEADER, "tokenName")
      .addQoP(QualityOfProtection.AUTH_INT)
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.DIGEST, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.DigestSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertEquals(QualityOfProtection.AUTH_INT, scheme.getQoP());
    assertTrue(scheme.getTokenName().isPresent());
    assertEquals("tokenName", scheme.getTokenName().get());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(3, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
    assertEquals("tokenName", conf.get(WoTSec.name));
    assertEquals(QualityOfProtection.AUTH_INT, conf.get(WoTSec.qop));
  }

  @Test
  public void testDigestSecuritySchemeDefaultValues() {
    DigestSecurityScheme scheme = new DigestSecurityScheme.Builder()
      .build();

    assertEquals(SecurityScheme.DIGEST, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.DigestSecurityScheme));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertEquals(QualityOfProtection.AUTH, scheme.getQoP());
    assertFalse(scheme.getTokenName().isPresent());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(2, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
    assertEquals(QualityOfProtection.AUTH, conf.get(WoTSec.qop));
  }

  @Test
  public void testBearerSecurityScheme() {
    BearerSecurityScheme scheme = new BearerSecurityScheme.Builder()
      .addToken(TokenLocation.HEADER, "tokenName")
      .addAlg("algName")
      .addAuthorization("authURI")
      .addFormat("formatName")
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.BEARER, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BearerSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertEquals("algName", scheme.getAlg());
    assertEquals("formatName", scheme.getFormat());
    assertTrue(scheme.getAuthorization().isPresent());
    assertEquals("authURI", scheme.getAuthorization().get());
    assertTrue(scheme.getTokenName().isPresent());
    assertEquals("tokenName", scheme.getTokenName().get());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(5, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
    assertEquals("algName", conf.get(WoTSec.alg));
    assertEquals("formatName", conf.get(WoTSec.format));
    assertEquals("authURI", conf.get(WoTSec.authorization));
    assertEquals("tokenName", conf.get(WoTSec.name));
  }

  @Test
  public void testBearerSecuritySchemeDefaultValues() {
    BearerSecurityScheme scheme = new BearerSecurityScheme.Builder()
      .build();

    assertEquals(SecurityScheme.BEARER, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BearerSecurityScheme));

    assertEquals(TokenLocation.HEADER, scheme.getTokenLocation());
    assertEquals("ES256", scheme.getAlg());
    assertEquals("jwt", scheme.getFormat());
    assertFalse(scheme.getAuthorization().isPresent());
    assertFalse(scheme.getTokenName().isPresent());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(3, conf.keySet().size());
    assertEquals(TokenLocation.HEADER, conf.get(WoTSec.in));
    assertEquals("ES256", conf.get(WoTSec.alg));
    assertEquals("jwt", conf.get(WoTSec.format));
  }

  @Test
  public void testPSKSecurityScheme() {
    PSKSecurityScheme scheme = new PSKSecurityScheme.Builder()
      .addIdentity("192.0.2.1")
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.PSK, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.PSKSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertTrue(scheme.getIdentity().isPresent());
    assertEquals("192.0.2.1", scheme.getIdentity().get());

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(1, conf.keySet().size());
    assertEquals("192.0.2.1", conf.get(WoTSec.identity));
  }

  @Test
  public void testOAuth2SecurityScheme() {
    OAuth2SecurityScheme scheme = new OAuth2SecurityScheme.Builder("code")
      .addAuthorization("https://example.com/authorization")
      .addToken("https://example.com/token/1")
      .addRefresh("https://example.com/token/2")
      .addScope("firstScope")
      .addScopes(new HashSet<>(Arrays.asList("limited", "special")))
      .addScope("lastScope")
      .addSemanticType("sem")
      .build();

    assertEquals(SecurityScheme.OAUTH2, scheme.getSchemeName());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.OAuth2SecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("sem"));

    assertEquals("code", scheme.getFlow());

    assertTrue(scheme.getAuthorization().isPresent());
    assertEquals("https://example.com/authorization", scheme.getAuthorization().get());

    assertTrue(scheme.getToken().isPresent());
    assertEquals("https://example.com/token/1", scheme.getToken().get());

    assertTrue(scheme.getRefresh().isPresent());
    assertEquals("https://example.com/token/2", scheme.getRefresh().get());

    assertTrue(scheme.getScopes().isPresent());
    assertEquals(4, scheme.getScopes().get().size());
    assertTrue(scheme.getScopes().get().contains("firstScope"));
    assertTrue(scheme.getScopes().get().contains("limited"));
    assertTrue(scheme.getScopes().get().contains("special"));
    assertTrue(scheme.getScopes().get().contains("lastScope"));

    Map<String, Object> conf = scheme.getConfiguration();
    assertEquals(5, conf.keySet().size());
    assertEquals("code", conf.get(WoTSec.flow));
    assertEquals("https://example.com/authorization", conf.get(WoTSec.authorization));
    assertEquals("https://example.com/token/1", conf.get(WoTSec.token));
    assertEquals("https://example.com/token/2", conf.get(WoTSec.refresh));

    Set<String> scopes = new HashSet<>(Arrays.asList("firstScope", "limited", "special", "lastScope"));
    assertEquals(scopes, conf.get(WoTSec.scopes));
  }
}

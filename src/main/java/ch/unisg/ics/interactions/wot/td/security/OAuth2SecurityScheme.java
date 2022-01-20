package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OAuth2SecurityScheme extends SecurityScheme {

  private final Optional<String> authorization;
  private final Optional<String> token;
  private final Optional<String> refresh;
  private final Optional<Set<String>> scopes;
  private final String flow;

  protected OAuth2SecurityScheme(Optional<String> authorization, Optional<String> token,
                                 Optional<String> refresh, Optional<Set<String>> scopes,
                                 String flow, Map<String, Object> configuration,
                                 Set<String> semanticTypes) {
    super(SecurityScheme.OAUTH2, configuration, semanticTypes);
    this.authorization = authorization;
    this.token = token;
    this.refresh = refresh;
    this.scopes = scopes;
    this.flow = flow;
  }

  public Optional<String> getAuthorization() {
    return this.authorization;
  }

  public Optional<String> getToken() {
    return this.token;
  }

  public Optional<String> getRefresh() {
    return this.refresh;
  }

  public Optional<Set<String>> getScopes() {
    return this.scopes;
  }

  public String getFlow() {
    return this.flow;
  }

  public static class Builder extends SecurityScheme.Builder<OAuth2SecurityScheme,
    OAuth2SecurityScheme.Builder> {

    private Optional<String> authorization;
    private Optional<String> token;
    private Optional<String> refresh;
    private Optional<Set<String>> scopes;
    private String flow;

    public Builder(String flow) {
      this.authorization = Optional.empty();
      this.token = Optional.empty();
      this.refresh = Optional.empty();
      this.scopes = Optional.empty();
      this.flow = flow;
      this.configuration.put(WoTSec.flow, this.flow);
      this.semanticTypes.add(WoTSec.OAuth2SecurityScheme);
    }

    public OAuth2SecurityScheme.Builder addAuthorization(String authorization) {
      this.authorization = Optional.of(authorization);
      this.configuration.put(WoTSec.authorization, authorization);
      return this;
    }

    public OAuth2SecurityScheme.Builder addToken(String token) {
      this.token = Optional.of(token);
      this.configuration.put(WoTSec.token, token);
      return this;
    }

    public OAuth2SecurityScheme.Builder addRefresh(String refresh) {
      this.refresh = Optional.of(refresh);
      this.configuration.put(WoTSec.refresh, refresh);
      return this;
    }

    public OAuth2SecurityScheme.Builder addScopes(Set<String> scopes) {
      this.scopes = Optional.of(scopes);
      this.configuration.put(WoTSec.scopes, scopes);
      return this;
    }

    /**
     * Specifies the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     *
     * @param configuration the security configuration
     * @return the builder
     */
    @Override
    public OAuth2SecurityScheme.Builder addConfiguration(Map<String, Object> configuration) {
      super.addConfiguration(configuration);
      if (configuration.containsKey(WoTSec.identity)) {
        //this.addIdentity(configuration.get(WoTSec.identity));
      }
      return this;
    }

    @Override
    public OAuth2SecurityScheme build() {
      return new OAuth2SecurityScheme(authorization, token, refresh, scopes, flow, configuration,
        semanticTypes);
    }
  }
}

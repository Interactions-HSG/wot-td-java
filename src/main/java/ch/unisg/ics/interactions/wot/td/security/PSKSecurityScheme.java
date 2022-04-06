package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PSKSecurityScheme extends SecurityScheme {

  private final Optional<String> identity;

  protected PSKSecurityScheme(Optional<String> identity, Map<String, Object> configuration,
                              Set<String> semanticTypes) {
    super(SecurityScheme.PSK, configuration, semanticTypes);
    this.identity = identity;
  }

  public Optional<String> getIdentity() {
    return identity;
  }

  public static class Builder extends SecurityScheme.Builder<PSKSecurityScheme,
    PSKSecurityScheme.Builder> {

    private Optional<String> identity;

    public Builder() {
      this.identity = Optional.empty();
      this.semanticTypes.add(WoTSec.PSKSecurityScheme);
    }

    public PSKSecurityScheme.Builder addIdentity(String identity) {
      this.identity = Optional.of(identity);
      this.configuration.put(WoTSec.identity, identity);
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
    public PSKSecurityScheme.Builder addConfiguration(Map<String, Object> configuration) {
      super.addConfiguration(configuration);
      validateConfiguration(Arrays.asList(WoTSec.identity));
      if (configuration.containsKey(WoTSec.identity)) {
        this.addIdentity(String.valueOf(configuration.get(WoTSec.identity)));
      }
      return this;
    }


    @Override
    public PSKSecurityScheme build() {
      return new PSKSecurityScheme(identity, configuration, semanticTypes);
    }
  }
}

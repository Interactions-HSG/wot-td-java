package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicSecurityScheme extends SecurityScheme{

  private final BasicSecurityScheme.TokenLocation in;
  private final Optional<String> name;
  protected BasicSecurityScheme(BasicSecurityScheme.TokenLocation in, Optional<String> name,
                                Map<String, String> configuration, Set<String> semanticTypes) {
    super(SecurityScheme.BASIC, configuration, semanticTypes);
    this.in = in;
    this.name = name;
  }

  public BasicSecurityScheme.TokenLocation getTokenLocation() {
    return in;
  }

  public Optional<String> getTokenName() {
    return name;
  }

  public enum TokenLocation {
    HEADER, QUERY, BODY, COOKIE
  }

  public static class Builder extends SecurityScheme.Builder<BasicSecurityScheme,
    BasicSecurityScheme.Builder> {

    private BasicSecurityScheme.TokenLocation in;
    private Optional<String> name;

    public Builder() {
      this.in = BasicSecurityScheme.TokenLocation.HEADER;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.BasicSecurityScheme);
    }

    public BasicSecurityScheme.Builder addTokenLocation(BasicSecurityScheme.TokenLocation in) {
      this.in = in;
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      return this;
    }

    public BasicSecurityScheme.Builder addTokenName(String name) {
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    @Override
    public BasicSecurityScheme.Builder addConfiguration(Map<String, String> configuration) {
      this.configuration.putAll(configuration);
      if (configuration.containsKey(WoTSec.in)) {
        try {
          addTokenLocation(BasicSecurityScheme.TokenLocation.valueOf(configuration.get(WoTSec.in)
            .toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
          throw new InvalidTDException("Invalid token location", e);
        }
      }
      if (configuration.containsKey(WoTSec.name)) {
        addTokenName(configuration.get(WoTSec.name));
      }
      return this;
    }

    public BasicSecurityScheme.Builder addToken(BasicSecurityScheme.TokenLocation in, String name) {
      this.in = in;
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    @Override
    public BasicSecurityScheme build() {
      return new BasicSecurityScheme(in, name, configuration, semanticTypes);
    }
  }

}

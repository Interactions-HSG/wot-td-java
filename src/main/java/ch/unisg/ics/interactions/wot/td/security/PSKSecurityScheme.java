package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PSKSecurityScheme extends SecurityScheme {

    private final Optional<String> identity;

    protected PSKSecurityScheme(Map<String, Object> configuration, Set<String> semanticTypes, Optional<String> identity) {
        super(SecurityScheme.PSK, configuration, semanticTypes);
        this.identity = identity;
    }

    /**
     * Gets the identifier providing information which can be used for selection or confirmation.
     *
     * @return the quality of protection
     */
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

        /**
         * Specifies the identifier providing information which can be used for selection or confirmation.
         *
         * @param identity the identifier
         * @return the builder
         */
        public PSKSecurityScheme.Builder addIdentity(String identity) {
            this.identity = Optional.of(identity);
            this.configuration.put(WoTSec.identity, identity);
            return this;
        }

        @Override
        public PSKSecurityScheme build() {
            return new PSKSecurityScheme(configuration, semanticTypes, identity);
        }
    }
}

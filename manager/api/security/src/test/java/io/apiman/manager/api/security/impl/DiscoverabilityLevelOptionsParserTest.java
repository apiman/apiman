package io.apiman.manager.api.security.impl;

import io.apiman.common.config.options.exceptions.InvalidOptionConfigurationException;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.security.impl.DiscoverabilityOptionsParser.RoleSourceDiscoverability;
import io.apiman.manager.api.security.impl.DiscoverabilityOptionsParser.Source;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
class DiscoverabilityLevelOptionsParserTest {

    @Test
    public void parse_options_into_submap() {
        var opts = new DiscoverabilityOptionsParser(Map.of(
                "api-user.source", "IDM_ROLE",
                "api-user.discoverabilities", "ANONYMOUS, FULL_PLATFORM_MEMBERS",

                "portal-user.source", "APIMAN_ROLE",
                "portal-user.discoverabilities", "ANONYMOUS"
        ));

        Map<String, RoleSourceDiscoverability> results = opts.getNameToDiscoverability();

        assertThat(results)
                .containsEntry("api-user",
                        new RoleSourceDiscoverability(
                                "api-user",
                                Source.IDM_ROLE,
                                Set.of(DiscoverabilityLevel.ANONYMOUS, DiscoverabilityLevel.FULL_PLATFORM_MEMBERS))
                )
                .containsEntry("portal-user",
                        new RoleSourceDiscoverability(
                                "api-user",
                                Source.APIMAN_ROLE,
                                Set.of(DiscoverabilityLevel.ANONYMOUS))
                );
    }

    @Test
    public void is_case_insensitive() {
        var opts = new DiscoverabilityOptionsParser(Map.of(
                "api-user.source", "idm_role",
                "api-user.discoverabilities", "aNoNyMoUs"
        ));

        Map<String, RoleSourceDiscoverability> results = opts.getNameToDiscoverability();

        assertThat(results)
                .containsEntry("api-user",
                        new RoleSourceDiscoverability(
                                "api-user",
                                Source.IDM_ROLE,
                                Set.of(DiscoverabilityLevel.ANONYMOUS))
                );
    }

    @Test
    public void throws_on_bad_discoverability() {
        Assertions.assertThrows(InvalidOptionConfigurationException.class, () -> {
            new DiscoverabilityOptionsParser(Map.of(
                    "api-user.discoverabilities", "ANONYMOUS, XXXXX"
            ));
        });
    }

}
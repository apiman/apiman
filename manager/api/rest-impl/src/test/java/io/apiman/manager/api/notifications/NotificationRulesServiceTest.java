package io.apiman.manager.api.notifications;

import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.rules.SimpleSpELRule;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.jeasy.rules.api.Fact;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.support.composite.ActivationRuleGroup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationRulesServiceTest {

    private NotificationDto<IVersionedApimanEvent> notification;

    @Before
    public void setup() {
        var appDeveloper = new UserDto()
                .setFullName("John Smith Appdev")
                .setUsername("JohnSmith123")
                .setEmail("foo@apiman.io")
                .setLocale(Locale.ENGLISH);

        ContractCreatedEvent contractCreated = ContractCreatedEvent
                .builder()
                .setHeaders(ApimanEventHeaders.builder()
                        .setId("Event123")
                        .setSource(URI.create("https://example.org"))
                        .setSubject("Hello")
                        .setEventVersion(1L)
                        .setTime(OffsetDateTime.now())
                        .setType("X")
                        .build())
                .setApiOrgId("ApiOrg")
                .setApiId("CoolApi")
                .setApiVersion("1.0")
                .setClientOrgId("MobileKompany")
                .setClientId("MobileApp")
                .setClientVersion("2.0")
                .setContractId("1234")
                .setPlanId("Gold")
                .setPlanVersion("1.3")
                .setApprovalRequired(true)
                .setUser(appDeveloper)
                .build();

        UserDto recipient = new UserDto()
                .setEmail("approver@apiman.io")
                .setUsername("ApproverPerson")
                .setFullName("David Approver")
                .setLocale(Locale.ENGLISH);

        this.notification = new NotificationDto<>()
                .setId(123L)
                .setCategory(NotificationCategory.API_ADMINISTRATION)
                .setReason("whatever")
                .setReasonMessage("hi")
                .setStatus(NotificationStatus.OPEN)
                .setCreatedOn(OffsetDateTime.now())
                .setModifiedOn(OffsetDateTime.now())
                .setRecipient(recipient)
                .setSource("adsadsaad")
                .setPayload(contractCreated);
    }

    @Test
    public void generate_notification_rules() {
        Rule rule1 = new SimpleSpELRule()
                .name("some rule")
                .when("#{ ['notification'].payload.apiOrgId == 'ApiOrg'}");

        Rule rule2 = new SimpleSpELRule()
                .name("another rule")
                .when("#{ ['notification'].payload.clientOrgId == 'MobileKompany'}");

        Rule rule3 = new SimpleSpELRule()
                .name("another rule")
                .when("#{ ['notification'].payload.clientOrgId == 'XXX'}");

    ActivationRuleGroup ruleGroup = new ActivationRuleGroup();
       ruleGroup.addRule(rule3);
       ruleGroup.addRule(rule2);
       ruleGroup.addRule(rule1);

        Rules rules = new Rules();
        rules.register(ruleGroup);

        Facts facts = new Facts();
        facts.add(new Fact<>("notification", notification));

        RulesEngineParameters parameters = new RulesEngineParameters()
                .skipOnFirstFailedRule(false)
                .skipOnFirstAppliedRule(false)
                .skipOnFirstNonTriggeredRule(false);

        RulesEngine rulesEngine = new DefaultRulesEngine(parameters);
        rulesEngine.fire(rules, facts);

        Assert.assertTrue(ruleGroup.evaluate(facts));
    }
}
package io.apiman.manager.api.notifications;

import io.apiman.manager.api.beans.notifications.NotificationFilterEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationType;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.notifications.rules.SimpleSpELRule;

import java.util.Objects;
import java.util.StringJoiner;
import javax.inject.Inject;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jeasy.rules.api.Fact;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.support.composite.ActivationRuleGroup;

/**
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationRulesService {

    private final INotificationRepository notificationRepository;

    private final Cache<RuleKey, ActivationRuleGroup> notificationsRuleCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .build();

    @Inject
    public NotificationRulesService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationRulesService cache(NotificationPreferenceEntity npe) {
        RuleKey key = RuleKey.of(npe.getUserId(), npe.getType());
        notificationRepository.getNotificationPreferenceByUserIdAndType(key.userId, key.nType).map(entity -> buildRuleGroup(entity, key));
        return this;
    }

    public NotificationRulesService cache(String userId, NotificationType notificationType) {
        RuleKey key = RuleKey.of(userId, notificationType);
        notificationRepository.getNotificationPreferenceByUserIdAndType(key.userId, key.nType).map(entity -> buildRuleGroup(entity, key));
        return this;
    }

    public NotificationRulesService invalidate(String userId, NotificationType notificationType) {
        notificationsRuleCache.invalidate(RuleKey.of(userId, notificationType));
        return this;
    }

    public NotificationRulesService invalidateAll() {
        notificationsRuleCache.invalidateAll();
        return this;
    }

    public boolean wantsNotification(String userId, NotificationType notificationType, NotificationDto<?> notification) {
        RuleKey key = RuleKey.of(userId, notificationType);
        ActivationRuleGroup ruleGroup = notificationsRuleCache.get(key,
                k -> notificationRepository.getNotificationPreferenceByUserIdAndType(userId, notificationType).map(entity -> buildRuleGroup(entity, key)).orElse(null));
        if (ruleGroup == null) {
            return true;
        }
        Facts facts = new Facts();
        facts.add(new Fact<>("notification", notification));
        return !ruleGroup.evaluate(facts);
    }

    private ActivationRuleGroup buildRuleGroup(NotificationPreferenceEntity npe, RuleKey key) {
        ActivationRuleGroup ruleGroup = new ActivationRuleGroup();
        for (NotificationFilterEntity nfe : npe.getRules()) {
            Rule rule = new SimpleSpELRule().name(nfe.getSource()).when(nfe.getExpression());
            ruleGroup.addRule(rule);
        }
        notificationsRuleCache.put(key, ruleGroup);
        return ruleGroup;
    }

    private static final class RuleKey {
        final String userId;
        final NotificationType nType;

        RuleKey(String userId, NotificationType nType) {
            this.userId = userId;
            this.nType = nType;
        }

        public static RuleKey of(String userId, NotificationType nType) {
            return new RuleKey(userId, nType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RuleKey ruleKey = (RuleKey) o;
            return Objects.equals(userId, ruleKey.userId) && nType == ruleKey.nType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, nType);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RuleKey.class.getSimpleName() + "[", "]")
                    .add("userId='" + userId + "'")
                    .add("nType=" + nType)
                    .toString();
        }
    }

}

package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.war.WarApiManagerConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SimpleMailNotificationServiceTest {
    @Test
    public void readTemplatesFromFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("io/apiman/manager/test/notifications/email-notification-templates.json").getFile());

        ApiManagerConfig config = new WarApiManagerConfig() {
            public Path getConfigDirectory() {
                return file.getParentFile().toPath();
            }

            @Override
            public Map<String, String> getNotificationProperties() {
                return Map.of(
                     "email.smtp.from", "foo",
                     "email.smtp.host", "example.org",
                     "email.smtp.post", "8080",
                     "email.smtp.username", "foo",
                     "email.smtp.password", "blah"
                );
            }
        };

        SimpleMailNotificationService service = new SimpleMailNotificationService(config);
        Optional<EmailNotificationTemplate> tplOpt = service.findTemplateFor("apiman.account.approval.request");

        assertThat(tplOpt).isPresent();
        EmailNotificationTemplate tpl = tplOpt.get();

        assertThat(tpl.getNotificationReason())
             .isEqualTo("apiman.account.approval.request");

        assertThat(tpl.getNotificationCategory())
             .isEqualTo(NotificationCategory.USER_ADMINISTRATION);
    }
}

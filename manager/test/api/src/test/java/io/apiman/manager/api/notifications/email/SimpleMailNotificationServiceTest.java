package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.war.WarApiManagerConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SimpleMailNotificationServiceTest {
    // Pull templates from resources
    File file = new File(getClass().getClassLoader().getResource("apiman/config").getFile());

    // Override default config with our test config
    ApiManagerConfig config = new WarApiManagerConfig() {
        public Path getConfigDirectory() {
            return file.toPath();
        }

        @Override
        public Map<String, String> getEmailNotificationProperties() {
            return TEST_CONFIG;
        }
    };

    Map<String, String> TEST_CONFIG = Map.of(
            "enable", "true",
         "smtp.mock", "false",
         "smtp.ssl", "false",
         "smtp.startTLSMode", "DISABLED",
         "smtp.fromName", "Apiman",
         "smtp.fromEmail", "apiman@apiman.io",
         "smtp.host", "localhost",
         "smtp.port", Integer.toString(ServerSetupTest.SMTP.getPort()),
         "smtp.username", "marc",
         "smtp.password", "don't_look!"
     );

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Before
    public void beforeEach() throws Exception {
        greenMail.getUserManager().createUser("marc@blackparrotlabs.io", "marc", "don't_look!");
    }

    @Test
    public void Html_notification_sent_via_email_is_received_by_mail_server_as_specified() throws Exception {
        var template = new EmailNotificationTemplate()
             .setCategory(NotificationCategory.OTHER)
             .setNotificationReason("hello")
             .setHtmlBody("<marquee>Hello, {name}</marquee> Σ")
             .setPlainBody("Hello, {name} Σ") // Add some non-ASCII characters
             .setSubject("Greetings, {name}");

        Map<String, Object> templateVars = Map.of("name", "Marc Savy");

        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        var mail = SimpleEmail
             .builder()
             .setToEmail("marc@blackparrotlabs.io")
             .setToName("Marc")
             .setLocale(Locale.ENGLISH)
             .setTemplate(template)
             .setTemplateVariables(templateVars)
             .setHeaders(Map.of("X-Super-Secret", "BPL"))
             .build();

        service.send(mail);

        List<MimeMessage> emails = List.of(greenMail.getReceivedMessages());

        assertThat(emails).hasSize(1);

        MimeMessage receivedMail = emails.get(0);

        assertThat(receivedMail.getSubject())
             .isEqualTo("Greetings, Marc Savy");

        assertThat(receivedMail.getFrom())
             .extracting(Address::toString)
             .isEqualTo(List.of("Apiman <apiman@apiman.io>"));

        assertThat(receivedMail.getHeader("X-Super-Secret"))
             .extracting(Objects::toString)
             .isEqualTo(List.of("BPL"));

        assertThat(receivedMail.getContent())
             .isInstanceOf(MimeMultipart.class);

        MimeMultipart body = (MimeMultipart) receivedMail.getContent();

        assertThat(body.getCount())
             .isEqualTo(2);

        assertThat(bodyPartToString(body.getBodyPart(0)))
             .isEqualTo("Hello, Marc Savy Σ");

        assertThat(bodyPartToString(body.getBodyPart(1)))
             .isEqualTo("<marquee>Hello, Marc Savy</marquee> Σ");

    }

    @Test
    public void Notification_emails_support_utf8() throws MessagingException, IOException {
        // From https://www.w3.org/2001/06/utf-8-test/UTF-8-demo.html
        String sample = "๏ แผ่นดินฮั่นเสื่อมโทรมแสนสังเวช  พระปกเกศกองบู๊กู้ขึ้นใหม่\n"
                      + "  สิบสองกษัตริย์ก่อนหน้าแลถัดไป       สององค์ไซร้โง่เขลาเบาปัญญา\n"
                      + "    ทรงนับถือขันทีเป็นที่พึ่ง           บ้านเมืองจึงวิปริตเป็นนักหนา\n"
                      + "  โฮจิ๋นเรียกทัพทั่วหัวเมืองมา         หมายจะฆ่ามดชั่วตัวสำคัญ\n"
                      + "    เหมือนขับไสไล่เสือจากเคหา      รับหมาป่าเข้ามาเลยอาสัญ\n"
                      + "  ฝ่ายอ้องอุ้นยุแยกให้แตกกัน          ใช้สาวนั้นเป็นชนวนชื่นชวนใจ\n"
                      + "    พลันลิฉุยกุยกีกลับก่อเหตุ          ช่างอาเพศจริงหนาฟ้าร้องไห้\n"
                      + "  ต้องรบราฆ่าฟันจนบรรลัย           ฤๅหาใครค้ำชูกู้บรรลังก์ ฯ";

        var template = new EmailNotificationTemplate()
             .setCategory(NotificationCategory.OTHER)
             .setNotificationReason("hello")
             .setHtmlBody(sample)
             .setPlainBody(sample) // Add some non-ASCII characters
             .setSubject("Test UTF-8");

        Map<String, Object> templateVars = Map.of();

        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        var mail = SimpleEmail
             .builder()
             .setToEmail("marc@blackparrotlabs.io")
             .setToName("Marc")
             .setLocale(Locale.ENGLISH)
             .setTemplate(template)
             .setTemplate(template)
             .setTemplateVariables(templateVars)
             .setHeaders(Map.of("X-Super-Secret", "BPL"))
             .build();

        service.send(mail);

        MimeMessage receivedMail = greenMail.getReceivedMessages()[0];

        MimeMultipart body = (MimeMultipart) receivedMail.getContent();

        // HTML and plaintext as same sample for this.
        assertThat(bodyPartToString(body.getBodyPart(0)))
             .isEqualTo(sample);

        assertThat(bodyPartToString(body.getBodyPart(1)))
             .isEqualTo(sample);
    }

    @Test
    public void Read_the_email_notification_templates_from_file() {
        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        Optional<EmailNotificationTemplate> tplOpt = service.findTemplateFor("test.notification.reason", Locale.ENGLISH);

        assertThat(tplOpt).isPresent();
    }

    @Test
    public void Template_is_looked_up_by_exact_reason() {
        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        EmailNotificationTemplate tpl = service.findTemplateFor("test.notification.reason", Locale.ENGLISH).orElseThrow();

        assertThat(tpl.getNotificationReason())
             .isEqualTo("test.notification.reason");

        assertThat(tpl.getCategory())
             .isEqualTo(NotificationCategory.OTHER);
    }

    @Test
    public void Html_template_is_loaded_from_file() {
        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        EmailNotificationTemplate tpl = service.findTemplateFor("test.notification", Locale.forLanguageTag("crs")).orElseThrow();

        assertThat(tpl.getHtmlBody())
                .isEqualTo("<html lang=\"crs\">\n"
                                   + "Koste Seselwa\n"
                                   + "</html>");
    }

    @Test
    public void Plain_template_is_loaded_from_file() {
        SimpleMailNotificationService service = new SimpleMailNotificationService(config, new QuteTemplateEngine(config));
        EmailNotificationTemplate tpl = service.findTemplateFor("test.notification", Locale.forLanguageTag("crs")).orElseThrow();

        assertThat(tpl.getPlainBody())
                .isEqualTo("Bonswar");
    }


    public static String bodyPartToString(Part msg) throws MessagingException, IOException {
        if (!(msg.getContent() instanceof String)) {
            throw new IllegalStateException("Part getContent should be a String (might be multipart and you've not "
                   + "drilled down to the terminal payload)?");
        }
        return (String) msg.getContent();
    }
}

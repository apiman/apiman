package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.config.ApiManagerConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class QuteTemplateEngineTest {
    public static final class Foo extends NotificationDto<AccountSignupEvent> {
        public int blah = 231;
    }

    ApiManagerConfig CONFIG = new ApiManagerConfig() {

        @Override
        public Path getConfigDirectory() {
            return Paths.get(QuteTemplateEngineTest.class.getClassLoader().getResource("apiman/config").getPath());
        }
    };


    @Test
    public void render_nested_variables() {
        QuteTemplateEngine engine = new QuteTemplateEngine(CONFIG);

        Foo test = new Foo();

        String rendered = engine.applyTemplate(
             "Hello, {test.blah}",
             Map.of("test", test)
        );
        assertThat(rendered).isEqualTo("Hello, 231");
    }


    @Test
    public void render_simple_variable() {
        QuteTemplateEngine engine = new QuteTemplateEngine(CONFIG);
        String rendered = engine.applyTemplate(
             "Hello, {name}",
             Map.of("name", "Marc")
        );
        assertThat(rendered).isEqualTo("Hello, Marc");
    }

    @Test
    public void render_large_template() {
        var rawTpl = "Dear {recipient},\n"
             + "\n"
             + "An account is awaiting your approval\n"
             + "\n"
             + "Here's what we know: \n"
             + "\n"
             + "Name: {userId}\n"
             + "Email address: {emailAddress}\n"
             + "Organisation: {organization}\n"
             + "\n"
             + "To approve or deny this account, please go to: {resource}\n"
             + "\n"
             + "Regards,\n"
             + "Apiman";

        QuteTemplateEngine engine = new QuteTemplateEngine(CONFIG);
        String rendered = engine.applyTemplate(
             rawTpl,
             Map.of(
                  "recipient", "Marc Savy",
                  "userId", "newUser123",
                  "emailAddress", "developer@example.org",
                  "organization", "Example Ltd",
                  "resource", "http://foo.example/apimanui/signups/approvals/example/1234"
             )
        );

        assertThat(rendered).isEqualTo("Dear Marc Savy,\n"
             + "\n"
             + "An account is awaiting your approval\n"
             + "\n"
             + "Here's what we know: \n"
             + "\n"
             + "Name: newUser123\n"
             + "Email address: developer@example.org\n"
             + "Organisation: Example Ltd\n"
             + "\n"
             + "To approve or deny this account, please go to: http://foo.example/apimanui/signups/approvals/example/1234\n"
             + "\n"
             + "Regards,\n"
             + "Apiman"
        );
    }

    @Test
    public void merge_files_from_includes_directory() {
        QuteTemplateEngine engine = new QuteTemplateEngine(CONFIG);
        String rendered = engine.applyTemplate(
                "<html>"
                        + "{#include header.include.html generator='Apiman Notifications' }"
                        + "{#title}Marc{/title}"
                        + "{/include}"
                        + "<body>"
                        + "<p>This should be in the middle</p>"
                        + "</body>"
                        + "{#include footer.include.html /}"
                        + "</html>",
                Map.of()
        );

        String expected = "<html><head>\n"
                      + "  <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js\" crossorigin=\"anonymous\"></script>\n"
                      + "  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" crossorigin=\"anonymous\">\n"
                      + "  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.0/font/bootstrap-icons.css\" crossorigin=\"anonymous\">\n"
                      + "  <title>Marc</title>\n"
                      + "  <meta name=\"generator\" content=\"Apiman Notifications\">\n"
                      + "</head><body><p>This should be in the middle</p></body><footer>\n"
                      + "  This notification was generated automatically by the <a href=\"https://www.apiman.io\" target=\"_blank\">Apiman</a> platform.\n"
                      + "</footer>\n"
                      + "</html>";

        assertThat(rendered)
                .isEqualTo(expected);
    }

    @Test
    public void supports_includes_in_subdirectories() {
        QuteTemplateEngine engine = new QuteTemplateEngine(CONFIG);
        String rendered = engine.applyTemplate("<dl><dt>{#include subfolder/deacon.include.html /}</dt><dd>Felis catus {emoji}</dd></dl>",
                Map.of("emoji", "🐈‍⬛"));
        assertThat(rendered)
                .isEqualTo("<dl><dt>Deacon</dt><dd>Felis catus 🐈‍⬛</dd></dl>");
    }
}

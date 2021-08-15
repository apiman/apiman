package io.apiman.manager.api.notifications.email;

import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class QteTemplateEngineTest {
    @Test
    public void renderSomethingCool() {
        QteTemplateEngine engine = new QteTemplateEngine();
        String rendered = engine.applyTemplate(
          "Hello, {name}",
             Map.of("name", "Marc")
        );
        assertThat(rendered).isEqualTo("Hello, Marc");
    }

    @Test
    public void renderLargeTemplate() {
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

        QteTemplateEngine engine = new QteTemplateEngine();
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
}

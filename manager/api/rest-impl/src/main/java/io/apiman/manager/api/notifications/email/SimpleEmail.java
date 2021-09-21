package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.events.ApimanBuilderMixin;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * Simple email to send.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = SimpleEmail.Builder.class)
public class SimpleEmail {
    private String toName;
    private String toEmail;
    private Map<String, String> headers;
    private EmailNotificationTemplate template;
    private Map<String, Object> templateVariables;

    SimpleEmail(String toName, String toEmail, Map<String, String> headers, EmailNotificationTemplate template,
         Map<String, Object> templateVariables) {
        this.toName = toName;
        this.toEmail = toEmail;
        this.headers = headers;
        this.template = template;
        this.templateVariables = templateVariables;
    }

    public SimpleEmail() {
    }

    /**
     * Build a SimpleEmail
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Name of person to be emailed (e.g. "Mr. A. Nonymous").
     */
    public String getToName() {
        return toName;
    }

    /**
     * Email address to send mail to.
     */
    public String getToEmail() {
        return toEmail;
    }

    /**
     * Additional email headers. These are analogous to HTTP headers For example: <samp>X-Foo-Bar: 1234</samp>.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Template for template renderer.
     */
    public EmailNotificationTemplate getTemplate() {
        return template;
    }

    /**
     * Variables to populate template.
     */
    public Map<String, Object> getTemplateVariables() {
        return templateVariables;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SimpleEmail.class.getSimpleName() + "[", "]")
             .add("toName='" + toName + "'")
             .add("toEmail='" + toEmail + "'")
             .add("headers=" + headers)
             .add("template=" + template)
             .add("templateVariables=" + templateVariables)
             .toString();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements ApimanBuilderMixin {
        @NotBlank
        private String toName;
        @NotBlank
        private String toEmail;
        @NotNull
        private Map<String, String> headers = Collections.emptyMap();
        @NotNull
        private EmailNotificationTemplate template;
        @NotNull
        private Map<String, Object> templateVariables;
        @Nullable
        private UserDto userDto;

        Builder() {
        }

        /**
         * Convenience function that sets {@link #toName} and {@link #toEmail}.
         */
        public Builder setRecipient(@org.jetbrains.annotations.NotNull UserDto userDto) {
            this.userDto = userDto;
            return this;
        }

        /**
         * Name of person to be emailed (e.g. "Mr. A. Nonymous").
         */
        public Builder setToName(@NotBlank String toName) {
            this.toName = toName;
            return this;
        }

        /**
         * Email address to send mail to.
         */
        public Builder setToEmail(@NotBlank String toEmail) {
            this.toEmail = toEmail;
            return this;
        }

        /**
         * Set email template.
         */
        public Builder setTemplate(EmailNotificationTemplate template) {
            this.template = template;
            return this;
        }

        /**
         * Map of keys -> objects that will be passed to the template renderer.
         */
        public Builder setTemplateVariables(Map<String, Object> templateVariables) {
            this.templateVariables = templateVariables;
            return this;
        }

        /**
         * Headers to set on the email.
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Build.
         */
        public SimpleEmail build() {
            if (userDto != null) {
                this.toName = userDto.getFullName();
                this.toEmail = userDto.getEmail();
            }
            beanValidate(this);
            return new SimpleEmail(toName, toEmail, headers, template, templateVariables);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Builder.class.getSimpleName() + "[", "]")
                 .add("toName='" + toName + "'")
                 .add("toEmail='" + toEmail + "'")
                 .add("headers=" + headers)
                 .add("template=" + template)
                 .add("templateVariables=" + templateVariables)
                 .add("userDto=" + userDto)
                 .toString();
        }
    }
}

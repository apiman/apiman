package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.notifications.NotificationCategory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailTemplateFileEntry {
    private String subject;
    private Path html;
    private Path plain;
    private NotificationCategory category;

    // TODO(msavy): record candidate
    public EmailTemplateFileEntry() {
    }

    public String subject() {
        return subject;
    }

    public EmailTemplateFileEntry setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public Path html() {
        return html;
    }

    public EmailTemplateFileEntry setHtml(Path html) {
        this.html = html;
        return this;
    }

    public Path plain() {
        return plain;
    }

    public EmailTemplateFileEntry setPlain(Path plain) {
        this.plain = plain;
        return this;
    }

    public NotificationCategory category() {
        return category;
    }

    public EmailTemplateFileEntry setCategory(NotificationCategory category) {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailTemplateFileEntry that = (EmailTemplateFileEntry) o;
        return Objects.equals(subject, that.subject) && Objects.equals(html, that.html) && Objects.equals(plain, that.plain)
                       && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, html, plain, category);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmailTemplateFileEntry.class.getSimpleName() + "[", "]")
                .add("subject='" + subject + "'")
                .add("html=" + html)
                .add("plain=" + plain)
                .add("category=" + category)
                .toString();
    }
}

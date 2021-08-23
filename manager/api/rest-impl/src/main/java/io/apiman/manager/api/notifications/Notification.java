package io.apiman.manager.api.notifications;

import io.apiman.manager.api.beans.notifications.NotificationStatus;

import java.util.Date;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class Notification<P extends INotificationPayload> {
    private String id;
    private String reason;
    private String reasonMessage;
    private NotificationStatus status;
    private Date createdOn;
    private Date modifiedOn;
    private String recipient;
    private String source;
    private P data;

    public Notification() {
    }

    public String getId() {
        return id;
    }

    public Notification<P> setId(String id) {
        this.id = id;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public Notification<P> setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Notification<P> setStatus(NotificationStatus status) {
        this.status = status;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Notification<P> setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public Notification<P> setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public Notification<P> setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Notification<P> setSource(String source) {
        this.source = source;
        return this;
    }

    public P getData() {
        return data;
    }

    public Notification<P> setData(P data) {
        this.data = data;
        return this;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public Notification<P> setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
        return this;
    }
}

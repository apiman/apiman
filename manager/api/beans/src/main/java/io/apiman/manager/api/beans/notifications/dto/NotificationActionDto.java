package io.apiman.manager.api.beans.notifications.dto;

import io.apiman.manager.api.beans.notifications.NotificationStatus;

import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationActionDto {
    private boolean markAll = false;
    private List<Long> notificationIds;
    @NotNull
    private NotificationStatus status;

    public NotificationActionDto() {
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public NotificationActionDto setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
        return this;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public NotificationActionDto setStatus(NotificationStatus status) {
        this.status = status;
        return this;
    }

    public boolean isMarkAll() {
        return markAll;
    }

    public NotificationActionDto setMarkAll(boolean markAll) {
        this.markAll = markAll;
        return this;
    }
}

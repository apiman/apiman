package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.common.util.Preconditions;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.notifications.mappers.NotificationMapper;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Notifications for tell users useful things. Once a notification has been created with {@link
 * #sendNotification(CreateNotificationDto)}, the event is fired through CDI's notification system. These are then
 * caught by various handlers (such as email), which will do something sensible with emails they know how to handle.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class NotificationService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationService.class);

    private IStorage storage;
    private INotificationRepository notificationRepository;
    private NotificationMapper notificationMapper;
    private ISecurityContext securityContext;
    private Event<NotificationDto<?>> notificationDispatcher;

    @Inject
    public NotificationService(
         IStorage storage,
         INotificationRepository notificationRepository,
         NotificationMapper notificationMapper,
         Event<NotificationDto<?>> notificationDispatcher,
         ISecurityContext securityContext) {
        this.storage = storage;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.notificationDispatcher = notificationDispatcher;
        this.securityContext = securityContext;
    }

    public NotificationService() {
    }

    /**
     * Get the number of notifications for a given user.
     *
     * @param recipientId the user's ID
     * @param unreadOnly true if only unread notifications should be counted
     * @return the count of notifications
     */
    public int getNotificationsCount(@NotNull String recipientId, boolean unreadOnly) {
        if (unreadOnly) {
            LOGGER.debug("Getting unread notifications count for {0}", recipientId);
            return notificationRepository.countNotificationsByUserId(recipientId, List.of(NotificationStatus.OPEN));
        } else {
            LOGGER.debug("Getting all notifications count for {0}", recipientId);
            return notificationRepository.countNotificationsByUserId(recipientId, List.of(NotificationStatus.values()));
        }
    }

    /**
     * Search for any notification for a given user/recipient.
     *
     * @param recipientId the user's ID
     * @param searchCriteriaBeanIn the search criteria
     */
    public SearchResultsBean<NotificationDto<?>> searchNotificationsByRecipient(@NotNull String recipientId, SearchCriteriaBean searchCriteriaBeanIn) {
        var searchCriteria = new SearchCriteriaBean(searchCriteriaBeanIn);
        if (searchCriteriaBeanIn.getOrderBy() == null) {
            searchCriteria.setOrder("id", false);
        }
        SearchResultsBean<NotificationEntity> results = tryAction(
             () -> notificationRepository.searchNotificationsByUser(recipientId, searchCriteria)
        );

        List<NotificationDto<?>> dtos = results.getBeans().stream()
                                               .map(notificationMapper::entityToDto)
                                               .collect(Collectors.toList());

        return new SearchResultsBean<NotificationDto<?>>(dtos, results.getTotalSize());
    }

    /**
     * Get the latest notifications for a given user/recipient.
     *
     * @param recipientId intended recipient of the notification.
     * @param paging pagination.
     * @return results with list of NotificationEntity and paging info.
     */
    public SearchResultsBean<NotificationDto<?>> getLatestNotificationsByRecipient(@NotNull String recipientId, @Nullable PagingBean paging) {
        SearchResultsBean<NotificationEntity> results = tryAction(() -> notificationRepository.getLatestNotificationsByRecipientId(recipientId, paging));

        List<NotificationDto<?>> dtos = results.getBeans().stream()
                                        .map(notificationMapper::entityToDto)
                                        .collect(Collectors.toList());

        return new SearchResultsBean<NotificationDto<?>>(dtos, results.getTotalSize());
    }

    /**
     * Send a new notification to a specified recipient (userId)
     *
     * @param newNotification the new notification.
     */
    public void sendNotification(@NotNull CreateNotificationDto newNotification) {
        LOGGER.debug("Creating new notification(s): {0}", newNotification);

        List<UserDto> resolvedRecipients = calculateRecipients(newNotification.getRecipient());

        for (UserDto resolvedRecipient : resolvedRecipients) {
            NotificationEntity notificationEntity = new NotificationEntity()
                 .setCategory(newNotification.getCategory())
                 .setReason(newNotification.getReason())
                 .setReasonMessage(newNotification.getReasonMessage())
                 .setStatus(NotificationStatus.OPEN)
                 .setRecipient(resolvedRecipient.getUsername())
                 .setSource(newNotification.getSource())
                 .setPayload(JsonUtil.toJsonTree(newNotification.getPayload()));

            tryAction(() -> {
                // 1. Save notification into notifications table.
                LOGGER.trace("Creating notification entity in repository layer: {0}", notificationEntity);
                notificationRepository.create(notificationEntity);

                // Avoiding serializing and deserializing the payload immediately!
                NotificationDto<?> dto = toDto(notificationEntity, newNotification.getPayload(), resolvedRecipient);

                // 2. Emit notification onto notification bus.
                LOGGER.trace("Firing notification: {0}", dto);
                notificationDispatcher.fire(dto);
            });
        }
    }

    /**
     * Mark a list of notifications as read. They must be owned by the same recipient.
     *
     * <p>Any attempt by a user to mark the notifications that do not actually belong to them will be silently ignored.
     *
     * <p>You may want to take the userId from the security context when executing on behalf of an external entity to
     * ensure that the user is actually who they claim to be. This implementation does not check.
     *
     * @throws IllegalArgumentException if status argument is {@link NotificationStatus#OPEN}.
     *
     * @param recipientId     ID of the owner/recipient of the notification. We need this to prevent unauthorised users
     *                        interfering with other users' notifications.
     * @param notificationIds list of notification IDs
     * @param status          status to set (e.g. system read, user read)
     */
    public void markNotificationsWithStatus(@NotNull String recipientId, @NotNull List<Long> notificationIds, @NotNull NotificationStatus status) {
        if (notificationIds.isEmpty()) {
            return;
        }

        LOGGER.trace("Marking recipient {0} notifications {1} as {2}", recipientId, notificationIds, status);

        tryAction(() -> notificationRepository.markNotificationsWithStatusById(recipientId, notificationIds, status));
    }

    /**
     * As {@link #markNotificationsWithStatus(String, List, NotificationStatus)}, but only accepts non-OPEN status.
     *
     * <p>An exception will be thrown if an attempt to mark as read is made using {@link NotificationStatus#OPEN}.
     *
     * @see #markNotificationsWithStatus
     */
    public void markAllNotificationsReadByUserId(@NotNull String recipientId, @NotNull NotificationStatus status) {
        Preconditions.checkArgument(status != NotificationStatus.OPEN,
             "When marking all notifications as read a non-OPEN status must be provided: " + status);

        LOGGER.trace("Marking all recipient {0} notifications as read {1}");

        tryAction(() -> notificationRepository.markAllNotificationsReadByUserId(recipientId, status));
    }

    public Optional<NotificationPreferenceEntity> getNotificationPreference(String userId, String notificationType) {
        return tryAction(() -> notificationRepository.getNotificationPreferenceByUserIdAndType(userId, notificationType));
    }

    private List<UserDto> calculateRecipients(List<RecipientDto> recipientDto) {
        return recipientDto.stream()
                           .flatMap(recipient -> calculateRecipient(recipient).stream())
                           .collect(Collectors.toList());
    }

    private List<UserDto> calculateRecipient(RecipientDto recipient) {
        switch (recipient.getRecipientType()) {
            case INDIVIDUAL:
                return Optional
                     .ofNullable(tryAction(() -> storage.getUser(recipient.getRecipient())))
                     .map(user ->  List.of(UserMapper.toDto(user)))
                     .orElse(Collections.emptyList());
            case ROLE:
                if (recipient.getOrgId() != null) {
                    return securityContext.getUsersWithRole(recipient.getRecipient(), recipient.getOrgId());
                } else {
                    return securityContext.getRemoteUsersWithRole(recipient.getRecipient());
                }
            case PERMISSION:
                PermissionType pType = PermissionType.valueOf(recipient.getRecipient());
                return securityContext.getUsersWithPermission(pType, recipient.getOrgId());
            default:
                throw new IllegalStateException("Unexpected value: " + recipient.getRecipientType());
        }
    }

    // TODO(msavy): can we change this to a mapper? Requires a bit of extra twiddling.
    private NotificationDto<?> toDto(NotificationEntity newNotification, IVersionedApimanEvent event, UserDto user) {
        return new NotificationDto<>()
             .setId(newNotification.getId())
             .setCategory(newNotification.getCategory())
             .setReason(newNotification.getReason())
             .setReasonMessage(newNotification.getReasonMessage())
             .setStatus(NotificationStatus.OPEN)
             .setCreatedOn(newNotification.getCreatedOn())
             .setModifiedOn(newNotification.getModifiedOn())
             .setRecipient(user)
             .setSource(newNotification.getSource())
             .setPayload(event);
    }

}

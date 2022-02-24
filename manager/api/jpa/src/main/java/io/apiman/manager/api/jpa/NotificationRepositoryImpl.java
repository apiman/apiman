package io.apiman.manager.api.jpa;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.NotificationType;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;
import javax.validation.constraints.NotEmpty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Storage for simple notifications system
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@ParametersAreNonnullByDefault
public class NotificationRepositoryImpl extends AbstractJpaStorage implements INotificationRepository {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationRepositoryImpl.class);

    public NotificationRepositoryImpl() {
        super();
    }

    @Override
    public NotificationEntity getNotificationById(Long notificationId) throws StorageException {
        return super.get(notificationId, NotificationEntity.class);
    }

    @Override
    public SearchResultsBean<NotificationEntity> searchNotificationsByUser(String recipientUserId,
         @Nullable SearchCriteriaBean searchCriteria)
         throws StorageException {

        var recipientFilter = new SearchCriteriaFilterBean()
             .setName("recipient")
             .setOperator(SearchCriteriaFilterOperator.eq)
             .setValue(recipientUserId);

        searchCriteria.getFilters().add(recipientFilter);

        return super.find(searchCriteria, NotificationEntity.class);
    }

    @Override
    public SearchResultsBean<NotificationEntity> getLatestNotificationsByRecipientId(String recipientUserId, @Nullable PagingBean paging)
         throws StorageException {
        var filter = new SearchCriteriaFilterBean()
             .setName("recipient")
             .setOperator(SearchCriteriaFilterOperator.eq)
             .setValue(recipientUserId);

        var searchCriteria = new SearchCriteriaBean()
             .setFilters(List.of(filter))
             .setPaging(paging)
             .setOrder("id", false);

        return super.find(searchCriteria, NotificationEntity.class);
    }

    @Override
    public void create(NotificationEntity bean) throws StorageException {
        super.create(bean);
    }

    @Override
    public void update(NotificationEntity bean) throws StorageException {
        super.update(bean);
    }

    @Override
    public void delete(NotificationEntity bean) throws StorageException {
        super.delete(bean);
    }

    @Override
    public void deleteById(Long id) throws StorageException {
        delete(super.get(id, NotificationEntity.class));
    }

    @Override
    public void deleteAll() {
        int n = getActiveEntityManager().createQuery("DELETE FROM NotificationEntity").executeUpdate();
        LOGGER.debug("Deleted all Notifications, this resulted in {0} records being removed.", n);
    }

    @Override
    public void deleteByUserId(String recipientUserId) {
        int n = getActiveEntityManager()
             .createQuery("DELETE FROM NotificationEntity n WHERE n.recipient = :recipientId")
             .setParameter("recipientId", recipientUserId)
             .executeUpdate();
        LOGGER.debug("Deleted all Notifications for recipient {0}, this resulted in {1} "
             + "records being removed.", recipientUserId, n);
    }

    @Override
    public int countNotificationsByUserId(String recipientUserId, List<NotificationStatus> notificationStatus) {
        List<String> statusNames = notificationStatus.stream().map(Enum::name).collect(Collectors.toList());
        return getJdbi().withHandle(jdbi ->
             jdbi.createQuery("SELECT COUNT(n.id) "
                      + "FROM NOTIFICATIONS n "
                      + "WHERE n.recipient = :userId "
                      + "AND n.status IN (<status>)")
                 .bind("userId", recipientUserId)
                 .bindList("status", statusNames)
                 .mapTo(int.class)
                 .one()
        );
    }

    @Override
    public void markNotificationsWithStatusById(String recipientUserId, List<Long> idList, NotificationStatus status) throws StorageException {
        int n = getActiveEntityManager()
             .createQuery(
                  "UPDATE NotificationEntity n "
                       + "SET n.status = :newStatus, "
                       + "    n.modifiedOn = :now "
                       + "WHERE n.recipient = :recipientId "
                       + "AND n.id IN :idList"
             )
             .setParameter("newStatus", status)
             .setParameter("now", OffsetDateTime.now())
             .setParameter("recipientId", recipientUserId) // This ensures you can't mark someone else's notifications as read.
             .setParameter("idList", idList)
             .executeUpdate();
        LOGGER.debug("Marked all unread notifications for recipient {0} to status {1}, "
             + "this affected {2} records.", recipientUserId, status, n);
    }

    @Override
    public void markAllNotificationsReadByUserId(String recipientUserId, NotificationStatus status) {
        int n = getActiveEntityManager()
             .createQuery(
                  "UPDATE NotificationEntity n "
                       + "SET n.status = :newStatus, "
                       + "    n.modifiedOn = :now "
                       + "WHERE n.recipient = :recipientId AND n.status = 'OPEN'"
             )
             .setParameter("newStatus", status)
             .setParameter("recipientId", recipientUserId)
             .setParameter("now", OffsetDateTime.now())
             .executeUpdate();
        LOGGER.debug("Marked all unread notifications for recipient {0} to status {1}, "
             + "this affected {2} records.", recipientUserId, status, n);
    }

    @Override
    public Optional<NotificationPreferenceEntity> getNotificationPreferenceByUserIdAndType(String userId, NotificationType notificationType) {
        Query query = getActiveEntityManager()
             .createQuery(
                  "SELECT pref "
                       + "FROM NotificationPreferenceEntity pref "
                       + "WHERE pref.userId = :userId "
                       + "AND pref.type = :notificationType"
             )
             .setParameter("userId", userId)
             .setParameter("notificationType", notificationType);
        return super.getOne(query);
    }
}

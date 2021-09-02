package io.apiman.manager.api.jpa;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.time.OffsetDateTime;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.jetbrains.annotations.NotNull;

/**
 * Storage for simple notifications system
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class NotificationRepositoryImpl extends AbstractJpaStorage implements INotificationRepository {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationRepositoryImpl.class);

    public NotificationRepositoryImpl() {
        super();
    }

    @Override
    public NotificationEntity getNotificationById(@NotNull Long notificationId) throws StorageException {
        return super.get(notificationId, NotificationEntity.class);
    }

    @Override
    public SearchResultsBean<NotificationEntity> getUnreadNotificationsByRecipientId(@NotNull String recipientUserId, @NotNull PagingBean paging)
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
    public void create(@NotNull NotificationEntity bean) throws StorageException {
        super.create(bean);
    }

    @Override
    public void update(@NotNull NotificationEntity bean) throws StorageException {
        super.update(bean);
    }

    @Override
    public void delete(@NotNull NotificationEntity bean) throws StorageException {
        super.delete(bean);
    }

    @Override
    public void deleteById(@NotNull Long id) throws StorageException {
        delete(super.get(id, NotificationEntity.class));
    }

    @Override
    public void deleteAll() {
        int n = getActiveEntityManager().createQuery("DELETE FROM NotificationEntity").executeUpdate();
        LOGGER.debug("Deleted all Notifications, this resulted in {0} records being removed.", n);
    }

    @Override
    public void deleteByUserId(@NotNull String recipientUserId) {
        int n = getActiveEntityManager()
             .createQuery("DELETE FROM NotificationEntity n WHERE n.recipient = :recipientId")
             .setParameter("recipientId", recipientUserId)
             .executeUpdate();
        LOGGER.debug("Deleted all Notifications for recipient {0}, this resulted in {1} "
             + "records being removed.", recipientUserId, n);
    }

    @Override
    public int countUnreadNotificationsByUserId(@NotNull String recipientUserId) {
        return getJdbi().withHandle(jdbi ->
             jdbi.createQuery("SELECT COUNT(n.id) "
                      + "FROM NOTIFICATIONS n "
                      + "WHERE n.recipient = :userId "
                      + "AND n.notification_status = 'OPEN'")
                 .bind("userId", recipientUserId)
                 .mapTo(int.class)
                 .one()
        );
    }

    @Override
    public void markNotificationsReadById(@NotNull String recipientUserId, @NotNull List<Long> idList, @NotNull NotificationStatus status) throws StorageException {
        int n = getActiveEntityManager()
             .createQuery(
                  "UPDATE NotificationEntity n "
                       + "SET n.notificationStatus = :newStatus, "
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
    public void markAllNotificationsReadByUserId(@NotNull String recipientUserId, @NotNull NotificationStatus status) {
        int n = getActiveEntityManager()
             .createQuery(
                  "UPDATE NotificationEntity n "
                       + "SET n.notificationStatus = :newStatus, "
                       + "    n.modifiedOn = :now "
                       + "WHERE n.recipient = :recipientId AND n.notificationStatus = 'OPEN'"
             )
             .setParameter("newStatus", status)
             .setParameter("recipientId", recipientUserId)
             .setParameter("now", OffsetDateTime.now())
             .executeUpdate();
        LOGGER.debug("Marked all unread notifications for recipient {0} to status {1}, "
             + "this affected {2} records.", recipientUserId, status, n);
    }
}

// package io.apiman.manager.api.beans.notifications;
//
// import javax.persistence.Column;
// import javax.persistence.Entity;
// import javax.persistence.Id;
// import javax.persistence.Table;
//
// /**
//  * Notifications types that we know about, so users can
//  * opt into and out of these.
//  *
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// @Entity
// @Table(name = "notification_reasons")
// public class NotificationReason {
//
//     @Id
//     @Column(name = "reason", unique = true, nullable = false)
//     private String reason;
//
//     public String getReason() {
//         return reason;
//     }
//
//     public NotificationReason setReason(String reason) {
//         this.reason = reason;
//         return this;
//     }
// }

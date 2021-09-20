// package io.apiman.manager.test.server;
//
// import javax.annotation.PostConstruct;
// import javax.annotation.Priority;
// import javax.inject.Inject;
// import javax.interceptor.AroundInvoke;
// import javax.interceptor.Interceptor;
// import javax.interceptor.InvocationContext;
// import javax.persistence.EntityManager;
// import javax.persistence.PersistenceContext;
// import javax.transaction.TransactionRolledbackException;
// import javax.transaction.Transactional;
//
// /**
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// @Priority(Interceptor.Priority.LIBRARY_BEFORE-10)
// @Interceptor
// @Transactional
// public class JettyTransactionalInterceptor {
//
//     @PersistenceContext(unitName = "apiman-manager-api-jpa")
//     @Inject
//     EntityManager em;
//
//     public JettyTransactionalInterceptor() {
//     }
//
//     @PostConstruct
//     public void hello() {
//         System.out.println("hello");
//     }
//
//     @AroundInvoke
//     public Object auditMethod(InvocationContext ctx) throws Exception {
//         if (!em.getTransaction().isActive()) {
//             System.out.println("Beginning TX");
//             em.getTransaction().begin();
//         } else {
//             System.out.println("Committing TX");
//             em.getTransaction().commit();
//             System.out.println("Beginning TX (after commit)");
//             em.getTransaction().begin();
//         }
//
//         Object result = null;
//         try {
//             result = ctx.proceed();
//             em.getTransaction().commit();
//             System.out.println("Committed TX");
//         } catch (Exception e) {
//             em.getTransaction().rollback();
//             System.out.println("Rolled back TX");
//             //throw new TransactionRolledbackException();
//             throw e;
//         }
//         return result;
//     }
// }

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
// import javax.transaction.Status;
// import javax.transaction.SystemException;
// import javax.transaction.Transaction;
// import javax.transaction.TransactionManager;
// import javax.transaction.Transactional;
//
// import com.atomikos.icatch.jta.UserTransactionManager;
//
// /**
//  *
//  */
// @Priority(Interceptor.Priority.LIBRARY_BEFORE)
// @Interceptor
// @Transactional
// public class JettyTransactionalInterceptor2 {
//
//     @PersistenceContext(unitName = "apiman-manager-api-jpa")
//     @Inject
//     EntityManager em;
//
//     // @Inject
//     // private TransactionManager tm;
//     // @Inject
//     private UserTransactionManager tm = new UserTransactionManager();
//
//     public JettyTransactionalInterceptor2() {
//
//     }
//
//     @PostConstruct
//     public void post() throws SystemException {
//         tm.init();
//     }
//
//     @AroundInvoke
//     public Object intercept(InvocationContext ic) throws Exception {
//         Transaction tx = tm.getTransaction();
//         if (tx == null) {
//             System.out.println("Invoke in our tx");
//             return invokeInOurTx(ic);
//         } else {
//             System.out.println("Invoke in caller tx");
//             return invokeInCallerTx(ic, tx);
//         }
//     }
//
//     private Object invokeInOurTx(InvocationContext ic) throws Exception {
//         tm.begin();
//         //em.joinTransaction();
//         System.out.println("Beginning transaction");
//         Transaction tx = tm.getTransaction();
//
//         try {
//             return ic.proceed();
//         } catch (Exception e) {
//             handleException(ic, e, tx);
//         } finally {
//             endTransaction(tm, tx);
//         }
//         throw new RuntimeException("UNREACHABLE");
//
//         // if (!tx.get()) {
//         //     System.out.println("Beginning TX");
//         //     em.getTransaction().begin();
//         // }
//         // Object result = null;
//         // try {
//         //     result = ctx.proceed();
//         // } catch (Exception e) {
//         //     tx.rollback();
//         //     System.out.println("Rolled back TX");
//         //     throw e;
//         // } finally {
//         //     if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
//         //         tm.rollback();
//         //     } else {
//         //         tm.commit();
//         //     }
//         // }
//         // return result;
//     }
//
//     private void endTransaction(TransactionManager tm, Transaction tx) throws Exception {
//         if (tx != tm.getTransaction()) {
//             throw new RuntimeException("tx on wrong thread");
//         }
//
//         if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
//             System.out.println("Rolling back");
//             tm.rollback();
//         } else {
//             System.out.println("Committing transaction");
//             System.out.println("tx active? " + em.getTransaction().isActive());
//             em.flush();
//             tm.commit();
//             System.out.println("tx active? " + em.getTransaction().isActive());
//         }
//     }
//
//     private void handleException(InvocationContext ic, Exception e, Transaction tx) throws Exception {
//         if (e instanceof RuntimeException) {
//             System.out.println("Rollback only set");
//             tx.setRollbackOnly();
//             throw e;
//         }
//
//         throw e;
//     }
//
//
//     private Object invokeInCallerTx(InvocationContext ic, Transaction tx) throws Exception {
//         try {
//             return ic.proceed();
//         } catch (Exception e) {
//             handleException(ic, e, tx);
//         }
//         throw new RuntimeException("UNREACHABLE");
//     }
// }

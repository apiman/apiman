package io.apiman.manager.test.server;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.EntityManagerFactoryAccessor;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;

/**
 *
 */
@Priority(Interceptor.Priority.LIBRARY_AFTER)
@Interceptor
@Transactional
public class JettyTransactionalInterceptor implements DataAccessUtilMixin {

    // @PersistenceContext(unitName = "apiman-manager-api-jpa")
    @Inject
    EntityManagerFactoryAccessor emf;
    private EntityManager em;
    static ThreadLocal<AtomicInteger> test = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    //@Inject
    //private TransactionManager tm;
    //@Inject
    // private J2eeTransactionManager tm = new J2eeTransactionManager();

    public JettyTransactionalInterceptor() {
    }

    // @PostConstruct
    // public void after() {
    //     em.setFlushMode(FlushModeType.AUTO);
    // }

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        em = emf.getEntityManager();
        System.out.println("em=" + em.hashCode());

        if (!em.getTransaction().isActive()) {
            System.out.println("Invoke in our tx");
            return invokeInOurTx(ic);
        } else {
            System.out.println("Invoke in caller tx");
            return invokeInCallerTx(ic, em.getTransaction());
        }
    }

    private Object invokeInOurTx(InvocationContext ic) throws Exception {
        //tm.begin();
        //em.joinTransaction();
        System.out.println("Beginning transaction");
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        int depth = test.get().incrementAndGet();
        System.out.println(depth);
        try {
            return ic.proceed();
        } catch (Exception e) {
            handleException(e, tx);
        } finally {
            endTransaction(tx);
        }
        throw new RuntimeException("UNREACHABLE");

        // if (!tx.get()) {
        //     System.out.println("Beginning TX");
        //     em.getTransaction().begin();
        // }
        // Object result = null;
        // try {
        //     result = ctx.proceed();
        // } catch (Exception e) {
        //     tx.rollback();
        //     System.out.println("Rolled back TX");
        //     throw e;
        // } finally {
        //     if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
        //         tm.rollback();
        //     } else {
        //         tm.commit();
        //     }
        // }
        // return result;
    }

    private void endTransaction(EntityTransaction tx) throws Exception {
        // if (tx != tm.getTransaction()) {
        //     throw new RuntimeException("tx on wrong thread");
        // }
        try {
            if (!tx.getRollbackOnly()) {
                // em.flush();
                System.out.println("commit");
                tryAction(tx::commit);
            } else {
                tx.rollback();
            }
        } catch (Exception e) {
            handleException(e, tx);
        } finally {
            em.close();
            test.get().decrementAndGet();
        }

        // if (tx.isActive() == Status.STATUS_MARKED_ROLLBACK) {
        //     System.out.println("Rolling back");
        //     tm.rollback();
        // } else {
        //     System.out.println("Committing transaction");
        //     System.out.println("tx active? " + em.getTransaction().isActive());
        //     em.flush();
        //     tm.commit();
        //     System.out.println("tx active? " + em.getTransaction().isActive());
        // }
    }

    private void handleException(Exception e, EntityTransaction tx) throws Exception {
        e.printStackTrace();
        if (e instanceof RuntimeException || e instanceof StorageException) {
            System.out.println("Rollback only set");
            tx.setRollbackOnly();
            throw e;
        }

        throw new SystemErrorException(e);
    }


    private Object invokeInCallerTx(InvocationContext ic, EntityTransaction tx) throws Exception {
        try {
            return ic.proceed();
        } catch (Exception e) {
            handleException(e, tx);
        }
        throw new RuntimeException("UNREACHABLE");
    }
}

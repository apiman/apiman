package io.apiman.manager.test.server;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.EntityManagerFactoryAccessor;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

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

    @Inject
    EntityManagerFactoryAccessor emf;
    private EntityManager em;

    public JettyTransactionalInterceptor() {
    }

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
        System.out.println("Beginning transaction");
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            return ic.proceed();
        } catch (Exception e) {
            handleException(e, tx);
        } finally {
            endTransaction(tx);
        }
        throw new RuntimeException("UNREACHABLE");
    }

    private void endTransaction(EntityTransaction tx) throws Exception {
        try {
            if (!tx.getRollbackOnly()) {
                // em.flush();
                System.out.println("commit");
                tx.commit();
            } else {
                tx.rollback();
            }
        } catch (Exception e) {
            handleException(e, tx);
        } finally {
            em.close();
        }
    }

    private void handleException(Exception e, EntityTransaction tx) throws Exception {
        if (e instanceof RuntimeException || e instanceof StorageException) {
            System.out.println("Rollback only set");
            tx.setRollbackOnly();
            throw e;
        }
        throw e;
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

package io.apiman.manager.api.war.tomcat8;

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
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@Interceptor
@Transactional
public class ApimanTransactionalInterceptor implements DataAccessUtilMixin {

    @Inject
    EntityManagerFactoryAccessor emf;
    private EntityManager em;

    public ApimanTransactionalInterceptor() {
        System.err.println("Apiman transactional interceptor is running");
    }

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        em = emf.getEntityManager();
        if (!em.getTransaction().isActive()) {
            return invokeInOurTx(ic);
        } else {
            return invokeInCallerTx(ic, em.getTransaction());
        }
    }

    private Object invokeInOurTx(InvocationContext ic) throws Exception {
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
                if (tx.isActive()) {
                    tx.commit();
                } else {
                    System.err.println("Someone else closed the TX?");
                }
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
        e.printStackTrace();
        if (e instanceof RuntimeException || e instanceof StorageException) {
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

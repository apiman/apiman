package io.apiman.manager.api.war.tomcat8;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.EntityManagerFactoryAccessor;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
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

    public ApimanTransactionalInterceptor() {
    }

    private EntityManager getEm() {
        return emf.getEntityManager();
    }

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        if (!getEm().getTransaction().isActive()) {
            return invokeInOurTx(ic);
        } else {
            return invokeInCallerTx(ic, getEm().getTransaction());
        }
    }

    private Object invokeInOurTx(InvocationContext ic) throws Exception {
        EntityTransaction tx = getEm().getTransaction();
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
                if (getEm().isOpen() && tx.isActive()) {
                    try {
                        tx.commit();
                    } catch (AbstractRestException e) {  // Emulates the way Apiman handled commit exceptions in earlier versions
                        throw e;
                    } catch (Throwable e) {
                        throw new SystemErrorException(e);
                    }
                }
            } else {
                tx.rollback();
            }
        } catch (Exception e) {
            handleException(e, tx);
        } finally {
            if (getEm().isOpen()) {
                getEm().close();
            }
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

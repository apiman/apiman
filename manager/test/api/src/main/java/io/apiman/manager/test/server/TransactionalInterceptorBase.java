// package io.apiman.manager.test.server;
//
// import java.io.Serializable;
// import java.lang.annotation.Annotation;
// import java.security.PrivilegedAction;
// import java.util.HashSet;
// import java.util.Set;
// import javax.enterprise.inject.Intercepted;
// import javax.enterprise.inject.spi.AnnotatedMethod;
// import javax.enterprise.inject.spi.AnnotatedType;
// import javax.enterprise.inject.spi.Bean;
// import javax.inject.Inject;
// import javax.interceptor.InvocationContext;
// import javax.transaction.Status;
// import javax.transaction.Transaction;
// import javax.transaction.TransactionManager;
// import javax.transaction.Transactional;
//
// /**
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// public abstract class TransactionalInterceptorBase implements Serializable {
//
//     private static final long serialVersionUID = 1L;
//
//     @Inject
//     transient javax.enterprise.inject.spi.BeanManager beanManager;
//
//     @Inject
//     @Intercepted
//     private Bean<?> interceptedBean;
//
//     @Inject
//     private TransactionManager transactionManager;
//
//     private final boolean userTransactionAvailable;
//
//     protected TransactionalInterceptorBase(boolean userTransactionAvailable) {
//         this.userTransactionAvailable = userTransactionAvailable;
//     }
//
//     public Object intercept(InvocationContext ic) throws Exception {
//
//         final Transaction tx = transactionManager.getTransaction();
//
//         boolean previousUserTransactionAvailability = setUserTransactionAvailable(userTransactionAvailable);
//         try {
//             return doIntercept(transactionManager, tx, ic);
//         } finally {
//             resetUserTransactionAvailability(previousUserTransactionAvailability);
//         }
//     }
//
//     protected abstract Object doIntercept(TransactionManager tm, Transaction tx, InvocationContext ic) throws Exception;
//
//     /**
//      * <p>
//      * Looking for the {@link Transactional} annotation first on the method, second on the class.
//      * <p>
//      * Method handles CDI types to cover cases where extensions are used.
//      * In case of EE container uses reflection.
//      *
//      * @param ic  invocation context of the interceptor
//      * @return instance of {@link Transactional} annotation or null
//      */
//     private Transactional getTransactional(InvocationContext ic) {
//         if(interceptedBean != null) { // not-null for CDI
//             // getting annotated type and method corresponding of the intercepted bean and method
//             AnnotatedType<?> currentAnnotatedType = extension.getBeanToAnnotatedTypeMapping().get(interceptedBean);
//             AnnotatedMethod<?> currentAnnotatedMethod = null;
//             for(AnnotatedMethod<?> methodInSearch: currentAnnotatedType.getMethods()) {
//                 if(methodInSearch.getJavaMember().equals(ic.getMethod())) {
//                     currentAnnotatedMethod = methodInSearch;
//                     break;
//                 }
//             }
//
//             // check existence of the stereotype on method
//             Transactional transactionalMethod = getTransactionalAnnotationRecursive(currentAnnotatedMethod.getAnnotations());
//             if(transactionalMethod != null) return transactionalMethod;
//             // stereotype recursive search, covering ones added by an extension too
//             Transactional transactionalExtension = getTransactionalAnnotationRecursive(currentAnnotatedType.getAnnotations());
//             if(transactionalExtension != null) return transactionalExtension;
//             // stereotypes already merged to one chunk by BeanAttributes.getStereotypes()
//             for(Class<? extends Annotation> stereotype: interceptedBean.getStereotypes()) {
//                 Transactional transactionalAnn = stereotype.getAnnotation(Transactional.class);
//                 if(transactionalAnn != null) return transactionalAnn;
//             }
//         } else { // null for EE components
//             Transactional transactional = ic.getMethod().getAnnotation(Transactional.class);
//             if (transactional != null) {
//                 return transactional;
//             }
//
//             Class<?> targetClass = ic.getTarget().getClass();
//             transactional = targetClass.getAnnotation(Transactional.class);
//             if (transactional != null) {
//                 return transactional;
//             }
//         }
//
//         throw new RuntimeException();
//     }
//
//     private Transactional getTransactionalAnnotationRecursive(Annotation... annotationsOnMember) {
//         if(annotationsOnMember == null) return null;
//         Set<Class<? extends Annotation>> stereotypeAnnotations = new HashSet<>();
//
//         for(Annotation annotation: annotationsOnMember) {
//             if(annotation.annotationType().equals(Transactional.class)) {
//                 return (Transactional) annotation;
//             }
//             if (beanManager.isStereotype(annotation.annotationType())) {
//                 stereotypeAnnotations.add(annotation.annotationType());
//             }
//         }
//         for(Class<? extends Annotation> stereotypeAnnotation: stereotypeAnnotations) {
//             return getTransactionalAnnotationRecursive(beanManager.getStereotypeDefinition(stereotypeAnnotation));
//         }
//         return null;
//     }
//
//     private Transactional getTransactionalAnnotationRecursive(Set<Annotation> annotationsOnMember) {
//         return getTransactionalAnnotationRecursive(
//              annotationsOnMember.toArray(new Annotation[annotationsOnMember.size()]));
//     }
//
//     protected Object invokeInOurTx(InvocationContext ic, TransactionManager tm) throws Exception {
//
//         tm.begin();
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
//     }
//
//     protected Object invokeInCallerTx(InvocationContext ic, Transaction tx) throws Exception {
//
//         try {
//             return ic.proceed();
//         } catch (Exception e) {
//             handleException(ic, e, tx);
//         }
//         throw new RuntimeException("UNREACHABLE");
//     }
//
//     protected Object invokeInNoTx(InvocationContext ic) throws Exception {
//
//         return ic.proceed();
//     }
//
//     protected void handleException(InvocationContext ic, Exception e, Transaction tx) throws Exception {
//
//         Transactional transactional = getTransactional(ic);
//
//         for (Class<?> dontRollbackOnClass : transactional.dontRollbackOn()) {
//             if (dontRollbackOnClass.isAssignableFrom(e.getClass())) {
//                 throw e;
//             }
//         }
//
//         for (Class<?> rollbackOnClass : transactional.rollbackOn()) {
//             if (rollbackOnClass.isAssignableFrom(e.getClass())) {
//                 tx.setRollbackOnly();
//                 throw e;
//             }
//         }
//
//         if (e instanceof RuntimeException) {
//             tx.setRollbackOnly();
//             throw e;
//         }
//
//         throw e;
//     }
//
//     protected void endTransaction(TransactionManager tm, Transaction tx) throws Exception {
//
//         if (tx != tm.getTransaction()) {
//             throw new RuntimeException("");
//         }
//
//         if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
//             tm.rollback();
//         } else {
//             tm.commit();
//         }
//     }
//
//     protected boolean setUserTransactionAvailable(boolean available) {
//         return true;
//     }
//
//     protected void resetUserTransactionAvailability(boolean previousUserTransactionAvailability) {
//     }
//
// }

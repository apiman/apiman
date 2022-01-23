// package io.apiman.manager.api.jpa;
//
// import io.apiman.manager.api.beans.apis.ApiPlanBean;
// import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
//
// import java.util.List;
// import java.util.Map;
//
// import org.hibernate.HibernateException;
// import org.hibernate.event.spi.MergeEvent;
// import org.hibernate.event.spi.MergeEventListener;
// import org.hibernate.event.spi.PostLoadEvent;
// import org.hibernate.event.spi.PostLoadEventListener;
//
// /**
//  *
//  *
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// public class DiscoverabilityListener implements PostLoadEventListener, MergeEventListener {
//
//     @Override
//     public void onPostLoad(PostLoadEvent event) {
//         if (event.getEntity() instanceof ApiPlanBean) {
//             System.out.println("Hello there, a load event happened! " + event.getEntity());
//             ApiPlanBean apb = (ApiPlanBean) event.getEntity();
//
//             String orgId = apb.getApiVersion().getApi().getOrganization().getId();
//             String apiId = apb.getApiVersion().getApi().getId();
//             String apiVer = apb.getApiVersion().getVersion();
//             // Long apiVerId = apb.getApiVersion().getId();
//             String planId = apb.getPlanId();
//             String planVer = apb.getVersion();
//
//             // TODO(msavy): this will cause N+1 problem, so we should see if we can operate on collections
//             List<DiscoverabilityEntity> de = event.getSession()
//                     .createQuery("SELECT de FROM DiscoverabilityEntity de "
//                                          + "WHERE de.orgId = :orgId "
//                                          + "AND de.apiId = :apiId "
//                                          + "AND de.apiVersion = :apiVersion "
//                                          + "AND de.planId = :planId "
//                                          + "AND de.planVersion = :planVersion ",
//                             DiscoverabilityEntity.class)
//                     .setParameter("orgId", orgId)
//                     .setParameter("apiId", apiId)
//                     .setParameter("apiVersion", apiVer)
//                     .setParameter("planId", planId)
//                     .setParameter("planVersion", planVer)
//                     .getResultList();
//
//             if (!de.isEmpty()) {
//                 apb.setDiscoverability(de.get(0));
//             }
//         }
//     }
//
//     @Override
//     public void onMerge(MergeEvent event) throws HibernateException {
//         System.out.println("onMerge");
//         if (event.getOriginal() instanceof ApiPlanBean) {
//             ApiPlanBean apbNew = (ApiPlanBean) event.getEntity();
//             ApiPlanBean original = (ApiPlanBean) event.getOriginal();
//             if (original.getDiscoverability() != null) {
//                 apbNew.setDiscoverability(original.getDiscoverability());
//             }
//         }
//     }
//
//     @Override
//     public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
//         System.out.println("onMerge2");
//     }
// }

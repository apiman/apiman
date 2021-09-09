package io.apiman.manager.api.beans.notifications;

import io.apiman.manager.api.beans.search.OrderByBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.hibernate.Criteria;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationCriteriaBean extends SearchCriteriaBean {
    public static final String[] ALLOWED_VALUES = {
         "id",
         "category",
         "reason",
         "reasonMessage",
         "notificationStatus",
         "createdOn",
         "modifiedOn",
         "source"
    };

    /**
     * Get filters, with any impermissible filters removed
     */
    public List<SearchCriteriaFilterBean> getFilters() {
        Set<String> allowedValues = new HashSet<>(Arrays.asList(ALLOWED_VALUES));
        return super.filters
             .stream()
             .filter(f -> allowedValues.contains(f.getName()))
             .collect(Collectors.toList());
    }
}

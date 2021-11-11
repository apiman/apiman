/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.policies;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Models a policy definition description template.  A policy definition
 * template is an MVEL template used to dynamically generate a description
 * of a policy instance.  This allows policies to have different descriptions
 * depending on their configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
@Embeddable
public class PolicyDefinitionTemplateBean {

    private String language;
    @Column(length=2048)
    private String template;

    /**
     * Constructor.
     */
    public PolicyDefinitionTemplateBean() {
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "PolicyDefinitionTemplateBean [language=" + language + ", template=" + template + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PolicyDefinitionTemplateBean that = (PolicyDefinitionTemplateBean) o;
        return Objects.equals(language, that.language) && Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, template);
    }
}

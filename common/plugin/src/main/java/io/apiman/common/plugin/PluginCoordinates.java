/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.common.plugin;

import java.io.Serializable;

/**
 * A simple bean that models unique plugin coordinates.  A plugin is uniquely identified
 * by its Maven properties:
 * 
 * groupId
 * artifactId
 * version
 * classifier (optional)
 * type (optional defaults to war)
 *
 * @author eric.wittmann@redhat.com
 */
public class PluginCoordinates implements Serializable {
    
    private static final long serialVersionUID = -1368521324833902631L;
    
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String type = "war"; //$NON-NLS-1$
    
    /**
     * Constructor.
     */
    public PluginCoordinates() {
    }

    /**
     * Constructor.
     * @param groupId Maven group id
     * @param artifactId Maven artifact id 
     * @param version Maven version
     */
    public PluginCoordinates(String groupId, String artifactId, String version) {
        setGroupId(groupId);
        setArtifactId(artifactId);
        setVersion(version);
    }

    /**
     * Constructor.
     * @param groupId  Maven group id
     * @param artifactId Maven artifact id 
     * @param version Maven version
     * @param classifier Maven classifier
     * @param type Maven type
     */
    public PluginCoordinates(String groupId, String artifactId, String version, String classifier, String type) {
        setGroupId(groupId);
        setArtifactId(artifactId);
        setVersion(version);
        setClassifier(classifier);
        if (type != null) {
            setType(type);
        }
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifactId the artifactId to set
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the classifier
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier the classifier to set
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginCoordinates other = (PluginCoordinates) obj;
        if (artifactId == null) {
            if (other.artifactId != null)
                return false;
        } else if (!artifactId.equals(other.artifactId))
            return false;
        if (classifier == null) {
            if (other.classifier != null)
                return false;
        } else if (!classifier.equals(other.classifier))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getGroupId());
        builder.append(':');
        builder.append(getArtifactId());
        builder.append(':');
        builder.append(getVersion());
        if (getClassifier() != null) {
            builder.append('-').append(getClassifier());
        }
        builder.append(':');
        if (getType() == null) {
            builder.append("war"); //$NON-NLS-1$
        } else {
            builder.append(getType());
        }

        return builder.toString();
    }
    
    /**
     * Returns the plugin coordinates associated with the given plugin policy specification.  The format
     * of a plugin policy specification is:
     * 
     * plugin:groupId:artifactId:version[:classifier][:type]/org.example.plugins.PluginImpl
     * 
     * @param pluginPolicySpec the policy specification
     * @return plugin coordinates
     */
    public static final PluginCoordinates fromPolicySpec(String pluginPolicySpec) {
        if (pluginPolicySpec == null) {
            return null;
        }
        int startIdx = 7;
        int endIdx = pluginPolicySpec.indexOf('/');
        
        String [] split = pluginPolicySpec.substring(startIdx, endIdx).split(":"); //$NON-NLS-1$
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        String classifier = null;
        String type = null;
        if (split.length == 4) {
            type = split[3];
        }
        if (split.length == 5) {
            classifier = split[3];
            type = split[4];
        }
        PluginCoordinates rval = new PluginCoordinates(groupId, artifactId, version, classifier, type);
        return rval;
    }

}

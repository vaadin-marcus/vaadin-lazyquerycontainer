/**
 * Copyright 2010 Tommi S.E. Laukkanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.addons.lazyquerycontainer.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Task entity for JPA testing.
 */
@Entity

public final class Task implements Serializable {
    /**
     * Java serialization version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Unique identifier of the task.
     */
    @Id
    @GeneratedValue
    private long taskId;
    /**
     * Name of the task.
     */
    private String name;
    /**
     * Reporter of the task.
     */
    private String reporter;
    /**
     * Assignee of the task.
     */
    private String assignee;

    /**
     * @return the taskId
     */
    public long getTaskId() {
        return taskId;
    }

    /**
     * @param taskId the taskId to set
     */
    public void setTaskId(final long taskId) {
        this.taskId = taskId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the reporter
     */
    public String getReporter() {
        return reporter;
    }

    /**
     * @param reporter the reporter to set
     */
    public void setReporter(final String reporter) {
        this.reporter = reporter;
    }

    /**
     * @return the assignee
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * @param assignee the assignee to set
     */
    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "Task name: " + name + " reporter: " + reporter + " assignee: " + assignee;
    }

}
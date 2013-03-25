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

import com.vaadin.data.Item;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.EntityContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatus;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for EntityContainer.
 *
 * @author Tommi Laukkanen
 */
public class EntityContainerDetachedEntitiesPropertyIdsTest {

    /**
     * Query cache size.
     */
    private static final int QUERY_CACHE_SIZE = 1000;
    /**
     * Item count for cache test.
     */
    private static final int ITEM_COUNT_FOR_CACHE_TEST = 2000;
    /**
     * Batch size for entity container.
     */
    private static final int ENTITY_CONTAINER_BATCH_SIZE = 100;
    /**
     * The JPA EntityManagerFactory.
     */
    private static EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("vaadin-lazyquerycontainer-test");
    /**
     * The JPA EntityManager.
     */
    private EntityManager entityManager;

    /**
     * Unit test setup.
     */
    @Before
    public void before() {
        entityManager = entityManagerFactory
                .createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from Task").executeUpdate();
        entityManager.getTransaction().commit();
    }

    /**
     * Unit test teardown.
     */
    @After
    public void after() {
    }

    /**
     * Test for entity container functionality.
     */
    @Test
    public final void testEntityContainer() {
        final EntityContainer<Task> entityContainer = new EntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, "taskId", true, true, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});


        final Task taskAlpha = entityContainer.addEntity();
        taskAlpha.setName("alpha");
        taskAlpha.setAssignee("assignee-alpha");
        taskAlpha.setReporter("reporter-alpha");

        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(0));

        final Task taskBeta = entityContainer.addEntity();
        taskBeta.setName("beta");
        taskBeta.setAssignee("assignee-beta");
        taskBeta.setReporter("reporter-beta");

        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha.getTaskId(),
                entityContainer.getEntity(0).getTaskId());
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(1));

        entityContainer.sort(new String[]{"name", "assignee"}, new boolean[]{false, false});

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha.getTaskId(),
                entityContainer.getEntity(1).getTaskId());
        Assert.assertEquals("Verify entity beta is same", taskBeta.getTaskId(),
                entityContainer.getEntity(0).getTaskId());

        /*final Map<String, Object> whereParameters = new HashMap<String, Object>();
        whereParameters.put("name", "alpha");
        whereParameters.put("assignee", "assignee-alpha");
        entityContainer.filter("e.name=:name and e.assignee=:assignee", whereParameters);*/

        entityContainer.addContainerFilter(
                new And(new Compare.Equal("name", "alpha"), new Compare.Equal("assignee", "assignee-alpha")));

        Assert.assertEquals("Verify entity alpha is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha.getTaskId(),
                entityContainer.getEntity(0).getTaskId());

        entityContainer.removeAllContainerFilters();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha.getTaskId(),
                entityContainer.getEntity(1).getTaskId());
        Assert.assertEquals("Verify entity beta is same", taskBeta.getTaskId(),
                entityContainer.getEntity(0).getTaskId());

        entityContainer.getItem(entityContainer.getIdByIndex(1)).getItemProperty("name").setValue("gamme");

        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha.getTaskId(),
                entityContainer.getEntity(0).getTaskId());
        Assert.assertEquals("Verify entity beta is same", taskBeta.getTaskId(),
                entityContainer.getEntity(1).getTaskId());

        final Task removedTask = entityContainer.removeEntity(0);
        Assert.assertEquals("Verify entity alpha was the removed entity", taskAlpha.getTaskId(),
                removedTask.getTaskId());

        entityContainer.commit();

        Assert.assertEquals("Verify entity beta is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity beta is same", taskBeta.getTaskId(),
                entityContainer.getEntity(0).getTaskId());

        final Item betaItemBeforeRefresh = entityContainer.getItem(entityContainer.getIdByIndex(0));
        Assert.assertNull("Verify new property does not exist before refresh.",
                betaItemBeforeRefresh.getItemProperty("description"));

        entityContainer.addContainerProperty("description", String.class, "");
        entityContainer.refresh();

        final Item betaItem = entityContainer.getItem(entityContainer.getIdByIndex(0));
        Assert.assertNotNull("Verify new property exists.", betaItem.getItemProperty("description"));
        Assert.assertEquals("Verify new property has correct default value.",
                "", betaItem.getItemProperty("description").getValue());

        entityContainer.removeAllItems();
        Assert.assertEquals("Verify container is empty after remove all.", 0, entityContainer.size());
    }

    /**
     * Test cache.
     */
    @Test
    public final void testCache() {
        final EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("vaadin-lazyquerycontainer-test");
        final EntityManager entityManager = entityManagerFactory
                .createEntityManager();
        final EntityContainer<Task> entityContainer = new EntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, "taskId", true, true, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});

        entityContainer.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX,
                Integer.class, new Integer(0));

        for (int i = 0; i < ITEM_COUNT_FOR_CACHE_TEST; i++) {
            entityContainer.addItem();
        }
        entityContainer.commit();
        for (int i = 0; i < ITEM_COUNT_FOR_CACHE_TEST; i++) {
            final Item item = entityContainer.getItem(entityContainer.getIdByIndex(i));
            Assert.assertEquals("Verify batch index", i / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
        for (int i = ITEM_COUNT_FOR_CACHE_TEST - 1; i >= QUERY_CACHE_SIZE; i--) {
            final Item item = entityContainer.getItem(entityContainer.getIdByIndex(i));
            Assert.assertEquals("Verify batch index", i / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
        for (int i = QUERY_CACHE_SIZE - 1; i >= 0; i--) {
            final Item item = entityContainer.getItem(entityContainer.getIdByIndex(i));
            Assert.assertEquals("Verify batch index", ITEM_COUNT_FOR_CACHE_TEST / ENTITY_CONTAINER_BATCH_SIZE
                    + (QUERY_CACHE_SIZE - 1 - i) / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
    }

    /**
     * Test query with background deleted entities after initialization.
     */
    @Test
    public final void testQueryWithBackgroudDeletedEntities() {
        final EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("vaadin-lazyquerycontainer-test");
        final EntityManager entityManager = entityManagerFactory
                .createEntityManager();
        final EntityContainer<Task> entityContainer = new EntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, "taskId", true, true, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});

        entityContainer.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS,
                QueryItemStatus.class, QueryItemStatus.None);

        entityContainer.addEntity();
        final Task taskTwo = entityContainer.addEntity();
        entityContainer.commit();

        Assert.assertEquals("Verify container size", 2, entityContainer.size());

        entityManager.getTransaction().begin();
        entityManager.remove(taskTwo);
        entityManager.getTransaction().commit();

        Assert.assertEquals("Verify container size", 2, entityContainer.size());

        Assert.assertEquals("Verify that entity is deleted.",
                QueryItemStatus.None,
                entityContainer.getItem(entityContainer.getIdByIndex(0)).getItemProperty(
                        LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());

        Assert.assertEquals("Verify that entity is deleted.",
                QueryItemStatus.Removed,
                entityContainer.getItem(entityContainer.getIdByIndex(1)).getItemProperty(
                        LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());

        entityContainer.commit();

        Assert.assertEquals("Verify container size", 1, entityContainer.size());
    }
}
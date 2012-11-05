/**
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.guice.repository.junit.general.threading;

import com.google.code.guice.repository.model.User;
import com.google.code.guice.repository.testing.repo.UserRepository;
import com.google.code.guice.repository.junit.RepoTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.*;

public class MultiThreadedRepositoryTest extends RepoTestBase {

    /*===========================================[ STATIC VARIABLES ]=============*/

    private static final int COUNT_PER_THREAD = 100;

    /*===========================================[ INSTANCE VARIABLES ]=========*/

    @Inject
    private UserRepository userRepository;

    /*===========================================[ CLASS METHODS ]==============*/

    @Before
    public void cleanup() {
        injector.getInstance(UserRepository.class).deleteAll();
    }

    @SuppressWarnings({"MagicNumber"})
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        Assert.assertEquals("Repo is not empty", 0, injector.getInstance(UserRepository.class).count());
        Collection<Callable<Object>> callables = new ArrayList<Callable<Object>>();
        final CountDownLatch generateLatch = new CountDownLatch(MAX_CONCURRENT_THREADS * COUNT_PER_THREAD);

        Runnable producer = new Runnable() {
            public void run() {
                UserRepository instance = injector.getInstance(UserRepository.class);
                for (int i = 0; i < COUNT_PER_THREAD; i++) {
                    instance.save(new User(UUID.randomUUID().toString(), UUID.randomUUID().toString(), i));
                    generateLatch.countDown();
                }
            }
        };
        for (int i = 0; i < MAX_CONCURRENT_THREADS; i++) {
            callables.add(Executors.callable(producer));
        }

        Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS).invokeAll(callables);
        generateLatch.await(30, TimeUnit.SECONDS);
        Assert.assertEquals("Invalid entities count", MAX_CONCURRENT_THREADS * COUNT_PER_THREAD, injector.getInstance(UserRepository.class).count());
    }

    @Test
    public void testMultipleThreadsRepoEntityManagerAccess() throws InterruptedException {
        Collection<Callable<Object>> callables = new ArrayList<Callable<Object>>();
        final Set<Integer> entityManagerHashes = new ConcurrentSkipListSet<Integer>();

        for (int i = 0; i < MAX_CONCURRENT_THREADS; i++) {
            callables.add(Executors.callable(new Runnable() {
                @Override
                public void run() {
                    EntityManager em1 = userRepository.getEntityManager();
                    EntityManager em2 = userRepository.getEntityManager();
                    int hashCode1 = em1.hashCode();
                    int hashCode2 = em2.hashCode();
                    logger.info("Thread: " + Thread.currentThread().getName() + ": hc: " + hashCode1);
                    Assert.assertEquals("Different Repo EM's per one thread!", hashCode1, hashCode2);

                    if (entityManagerHashes.isEmpty()) {
                        entityManagerHashes.add(hashCode1);
                    } else if (!entityManagerHashes.add(hashCode1)) {
                        logger.info("Error thread: " + Thread.currentThread().getName());
                        Assert.fail("Returned Repo EntityManager is not unique: " + hashCode1);
                    }
                }
            }));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS);
        List<Future<Object>> futures = executorService.invokeAll(callables);
        for (Future<Object> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }
}

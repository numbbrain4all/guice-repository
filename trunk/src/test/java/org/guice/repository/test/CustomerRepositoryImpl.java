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

package org.guice.repository.test;

import com.googlecode.guicerepository.SimpleBatchStoreJpaRepository;
import org.guice.repository.test.model.Customer;
import org.junit.Assert;

import javax.persistence.EntityManager;

public class CustomerRepositoryImpl extends SimpleBatchStoreJpaRepository<Customer,Long> implements CustomerRepository {

    /*===========================================[ CONSTRUCTORS ]===============*/

    public CustomerRepositoryImpl(Class<Customer> domainClass, EntityManager em) {
        super(domainClass, em);
    }
    /*===========================================[ CLASS METHODS ]==============*/

    public void sharedCustomMethod(Long customerID) {
        Assert.assertNotNull(customerID);
        System.out.println("customerID = " + customerID);
    }
}

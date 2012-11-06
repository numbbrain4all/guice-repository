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

package com.google.code.guice.repository.mapping;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

/**
 * Creates a Spring-context for spring-data-jpa.
 *
 * @author Alexey Krylov
 * @version 1.0.1
 * @since 07.11.2012
 */
@Singleton
public class ApplicationContextProvider implements Provider<ApplicationContext> {

    /*===========================================[ INSTANCE VARIABLES ]=========*/

    private ApplicationContext context;

    /*===========================================[ CONSTRUCTORS ]===============*/

    @Inject
    public void init(Provider<EntityManagerFactory> entityManagerFactoryProvider, TransactionInterceptor transactionInterceptor) {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBeanDefinition("entityManagerFactory",
                BeanDefinitionBuilder.genericBeanDefinition(EntityManagerFactoryHolderBean.class).
                        addConstructorArgValue(entityManagerFactoryProvider).getBeanDefinition());

        context.registerBeanDefinition("transactionManager",
                BeanDefinitionBuilder.genericBeanDefinition(JpaTransactionManager.class).getBeanDefinition());


        context.registerBeanDefinition("jpaRepositoryFactory",
                BeanDefinitionBuilder.genericBeanDefinition(JpaRepositoryFactoryBean.class).getBeanDefinition());

        JpaTransactionManager transactionManager = context.getBean(JpaTransactionManager.class);
        transactionInterceptor.setTransactionManager(transactionManager);
        this.context = context;
    }

    /*===========================================[ CLASS METHODS ]==============*/

    @Override
    public ApplicationContext get() {
        return context;
    }
}
/**
 * Copyright gradecak.com

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

package com.gradecak.alfresco.mvc.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.ClassUtils;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;

public class TransactionalAdvice implements MethodInterceptor {

  private ServiceRegistry serviceRegistry;

  public Object invoke(final MethodInvocation invocation) throws Throwable {
    Class<?> targetClass = invocation.getThis() != null ? invocation.getThis().getClass() : null;

    Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
    // If we are dealing with method with generic parameters, find the original
    // method.
    specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
    AlfrescoTransaction alfrescoTransaction = parseAnnotation(specificMethod);

    if (alfrescoTransaction != null) {
      RetryingTransactionCallback<Object> exampleWork = new RetryingTransactionCallback<Object>() {
        public Object execute() throws Throwable {
          return invocation.proceed();
        }
      };
      boolean readonly = alfrescoTransaction.readOnly();
      Propagation propagation = alfrescoTransaction.propagation();

      boolean requiresNew = Propagation.REQUIRES_NEW.equals(propagation);
      return serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(exampleWork, readonly, requiresNew);
    } else {
      return invocation.proceed();
    }

  }

  private AlfrescoTransaction parseAnnotation(AnnotatedElement ae) {
    AlfrescoTransaction ann = ae.getAnnotation(AlfrescoTransaction.class);
    if (ann == null) {
      for (Annotation metaAnn : ae.getAnnotations()) {
        ann = metaAnn.annotationType().getAnnotation(AlfrescoTransaction.class);
        if (ann != null) {
          break;
        }
      }
    }
    if (ann != null) {
      return parseAnnotation(ann);
    } else {
      return null;
    }
  }

  private AlfrescoTransaction parseAnnotation(AlfrescoTransaction ann) {
    // parse if needed something else
    return ann;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

}

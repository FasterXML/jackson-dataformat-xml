package com.fasterxml.jackson.dataformat.xml.testutil.failure;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.*;

public class JacksonTestFailureExpectedInterceptor
    implements InvocationInterceptor
{
    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
        throws Throwable
    {
        try {
            invocation.proceed();
        } catch (Throwable t) {
            // do-nothing, we do expect an exception
            return;
        }
        handleUnexpectePassingTest(invocationContext);
    }

    private void handleUnexpectePassingTest(ReflectiveInvocationContext<Method> invocationContext) {
        // Collect information we need
        Object targetClass = invocationContext.getTargetClass();
        Object testMethod = invocationContext.getExecutable().getName();
        //List<Object> arguments = invocationContext.getArguments();

        // Create message
        String message = String.format("Test method %s.%s() passed, but should have failed", targetClass, testMethod);

        // throw exception
        throw new JacksonTestShouldFailException(message);
    }

}

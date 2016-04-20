/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.jaas.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.security.internal.CarbonSecurityDataHolder;
import org.wso2.carbon.security.jaas.HTTPCallbackHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Carbon Security Utils.
 *
 * @since 1.0.0
 */
public class CarbonSecurityUtils {

    public static List<HTTPCallbackHandler> getCallbackHandlers(String supportedLoginModule) {

        List<HTTPCallbackHandler> callbackHandlers = new ArrayList<>();
        BundleContext bundleContext = CarbonSecurityDataHolder.getInstance().getBundleContext();

        try {
            Collection<ServiceReference<HTTPCallbackHandler>> serviceReferences = bundleContext.getServiceReferences
                    (HTTPCallbackHandler.class, "(&(" + HTTPCallbackHandler.SUPPORTED_LOGIN_MODULE + "=" +
                                                supportedLoginModule + ")(service.scope=prototype))");
            if (serviceReferences != null) {
                serviceReferences.forEach(
                        serviceReference -> callbackHandlers.add(bundleContext.getServiceObjects(serviceReference)
                                                                         .getService())
                );
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("Invalid syntax found while searching Callback handler " +
                                            supportedLoginModule);
        }
        return callbackHandlers;
    }

    private CarbonSecurityUtils() {

    }

}

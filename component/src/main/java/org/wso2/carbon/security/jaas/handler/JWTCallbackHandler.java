package org.wso2.carbon.security.jaas.handler;

import com.nimbusds.jwt.SignedJWT;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.security.jaas.CarbonCallback;
import org.wso2.carbon.security.jaas.HTTPCallbackHandler;
import org.wso2.carbon.security.jaas.util.CarbonSecurityConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.text.ParseException;

public class JWTCallbackHandler implements HTTPCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(JWTCallbackHandler.class);

    private HttpRequest httpRequest;

    private SignedJWT singedJWT;

    @Override
    public String getSupportedLoginModuleType() {
        return CarbonSecurityConstants.JWT_LOGIN_MODULE;
    }

    @Override
    public void setHTTPRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public boolean canHandle() {
        if (httpRequest != null) {

            HttpHeaders headers = httpRequest.headers();
            if (headers != null) {

                String authorizationHeader = headers.get(HttpHeaders.Names.AUTHORIZATION);
                if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                    if (authorizationHeader.trim().startsWith(CarbonSecurityConstants
                                                                      .HTTP_AUTHORIZATION_PREFIX_BEARER)) {

                        String jwt = authorizationHeader.trim().split(" ")[1];
                        if (jwt != null && !jwt.trim().isEmpty()) {
                            try {
                                singedJWT = SignedJWT.parse(jwt);
                                return true;
                            } catch (ParseException e) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Error while parsing the JWT token.", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        if(callbacks != null && callbacks.length > 0) {
            if (callbacks[0] instanceof CarbonCallback) {
                ((CarbonCallback) callbacks[0]).setContent(singedJWT);
            }
        }

    }
}

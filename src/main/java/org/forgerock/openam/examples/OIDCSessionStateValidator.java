/*
 * Copyright © 2016 ForgeRock, AS.
 *
 * This is unsupported code made available by ForgeRock for community development subject to the license detailed below. 
 * The code is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. 
 *
 * ForgeRock does not warrant or guarantee the individual success developers may have in implementing the code on their 
 * development platforms or in production configurations.
 *
 * ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness 
 * or completeness of any data or information relating to the alpha release of unsupported code. ForgeRock disclaims all 
 * warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related 
 * to the code, or any service or software related thereto.
 *
 * ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any 
 * action taken by you or others related to the code.
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License (the License). 
 * You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the License at https://forgerock.org/cddlv1-0/. See the License for the specific language governing 
 * permission and limitations under the License.
 *
 * Portions Copyrighted 2016 Charan Mann
 *
 * OIDCSessionStatePlugin: Created by Charan Mann on 12/20/16 , 11:59 AM.
 */

package org.forgerock.openam.examples;

import com.iplanet.dpro.session.Session;
import com.iplanet.dpro.session.SessionException;
import com.iplanet.dpro.session.SessionID;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenID;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.oauth2.core.*;
import org.forgerock.oauth2.core.exceptions.InvalidClientException;
import org.forgerock.oauth2.core.exceptions.NotFoundException;
import org.forgerock.oauth2.core.exceptions.ServerException;
import org.forgerock.openam.oauth2.IdentityManager;
import org.forgerock.openam.oauth2.OpenAMScopeValidator;
import org.forgerock.openam.scripting.ScriptEvaluator;
import org.forgerock.openam.scripting.service.ScriptingServiceFactory;
import org.forgerock.openam.session.SessionCache;
import org.forgerock.openam.session.SessionConstants;
import org.forgerock.openam.utils.OpenAMSettings;
import org.forgerock.openidconnect.OpenIDTokenIssuer;
import org.forgerock.openidconnect.OpenIdConnectClientRegistrationStore;
import org.forgerock.openidconnect.OpenIdConnectToken;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

import static org.forgerock.openam.oauth2.OAuth2Constants.Params.OPENID;
import static org.forgerock.openam.scripting.ScriptConstants.OIDC_CLAIMS_NAME;

/**
 * Custom OpenAM Scope validator: This sets "session_state" in token response for OAuth Authorization Code and implicit flows.
 */
@Singleton
public class OIDCSessionStateValidator extends OpenAMScopeValidator {

    private final Debug logger = Debug.getInstance("OIDCAuthzPartyValidator");

    private final ResourceOwnerSessionValidator resourceOwnerSessionValidator;

    private final Map<Integer, String> sessionStateMap;


    /**
     * Constructs a new OIDCSessionStateValidator.
     *
     * @param identityManager         An instance of the IdentityManager.
     * @param openIDTokenIssuer       An instance of the OpenIDTokenIssuer.
     * @param providerSettingsFactory An instance of the CTSPersistentStore.
     * @param openAMSettings          An instance of the OpenAMSettings.
     * @param scriptEvaluator         An instance of the OIDC Claims ScriptEvaluator.
     * @param clientRegistrationStore An instance of the OpenIdConnectClientRegistrationStore.
     * @param scriptingServiceFactory An instance of the ScriptingServiceFactory.
     */
    @Inject
    public OIDCSessionStateValidator(IdentityManager identityManager, OpenIDTokenIssuer openIDTokenIssuer,
                                     OAuth2ProviderSettingsFactory providerSettingsFactory, OpenAMSettings openAMSettings,
                                     @Named(OIDC_CLAIMS_NAME) ScriptEvaluator scriptEvaluator,
                                     OpenIdConnectClientRegistrationStore clientRegistrationStore,
                                     ScriptingServiceFactory scriptingServiceFactory, ResourceOwnerSessionValidator resourceOwnerSessionValidator) {
        super(identityManager, openIDTokenIssuer, providerSettingsFactory, openAMSettings,
                scriptEvaluator, clientRegistrationStore, scriptingServiceFactory);
        this.resourceOwnerSessionValidator = resourceOwnerSessionValidator;

        // Static map of Session state values with corresponding flags
        Map<Integer, String> sessionSMap = new HashMap<Integer, String>();
        sessionSMap.put(SessionConstants.INVALID, "INVALID");
        sessionSMap.put(SessionConstants.VALID, "VALID");
        sessionSMap.put(SessionConstants.INACTIVE, "INACTIVE");
        sessionSMap.put(SessionConstants.DESTROYED, "DESTROYED");
        this.sessionStateMap = Collections.unmodifiableMap(sessionSMap);
    }

    @Override
    public void additionalDataToReturnFromTokenEndpoint(AccessToken accessToken, OAuth2Request request) throws ServerException, InvalidClientException, NotFoundException {
        super.additionalDataToReturnFromTokenEndpoint(accessToken, request);

        final Set<String> scope = accessToken.getScope();
        if (scope != null && scope.contains(OPENID)) {
            SSOToken ssoToken = resourceOwnerSessionValidator.getResourceOwnerSession(request);

            // In case no token is found
            if (null == ssoToken) {
                accessToken.addExtraData("session_state", sessionStateMap.get(SessionConstants.DESTROYED));
            } else {
                SSOTokenID ssoTokenID = ssoToken.getTokenID();
                try {
                    // Get the session from session cache; this session object has session state
                    Session session = SessionCache.getInstance().getSession(new SessionID(ssoTokenID.toString()));
                    accessToken.addExtraData("session_state", sessionStateMap.get(session.getState(false)));
                } catch (SessionException e) {
                    accessToken.addExtraData("session_state", sessionStateMap.get(SessionConstants.DESTROYED));
                }
            }

        }
    }

    /**
     * Sets "session_state" is token response, Note that this will only work for authorization code and implicit OAuth flows
     */
    public Map<String, String> additionalDataToReturnFromAuthorizeEndpoint(Map<String, Token> tokens,
                                                                           OAuth2Request request) {
        Map<String, String> additionalData = new HashMap<>();

        Iterator<Map.Entry<String, Token>> it = tokens.entrySet().iterator();
        while (it.hasNext()) {
            Token token = it.next().getValue();
            if (token instanceof OpenIdConnectToken) {
                OpenIdConnectToken openIdConnectToken = (OpenIdConnectToken) token;
                SSOToken ssoToken = resourceOwnerSessionValidator.getResourceOwnerSession(request);

                // In case no token is found
                if (null == ssoToken) {
                    additionalData.put("session_state", sessionStateMap.get(SessionConstants.DESTROYED));
                } else {
                    SSOTokenID ssoTokenID = ssoToken.getTokenID();
                    try {
                        // Get the session from session cache; this session object has session state
                        Session session = SessionCache.getInstance().getSession(new SessionID(ssoTokenID.toString()));
                        additionalData.put("session_state", sessionStateMap.get(session.getState(false)));
                    } catch (SessionException e) {
                        additionalData.put("session_state", sessionStateMap.get(SessionConstants.DESTROYED));
                    }
                }
            }
        }

        return additionalData;
    }
}

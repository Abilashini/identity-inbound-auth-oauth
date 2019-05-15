/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.oauth.dao.impl;

import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth.dao.OAuthConsumerAppDAO;
import org.wso2.carbon.identity.oauth.exception.OAuthConsumerAppException;

/**
 * Cache backed implementation of {@link OAuthConsumerAppDAO}. This handles the caching layer of
 * {@link OAuthConsumerAppDAOImpl}.
 */
public class CacheBackedOAuthConsumerAppDAO implements OAuthConsumerAppDAO {

    private OAuthConsumerAppDAO oAuthConsumerAppDAO;

    public CacheBackedOAuthConsumerAppDAO(OAuthConsumerAppDAO oAuthConsumerAppDAO) {

        this.oAuthConsumerAppDAO = oAuthConsumerAppDAO;
    }

    @Override
    public void addOAuthConsumerApplication(OAuthAppDO consumerAppDO) throws OAuthConsumerAppException {

        oAuthConsumerAppDAO.addOAuthConsumerApplication(consumerAppDO);
        OAuthConsumerAppCache.getInstance().addToCache(consumerAppDO.getOauthConsumerKey(), consumerAppDO);
    }

    @Override
    public OAuthAppDO getAppInformationByConsumerKey(String consumerKey) throws OAuthConsumerAppException {

        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);

        if (oAuthAppDO == null) {
            oAuthAppDO = oAuthConsumerAppDAO.getAppInformationByConsumerKey(consumerKey);
            if (oAuthAppDO != null) {
                OAuthConsumerAppCache.getInstance().addToCache(consumerKey, oAuthAppDO);
            }
        }

        return oAuthAppDO;
    }

    @Override
    public OAuthAppDO getAppInformationByAppName(String appName) throws OAuthConsumerAppException {

        OAuthAppDO oAuthAppDO = oAuthConsumerAppDAO.getAppInformationByAppName(appName);
        // since we have persisted the OAuth2 app data against the appname, we are storing it in the cache against
        // the consumer key for future references
        if (OAuthConsumerAppCache.getInstance()
                .getValueFromCache(oAuthAppDO.getOauthConsumerKey()) == null) {
            OAuthConsumerAppCache.getInstance().addToCache(oAuthAppDO.getOauthConsumerKey(), oAuthAppDO);
        }
        return oAuthAppDO;
    }

    @Override
    public OAuthAppDO[] getOAuthConsumerAppsOfUser(String username,
                                                   int tenantId)
            throws OAuthConsumerAppException {

        return oAuthConsumerAppDAO.getOAuthConsumerAppsOfUser(username, tenantId);
    }

    @Override
    public String getOAuthConsumerSecret(String consumerKey) throws OAuthConsumerAppException {

        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);
        if (oAuthAppDO != null) {
            return oAuthAppDO.getOauthConsumerSecret();
        }
        return oAuthConsumerAppDAO.getOAuthConsumerSecret(consumerKey);
    }

    @Override
    public String getConsumerApplicationOwnerName(String consumerKey) throws OAuthConsumerAppException {

        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);
        if (oAuthAppDO != null) {
            return oAuthAppDO.getAppOwner().getUserName();
        }
        return oAuthConsumerAppDAO.getConsumerApplicationOwnerName(consumerKey);
    }

    @Override
    public void updateOAuthConsumerApplication(OAuthAppDO oauthAppDO) throws OAuthConsumerAppException {

        oAuthConsumerAppDAO.updateOAuthConsumerApplication(oauthAppDO);
        OAuthConsumerAppCache.getInstance().addToCache(oauthAppDO.getOauthConsumerKey(), oauthAppDO);
    }

    @Override
    public void updateOAuthConsumerAppName(String consumerKey,
                                           String appName)
            throws OAuthConsumerAppException {

        oAuthConsumerAppDAO.updateOAuthConsumerAppName(consumerKey, appName);
        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);
        if (oAuthAppDO != null) {
            oAuthAppDO.setApplicationName(appName);
            OAuthConsumerAppCache.getInstance().addToCache(consumerKey, oAuthAppDO);
        }
    }

    @Override
    public void updateOAuthConsumerSecret(String consumerKey, String consumerSecret) throws OAuthConsumerAppException {

        oAuthConsumerAppDAO.updateOAuthConsumerSecret(consumerKey, consumerSecret);
        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);
        if (oAuthAppDO != null) {
            oAuthAppDO.setOauthConsumerSecret(consumerSecret);
            OAuthConsumerAppCache.getInstance().addToCache(consumerKey, oAuthAppDO);
        }
    }

    @Override
    public void updateOAuthConsumerAppState(String consumerKey, String state) throws OAuthConsumerAppException {

        oAuthConsumerAppDAO.updateOAuthConsumerAppState(consumerKey, state);
        OAuthAppDO oAuthAppDO = OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey);
        if (oAuthAppDO != null) {
            oAuthAppDO.setState(state);
            OAuthConsumerAppCache.getInstance().addToCache(consumerKey, oAuthAppDO);
        }
    }

    @Override
    public void removeOAuthConsumerApplication(String consumerKey) throws OAuthConsumerAppException {

        if (OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey) != null) {
            OAuthConsumerAppCache.getInstance().clearCacheEntry(consumerKey);
        }
        oAuthConsumerAppDAO.removeOAuthConsumerApplication(consumerKey);
    }

    @Override
    public void removeOIDCProperties(String consumerKey,
                                     String tenantDomain)
            throws OAuthConsumerAppException {

        if (OAuthConsumerAppCache.getInstance().getValueFromCache(consumerKey) != null) {
            OAuthConsumerAppCache.getInstance().clearCacheEntry(consumerKey);
        }
        oAuthConsumerAppDAO.removeOIDCProperties(consumerKey, tenantDomain);
    }
}

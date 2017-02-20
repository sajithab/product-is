/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api.unit.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.ProfileMgtClientService;
import org.wso2.is.portal.user.client.api.bean.ProfileUIEntry;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ProfileMgtClientServiceTest {

    private static final String DEFAULT = "default";
    private static final String PROFILE_INVALID = "invalid";
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceTest.class);
    private static List<UUFUser> users = new ArrayList<>();

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private ProfileMgtClientService profileMgtClientService;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = UserPortalOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(UserPortalOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "getProfile")
    public void testGetProfileNames() throws UserPortalUIException {
        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        Set<String> profileNames = profileMgtClientService.getProfileNames();
        Assert.assertNotNull(profileNames, "Failed to retrieve the profile names.");
    }

    @Test(groups = "getProfile")
    public void testGetProfile() throws UserPortalUIException {
        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        ProfileEntry profileEntry = profileMgtClientService.getProfile(DEFAULT);
        Assert.assertNotNull(profileEntry, "Failed to retrieve the default profile.");
    }

    @Test(groups = "getProfile")
    public void testGetProfileForInvalidProfileName() {
        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        ProfileEntry profileEntry = null;
        try {
            profileEntry = profileMgtClientService.getProfile(PROFILE_INVALID);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Get profile failed with invalid profile name.");
            return;
        }
        Assert.assertNull(profileEntry, "Test Failed. Get profile successfully with invalid profile name.");
    }

    @Test(groups = "getProfile")
    public void testGetProfileEntries() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user1");
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = identityStoreClientService.addUser(userClaims, credentials);

        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        users.add(user);

        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        List<ProfileUIEntry> profileEntries = profileMgtClientService.getProfileEntries(DEFAULT, user.getUserId());
        Assert.assertNotNull(profileEntries, "Failed to retrieve the profile entries.");
    }

    @Test(groups = "getProfile")
    public void testGetProfileEntriesForInvalidUserId() {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        List<ProfileUIEntry> profileEntries = null;
        try {
            profileEntries = profileMgtClientService.getProfileEntries(DEFAULT, "9088383");
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Get profile entries failed with invalid user id.");
            return;
        }
        Assert.assertNull(profileEntries, "Test Failed. successfully get profile entries with invalid user id.");
    }

    @Test(groups = "getProfile")
    public void testGetProfileEntriesForInvalidProfile() {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        ProfileMgtClientService profileMgtClientService =
                bundleContext.getService(bundleContext.getServiceReference(ProfileMgtClientService.class));
        Assert.assertNotNull(profileMgtClientService, "Failed to get ProfileMgtClientService instance");

        List<ProfileUIEntry> profileEntries = null;
        try {
            profileEntries = profileMgtClientService.getProfileEntries(PROFILE_INVALID, users.get(0).getUserId());
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Get profile entries failed with invalid profile name.");
            return;
        }
        Assert.assertNull(profileEntries, "Test Failed. successfully get profile entries with invalid profile name.");
    }

}

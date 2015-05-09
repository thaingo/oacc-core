/*
 * Copyright 2009-2015, Acciente LLC
 *
 * Acciente LLC licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.acciente.oacc;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestAccessControl_revokeResourceCreatePermissions extends TestAccessControlBase {
   @Test
   public void revokeResourceCreatePermissions_validAsSystemResource() {
      authenticateSystemResource();
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_impersonate
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(ResourcePermissions.IMPERSONATE), false);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true), true);
      final ResourceCreatePermission createPerm_resetPwd
            = ResourceCreatePermissions.getInstance(ResourcePermissions
                                                          .getInstance(ResourcePermissions.RESET_CREDENTIALS, true));

      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(true, false);
      final Resource accessorResource = generateUnauthenticatableResource();
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // set up accessor
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create_withGrant,
                    createPerm_impersonate,
                    createPerm_inherit_withGrant,
                    createPerm_resetPwd);

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           createPerm_create_withGrant,
                                                           createPerm_impersonate,
                                                           createPerm_inherit_withGrant,
                                                           createPerm_resetPwd);

      final Set<ResourceCreatePermission> resourceCreatePermissions_post
            = accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                         resourceClassName,
                                                                         domainName);
      assertThat(resourceCreatePermissions_post.isEmpty(), is(true));

      // test set-based version
      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(resourceCreatePermissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName,
                                                           domainName,
                                                           setOf(createPerm_create_withGrant,
                                                                 createPerm_impersonate,
                                                                 createPerm_inherit_withGrant,
                                                                 createPerm_resetPwd));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName).isEmpty(),
                 is(true));
   }

   @Test
   public void revokeResourceCreatePermissions_validAsAuthorized() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, false),
                    createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions = setOf(createPerm_create_withGrant,
                                                               createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           ResourceCreatePermissions
                                                                 .getInstance(ResourceCreatePermissions.CREATE, false),
                                                           createPerm_inherit_withGrant);

      final Set<ResourceCreatePermission> resourceCreatePermissions_post
            = accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                         resourceClassName,
                                                                         domainName);
      assertThat(resourceCreatePermissions_post.isEmpty(), is(true));

      // test set-based version
      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName,
                                                           domainName,
                                                           setOf(ResourceCreatePermissions
                                                                       .getInstance(ResourceCreatePermissions.CREATE,
                                                                                    false),
                                                                 createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName).isEmpty(),
                 is(true));
   }

   @Test
   public void revokeResourceCreatePermissions_validWithDefaultSessionDomain() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up the accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, false),
                    createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions using the implicit session domain and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           ResourceCreatePermissions
                                                                 .getInstance(ResourceCreatePermissions.CREATE, false),
                                                           createPerm_inherit_withGrant);

      final Set<ResourceCreatePermission> resourceCreatePermissions_post
            = accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                         resourceClassName,
                                                                         domainName);
      assertThat(resourceCreatePermissions_post.isEmpty(), is(true));

      // test set-based version
      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName,
                                                           setOf(ResourceCreatePermissions
                                                                       .getInstance(ResourceCreatePermissions.CREATE,
                                                                                    false),
                                                                 createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName).isEmpty(),
                 is(true));
   }

   @Test
   public void revokeResourceCreatePermissions_ungrantedPermissions_shouldSucceedAsAuthorized() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions = setOf(createPerm_create_withGrant,
                                                               createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           ResourceCreatePermissions
                                                                 .getInstance(ResourceCreatePermissions.CREATE, false),
                                                           createPerm_inherit_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName).isEmpty(),
                 is(true));

      // revoke create permissions with implicit domain and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           ResourceCreatePermissions
                                                                 .getInstance(ResourceCreatePermissions.CREATE, false),
                                                           createPerm_inherit_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName).isEmpty(),
                 is(true));

      // test set-based version
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           setOf(ResourceCreatePermissions
                                                                       .getInstance(ResourceCreatePermissions.CREATE,
                                                                                    false),
                                                                 createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName).isEmpty(),
                 is(true));

      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           setOf(ResourceCreatePermissions
                                                                       .getInstance(ResourceCreatePermissions.CREATE,
                                                                                    false),
                                                                 createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName).isEmpty(),
                 is(true));
   }

   @Test
   public void revokeResourceCreatePermissions_ungrantedPermissionsWithAndWithoutGrant_shouldFail() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission grantedCreatePerm
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);
      final ResourceCreatePermission ungrantedCreatePerm
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(ResourcePermissions.IMPERSONATE));

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(true, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions = setOf(createPerm_create_withGrant,
                                                               grantedCreatePerm);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_create_withGrant,
                                                              grantedCreatePerm,
                                                              ungrantedCreatePerm);
         fail("revoking permissions without grant-authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantedCreatePerm.toString().toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(createPerm_create_withGrant.toString().toLowerCase())));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantedCreatePerm.toString().toLowerCase())));
      }

      // revoke create permissions with implicit domain and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_create_withGrant,
                                                              grantedCreatePerm,
                                                              ungrantedCreatePerm);
         fail("revoking permissions without grant-authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantedCreatePerm.toString().toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(createPerm_create_withGrant.toString().toLowerCase())));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantedCreatePerm.toString().toLowerCase())));
      }

      // test set-based versions
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_create_withGrant,
                                                                    grantedCreatePerm,
                                                                    ungrantedCreatePerm));
         fail("revoking permissions without grant-authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantedCreatePerm.toString().toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(createPerm_create_withGrant.toString().toLowerCase())));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantedCreatePerm.toString().toLowerCase())));
      }

      // revoke create permissions with implicit domain and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_create_withGrant,
                                                                    grantedCreatePerm,
                                                                    ungrantedCreatePerm));
         fail("revoking permissions without grant-authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantedCreatePerm.toString().toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(createPerm_create_withGrant.toString().toLowerCase())));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantedCreatePerm.toString().toLowerCase())));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_reRevokePermissions() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions = setOf(createPerm_create_withGrant,
                                                               createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           createPerm_create_withGrant,
                                                           createPerm_inherit_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName).isEmpty(),
                 is(true));

      // revoke create permissions again and verify that nothing changed
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           createPerm_create_withGrant,
                                                           createPerm_inherit_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName).isEmpty(),
                 is(true));

      // revoke create permissions again via set-based version and verify that nothing changed
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           setOf(createPerm_create_withGrant,
                                                                 createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName).isEmpty(),
                 is(true));
   }

   @Test
   public void revokeResourceCreatePermissions_revokeSubsetOfPermissions() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourcePermission resourcePermission_impersonate
            = ResourcePermissions.getInstance(ResourcePermissions.IMPERSONATE);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);
      final ResourceCreatePermission createPerm_impersonate_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_impersonate, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(true, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create, createPerm_inherit_withGrant, createPerm_impersonate_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant, createPerm_impersonate_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           domainName,
                                                           createPerm_inherit_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create, createPerm_impersonate_withGrant)));

      // revoke create permissions with implicit domain and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName,
                                                           createPerm_impersonate_withGrant);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(setOf(createPerm_create)));

      // test set-based versions
      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2,
                                                        resourceClassName,
                                                        domainName,
                                                        resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName,
                                                           domainName,
                                                           setOf(createPerm_inherit_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(setOf(createPerm_create, createPerm_impersonate_withGrant)));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName,
                                                           setOf(createPerm_impersonate_withGrant));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(setOf(createPerm_create)));
   }

   @Test
   public void revokeResourceCreatePermissions_revokeSubsetWithCreate_shouldFail() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourcePermission resourcePermission_impersonate
            = ResourcePermissions.getInstance(ResourcePermissions.IMPERSONATE);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);
      final ResourceCreatePermission createPerm_impersonate_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_impersonate, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(true, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create, createPerm_inherit_withGrant, createPerm_impersonate_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant, createPerm_impersonate_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_inherit_withGrant,
                                                              createPerm_create);
         fail("revoking subset of permissions including *CREATE should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(
               "subset of resource create permissions that includes the *create"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_inherit_withGrant,
                                                                    createPerm_create));
         fail("revoking subset of permissions including *CREATE should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(
               "subset of resource create permissions that includes the *create"));
      }

      // revoke create permissions with implicit domain and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_inherit_withGrant,
                                                              createPerm_create);
         fail("revoking subset of permissions including *CREATE should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(
               "subset of resource create permissions that includes the *create"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_inherit_withGrant,
                                                                    createPerm_create));
         fail("revoking subset of permissions including *CREATE should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(
               "subset of resource create permissions that includes the *create"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_withUnauthorizedPermissionsGrantedElsewhere_shouldFail() {
      authenticateSystemResource();
      final String resourceClassName = generateResourceClass(false, false);
      final String grantablePermissionName = generateResourceClassPermission(resourceClassName);
      final String ungrantablePermissionName = ResourcePermissions.INHERIT;
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final Resource accessorResource = generateUnauthenticatableResource();
      final String domainName = generateDomain();

      // setup accessor permissions
      Set<ResourceCreatePermission> accessorPermissions_pre
            = setOf(ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantablePermissionName)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(ungrantablePermissionName)));

      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        accessorPermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(accessorPermissions_pre));

      // setup grantor permissions
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantablePermissionName), true));
      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource, resourceClassName, domainName),
                 is(grantorPermissions));

      final String grantorDomain = accessControlContext.getDomainNameByResource(grantorResource);
      accessControlContext.setResourceCreatePermissions(grantorResource, resourceClassName, grantorDomain, grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource, resourceClassName, grantorDomain),
                 is(grantorPermissions));

      // authenticate grantor resource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke permissions as grantor and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourceCreatePermissions.CREATE),
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourcePermissions.getInstance(grantablePermissionName)),
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourcePermissions.getInstance(ungrantablePermissionName)));
         fail("revoking existing resource create permission granted elsewhere without authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantablePermissionName.toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantablePermissionName)));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(ResourceCreatePermissions
                                                                          .getInstance(ResourceCreatePermissions.CREATE),
                                                                    ResourceCreatePermissions
                                                                          .getInstance(ResourcePermissions.getInstance(
                                                                                grantablePermissionName)),
                                                                    ResourceCreatePermissions
                                                                          .getInstance(ResourcePermissions.getInstance(
                                                                                ungrantablePermissionName))));
         fail("revoking existing resource create permission granted elsewhere without authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantablePermissionName.toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantablePermissionName)));
      }

      // revoke create permissions with implicit domain and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourceCreatePermissions.CREATE),
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourcePermissions.getInstance(
                                                                          grantablePermissionName)),
                                                              ResourceCreatePermissions
                                                                    .getInstance(ResourcePermissions.getInstance(
                                                                          ungrantablePermissionName)));
         fail("revoking existing resource create permission granted elsewhere without authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantablePermissionName.toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantablePermissionName)));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(ResourceCreatePermissions
                                                                          .getInstance(ResourceCreatePermissions.CREATE),
                                                                    ResourceCreatePermissions
                                                                          .getInstance(ResourcePermissions.getInstance(
                                                                                grantablePermissionName)),
                                                                    ResourceCreatePermissions
                                                                          .getInstance(ResourcePermissions.getInstance(
                                                                                ungrantablePermissionName))));
         fail("revoking existing resource create permission granted elsewhere without authorization should have failed");
      }
      catch (NotAuthorizedException e) {
         assertThat(e.getMessage().toLowerCase(), containsString(ungrantablePermissionName.toLowerCase()));
         assertThat(e.getMessage().toLowerCase(), not(containsString(grantablePermissionName)));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_ungrantedImpersonatePermissionOnUnauthenticatables_shouldFail() {
      final ResourcePermission resourcePermission_impersonate
            = ResourcePermissions.getInstance(ResourcePermissions.IMPERSONATE);
      final ResourceCreatePermission createPerm_impersonate_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_impersonate, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke *IMPERSONATE system permission
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_impersonate_withGrant);
         fail("revoking impersonate create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_impersonate_withGrant));
         fail("revoking impersonate create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }

      // attempt to revoke *IMPERSONATE system permission with implicit domain
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_impersonate_withGrant);
         fail("revoking impersonate create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_impersonate_withGrant));
         fail("revoking impersonate create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_ungrantedResetCredentialsPermissionOnUnauthenticatables_shouldFail() {
      final ResourcePermission resourcePermission_reset
            = ResourcePermissions.getInstance(ResourcePermissions.RESET_CREDENTIALS);
      final ResourceCreatePermission createPerm_reset_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_reset, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke *RESET-CREDENTIALS system permission
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_reset_withGrant);
         fail("revoking reset-credentials create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_reset_withGrant));
         fail("revoking reset-credentials create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }

      // attempt to revoke *RESET-CREDENTIALS with implicit domain
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_reset_withGrant);
         fail("revoking reset-credentials create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_reset_withGrant));
         fail("revoking reset-credentials create permission for unauthenticatable resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not valid for unauthenticatable resource"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_identicalPermissions_shouldSucceedAsAuthorized() {
      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final String grantedPermissionName1 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName2 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName3 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName4 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName5 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName6 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName7 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName8 = generateResourceClassPermission(resourceClassName);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);

      // set up the accessorResource
      final Resource accessorResource = generateUnauthenticatableResource();
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true));

      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2,
                                                        resourceClassName,
                                                        domainName,
                                                        resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true));

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource, resourceClassName, domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             domainName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName1)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName2), true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true))));

      // revoke create permissions with implicit domain and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName6), true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create)));

      // test set-based versions
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             domainName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName1)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName2), true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true))));

      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName6), true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName7, true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName8, true), true)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create)));
   }

   @Test
   public void revokeResourceCreatePermissions_lesserPermissions_shouldSucceedAsAuthorized() {
      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final String grantedPermissionName1 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName2 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName3 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName4 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName5 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName6 = generateResourceClassPermission(resourceClassName);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);

      // set up the accessorResource
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6)));

      final Resource accessorResource = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true));

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource, resourceClassName, domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             domainName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName1), true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6)))));

      // revoke create permissions with implicit domain and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName4), true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create)));

      // test set-based versions
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             domainName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName1), true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5)),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6)))));

      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName4), true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create)));
   }

   @Test
   public void revokeResourceCreatePermissions_greaterPermissions_shouldSucceedAsAuthorized() {
      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final String grantedPermissionName1 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName2 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName3 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName4 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName5 = generateResourceClassPermission(resourceClassName);
      final String grantedPermissionName6 = generateResourceClassPermission(resourceClassName);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);

      // set up the accessorResource
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true));

      final Resource accessorResource = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2, resourceClassName, domainName, resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant,
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName1, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName2, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName3, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                    ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true));

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             domainName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName1),
                                                                true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName2,
                                                                                                true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions
                                                                      .getInstance(grantedPermissionName3)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true))));

      // revoke create permissions with implicit domain and verify
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource,
                                             resourceClassName,
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName4),
                                                                true),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions.getInstance(grantedPermissionName5,
                                                                                                true)),
                                             ResourceCreatePermissions
                                                   .getInstance(ResourcePermissions
                                                                      .getInstance(grantedPermissionName6)));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create)));

      // test set-based versions
      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             domainName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName1),
                                                                      true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName2,
                                                                                                      true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName3))));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create,
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName4, true), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName5, true), true),
                          ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(grantedPermissionName6, true), true))));

      accessControlContext
            .revokeResourceCreatePermissions(accessorResource2,
                                             resourceClassName,
                                             setOf(ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName4),
                                                                      true),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName5,
                                                                                                      true)),
                                                   ResourceCreatePermissions
                                                         .getInstance(ResourcePermissions.getInstance(grantedPermissionName6))));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create)));
   }

   @Test
   public void revokeResourceCreatePermissions_duplicateIdenticalPermissions_shouldFail() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(true, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource, resourceClassName, domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_inherit_withGrant,
                                                              createPerm_inherit_withGrant);
         fail("revoking resource create permissions for duplicate (identical) permissions, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate element"));
      }

      // revoke duplicate create permissions for implicit domain
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_inherit_withGrant,
                                                              createPerm_inherit_withGrant);
         fail("revoking resource create permissions for duplicate (identical) permissions, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate element"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_duplicatePermissions_shouldFail() {
      final ResourcePermission resourcePermission_inherit_withGrant
            = ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant);
      final ResourceCreatePermission createPerm_inherit_withGrant
            = ResourceCreatePermissions.getInstance(resourcePermission_inherit_withGrant, true);

      authenticateSystemResource();
      final char[] password = generateUniquePassword();
      final Resource grantorResource = generateAuthenticatableResource(password);
      final String domainName = accessControlContext.getDomainNameByResource(grantorResource);
      final String resourceClassName = generateResourceClass(false, false);
      final Resource accessorResource = generateUnauthenticatableResource();

      // set up accessorResource
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));
      Set<ResourceCreatePermission> resourceCreatePermissions_pre
            = setOf(createPerm_create, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        resourceCreatePermissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(resourceCreatePermissions_pre));

      // set up grantor
      Set<ResourceCreatePermission> grantorPermissions
            = setOf(createPerm_create_withGrant, createPerm_inherit_withGrant);

      accessControlContext.setResourceCreatePermissions(grantorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        grantorPermissions);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(grantorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(grantorPermissions));

      // now authenticate as the granterResource
      accessControlContext.authenticate(grantorResource, PasswordCredentials.newInstance(password));

      // revoke create permissions and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_inherit,
                                                              createPerm_inherit_withGrant);
         fail("revoking create-permissions that include the same permission, but with different grant-options, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate permission"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_inherit,
                                                                    createPerm_inherit_withGrant));
         fail("revoking create-permissions that include the same permission, but with different grant-options, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate permission"));
      }

      // revoke create permissions with implicit domain and verify
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_inherit,
                                                              createPerm_inherit_withGrant);
         fail("revoking create-permissions that include the same permission, but with different grant-options, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate permission"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_inherit,
                                                                    createPerm_inherit_withGrant));
         fail("revoking create-permissions that include the same permission, but with different grant-options, should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("duplicate permission"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_whitespaceConsistent() {
      authenticateSystemResource();
      final String domainName = generateDomain();
      final String domainName_whitespaced = " " + domainName + "\t";
      final String resourceClassName = generateResourceClass(false, false);
      final String resourceClassName_whitespaced = " " + resourceClassName + "\t";
      final String permissionName = generateResourceClassPermission(resourceClassName);
      final ResourceCreatePermission createPerm_create
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, false);
      final ResourceCreatePermission createPerm_customPerm
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(permissionName), false);
      final ResourceCreatePermission createPerm_customPerm_ws
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(permissionName + " \t"), false);

      // set up accessor
      final Set<ResourceCreatePermission> permissions_pre = setOf(createPerm_create, createPerm_customPerm);
      final Resource accessorResource = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        domainName,
                                                        permissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(permissions_pre));

      final Resource accessorResource2 = generateUnauthenticatableResource();
      accessControlContext.setResourceCreatePermissions(accessorResource2,
                                                        resourceClassName,
                                                        domainName,
                                                        permissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName,
                                                                            domainName),
                 is(permissions_pre));

      // revoke create permissions and verify
      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName_whitespaced,
                                                           domainName_whitespaced,
                                                           createPerm_customPerm_ws);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource, resourceClassName, domainName),
                 is(setOf(createPerm_create)));

      // revoke create permissions with implicit domain and verify
      accessControlContext.setResourceCreatePermissions(accessorResource,
                                                        resourceClassName,
                                                        permissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName),
                 is(permissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                           resourceClassName_whitespaced,
                                                           createPerm_customPerm_ws);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource,
                                                                            resourceClassName,
                                                                            domainName),
                 is(setOf(createPerm_create)));

      // test set-based versions
      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName_whitespaced,
                                                           domainName_whitespaced,
                                                           setOf(createPerm_customPerm_ws));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create)));

      accessControlContext.setResourceCreatePermissions(accessorResource2,
                                                        resourceClassName,
                                                        permissions_pre);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2,
                                                                            resourceClassName),
                 is(permissions_pre));

      accessControlContext.revokeResourceCreatePermissions(accessorResource2,
                                                           resourceClassName_whitespaced,
                                                           setOf(createPerm_customPerm_ws));

      assertThat(accessControlContext.getEffectiveResourceCreatePermissions(accessorResource2, resourceClassName, domainName),
                 is(setOf(createPerm_create)));
   }

   @Test
   public void revokeResourceCreatePermissions_nulls_shouldFail() {
      authenticateSystemResource();
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);
      final ResourceCreatePermission createPerm_inherit
            = ResourceCreatePermissions.getInstance(ResourcePermissions.getInstance(ResourcePermissions.INHERIT, true));

      final Resource accessorResource = generateUnauthenticatableResource();
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke create permissions with null parameters
      try {
         accessControlContext.revokeResourceCreatePermissions(null,
                                                              resourceClassName,
                                                              createPerm_create_withGrant,
                                                              createPerm_inherit);
         fail("revoking create-permissions with null accessor resource should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(null,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_create_withGrant,
                                                              createPerm_inherit);
         fail("revoking create-permissions with null accessor resource should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              null,
                                                              createPerm_create_withGrant,
                                                              createPerm_inherit);
         fail("revoking create-permissions with null resource class name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource class required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              null,
                                                              domainName,
                                                              createPerm_create_withGrant,
                                                              createPerm_inherit);
         fail("revoking create-permissions with null resource class name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource class required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              (ResourceCreatePermission) null);
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permission required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              (ResourceCreatePermission) null);
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permission required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              createPerm_create_withGrant,
                                                              null);
         fail("revoking create-permissions with null element in permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("an array or a sequence"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_create_withGrant,
                                                              null);
         fail("revoking create-permissions with null element in permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("an array or a sequence"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              (String) null,
                                                              createPerm_create_withGrant,
                                                              createPerm_inherit);
         fail("revoking create-permissions with null domain name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("domain required"));
      }

      // test set-based versions
      try {
         accessControlContext.revokeResourceCreatePermissions(null,
                                                              resourceClassName,
                                                              setOf(createPerm_create_withGrant,
                                                                    createPerm_inherit));
         fail("revoking create-permissions with null accessor resource should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(null,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_create_withGrant,
                                                                    createPerm_inherit));
         fail("revoking create-permissions with null accessor resource should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              null,
                                                              setOf(createPerm_create_withGrant,
                                                                    createPerm_inherit));
         fail("revoking create-permissions with null resource class name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource class required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              null,
                                                              domainName,
                                                              setOf(createPerm_create_withGrant,
                                                                    createPerm_inherit));
         fail("revoking create-permissions with null resource class name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("resource class required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              (Set<ResourceCreatePermission>) null);
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permissions required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              (Set<ResourceCreatePermission>) null);
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permissions required"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              setOf(createPerm_create_withGrant,
                                                                    null));
         fail("revoking create-permissions with null element in permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("null element"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_create_withGrant,
                                                                    null));
         fail("revoking create-permissions with null element in permission set should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("null element"));
      }

      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              (String) null,
                                                              setOf(createPerm_create_withGrant,
                                                                    createPerm_inherit));
         fail("revoking create-permissions with null domain name should have failed");
      }
      catch (NullPointerException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("domain required"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_emptyPermissionSet_shouldFail() {
      authenticateSystemResource();
      final Resource accessorResource = generateUnauthenticatableResource();
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke create permissions with empty permission set
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              Collections.<ResourceCreatePermission>emptySet());
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permissions required"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              Collections.<ResourceCreatePermission>emptySet());
         fail("revoking create-permissions with null permission set should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("permissions required"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_nonExistentReferences_shouldFail() {
      authenticateSystemResource();
      final Resource accessorResource = generateUnauthenticatableResource();
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      final ResourceCreatePermission createPerm_create_withGrant
            = ResourceCreatePermissions.getInstance(ResourceCreatePermissions.CREATE, true);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke create permissions with invalid references
      try {
         accessControlContext.revokeResourceCreatePermissions(Resources.getInstance(-999L),
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_create_withGrant);
         fail("revoking create-permissions with reference to non-existent accessor resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not found"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              "invalid_resource_class",
                                                              domainName,
                                                              createPerm_create_withGrant);
         fail("revoking create-permissions with reference to non-existent resource class name should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("could not find resource class"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              "invalid_resource_domain",
                                                              createPerm_create_withGrant);
         fail("revoking create-permissions with reference to non-existent domain name should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("could not find domain"));
      }

      // test set-based version
      try {
         accessControlContext.revokeResourceCreatePermissions(Resources.getInstance(-999L),
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_create_withGrant));
         fail("revoking create-permissions with reference to non-existent accessor resource should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not found"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              "invalid_resource_class",
                                                              domainName,
                                                              setOf(createPerm_create_withGrant));
         fail("revoking create-permissions with reference to non-existent resource class name should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("could not find resource class"));
      }
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              "invalid_resource_domain",
                                                              setOf(createPerm_create_withGrant));
         fail("revoking create-permissions with reference to non-existent domain name should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("could not find domain"));
      }
   }

   @Test
   public void revokeResourceCreatePermissions_mismatchedResourceClass_shouldFail() {
      authenticateSystemResource();
      final Resource accessorResource = generateUnauthenticatableResource();
      final String domainName = generateDomain();
      final String resourceClassName = generateResourceClass(false, false);
      final ResourcePermission permission_valid
            = ResourcePermissions.getInstance(generateResourceClassPermission(resourceClassName));
      final ResourcePermission permission_invalid
            = ResourcePermissions.getInstance(generateResourceClassPermission(generateResourceClass(false, false)));

      final ResourceCreatePermission createPerm_valid
            = ResourceCreatePermissions.getInstance(permission_valid);
      final ResourceCreatePermission createPerm_invalid
            = ResourceCreatePermissions.getInstance(permission_invalid);

      assertThat(accessControlContext.getEffectiveResourceCreatePermissionsMap(accessorResource).isEmpty(), is(true));

      // attempt to revoke create permissions with invalid references
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              createPerm_valid,
                                                              createPerm_invalid);
         fail("revoking create-permissions with mismatched resource class and permission should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not defined for resource class"));
      }

      // test set-based version
      try {
         accessControlContext.revokeResourceCreatePermissions(accessorResource,
                                                              resourceClassName,
                                                              domainName,
                                                              setOf(createPerm_valid,
                                                                    createPerm_invalid));
         fail("revoking create-permissions with mismatched resource class and permission should have failed");
      }
      catch (IllegalArgumentException e) {
         assertThat(e.getMessage().toLowerCase(), containsString("not defined for resource class"));
      }
   }
}
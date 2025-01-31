package org.exoplatform.commons.api.notification.rest;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.rest.model.UserNotificationSettings;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.services.BaseRestServicesTestCase;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.impl.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.test.mock.MockHttpServletRequest;

public class NotificationSettingsRestServiceTest extends BaseRestServicesTestCase {

  private static final String       USER_1            = "testuser1";

  private static final String       USER_2            = "testuser2";

  private static final String       CHANNEL_ID        = "channelId";

  private static final String       GROUP_PROVIDER_ID = "groupId";

  private static final String       PLUGIN_ID         = "pluginId";

  private static final PluginInfo   PLUGIN_PROVIDER   = new PluginInfo();

  private static final PluginConfig PLUGIN_CONFIG     = new PluginConfig();
  static {
    PLUGIN_PROVIDER.setType(PLUGIN_ID);
    PLUGIN_PROVIDER.setChannelActive(CHANNEL_ID);
    PLUGIN_CONFIG.setPluginId(PLUGIN_ID);
    PLUGIN_CONFIG.setGroupId(GROUP_PROVIDER_ID);
  }

  private static final List<PluginInfo> PLUGINS        = Collections.singletonList(PLUGIN_PROVIDER);

  private static final GroupProvider    GROUP_PROVIDER = new GroupProvider(GROUP_PROVIDER_ID);
  static {
    GROUP_PROVIDER.setPluginInfos(PLUGINS);
  }

  private static final List<GroupProvider>   GROUPS   = Collections.singletonList(GROUP_PROVIDER);

  private static final List<GroupProvider>   EMPTY_GROUP_PROVIDER =
                                                                  Collections.singletonList(new GroupProvider(GROUP_PROVIDER_ID));

  private static final List<AbstractChannel> CHANNELS = Collections.singletonList(newChannel());

  private ChannelManager                     channelManager;

  private UserSettingService                 userSettingService;

  private UserACL                            userACL;

  private ResourceBundleService              resourceBundleService;

  private PluginSettingService               pluginSettingService;

  @Override
  protected Class<?> getComponentClass() {
    return NotificationSettingsRestService.class;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    resourceBundleService = mock(ResourceBundleService.class);
    pluginSettingService = mock(PluginSettingService.class);
    channelManager = mock(ChannelManager.class);
    userSettingService = mock(UserSettingService.class);
    userACL = mock(UserACL.class);

    when(userACL.isSuperUser()).thenReturn(false);
    when(userACL.getAdminGroups()).thenReturn("admins");

    when(channelManager.getChannels()).thenReturn(CHANNELS);

    when(pluginSettingService.getGroupPlugins()).thenReturn(GROUPS);
    when(pluginSettingService.getPluginConfig(eq(PLUGIN_ID))).thenReturn(PLUGIN_CONFIG);

    UserSetting userSetting = new UserSetting();
    userSetting.setChannelActive(CHANNEL_ID);
    userSetting.setEnabled(true);
    userSetting.setUserId(USER_1);
    userSetting.setChannelPlugins(CHANNEL_ID, Collections.singletonList(PLUGIN_ID));
    when(userSettingService.get(eq(USER_1))).thenReturn(userSetting);

    getContainer().unregisterComponent(UserACL.class);

    getContainer().registerComponentInstance(ResourceBundleService.class.getName(), resourceBundleService);
    getContainer().registerComponentInstance(PluginSettingService.class.getName(), pluginSettingService);
    getContainer().registerComponentInstance(ChannelManager.class.getName(), channelManager);
    getContainer().registerComponentInstance(UserSettingService.class.getName(), userSettingService);
    getContainer().registerComponentInstance(UserACL.class.getName(), userACL);
  }

  @Override
  public void tearDown() throws Exception {
    getContainer().unregisterComponent(ResourceBundleService.class.getName());
    getContainer().unregisterComponent(PluginSettingService.class.getName());
    getContainer().unregisterComponent(ChannelManager.class.getName());
    getContainer().unregisterComponent(UserSettingService.class.getName());
    getContainer().unregisterComponent(UserACL.class.getName());
    super.tearDown();
  }

  public void testUnauthorizedNotSameUserGetSettings() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_2);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 401, resp.getStatus());
  }

  public void testAdminGetSettings() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_2);
    when(userACL.isUserInGroup(eq("admins"))).thenReturn(true);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 200, resp.getStatus());
    UserNotificationSettings notificationSettings = (UserNotificationSettings) resp.getEntity();
    assertNotNull(notificationSettings);
  }

  public void testGetSettingsSameUser() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 200, resp.getStatus());
    UserNotificationSettings notificationSettings = (UserNotificationSettings) resp.getEntity();
    assertNotNull(notificationSettings);
    assertNotNull(notificationSettings.getChannels());
    assertEquals(1, notificationSettings.getChannels().size());
    assertNotNull(notificationSettings.getGroups());
    assertEquals(1, notificationSettings.getGroups().size());
    assertNotNull(notificationSettings.getGroups().get(0));
    assertEquals(GROUP_PROVIDER_ID, notificationSettings.getGroups().get(0).getGroupId());
    assertNotNull(notificationSettings.getGroups().get(0).getGroupId());
    assertNotNull(notificationSettings.getChannelStatus());
    assertEquals(1, notificationSettings.getChannelStatus().size());
    assertTrue(notificationSettings.getChannelStatus().get(CHANNEL_ID));
  }

  public void testGetSettingsSameUserEmptyPlugins() throws Exception {
    // Given
    when(pluginSettingService.getGroupPlugins()).thenReturn(EMPTY_GROUP_PROVIDER);

    // Ensure no error is raised
    testGetSettingsSameUser();
  }

  public void testSaveDisableChannel() throws Exception {
    // Given
    String path = getPath(USER_1, "channel/" + CHANNEL_ID);
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "PATCH",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("PATCH",
                                              path,
                                              "",
                                              getFormHeaders(),
                                              ("enable=false").getBytes(),
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 204, resp.getStatus());
    verify(userSettingService, times(1)).save(any());
  }

  public void testSaveSettings() throws Exception {
    // Given
    String path = getPath(USER_1, "plugin/" + PLUGIN_ID);
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "PATCH",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("PATCH",
                                              path,
                                              "",
                                              getFormHeaders(),
                                              ("channels=" + CHANNEL_ID + "=true&digest=Weekly").getBytes(),
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 204, resp.getStatus());
    verify(userSettingService, times(1)).save(argThat(new ArgumentMatcher<UserSetting>() {
      @Override
      public boolean matches(UserSetting userSetting) {
        return userSetting.isActive(CHANNEL_ID, PLUGIN_ID) && userSetting.isChannelActive(CHANNEL_ID)
            && userSetting.isInWeekly(PLUGIN_ID);
      }
    }));
  }

  private MultivaluedMap<String, String> getFormHeaders() {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    return headers;
  }

  private String getPath(String username, String prefix) {
    return "/notifications/settings/" + username + "/" + prefix;
  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  private static AbstractChannel newChannel() {
    return new AbstractChannel() {

      @Override
      public void registerTemplateProvider(TemplateProvider provider) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected AbstractTemplateBuilder getTemplateBuilderInChannel(PluginKey key) {
        throw new UnsupportedOperationException();
      }

      @Override
      public ChannelKey getKey() {
        return ChannelKey.key(CHANNEL_ID);
      }

      @Override
      public String getId() {
        return CHANNEL_ID;
      }

      @Override
      public void dispatch(NotificationContext ctx, String userId) {
        throw new UnsupportedOperationException();
      }
    };
  }
}

package org.mortbay.cometd.continuation;

import java.util.*;
import java.util.Map.Entry;

/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
import javax.servlet.ServletConfig;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.*;
import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.cometd.server.*;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.picocontainer.Disposable;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class EXoContinuationBayeux extends BayeuxServerImpl implements Disposable {

  /**
   * Map for userToken.
   */
  private static Map<String, String>      userToken         = new HashMap<>();

  /**
   * Stores the eXoID <=> clientID association
   */
  private static Map<String, Set<String>> clientIDs         = Collections.synchronizedMap(new LinkedHashMap<>());

  /**
   * Cometd webapp context name
   */
  private String                          cometdContextName = "cometd";

  private String                          cloudIDSeparator  = "/";

  /**
   * Used to send message to all client or a specific client that listen a
   * specific channel
   */
  private ServerSessionImpl               systemClient;

  private Seti                            seti;

  private Oort                            oort;

  private ServletConfig                   servletConfig;

  /**
   * Logger.
   */
  private static final Log                LOG               = ExoLogger.getLogger(EXoContinuationBayeux.class);

  /**
   * Default constructor.
   */
  public EXoContinuationBayeux() {
    super();
    this.setSecurityPolicy(new EXoSecurityPolicy(this));
    //
    EXoContinuationCometdServlet servlet = EXoContinuationCometdServlet.getInstance();
    if (servlet != null) {
      servlet.setContainer(ExoContainerContext.getCurrentContainer());
      servlet.reInit();
    }
  }

  /**
   * {@inheritDoc}
   */
  public ServerSession newRemoteClient() {
    ServerSessionImpl client = newServerSession();
    return client;
  }

  @Deprecated
  public void setTimeout(long timeout) {
  }

  public long getTimeout() {
    return getOption(AbstractServerTransport.TIMEOUT_OPTION, 30000);
  }

  /**
   * @return context name of cometd webapp
   */
  public String getCometdContextName() {
    return cometdContextName;
  }

  public void setCometdContextName(String context) {
    this.cometdContextName = context;
  }

  /**
   * {@inheritDoc}
   */
  long getRandom(long variation) {
    long l = randomLong() ^ variation;
    return l < 0 ? -l : l;
  }

  /**
   * @param eXoId the client id.
   * @return token for client
   */
  public String getUserToken(String eXoId) {
    if (userToken.containsKey(eXoId)) {
      return (String) userToken.get(eXoId);
    }
    String token = Long.toString(this.getRandom(System.identityHashCode(this) ^ System.currentTimeMillis()), 36);
    userToken.put(eXoId, token);
    return token;
  }

  /**
   * @param eXoID the id of client.
   * @return client with eXoID
   */
  @Deprecated
  public EXoContinuationClient getClientByEXoId(String eXoID) {
    List<ServerSession> ids = getSessions();
    for (ServerSession client : ids) {
      if (client instanceof EXoContinuationClient) {
        EXoContinuationClient exoClient = (EXoContinuationClient) client;
        if (exoClient.getEXoId() != null && exoClient.getEXoId().equals(eXoID))
          return exoClient;
      }
    }
    return null;
  }

  public boolean isSubscribed(String eXoID, String clientID) {
    return (clientIDs.get(eXoID) != null && clientIDs.get(eXoID).contains(clientID));
  }

  public boolean isPresent(String eXoID) {
    return seti.isPresent(eXoID);
  }

  /**
   * @param channel the id of channel.
   * @param data the message
   * @param msgId the message id
   */
  public void sendBroadcastMessage(String channel, Object data, String msgId) {
    ServerSessionImpl fromClient = getSystemClient();
    ServerChannel ch = getChannel(channel);
    if (ch != null) {
      ch.publish(fromClient, data);
      if (LOG.isDebugEnabled())
        LOG.debug("Send broadcast message " + data.toString() + " on channel " + channel);
    } else {
      if (LOG.isDebugEnabled())
        LOG.debug("Message " + data.toString() + " not send. Channel " + channel + " not exist!");
    }
  }

  /**
   * Send data to a individual client. The data passed is sent to the client as
   * the "data" member of a message with the given channel and id. The message
   * is not published on the channel and is thus not broadcast to all channel
   * subscribers. However to the target client, the message appears as if it was
   * broadcast.
   * <p>
   * Typcially this method is only required if a service method sends
   * response(s) to channels other than the subscribed channel. If the response
   * is to be sent to the subscribed channel, then the data can simply be
   * returned from the subscription method.
   * 
   * @param eXoID the id of target client
   * @param channel The id of channel the message is for
   * @param data The data of the message
   * @param id The id of the message (or null for a random id).
   */
  public void sendMessage(String eXoID, String channel, Object data, String id) {
    seti.sendMessage(eXoID, channel, data);
  }

  public void setSeti(Seti seti) {
    this.seti = seti;
  }

  public void setOort(Oort oort) {
    this.oort = oort;
  }

  public void setCloudIDSeparator(String cloudIDSeparator) {
    if (cloudIDSeparator != null) {
      this.cloudIDSeparator = cloudIDSeparator;
    } else {
      LOG.warn("Can't set null for cloudIDSeparator");
    }
  }

  private ServerSessionImpl getSystemClient() {
    if (systemClient == null) {
      systemClient = newServerSession();
    }
    return systemClient;
  }

  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  public void setServletConfig(ServletConfig config) {
    this.servletConfig = config;
  }

  @Override
  public void dispose() {
    for (ServerSession session : getSessions()) {
      ((ServerSessionImpl) session).cancelSchedule();
    }

    if (seti != null && oort != null) {
      try {
        seti.stop();
        oort.stop();
        super.stop();
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public static class EXoSecurityPolicy implements SecurityPolicy, ServerSession.RemoveListener {

    private EXoContinuationBayeux bayeux;

    public EXoSecurityPolicy(EXoContinuationBayeux bayeux) {
      this.bayeux = bayeux;
    }

    /**
     * @param message the cometd message.
     * @return true if user valid else false.
     */
    private boolean checkUser(ServerMessage message) {
      String userId = (String) message.get("exoId");
      String eXoToken = (String) message.get("exoToken");
      return (userId != null && userToken.containsKey(userId) && userToken.get(userId).equals(eXoToken));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCreate(BayeuxServer server, ServerSession client, String channelId, ServerMessage message) {
      //
      Boolean b = client != null && !ChannelId.isMeta(channelId);
      return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession client, ServerMessage message) {
      if (client.isLocalSession() || bayeux.oort.isOortHandshake(message)) {
        return true;
      } else if (checkUser(message)) {
        client.addListener(this);

        String eXoID = (String) message.get("exoId");
        Set<String> cIds = clientIDs.get(eXoID);
        if (cIds == null) {
          cIds = new ConcurrentHashSet<String>();
          clientIDs.put(eXoID, cIds);
        }
        bayeux.seti.associate(eXoID, client);

        cIds.add(client.getId());
        return true;
      } else {
        return false;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPublish(BayeuxServer server, ServerSession client, ServerChannel channel, ServerMessage message) {
      return client != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSubscribe(BayeuxServer server, ServerSession client, ServerChannel channel, ServerMessage message) {
      return client != null && (checkUser(message) || client.isLocalSession() || bayeux.oort.isOort(client));
    }

    @Override
    public void removed(ServerSession session, boolean timeout) {
      Iterator<Entry<String, Set<String>>> iter = clientIDs.entrySet().iterator();

      while (iter.hasNext()) {
        Entry<String, Set<String>> client = iter.next();
        Set<String> ids = client.getValue();
        ids.remove(session.getId());
        if (ids.isEmpty()) {
          iter.remove();
        }
      }
    }
  }

  public Set<String> getConnectedUserIds() {
    return clientIDs.keySet();
  }
}

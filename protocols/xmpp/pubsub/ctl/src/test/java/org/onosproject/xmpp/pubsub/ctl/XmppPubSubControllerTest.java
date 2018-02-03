/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.xmpp.pubsub.ctl;

import com.google.common.collect.Lists;
import org.dom4j.Document;
import org.dom4j.tree.DefaultElement;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.onosproject.net.DeviceId;
import org.onosproject.xmpp.core.XmppController;
import org.onosproject.xmpp.core.XmppDevice;
import org.onosproject.xmpp.core.XmppDeviceId;
import org.onosproject.xmpp.core.XmppDeviceListener;
import org.onosproject.xmpp.core.XmppIqListener;
import org.onosproject.xmpp.core.XmppMessageListener;
import org.onosproject.xmpp.core.XmppPresenceListener;
import org.onosproject.xmpp.core.XmppSession;
import org.onosproject.xmpp.pubsub.XmppPublishEventsListener;
import org.onosproject.xmpp.pubsub.XmppSubscribeEventsListener;
import org.onosproject.xmpp.pubsub.model.XmppEventNotification;
import org.onosproject.xmpp.pubsub.model.XmppPubSubError;
import org.onosproject.xmpp.pubsub.model.XmppPublish;
import org.onosproject.xmpp.pubsub.model.XmppRetract;
import org.onosproject.xmpp.pubsub.model.XmppSubscribe;
import org.onosproject.xmpp.pubsub.model.XmppUnsubscribe;
import org.osgi.service.component.ComponentContext;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import java.net.InetSocketAddress;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test class for XmppPubSubController class.
 */
public class XmppPubSubControllerTest {

    XmppPubSubControllerImpl pubSubController;
    XmppControllerAdapter xmppControllerAdapter;
    XmppDeviceAdapter testDevice;


    TestXmppPublishEventsListener testXmppPublishEventsListener;
    TestXmppSubscribeEventsListener testXmppSubscribeEventsListener;


    static class TestXmppPublishEventsListener implements XmppPublishEventsListener {

        final List<XmppPublish> handledPublishMsgs = Lists.newArrayList();
        final List<XmppRetract> handledRetractMsgs = Lists.newArrayList();

        @Override
        public void handlePublish(XmppPublish publishEvent) {
            handledPublishMsgs.add(publishEvent);
        }

        @Override
        public void handleRetract(XmppRetract retractEvent) {
            handledRetractMsgs.add(retractEvent);
        }
    }

    static class TestXmppSubscribeEventsListener implements XmppSubscribeEventsListener {

        final List<XmppSubscribe> handledSubscribeMsgs = Lists.newArrayList();
        final List<XmppUnsubscribe> handledUnsubscribeMsgs = Lists.newArrayList();


        @Override
        public void handleSubscribe(XmppSubscribe subscribeEvent) {
            handledSubscribeMsgs.add(subscribeEvent);
        }

        @Override
        public void handleUnsubscribe(XmppUnsubscribe unsubscribeEvent) {
            handledUnsubscribeMsgs.add(unsubscribeEvent);
        }
    }

    @Before
    public void setUp() {
        testDevice = new XmppDeviceAdapter();
        xmppControllerAdapter = new XmppControllerAdapter();
        pubSubController = new XmppPubSubControllerImpl();
        pubSubController.xmppController = xmppControllerAdapter;
        testXmppPublishEventsListener = new TestXmppPublishEventsListener();
        pubSubController.addXmppPublishEventsListener(testXmppPublishEventsListener);
        testXmppSubscribeEventsListener = new TestXmppSubscribeEventsListener();
        pubSubController.addXmppSubscribeEventsListener(testXmppSubscribeEventsListener);

    }

    @Test
    public void testActivate() {
        ComponentContext mockContext = EasyMock.createMock(ComponentContext.class);
        pubSubController.activate(mockContext);
        assertThat(xmppControllerAdapter.iqListener, is(notNullValue()));
    }

    @Test
    public void testDeactivate() {
        pubSubController.deactivate();
        assertThat(xmppControllerAdapter.iqListener, is(nullValue()));
    }

    @Test
    public void testAddRemoveListeners() {
        pubSubController.addXmppPublishEventsListener(testXmppPublishEventsListener);
        assertThat(pubSubController.xmppPublishEventsListeners.size(), is(1));
        pubSubController.addXmppSubscribeEventsListener(testXmppSubscribeEventsListener);
        assertThat(pubSubController.xmppSubscribeEventsListeners.size(), is(1));
        pubSubController.removeXmppPublishEventsListener(testXmppPublishEventsListener);
        assertThat(pubSubController.xmppPublishEventsListeners.size(), is(0));
        pubSubController.removeXmppSubscribeEventsListener(testXmppSubscribeEventsListener);
        assertThat(pubSubController.xmppSubscribeEventsListeners.size(), is(0));
    }

    @Test
    public void testNotifyEvent() {
        XmppEventNotification eventNotification = new XmppEventNotification("test", new DefaultElement("test"));
        pubSubController.notify(DeviceId.NONE, eventNotification);
        assertThat(testDevice.sentPackets.size(), is(1));
        assertThat(testDevice.sentPackets.get(0), is(eventNotification));
    }

    @Test
    public void testNotifyError() {
        XmppPubSubError xmppPubSubError =
                new XmppPubSubError(XmppPubSubError.PubSubApplicationCondition.ITEM_NOT_FOUND);
        pubSubController.notify(DeviceId.NONE, xmppPubSubError);
        assertThat(testDevice.sentErrors.size(), is(1));
    }

//    @Test
//    public void testHandlePubSubMessages() {
//        pubSubController.addXmppPublishEventsListener(testXmppPublishEventsListener);
//        pubSubController.addXmppSubscribeEventsListener(testXmppSubscribeEventsListener);
//        XmppSubscribe
//        xmppControllerAdapter.iqListener.handleIqStanza();
//    }


    private class XmppControllerAdapter implements XmppController {

        XmppIqListener iqListener;

        @Override
        public XmppDevice getDevice(XmppDeviceId xmppDeviceId) {
            return testDevice;
        }

        @Override
        public void addXmppDeviceListener(XmppDeviceListener deviceListener) {

        }

        @Override
        public void removeXmppDeviceListener(XmppDeviceListener deviceListener) {

        }

        @Override
        public void addXmppIqListener(XmppIqListener iqListener) {
            this.iqListener = iqListener;
        }

        @Override
        public void removeXmppIqListener(XmppIqListener iqListener) {
            this.iqListener = null;
        }

        @Override
        public void addXmppMessageListener(XmppMessageListener messageListener) {

        }

        @Override
        public void removeXmppMessageListener(XmppMessageListener messageListener) {

        }

        @Override
        public void addXmppPresenceListener(XmppPresenceListener presenceListener) {

        }

        @Override
        public void removeXmppPresenceListener(XmppPresenceListener presenceListener) {

        }
    }

    private class XmppDeviceAdapter implements XmppDevice {

        final List<Packet> sentPackets = Lists.newArrayList();
        final List<PacketError> sentErrors = Lists.newArrayList();

        @Override
        public void setSession(XmppSession session) {

        }

        @Override
        public XmppSession getSession() {
            return null;
        }

        @Override
        public InetSocketAddress getIpAddress() {
            return null;
        }

        @Override
        public void registerConnectedDevice() {

        }

        @Override
        public void disconnectDevice() {

        }

        @Override
        public void sendPacket(Packet packet) {
            sentPackets.add(packet);
        }

        @Override
        public void writeRawXml(Document document) {

        }

        @Override
        public void handlePacket(Packet packet) {

        }

        @Override
        public void sendError(PacketError packetError) {
            sentErrors.add(packetError);
        }

    }

}

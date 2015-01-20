/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.ui.client.local.services;

import io.apiman.manager.ui.client.local.events.MouseInEvent;
import io.apiman.manager.ui.client.local.events.MouseOutEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * A service that allows widgets to register for mouse-in and
 * mouse-out events.  This is here because the built-in support
 * for mouse-in and mouse-out in GWT is not bullet-proof.  Sometimes
 * you can get the mouse-in event but not the mouse-out event.
 * 
 * This service watches the mouse move around the screen (captures
 * all mouse movement) and then does its own calculations and
 * tracking.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class MouseOverService {
    
    @Inject
    LoggerService logger;
    
    private Map<Widget, WidgetMouseTrackingInfo> enabledWidgets = new HashMap<Widget, WidgetMouseTrackingInfo>();
    
    /**
     * Constructor.
     */
    public MouseOverService() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONMOUSEMOVE) {
                    int x = event.getNativeEvent().getClientX();
                    int y = event.getNativeEvent().getClientY();
                    handleMouseMove(x, y);
                }
            }
        });
        Window.addWindowScrollHandler(new ScrollHandler() {
            @Override
            public void onWindowScroll(ScrollEvent event) {
                clearAll();
            }
        });
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                clearAll();
            }
        });
    }
    
    /**
     * Called to enable mouse-in and mouse-out events for a 
     * given widget.
     * @param widget
     */
    public void enableMouseEvents(final Widget widget) {
        enableMouseEvents(widget, null);
    }

    /**
     * Called to enable mouse-in and mouse-out events for a 
     * given widget.
     * @param widget
     * @param tracking
     */
    public void enableMouseEvents(final Widget widget, final WidgetMouseTrackingInfo tracking) {
        widget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    enabledWidgets.put(widget, tracking == null ? new WidgetMouseTrackingInfo() : tracking);
                } else {
                    enabledWidgets.remove(widget);
                }
            }
        });
    }

    /**
     * Called when the service spies on a native mouse movement event.
     * @param clientX
     * @param clientY
     */
    private void handleMouseMove(int clientX, int clientY) {
        for (Entry<Widget, WidgetMouseTrackingInfo> entry : enabledWidgets.entrySet()) {
            Widget w = entry.getKey();
            WidgetMouseTrackingInfo info = entry.getValue();
            if (isMouseInWidget(w, info, clientX, clientY)) {
                if (!info.mouseIn) {
                    info.mouseIn = true;
                    MouseInEvent.fire(w);
                }
            } else {
                if (info.mouseIn) {
                    info.mouseIn = false;
                    MouseOutEvent.fire(w);
                }
            }
        }
    }
    
    /**
     * Returns true if the mouse coordinates fall within the bounding box
     * of the given widget.
     * @param w
     * @param info 
     * @param clientX
     * @param clientY
     */
    private boolean isMouseInWidget(Widget w, WidgetMouseTrackingInfo info, int clientX, int clientY) {
        try {
            int top = w.getElement().getAbsoluteTop() - Window.getScrollTop() - info.extraTop;
            int bottom = top + w.getElement().getClientHeight() + info.extraBottom;
            int left = w.getElement().getAbsoluteLeft() - Window.getScrollLeft() - info.extraLeft;
            int right = left + w.getElement().getClientWidth() + info.extraRight;

//            logger.info("Client: {0},{1}  Widget: {2},{3} -> {4},{5}", clientX, clientY, left, top, right, bottom);

            return clientX >= left && clientX <= right && clientY >= top && clientY <= bottom;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Clear the mouse-in info for all tracked widgets.
     */
    protected void clearAll() {
        for (Entry<Widget, WidgetMouseTrackingInfo> entry : enabledWidgets.entrySet()) {
            Widget w = entry.getKey();
            WidgetMouseTrackingInfo info = entry.getValue();
            if (info.mouseIn) {
                info.mouseIn = false;
                MouseOutEvent.fire(w);
            }
        }
    }

    public static final class WidgetMouseTrackingInfo {
        public boolean mouseIn = false;
        public int extraLeft = 0;
        public int extraRight = 0;
        public int extraTop = 0;
        public int extraBottom = 0;
    }
    
}

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
package io.apiman.manager.ui.client.local.widgets;

import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.LoggerService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * A label that supports inline editing of the value.  Can be enabled or 
 * disabled, which is useful when the user doesn't have permission to change
 * the value.  When disabled, this widget acts just like a label.  When 
 * enabled, the user may hover over the text and options to inline-edit 
 * the value will appear.
 * 
 * For this widget to work it must be injected by Errai, so that the 
 * appropriate event handlers can be set up during the @PostConstruct
 * phase.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class InlineEditableLabel extends FlowPanel implements HasValue<String>, HasAllMouseHandlers {
    
    @Inject
    LoggerService logger;
    @Inject
    TranslationService i18n;
    @Inject
    RootPanel rootPanel;
    
    Label valueLabel = new Label();
    TextArea editableValue = new TextArea();
    
    private String emptyValueMessage = "<no value>"; //$NON-NLS-1$
    
    String value;
    
    boolean enabled = true;
    Label overlay;
    Anchor overlayEdit;
    Timer overlayRemovalTimer = null;
    HandlerRegistration resizeHandler;
    HandlerRegistration scrollHandler;
    boolean editing = false;
    FlowPanel buttons = new FlowPanel();
    Button save = new Button();
    Button cancel = new Button();
    
    /**
     * Constructor.
     */
    public InlineEditableLabel() {
        add(valueLabel);
    }
    
    @PostConstruct
    void postConstruct() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    getElement().addClassName("apiman-inline-edit"); //$NON-NLS-1$
                }
            }
        });
        valueLabel.getElement().setClassName("user-value"); //$NON-NLS-1$
        valueLabel.getElement().getStyle().setWidth(100, Unit.PCT);
        editableValue.getElement().setClassName("user-value-edit"); //$NON-NLS-1$
        editableValue.getElement().getStyle().setWidth(100, Unit.PCT);
        save.getElement().setClassName("btn btn-default inline-save-btn"); //$NON-NLS-1$
        save.setHTML("<i class=\"fa fa-check fa-fw\"></i>"); //$NON-NLS-1$
        cancel.getElement().setClassName("btn btn-default"); //$NON-NLS-1$
        cancel.setHTML("<i class=\"fa fa-times fa-fw\"></i>"); //$NON-NLS-1$
        buttons.add(save);
        buttons.add(cancel);
        addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                logger.debug("Mouse entered inline editable label, creating overlay."); //$NON-NLS-1$
                showOverlay();
            }
        });
        
        editableValue.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    cancel.click();
                } else {
                    int offsetHeight = editableValue.getElement().getOffsetHeight();
                    int scrollHeight = editableValue.getElement().getScrollHeight();
                    if (scrollHeight > offsetHeight) {
                        editableValue.getElement().getStyle().setHeight(scrollHeight + 8, Unit.PX);
                    }
                }
            }
        });
        
        save.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String newValue = editableValue.getValue();
                closeEditWidget();
                setValue(newValue, true);
            }
        });
        
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeEditWidget();
            }
        });
    }
    
    

    /**
     * Closes the edit fields and reverts back to the standard view.
     */
    protected void closeEditWidget() {
        remove(editableValue);
        remove(buttons);
        add(valueLabel);
        new Timer() {
            @Override
            public void run() {
                editing = false;
            }
        }.schedule(1000);
    }

    /**
     * Remove any visible overlay.
     */
    protected void removeOverlay() {
        logger.debug("Removing overlay: " + overlay); //$NON-NLS-1$
        if (overlay != null) {
            if (overlayRemovalTimer == null) {
                logger.debug("Creating/scheduling overlay removal timer."); //$NON-NLS-1$
                overlayRemovalTimer = new Timer() {
                    @Override
                    public void run() {
                        logger.debug("Overlay removal timer fired, removing overlay now."); //$NON-NLS-1$
                        removeOverlayNow();
                    }

                };
                overlayRemovalTimer.schedule(250);
            }
        }
    }

    /**
     * Immediately removes the overlay.
     */
    protected void removeOverlayNow() {
        rootPanel.remove(overlay);
        overlay = null;
        rootPanel.remove(overlayEdit);
        overlayEdit = null;
        resizeHandler.removeHandler();
        resizeHandler = null;
        scrollHandler.removeHandler();
        scrollHandler = null;
        overlayRemovalTimer = null;
    }
    
    /**
     * Create and show an overlay.
     */
    protected void showOverlay() {
        // If the old one isn't gone yet, just cancel the removal timer.  Otherwise
        // create a new overlay.
        if (overlayRemovalTimer != null) {
            overlayRemovalTimer.cancel();
            overlayRemovalTimer = null;
            return;
        }
        
        // Already have an overlay?  We don't need another one!
        if (overlay != null) {
            return;
        }

        if (!enabled) {
            return;
        }
        
        if (editing) {
            return;
        }

        int l = getElement().getAbsoluteLeft() - 5;
        int t = getElement().getAbsoluteTop() - 5;
        int w = getElement().getAbsoluteRight() - l + 5;
        int h = getElement().getAbsoluteBottom() - t + 5;
        
        overlay = new Label();
        overlay.setStyleName("apiman-inline-edit-overlay"); //$NON-NLS-1$
        overlay.getElement().getStyle().setPosition(Position.ABSOLUTE);
        overlay.getElement().getStyle().setTop(t, Unit.PX);
        overlay.getElement().getStyle().setLeft(l, Unit.PX);
        overlay.getElement().getStyle().setWidth(w, Unit.PX);
        overlay.getElement().getStyle().setHeight(h, Unit.PX);
        overlay.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                logger.debug("Mouse entered overlay, showing overlay."); //$NON-NLS-1$
                showOverlay();
            }
        });
        overlay.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                logger.debug("Mouse left overlay, removing overlay!"); //$NON-NLS-1$
                removeOverlay();
            }
        });
        overlay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onEditClicked();
            }
        });
        
        overlayEdit = new Anchor();
        overlayEdit.setHTML("<i class=\"fa fa-pencil fa-fw\"></i>"); //$NON-NLS-1$
        overlayEdit.setTitle(i18n.format(AppMessages.CLICK_TO_EDIT));
        overlayEdit.setStyleName("apiman-inline-edit-overlay-edit"); //$NON-NLS-1$
        overlayEdit.getElement().getStyle().setPosition(Position.ABSOLUTE);
        overlayEdit.getElement().getStyle().setTop(t, Unit.PX);
        overlayEdit.getElement().getStyle().setLeft(getElement().getAbsoluteRight() + 5, Unit.PX);
        overlayEdit.getElement().getStyle().setHeight(h, Unit.PX);
        overlayEdit.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                logger.debug("Mouse entered overlay-edit, showing overlay."); //$NON-NLS-1$
                showOverlay();
            }
        });
        overlayEdit.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                logger.debug("Mouse left overlay-edit, removing overlay."); //$NON-NLS-1$
                removeOverlay();
            }
        });
        overlayEdit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onEditClicked();
            }
        });
        
        rootPanel.add(overlay);
        rootPanel.add(overlayEdit);
        
        scrollHandler = Window.addWindowScrollHandler(new ScrollHandler() {
            @Override
            public void onWindowScroll(ScrollEvent event) {
                removeOverlay();
            }
        });
        
        resizeHandler = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                removeOverlay();
            }
        });
    }

    /**
     * Called when the user clicks to edit the value.
     */
    protected void onEditClicked() {
        logger.debug("User clicked EDIT."); //$NON-NLS-1$

        removeOverlayNow();

        editing = true;
        
        int t = getElement().getAbsoluteTop();
        int h = getElement().getAbsoluteBottom() - t;

        editableValue.getElement().getStyle().setHeight(h + 10, Unit.PX);
        editableValue.setValue(value);
        
        remove(valueLabel);
        add(editableValue);
        add(buttons);
        
        editableValue.setFocus(true);
        editableValue.selectAll();
    }
    
    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
      return addDomHandler(handler, MouseDownEvent.getType());
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
      return addDomHandler(handler, MouseMoveEvent.getType());
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
      return addDomHandler(handler, MouseOutEvent.getType());
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
      return addDomHandler(handler, MouseOverEvent.getType());
    }

    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
      return addDomHandler(handler, MouseUpEvent.getType());
    }

    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
      return addDomHandler(handler, MouseWheelEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        String oldValue = this.value;
        this.value = value;
        if (value == null || value.trim().length() == 0) {
            valueLabel.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
            valueLabel.setText(getEmptyValueMessage());
        } else {
            valueLabel.getElement().removeClassName("apiman-label-faded"); //$NON-NLS-1$
            valueLabel.setText(value);
        }
        ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
    }

    /**
     * @return the emptyValueMessage
     */
    public String getEmptyValueMessage() {
        return emptyValueMessage;
    }

    /**
     * @param emptyValueMessage the emptyValueMessage to set
     */
    public void setEmptyValueMessage(String emptyValueMessage) {
        this.emptyValueMessage = emptyValueMessage;
    }

}

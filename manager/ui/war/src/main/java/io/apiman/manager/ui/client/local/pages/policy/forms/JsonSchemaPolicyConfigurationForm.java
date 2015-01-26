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
package io.apiman.manager.ui.client.local.pages.policy.forms;

import io.apiman.manager.ui.client.local.events.IsFormValidEvent;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * An implementation of {@link IPolicyConfigurationForm} that gets used when the
 * policy definition came from a plugin and has an associated JSON Schema.  The
 * JSON Schema specification can be found here:  http://json-schema.org/
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonSchemaPolicyConfigurationForm extends FlowPanel implements IPolicyConfigurationForm {
    
    private static int counter = 1;

    /**
     * Returns a new ID for this JSON schema form wrapper.
     */
    private static String generateId() {
        return "_json_editor_holder_" + counter++; //$NON-NLS-1$
    }
    
    private JavaScriptObject editor;
    private boolean attached = false;
    private String schema = null;
    private String value = null;

    /**
     * Constructor.
     */
    public JsonSchemaPolicyConfigurationForm() {
        getElement().setId(generateId());
        addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    attached = true;
                    if (schema != null) {
                        init(schema);
                    }
                    if (value != null) {
                        _setEditorValue(value);
                    }
                } else {
                    attached = false;
                    if (editor != null) {
                        _cleanup();
                    }
                }
            }
        });
    }
    
    /**
     * Initialize the form with the given JSON Schema.  This will generate the editor
     * that will be used to edit the form.
     * @param jsonSchema
     */
    public void init(String jsonSchema) {
        schema = jsonSchema;
        if (attached) {
            this._initEditor(jsonSchema, getElement().getId());
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        if (editor != null) {
            return _getEditorValue();
        } else {
            return this.value;
        }
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
        this.value = value;
        if (value == null) {
            this.value = "{}"; //$NON-NLS-1$
        }
        if (editor != null) {
            _setEditorValue(this.value);
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see io.apiman.manager.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler)
     */
    @Override
    public HandlerRegistration addIsFormValidHandler(Handler handler) {
        return addHandler(handler, IsFormValidEvent.getType());
    }
    
    protected void onValid() {
        IsFormValidEvent.fire(this, true);
    }
    
    protected void onInvalid() {
        IsFormValidEvent.fire(this, false);
    }

    /**
     * Native code to initialize the JSON Editor.
     * @param jsonSchema
     * @param holderId
     */
    public final native void _initEditor(String jsonSchema, String holderId) /*-{
        var me = this;
        var schema = $wnd.JSON.parse(jsonSchema);
        var holder = $doc.getElementById(holderId);
        var editor = new $wnd.JSONEditor(holder, {
            // Disable fetching schemas via ajax
            ajax: false,
            // The schema for the editor
            schema: schema,
            // Disable additional properties
            no_additional_properties: true,
            // Require all properties by default
            required_by_default: true,
            disable_edit_json: true,
            disable_properties: true,
            iconlib: "fontawesome4",
            theme: "bootstrap3"
        });
        editor.on('change', function() {
            // Get an array of errors from the validator
            var errors = editor.validate();
            // Not valid
            if (errors.length) {
                console.log("invalid");
                me.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::onInvalid()();
            } else {
                console.log("valid");
                me.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::onValid()();
            }
        });
        this.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::setEditor(Lcom/google/gwt/core/client/JavaScriptObject;)(editor);
    }-*/;
    
    public final native String _getEditorValue() /*-{
        var editor = this.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::getEditor()();
        return $wnd.JSON.stringify(editor.getValue());
    }-*/;

    public final native void _setEditorValue(String value) /*-{
        var object = $wnd.JSON.parse(value);
        var editor = this.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::getEditor()();
        editor.setValue(object);
    }-*/;

    /**
     * Native code to shut down the JSON Editor.
     */
    public final native void _cleanup() /*-{
        var editor = this.@io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm::getEditor()();
        editor.destroy();
    }-*/;

    /**
     * @return the editor
     */
    protected JavaScriptObject getEditor() {
        return editor;
    }

    /**
     * @param editor the editor to set
     */
    protected void setEditor(JavaScriptObject editor) {
        this.editor = editor;
    }

}

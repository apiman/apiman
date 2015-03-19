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
package io.apiman.plugins.simpleheaderpolicy;

import static org.junit.Assert.*;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.simpleheaderpolicy.beans.AddHeaderBean;
import io.apiman.plugins.simpleheaderpolicy.beans.StripHeaderBean;
import io.apiman.plugins.simpleheaderpolicy.beans.AddHeaderBean.ApplyTo;
import io.apiman.plugins.simpleheaderpolicy.beans.StripHeaderBean.StripType;
import io.apiman.plugins.simpleheaderpolicy.beans.SimpleHeaderPolicyDefBean;
import io.apiman.plugins.simpleheaderpolicy.beans.StripHeaderBean.With;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class SimpleHeaderPolicyTest {
    @Mock
    private IPolicyChain<ServiceRequest> mRequestChain;
    @Mock
    private IPolicyChain<ServiceResponse> mResponseChain;

    @Mock
    private IPolicyContext mContext;
    private SimpleHeaderPolicy policy;
    private SimpleHeaderPolicyDefBean config;
    private ServiceRequest request;
    private ServiceResponse response;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        policy = new SimpleHeaderPolicy();
        config = new SimpleHeaderPolicyDefBean();
        request = new ServiceRequest();
        response = new ServiceResponse();
    }

    @Test
    public void shouldSetHeaderOnRequest() {
        AddHeaderBean header = new AddHeaderBean();
        header.setHeaderName("X-Clacks-Overhead");
        header.setHeaderValue("GNU Terry Pratchett");
        header.setOverwrite(false);
        header.setApplyTo(ApplyTo.REQUEST);
        config.getAddHeaders().add(header);

        policy.apply(request, mContext, config, mRequestChain);

        assertEquals("GNU Terry Pratchett", request.getHeaders().get("X-Clacks-Overhead"));
        assertEquals(1, request.getHeaders().size());
    }

    @Test
    public void shouldSetHeaderOnResponse() {
        AddHeaderBean header = new AddHeaderBean();
        header.setHeaderName("X-Clacks-Overhead");
        header.setHeaderValue("GNU Terry Pratchett");
        header.setOverwrite(false);
        header.setApplyTo(ApplyTo.RESPONSE);
        config.getAddHeaders().add(header);

        policy.apply(response, mContext, config, mResponseChain);

        assertEquals("GNU Terry Pratchett", response.getHeaders().get("X-Clacks-Overhead"));
        assertEquals(1, response.getHeaders().size());
    }

    @Test
    public void shouldSetHeaderOnBoth() {
        AddHeaderBean header = new AddHeaderBean();
        header.setHeaderName("Request-And-Response");
        header.setHeaderValue("Weatherwax");
        header.setOverwrite(false);
        header.setApplyTo(ApplyTo.BOTH);
        config.getAddHeaders().add(header);

        policy.apply(request, mContext, config, mRequestChain);

        assertEquals("Weatherwax", request.getHeaders().get("Request-And-Response"));
        assertEquals(1, request.getHeaders().size());

        policy.apply(response, mContext, config, mResponseChain);

        assertEquals("Weatherwax", response.getHeaders().get("Request-And-Response"));
        assertEquals(1, response.getHeaders().size());
    }

    @Test
    public void shouldOverwriteWhenFlagSet() {
        request.getHeaders().put("X-Clacks-Overhead", "Ridcully");

        AddHeaderBean header = new AddHeaderBean();
        header.setHeaderName("X-Clacks-Overhead");
        header.setHeaderValue("GNU Terry Pratchett");
        header.setOverwrite(true);
        header.setApplyTo(ApplyTo.REQUEST);
        config.getAddHeaders().add(header);

        policy.apply(request, mContext, config, mRequestChain);

        assertEquals("GNU Terry Pratchett", request.getHeaders().get("X-Clacks-Overhead"));
        assertEquals(1, request.getHeaders().size());
    }

    @Test
    public void shouldNotOverwriteWhenFlagUnset() {
        request.getHeaders().put("X-Clacks-Overhead", "Ridcully");

        AddHeaderBean header = new AddHeaderBean();
        header.setHeaderName("X-Clacks-Overhead");
        header.setHeaderValue("GNU Terry Pratchett");
        header.setOverwrite(false);
        header.setApplyTo(ApplyTo.REQUEST);
        config.getAddHeaders().add(header);

        policy.apply(request, mContext, config, mRequestChain);

        assertEquals("Ridcully", request.getHeaders().get("X-Clacks-Overhead"));
        assertEquals(1, request.getHeaders().size());
    }

    @Test
    public void shouldStripHeaderWithKey() {
        request.getHeaders().put("vanish", "begone");

        StripHeaderBean shb = new StripHeaderBean();
        shb.setPattern("vanish");
        shb.setStripType(StripType.KEY);
        shb.setWith(With.STRING);

        config.getStripHeaders().add(shb);

        policy.apply(request, mContext, config, mRequestChain);

        assertFalse(request.getHeaders().containsKey("vanish"));
    }

    @Test
    public void shouldStripHeaderWithValue() {
        request.getHeaders().put("lu", "tze");
        request.getHeaders().put("lu", "tze");

        StripHeaderBean shb = new StripHeaderBean();
        shb.setPattern("tze");
        shb.setStripType(StripType.VALUE);
        shb.setWith(With.STRING);

        config.getStripHeaders().add(shb);

        policy.apply(request, mContext, config, mRequestChain);
        policy.apply(response, mContext, config, mResponseChain);

        assertFalse(request.getHeaders().containsKey("lu"));
        assertFalse(response.getHeaders().containsKey("lu"));
    }

    @Test
    public void shouldStripHeaderWithRegexKey() {
        request.getHeaders().put("sybil", "ramkin");

        StripHeaderBean shb = new StripHeaderBean();
        shb.setPattern("sy.*l");
        shb.setStripType(StripType.KEY);
        shb.setWith(With.REGEX);

        config.getStripHeaders().add(shb);

        policy.apply(request, mContext, config, mRequestChain);

        assertFalse(request.getHeaders().containsKey("sybil"));
        assertTrue(request.getHeaders().isEmpty());
    }

    @Test
    public void shouldStripHeaderWithRegexValue() {
        request.getHeaders().put("lord", "Vetinari");

        StripHeaderBean shb = new StripHeaderBean();
        shb.setPattern("vetinar\\w+");
        shb.setStripType(StripType.VALUE);
        shb.setWith(With.REGEX);

        config.getStripHeaders().add(shb);

        policy.apply(request, mContext, config, mRequestChain);

        assertFalse(request.getHeaders().containsKey("vetinari"));
        assertTrue(request.getHeaders().isEmpty());
    }

    @Test
    public void shouldStripCaseInsensitively() {
        request.getHeaders().put("lord", "Vetinari");

        StripHeaderBean shb = new StripHeaderBean();
        shb.setPattern("VETINAR\\w+");
        shb.setStripType(StripType.VALUE);
        shb.setWith(With.REGEX);

        config.getStripHeaders().add(shb);

        policy.apply(request, mContext, config, mRequestChain);

        assertFalse(request.getHeaders().containsKey("vetinari"));
        assertTrue(request.getHeaders().isEmpty());
    }
    
    @Test
    public void shouldNotOverwrite() {
        request.getHeaders().put("nobby", "nobbs");
        
        AddHeaderBean ahb = new AddHeaderBean();
        ahb.setHeaderName("nobby");
        ahb.setHeaderValue("sgt-detritus");
        ahb.setApplyTo(ApplyTo.REQUEST);
        
        ahb.setOverwrite(false); // DO NOT OVERWRITE
        
        config.getAddHeaders().add(ahb);
        
        policy.apply(request, mContext, config, mRequestChain);
        
        assertEquals("nobbs", request.getHeaders().get("nobby"));
        assertEquals(1, request.getHeaders().size());
    }
    
    @Test
    public void shouldOverwrite() {
        request.getHeaders().put("nobby", "nobbs");
        
        AddHeaderBean ahb = new AddHeaderBean();
        ahb.setHeaderName("nobby");
        ahb.setHeaderValue("sgt-detritus");
        ahb.setApplyTo(ApplyTo.REQUEST);
        
        ahb.setOverwrite(true); // OVERWRITE
        
        config.getAddHeaders().add(ahb);
        
        policy.apply(request, mContext, config, mRequestChain);
        
        assertEquals("sgt-detritus", request.getHeaders().get("nobby"));
        assertEquals(1, request.getHeaders().size());
    }

    // @Test
    // public void shouldTrimWhitespace() {
    // request.getHeaders().put("      lord      ", " Vetinari ");
    //
    // StripHeaderBean shb = new StripHeaderBean();
    // shb.setPattern("VETINAR\\w+");
    // shb.setStripType(StripType.KEY);
    // shb.setWith(With.REGEX);
    //
    // config.getStripHeaders().add(shb);
    //
    // policy.apply(request, mContext, config, mRequestChain);
    //
    // assertFalse(request.getHeaders().containsKey("lord"));
    // assertTrue(request.getHeaders().isEmpty());
    // }
}

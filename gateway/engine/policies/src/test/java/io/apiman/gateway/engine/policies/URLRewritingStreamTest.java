/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.policies.rewrite.URLRewritingStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class URLRewritingStreamTest {

    @Test
    public void testNoBody() {
        doTest("google.com", "apiman.io", new String[] {}, "");
    }

    @Test
    public void testNoURLs_OneChunk() {
        doTest("google.com", "apiman.io", new String[] { "This is some text with no URLs in it." },
                "This is some text with no URLs in it.");
    }

    @Test
    public void testNoURLs_TwoChunks() {
        doTest("google.com", "apiman.io", new String[] { "This is some text with no URLs in it.", "And also a second chunk." },
                "This is some text with no URLs in it.And also a second chunk.");
    }

    @Test
    public void testOneURL_OneChunk() {
        doTest("google.com", "apiman.io", new String[] { "This is just an http://google.com test!" },
                "This is just an http://apiman.io test!");
    }

    @Test
    public void testOneURL_SpanningTwoChunks() {
        doTest("google.com", "apiman.io", new String[] { "This is just an http://goo", "gle.com test!" },
                "This is just an http://apiman.io test!");
    }

    @Test
    public void testOneURL_SpanningTwoChunks2() {
        doTest("google.com", "apiman.io", new String[] { "This is just an ht", "tp://google.com test!" },
                "This is just an http://apiman.io test!");
    }

    @Test
    public void testOneURL_SpanningThreeChunks() {
        doTest("google.com", "apiman.io", new String[] { "This is just an http://goo", "gle.", "com test!" },
                "This is just an http://apiman.io test!");
    }

    @Test
    public void testOneURL_MultipleChunks() {
        doTest("google.com", "apiman.io", new String[] { "This is just an http://google.com test!",
                "Another chunk is here.", "..and a third chunk is here!" },
                "This is just an http://apiman.io test!Another chunk is here...and a third chunk is here!");
    }

    @Test
    public void testMultipleURLs_OneChunk() {
        doTest("google.com", "apiman.io", new String[] { "This is just an http://google.com/url that we http://www.google.com/want to http://www.google.com/test", },
                "This is just an http://apiman.io/url that we http://www.apiman.io/want to http://www.apiman.io/test");
    }

    @Test
    public void testOneURL_IsTheChunk() {
        doTest("google.com", "apiman.io", new String[] { "http://google.com/path/to/resource", },
                "http://apiman.io/path/to/resource");
    }

    @Test
    public void testMultipleURLs_ManyChunks() {
        doTest("google.com",
                "apiman.io",
                new String[] {
                        "Lorem ipsum dolor sit amet, an accusamus http://www.google.com/path/to/resource usu. Malorum electram usu ea. Labores http://www.google.com/path/to/contentiones ad eam, id pri nullam convenire. Elitr temporibus ei nec, te quis disputando usu.\r\n",
                        "\r\n",
                        "Usu ex affert disputationi, stet novum nec cu. Dolor http://www.google.com/path/to/oportere http://www.google.com/path/to/forensibus vim an. Ex vix semper necessitatibus, ex vidit errem nonumy pro, in viris nostrud posidonium sed. Ut tincidunt mnesarchum pri, at sit affert sapientem definitiones. Ex eros clita comprehensam nam, alienum electram cum te, ne eirmod evertitur mea.\r\n",
                        "\r\n",
                        "Eu eum dolore ornatus accumsan, http://www.google.com/path/to/alienum insolens pericula has ea. Mundi mnesarchum interpretaris eu quo. Esse legere http://www.google.com/path/to/mediocritatem ei pri, per in stet suscipit quaerendum, usu et diceret debitis definitionem. Ius no invenire principes gubergren. At vivendo accumsan definitiones sit. Eam mucius noluisse ad, quis ferri sanctus te est. Diam natum no usu." },
                "Lorem ipsum dolor sit amet, an accusamus http://www.apiman.io/path/to/resource usu. Malorum electram usu ea. Labores http://www.apiman.io/path/to/contentiones ad eam, id pri nullam convenire. Elitr temporibus ei nec, te quis disputando usu.\r\n" +
                "\r\n" +
                "Usu ex affert disputationi, stet novum nec cu. Dolor http://www.apiman.io/path/to/oportere http://www.apiman.io/path/to/forensibus vim an. Ex vix semper necessitatibus, ex vidit errem nonumy pro, in viris nostrud posidonium sed. Ut tincidunt mnesarchum pri, at sit affert sapientem definitiones. Ex eros clita comprehensam nam, alienum electram cum te, ne eirmod evertitur mea.\r\n" +
                "\r\n" +
                "Eu eum dolore ornatus accumsan, http://www.apiman.io/path/to/alienum insolens pericula has ea. Mundi mnesarchum interpretaris eu quo. Esse legere http://www.apiman.io/path/to/mediocritatem ei pri, per in stet suscipit quaerendum, usu et diceret debitis definitionem. Ius no invenire principes gubergren. At vivendo accumsan definitiones sit. Eam mucius noluisse ad, quis ferri sanctus te est. Diam natum no usu.");
    }

    @Test
    public void testMultipleURLs_SpanningChunks() {
        doTest("google.com",
                "apiman.io",
                new String[] {
                        "Lorem ipsum dolor sit amet, an accusamus http://www.google.com/path/to/resource usu. Malorum electram usu ea. Labores http://www.google.com/path/to/contentiones ad eam, id pri nullam convenire. Elitr temporibus ei nec, te quis disputando usu.\r\n",
                        "\r\n",
                        "Usu ex affert disputationi, stet novum nec cu. Dolor http://www.google.com/",
                        "path/to/oportere http://www.google.com/path/to/forensibu",
                        "s vim an. Ex vix semper necessitatibus, ex vidit errem nonumy pro, in viris nostrud posidonium sed. Ut tincidunt mnesarchum pri, at sit affert sapientem definitiones. Ex eros clita comprehensam nam, alienum electram cum te, ne eirmod evertitur mea.\r\n",
                        "\r\n",
                        "Eu eum dolore ornatus accumsan, http://www.google.com/path/to/alienum insolens pericula has ea. Mundi mnesarchum interpretaris eu quo. Esse legere http://www.google.com/path/to/mediocritatem ei pri, per in stet suscipit quaerendum, usu et diceret debitis definitionem. Ius no invenire principes gubergren. At vivendo accumsan definitiones sit. Eam mucius noluisse ad, quis ferri sanctus te est. Diam natum no usu." },
                "Lorem ipsum dolor sit amet, an accusamus http://www.apiman.io/path/to/resource usu. Malorum electram usu ea. Labores http://www.apiman.io/path/to/contentiones ad eam, id pri nullam convenire. Elitr temporibus ei nec, te quis disputando usu.\r\n" +
                "\r\n" +
                "Usu ex affert disputationi, stet novum nec cu. Dolor http://www.apiman.io/path/to/oportere http://www.apiman.io/path/to/forensibus vim an. Ex vix semper necessitatibus, ex vidit errem nonumy pro, in viris nostrud posidonium sed. Ut tincidunt mnesarchum pri, at sit affert sapientem definitiones. Ex eros clita comprehensam nam, alienum electram cum te, ne eirmod evertitur mea.\r\n" +
                "\r\n" +
                "Eu eum dolore ornatus accumsan, http://www.apiman.io/path/to/alienum insolens pericula has ea. Mundi mnesarchum interpretaris eu quo. Esse legere http://www.apiman.io/path/to/mediocritatem ei pri, per in stet suscipit quaerendum, usu et diceret debitis definitionem. Ius no invenire principes gubergren. At vivendo accumsan definitiones sit. Eam mucius noluisse ad, quis ferri sanctus te est. Diam natum no usu.");
    }

    @Test
    public void testOneURL_Encoded() {
        doTest("google.com", "apiman.io", new String[] { "Trying to translate URL: https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CB4QFjAAahUKEwinzd27-p7HAhUJzoAKHT4IAx8&url=https%3A%2F%2Fwww.linux.com%2F&ei=K9TIVeeLKYmcgwS-kIz4AQ&usg=AFQjCNFO4N7_5ZjceNQLnmvTJH0ulgwg7w&sig2=0Ku21Nj4qGePklI2rkSYzQ&bvm=bv.99804247,d.eXY which is pretty complicated." },
                "Trying to translate URL: https://www.apiman.io/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CB4QFjAAahUKEwinzd27-p7HAhUJzoAKHT4IAx8&url=https%3A%2F%2Fwww.linux.com%2F&ei=K9TIVeeLKYmcgwS-kIz4AQ&usg=AFQjCNFO4N7_5ZjceNQLnmvTJH0ulgwg7w&sig2=0Ku21Nj4qGePklI2rkSYzQ&bvm=bv.99804247,d.eXY which is pretty complicated.");
    }

    // Ignored for now - not sure if we will support translating encoded URLs or just require users to
    // tranlate them raw (leaning towards the latter).
    @Test @Ignore
    public void testOneURL_TranslateEncoded() {
        doTest("great.URL", "alright url", new String[] { "http://www.google.com/this%20is%20a%20great%20URL!/I-%3Ewant%7B%7Dto%3C%3Eencode-it" },
                "http://www.google.com/this%20is%20a%20alright%20url!/I-%3Ewant%7B%7Dto%3C%3Eencode-it");
    }


    /**
     * @param fromRegexp
     * @param toReplacement
     * @param chunks
     * @param expectedResult
     */
    private void doTest(String fromRegexp, String toReplacement, String[] chunks, String expectedResult) {
        IBufferFactoryComponent bufferFactory = new ByteBufferFactoryComponent();
        URLRewritingStream stream = new URLRewritingStream(bufferFactory, (ServiceResponse) null, fromRegexp, toReplacement);
        RecordingHandler handler = new RecordingHandler(bufferFactory.createBuffer());
        stream.bodyHandler(handler);
        stream.endHandler(new IAsyncHandler<Void>() {
            @Override
            public void handle(Void result) {
            }
        });

        for (String chunk : chunks) {
            stream.write(bufferFactory.createBuffer(chunk));
        }
        stream.end();

        String result = handler.getBuffer().toString();
        Assert.assertEquals(expectedResult, result);
    }

    private class RecordingHandler implements IAsyncHandler<IApimanBuffer> {

        private IApimanBuffer buffer;

        /**
         * Constructor.
         */
        public RecordingHandler(IApimanBuffer buffer) {
            this.setBuffer(buffer);
        }

        /**
         * @see io.apiman.gateway.engine.async.IAsyncHandler#handle(java.lang.Object)
         */
        @Override
        public void handle(IApimanBuffer chunk) {
            buffer.append(chunk);
        }

        /**
         * @return the buffer
         */
        public IApimanBuffer getBuffer() {
            return buffer;
        }

        /**
         * @param buffer the buffer to set
         */
        public void setBuffer(IApimanBuffer buffer) {
            this.buffer = buffer;
        }

    }

}

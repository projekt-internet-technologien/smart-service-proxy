/**
 * Copyright (c) 2012, all partners of project SPITFIRE (core://www.spitfire-project.eu)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  - Neither the backendName of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.spitfire.ssp.server.http;

import eu.spitfire.ssp.server.http.handler.HttpSemanticPayloadFormatter;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;


/**
 * The {@link HttpProxyPipelineFactory} is a factory to generate pipelines for channels to handle
 * incoming {@link org.jboss.netty.handler.codec.http.HttpRequest}s.
 *
 * @author Oliver Kleine
 *
 */
public class HttpProxyPipelineFactory implements ChannelPipelineFactory {

    private static Logger log = LoggerFactory.getLogger(HttpProxyPipelineFactory.class.getName());

    private LinkedHashSet<ChannelHandler> handler;

    /**
     * Creates a new instance of {@link HttpProxyPipelineFactory}.
     *
     * @param handler a {@link java.util.LinkedHashSet} containing the handlers to be added to the
     *                pipeline (in most-downstream-first order)
     *
     * @throws Exception if something went terribly wrong
     */
    public HttpProxyPipelineFactory(LinkedHashSet<ChannelHandler> handler) throws Exception {
        this.handler = handler;
    }


    /**
     * The {@link org.jboss.netty.channel.ChannelPipeline} contains the handlers to handle incoming
     * {@link org.jboss.netty.handler.codec.http.HttpRequest}s and send appropriate
     * {@link org.jboss.netty.handler.codec.http.HttpResponse}s.
     *
     * @return the {@link org.jboss.netty.channel.ChannelPipeline} (chain of handlers) to handle incoming
     * {@link org.jboss.netty.handler.codec.http.HttpRequest}s
     *
     * @throws Exception if something went terribly wrong
     */
    @Override
	public ChannelPipeline getPipeline() throws Exception {

		ChannelPipeline pipeline = Channels.pipeline();
        Iterator<ChannelHandler> handlerIterator = handler.iterator();

        //pipeline.addLast("Logging Handler", new DummyHandler());

        //HTTP protocol handlers
		pipeline.addLast("HTTP Decoder", new HttpRequestDecoder());
		pipeline.addLast("HTTP Chunk Aggregator", new HttpChunkAggregator(4194304));
        pipeline.addLast("HTTP Encoder", new HttpResponseEncoder());
		pipeline.addLast("HTTP Deflater", new HttpContentCompressor());

        //SSP specific handlers
        pipeline.addLast("Payload Formatter", new HttpSemanticPayloadFormatter());

        //Execution handler
        ChannelHandler channelHandler = handlerIterator.next();
        pipeline.addLast(channelHandler.getClass().getSimpleName(), channelHandler);
        log.debug("Added {} to pipeline.", channelHandler.getClass().getSimpleName());

        while(handlerIterator.hasNext()){
            channelHandler = handlerIterator.next();
            pipeline.addLast(channelHandler.getClass().getSimpleName(), channelHandler);
            log.debug("Added {} to pipeline.", channelHandler.getClass().getSimpleName());
        }

        return pipeline;
	}
}

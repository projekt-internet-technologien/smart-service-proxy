package eu.spitfire.ssp.backend.coap;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.endpoint.CoapEndpoint;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.MessageCode;
import de.uzl.itm.ncoap.message.MessageType;
import de.uzl.itm.ncoap.message.options.ContentFormat;
import eu.spitfire.ssp.backend.generic.DataOriginAccessor;

import eu.spitfire.ssp.server.internal.wrapper.ExpiringNamedGraph;
import eu.spitfire.ssp.server.internal.exception.OperationTimeoutException;
import com.hp.hpl.jena.rdf.model.Model;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Date;

/**
 * A {@link CoapWebresourceAccessor} is the component to access external
 * {@link CoapWebresource}s, i.e. send GET, POST, PUT, or DELETE
 * message. Currently, only GET is supported.
 *
 * @author Oliver Kleine
 */
public class CoapWebresourceAccessor extends DataOriginAccessor<URI, CoapWebresource> {

    private CoapEndpoint coapApplication;

    /**
     * Creates a new instance of {@link CoapWebresourceAccessor}
     *
     * @param componentFactory the {@link CoapBackendComponentFactory} to
     *                         provide the appropriate resources
     */
    public CoapWebresourceAccessor(CoapBackendComponentFactory componentFactory) {
        super(componentFactory);
        this.coapApplication = componentFactory.getCoapApplication();
    }


    @Override
    public ListenableFuture<ExpiringNamedGraph> getStatus(CoapWebresource coapWebresource){
        final SettableFuture<ExpiringNamedGraph> resultFuture = SettableFuture.create();

        try{
            URI webserviceUri = coapWebresource.getIdentifier();
            CoapRequest coapRequest = new CoapRequest(MessageType.CON, MessageCode.GET, webserviceUri);
            coapRequest.setAccept(ContentFormat.APP_RDF_XML);
            coapRequest.setAccept(ContentFormat.APP_N3);
            coapRequest.setAccept(ContentFormat.APP_TURTLE);

            InetAddress remoteAddress = InetAddress.getByName(webserviceUri.getHost());
            int port = webserviceUri.getPort() == -1 ? 5683 : webserviceUri.getPort();

            coapApplication.sendCoapRequest(coapRequest,
                new InternalCoapResponseCallback(resultFuture, webserviceUri), new InetSocketAddress(remoteAddress, port)
            );
        }
        catch(Exception ex){
            resultFuture.setException(ex);
        }

        return resultFuture;
    }


    private class InternalCoapResponseCallback extends ClientCallback {

        private SettableFuture<ExpiringNamedGraph> resultFuture;
        private URI webserviceUri;

        private InternalCoapResponseCallback(SettableFuture<ExpiringNamedGraph> resultFuture, URI webserviceUri) {
            this.resultFuture = resultFuture;
            this.webserviceUri = webserviceUri;
        }


        @Override
        public void processCoapResponse(CoapResponse coapResponse) {
            try{
                Model model = CoapTools.getModelFromCoapResponse(coapResponse);
                Date expiry = new Date(System.currentTimeMillis() + coapResponse.getMaxAge() * 1000);

                this.resultFuture.set(new ExpiringNamedGraph(this.webserviceUri, model, expiry));
            }
            catch(Exception ex){
                this.resultFuture.setException(ex);
            }
        }


        @Override
        public void processTransmissionTimeout() {
            resultFuture.setException(new OperationTimeoutException(
                String.format("No response received from \"%s\" (Request timed out.)",  this.webserviceUri)
            ));
        }
    }
}

package eu.spitfire.ssp.server.pipeline.messages;

import com.hp.hpl.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;

/**
 * Instances of {@link ResourceStatusMessage} contain the {@link URI} to identify the resource, a {@link Model}
 * containing the status of the resource and a {@link Date} to define the expiry of the actual status.
 *
 * @author Oliver Kleine
 */
public class ResourceStatusMessage {

    private static Logger log = LoggerFactory.getLogger(ResourceStatusMessage.class.getName());
    private URI resourceUri;
    private final Model resourceStatus;
    private final Date expiry;

    public ResourceStatusMessage(URI resourceUri, Model resourceStatus, Date expiry){
        this.resourceUri = resourceUri;

        this.resourceStatus = resourceStatus;
        this.expiry = expiry;
    }

    /**
     * Returns the {@link Model} containing the actual status of the resource
     * @return the {@link Model} containing the actual status of the resource
     */
    public Model getResourceStatus() {
        return resourceStatus;
    }

    /**
     * Returns the expiry of the actual status
     * @return the expiry of the actual status
     */
    public Date getExpiry() {
        return expiry;
    }

    /**
     * Returns the {@link URI} identifying the resource
     * @return the {@link URI} identifying the resource
     */
    public URI getResourceUri() {
        return resourceUri;
    }

    @Override
    public String toString(){
        return "[Resource status message] " + getResourceUri() + " (URI), " + getExpiry() + " (expiry)";
    }
}
package it.regioneveneto.myp3.mystd.security.saml;

import org.joda.time.DateTime;
import org.opensaml.util.resource.ResourceException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
/**
 * Spring <-- OpenSAML resource adapter. It allows the use of Spring {@link Resource}s as an Open SAML {@link org.opensaml.util.resource.Resource}.
 * The implementation simply delegates all method calls to the Spring resource.
 *
 * @author Ulises Bocchio
 */
public class SpringResourceWrapperOpenSAMLResource implements org.opensaml.util.resource.Resource {
    private Resource springDelegate;
    public SpringResourceWrapperOpenSAMLResource(Resource springDelegate) throws ResourceException {
        this.springDelegate = springDelegate;
        if (!exists()) {
            throw new ResourceException("Wrapper resource does not exist: " + springDelegate);
        }
    }
    @Override
    public String getLocation() {
        try {
            return springDelegate.getURL().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean exists() throws ResourceException {
        return springDelegate.exists();
    }
    @Override
    public InputStream getInputStream() throws ResourceException {
        try {
            return springDelegate.getInputStream();
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }
    @Override
    public DateTime getLastModifiedTime() throws ResourceException {
        try {
            return new DateTime(springDelegate.lastModified());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }
    @Override
    public int hashCode() {
        return getLocation().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SpringResourceWrapperOpenSAMLResource) {
            return getLocation().equals(((SpringResourceWrapperOpenSAMLResource) o).getLocation());
        }
        return false;
    }
}
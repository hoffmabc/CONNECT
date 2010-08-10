/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.adapter.xdr.async.response;

import gov.hhs.fha.nhinc.adapter.xdr.async.response.proxy.AdapterXDRResponseProxy;
import gov.hhs.fha.nhinc.adapter.xdr.async.response.proxy.AdapterXDRResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterRegistryResponseType;
import gov.hhs.fha.nhinc.service.WebServiceHelper;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author patlollav
 */
public class AdapterXDRResponseImpl {
    private static final Log logger = LogFactory.getLog(AdapterXDRResponseImpl.class);

    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(AdapterRegistryResponseType body, WebServiceContext context) {
        getLogger().debug("Entering provideAndRegisterDocumentSetBResponse");
        WebServiceHelper oHelper = new WebServiceHelper();
        XDRAcknowledgementType response = new XDRAcknowledgementType();
        AdapterXDRResponseProxyObjectFactory factory = new AdapterXDRResponseProxyObjectFactory();
        AdapterXDRResponseProxy proxy = factory.getAdapterXDRResponseProxy();

        try
        {
            if (body != null && body.getRegistryResponse() != null && proxy != null)
            {
                response = (XDRAcknowledgementType) oHelper.invokeDeferredResponseWebService(proxy, proxy.getClass(), "provideAndRegisterDocumentSetBResponse", body.getAssertion(), body.getRegistryResponse(), context);
            } else
            {
                getLogger().error("Failed to call the web orchestration (" + proxy.getClass() + ".provideAndRegisterDocumentSetBResponse).  The input parameter is null.");
            }
        } catch (Exception e)
        {
            getLogger().error("Failed to call the web orchestration (" + proxy.getClass() + ".provideAndRegisterDocumentSetBResponse).  An unexpected exception occurred.  " +
                    "Exception: " + e.getMessage(), e);
        }

        getLogger().debug("Exiting provideAndRegisterDocumentSetBResponse");

        return response;
    }

    protected Log getLogger(){
        return logger;
    }

}
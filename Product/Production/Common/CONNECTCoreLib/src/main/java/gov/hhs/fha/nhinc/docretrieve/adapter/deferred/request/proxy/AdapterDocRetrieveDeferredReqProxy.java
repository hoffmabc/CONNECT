package gov.hhs.fha.nhinc.docretrieve.adapter.deferred.request.proxy;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.healthit.nhin.DocRetrieveAcknowledgementType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;


/**
 * Created by
 * User: ralph
 * Date: Jul 26, 2010
 * Time: 2:33:52 PM
 */
public interface AdapterDocRetrieveDeferredReqProxy {

    public DocRetrieveAcknowledgementType sendToAdapter(RetrieveDocumentSetRequestType body, AssertionType assertion);
}
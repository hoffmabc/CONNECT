package gov.hhs.fha.nhinc.docquery.proxy;

import gov.hhs.fha.nhinc.async.AsyncMessageIdExtractor;
import gov.hhs.fha.nhinc.common.auditlog.AdhocQueryResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docquery.DocQueryAuditLog;
import gov.hhs.fha.nhinc.nhindocquery.proxy.NhinDocQueryProxy;
import gov.hhs.fha.nhinc.nhindocquery.proxy.NhinDocQueryProxyObjectFactory;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenExtractor;
import javax.xml.ws.WebServiceContext;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayQueryRequestType;
import gov.hhs.fha.nhinc.nhinclib.LoggingContextHelper;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;

/**
 *
 *
 * @author Neil Webb
 */
public class NhincProxyDocQuerySecuredImpl {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(NhincProxyDocQuerySecuredImpl.class);

    public AdhocQueryResponse respondingGatewayCrossGatewayQuery(gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayQuerySecuredRequestType body, WebServiceContext context) {
        AdhocQueryResponse response = null;
        LoggingContextHelper loggingContextHelper = new LoggingContextHelper();
        try {
            // Collect assertion
            AssertionType assertion = SamlTokenExtractor.GetAssertion(context);

            // Extract the message id value from the WS-Addressing Header and
            // place it in the Assertion Class
            if (assertion != null) {
                assertion.setMessageId(AsyncMessageIdExtractor.GetAsyncMessageId(context));
            }

            loggingContextHelper.setContext(context);

            response = respondingGatewayCrossGatewayQuery(body, assertion);

        } finally {
            loggingContextHelper.clearContext();
        }
        return response;
    }

    public AdhocQueryResponse respondingGatewayCrossGatewayQuery(gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayQuerySecuredRequestType body, AssertionType assertion) {
        log.debug("Entering NhincProxyDocQuerySecuredImpl.respondingGatewayCrossGatewayQuery...");
        AdhocQueryResponse response = null;

        // Audit the Document Query Request Message sent on the Nhin Interface
        DocQueryAuditLog auditLog = new DocQueryAuditLog();
        AcknowledgementType ack = auditLog.auditDQRequest(body.getAdhocQueryRequest(), assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        try {
            log.debug("Creating NhinDocQueryProxy");
            NhinDocQueryProxyObjectFactory docQueryFactory = new NhinDocQueryProxyObjectFactory();
            NhinDocQueryProxy proxy = docQueryFactory.getNhinDocQueryProxy();

            RespondingGatewayCrossGatewayQueryRequestType request = new RespondingGatewayCrossGatewayQueryRequestType();

            request.setAdhocQueryRequest(body.getAdhocQueryRequest());
            request.setAssertion(assertion);
            request.setNhinTargetSystem(body.getNhinTargetSystem());

            log.debug("Calling NhinDocQueryProxy.respondingGatewayCrossGatewayQuery(request)");
            response = proxy.respondingGatewayCrossGatewayQuery(request);
        } catch (Throwable t) {
            log.error("Error sending NHIN Proxy message: " + t.getMessage(), t);
            response = new AdhocQueryResponse();
            response.setStatus("urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure");

            RegistryError registryError = new RegistryError();
            registryError.setCodeContext("Processing NHIN Proxy document retrieve");
            registryError.setErrorCode("XDSRepositoryError");
            registryError.setSeverity("Error");
            response.getRegistryErrorList().getRegistryError().add(registryError);
        }

        // Audit the Document Query Response Message received on the Nhin Interface
        AdhocQueryResponseMessageType auditMsg = new AdhocQueryResponseMessageType();
        auditMsg.setAdhocQueryResponse(response);
        auditMsg.setAssertion(assertion);
        ack = auditLog.auditDQResponse(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        log.debug("Leaving NhincProxyDocQuerySecuredImpl.respondingGatewayCrossGatewayQuery...");
        return response;
    }
}
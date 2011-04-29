/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 * Copyright 2010(Year date of delivery) United States Government, as represented by the Secretary of Health and Human Services.  All rights reserved.
 *  
 */
package gov.hhs.fha.nhinc.docquery.passthru;

import gov.hhs.fha.nhinc.common.auditlog.AdhocQueryResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayQueryRequestType;
import gov.hhs.fha.nhinc.docquery.DocQueryAuditLog;
import gov.hhs.fha.nhinc.docquery.nhin.proxy.NhinDocQueryProxy;
import gov.hhs.fha.nhinc.docquery.nhin.proxy.NhinDocQueryProxyObjectFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;

/**
 *
 * @author JHOPPESC
 */
public class PassthruDocQueryOrchImpl {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(PassthruDocQueryOrchImpl.class);

    /**
     *
     * @param body
     * @param assertion
     * @param target
     * @return <code>AdhocQueryResponse</code>
     */
    public AdhocQueryResponse respondingGatewayCrossGatewayQuery(AdhocQueryRequest body, AssertionType assertion, NhinTargetSystemType target) {
        log.debug("Entering NhincProxyDocQuerySecuredImpl.respondingGatewayCrossGatewayQuery...");
        AdhocQueryResponse response = null;

        // Requireed the responding home community id in the audit log
        String responseCommunityID = null;
        if (target != null &&
                target.getHomeCommunity() != null) {
            responseCommunityID = target.getHomeCommunity().getHomeCommunityId();
        }
        log.debug("=====>>>>> responseCommunityID is " + responseCommunityID);
        // Audit the Document Query Request Message sent on the Nhin Interface
        DocQueryAuditLog auditLog = new DocQueryAuditLog();
        AcknowledgementType ack = auditLog.auditDQRequest(body, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE, responseCommunityID);

        try {
            log.debug("Creating NhinDocQueryProxy");
            NhinDocQueryProxyObjectFactory docQueryFactory = new NhinDocQueryProxyObjectFactory();
            NhinDocQueryProxy proxy = docQueryFactory.getNhinDocQueryProxy();

            RespondingGatewayCrossGatewayQueryRequestType request = new RespondingGatewayCrossGatewayQueryRequestType();

            request.setAdhocQueryRequest(body);
            request.setAssertion(assertion);
            request.setNhinTargetSystem(target);
            log.debug("Calling NhinDocQueryProxy.respondingGatewayCrossGatewayQuery(request)");
            response = proxy.respondingGatewayCrossGatewayQuery(request.getAdhocQueryRequest(), request.getAssertion(), request.getNhinTargetSystem());
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
        ack = auditLog.auditDQResponse(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE, responseCommunityID);

        log.debug("Leaving NhincProxyDocQuerySecuredImpl.respondingGatewayCrossGatewayQuery...");
        return response;
    }
}

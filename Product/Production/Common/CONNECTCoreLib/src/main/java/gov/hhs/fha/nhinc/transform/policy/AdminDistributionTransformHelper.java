/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.transform.policy;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.NotifyEventType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunityType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewaySendAlertMessageType;
import oasis.names.tc.emergency.edxl.de._1.EDXLDistribution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author dunnek
 */
public class AdminDistributionTransformHelper {
    private static final String ActionInValue = "AdminDistIn";
    private static final String ActionOutValue = "AdminDistOut";
    private static Log log = null;
    
    public AdminDistributionTransformHelper() {
        log = createLogger();
    }
    /**
     * Instantiating log4j logger
     * @return
     */
    protected Log createLogger() {
        return ((log != null) ? log : LogFactory.getLog(getClass()));
    }
    public CheckPolicyRequestType transformNhinAlertToCheckPolicy(EDXLDistribution message, AssertionType assertion)
    {
        CheckPolicyRequestType result = new CheckPolicyRequestType();

         RequestType request = new RequestType();

        if(assertion == null)
        {
            log.error("Missing Assertion");
            return result;
        }
        if(message == null)
        {
            log.error("Missing message");
            return result;
        }
        log.debug("transformPatientDiscoveryNhincToCheckPolicy - adding assertion data");
        AssertionHelper assertHelp = new AssertionHelper();
        assertHelp.appendAssertionDataToRequest(request, assertion);

        request.setAction(ActionHelper.actionFactory(ActionInValue));
        
        return result;
    }
    public CheckPolicyRequestType transformEntityAlertToCheckPolicy(RespondingGatewaySendAlertMessageType message, String target) {

        CheckPolicyRequestType result = new CheckPolicyRequestType();
        if (message == null) {
            log.error("Request is null.");
            return result;
        }
        if (target == null || target.isEmpty())
        {
            log.error("target is missing");
            return result;
        }

        if (message.getEDXLDistribution() == null)
        {
            log.error("missing body");
            return result;
        }
        if(message.getAssertion() == null)
        {
            log.error("missing assertion");
            return result;
        }

        HomeCommunityType hc = new HomeCommunityType();
        hc.setHomeCommunityId(target);
        
        EDXLDistribution body = message.getEDXLDistribution();
        //RequestType request = getRequestType(patDiscReq, event.getAssertion());
        RequestType request = new RequestType();


        log.debug("transformPatientDiscoveryNhincToCheckPolicy - adding subject");
        SubjectType subject = createSubject(hc, message.getAssertion());
        request.getSubject().add(subject);


        log.debug("transformPatientDiscoveryNhincToCheckPolicy - adding assertion data");
        AssertionHelper assertHelp = new AssertionHelper();
        assertHelp.appendAssertionDataToRequest(request, message.getAssertion());

        request.setAction(ActionHelper.actionFactory(ActionOutValue));

        return result;

    }
    protected SubjectType createSubject(HomeCommunityType hc, AssertionType assertion)
    {
        SubjectHelper subjHelp = new SubjectHelper();
        //SubjectType subject = subjHelp.subjectFactory(event.getAssertion().getHomeCommunity(), event.getAssertion());
        SubjectType subject = subjHelp.subjectFactory(hc, assertion);

        subject.setSubjectCategory(SubjectHelper.SubjectCategory);

        return subject;
    }
}
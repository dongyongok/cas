package org.apereo.cas.web.flow.actions;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Client;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationStoreWebflowStateActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAuthenticationActions")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public class DelegatedClientAuthenticationStoreWebflowStateActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE)
    private Action delegatedAuthenticationStoreWebflowAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifyMissingClient() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationStoreWebflowAction.execute(context));
    }

    @Test
    public void verifyClient() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val result = delegatedAuthenticationStoreWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
        val ticket = context.getFlowScope().get(TransientSessionTicket.class.getName(), TransientSessionTicket.class);
        assertNotNull(ticket);
        assertNotNull(ticket.getProperty(Client.class.getName(), String.class));
    }
}

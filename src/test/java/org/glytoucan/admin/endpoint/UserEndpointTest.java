/**
 * 
 */
package org.glytoucan.admin.endpoint;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glytoucan.admin.Application;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserGenerateKeyRequest;
import org.glytoucan.admin.model.UserGenerateKeyResponse;
import org.glytoucan.admin.model.UserKeyCheckRequest;
import org.glytoucan.admin.model.UserKeyCheckResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * @author developer
 *
 *         This work is licensed under the Creative Commons Attribution 4.0
 *         International License. To view a copy of this license, visit
 *         http://creativecommons.org/licenses/by/4.0/.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class UserEndpointTest {
	
	private static final Log logger = LogFactory.getLog(UserEndpointTest.class);

	private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

	@Value("${local.server.port}")
	private int port = 0;

	@Autowired
	UserEndpoint userEndpoint;
	
	@Before
	public void init() throws Exception {
		marshaller.setPackagesToScan(ClassUtils.getPackageName(UserKeyRequest.class));
    marshaller.setPackagesToScan(ClassUtils.getPackageName(Authentication.class));
    
    marshaller.afterPropertiesSet();
	}
	
  String token="ya29.CjBXA4l-rJxxG7g2PpaTzo3061sa6KIlLzF6y-SX39VRQjKVRGaWcqoZkvxVb48FX6U";
//  String apiKey = "JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";
  String apiKey = "JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";

	@Test
	public void testInvalidAuth() {
		UserKeyRequest request = new UserKeyRequest();
		Authentication auth = new Authentication();
		auth.setId("1");
		auth.setApiKey("invalidtoken");
//  auth.setApiKey(apiKey);
		request.setAuthentication(auth);
		request.setPrimaryId("glytoucan@gmail.com");
		
		Object result = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
				+ port + "/ws", request);
		assertNotNull(result);
		UserKeyResponse response = (UserKeyResponse)result;
		logger.debug(response);
		logger.debug(response.getKey());
		Assert.assertEquals("-401",response.getResponseMessage().getErrorCode());
		Assert.assertNull(response.getKey());
	}
	
//	 @Test maybe setting 
	  public void testTokenRequest() {
	    UserKeyRequest request = new UserKeyRequest();
	    Authentication auth = new Authentication();
	    auth.setId("1");
	    auth.setApiKey(token);
	    request.setAuthentication(auth);
	    request.setPrimaryId("glytoucan@gmail.com");
	    
	    Object result = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
	        + port + "/ws", request);
	    assertNotNull(result);
	    UserKeyResponse response = (UserKeyResponse)result;
	    logger.debug(response);
	    logger.debug(response.getKey());
	    Assert.assertEquals(new BigInteger("0"),response.getResponseMessage().getErrorCode());
	    Assert.assertNotNull(response.getKey());

	  }
	 
	   @Test
	   @Transactional
	   public void testUserKeyDirect() {
	     UserKeyRequest request = new UserKeyRequest();
	     Authentication auth = new Authentication();
	     auth.setId("1");
	     auth.setApiKey(apiKey);
	     request.setAuthentication(auth);
	     request.setPrimaryId("glytoucan@gmail.com");
	     
	     UserKeyResponse result = userEndpoint.getKey(request);
	     assertNotNull(result);
	     UserKeyResponse response = (UserKeyResponse)result;
	     logger.debug(response);
	     logger.debug(response.getKey());
	     Assert.assertEquals("0",response.getResponseMessage().getErrorCode());
	     Assert.assertNotNull(response.getKey());

	   }
	   
   @Test
   @Transactional
   public void testUserKey() {
     UserKeyRequest request = new UserKeyRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("glytoucan@gmail.com");
     
//     UserKeyResponse result = userEndpoint.getKey(request);
     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:8031/ws", request);

     assertNotNull(wsResult);
     UserKeyResponse response = (UserKeyResponse)wsResult;
     logger.debug(response);
     logger.debug(response.getKey());
     Assert.assertEquals("0",response.getResponseMessage().getErrorCode());
     Assert.assertNotNull(response.getKey());

   }

   @Test
   @Transactional
   public void testUserDetails() {
     UserDetailsRequest request = new UserDetailsRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("glytoucan@gmail.com");
     
//     UserDetailsResponse result = userEndpoint.userDetailsRequest(request);
     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:8031/ws", request);

     assertNotNull(wsResult);
     UserDetailsResponse result = (UserDetailsResponse)wsResult;
     logger.debug(result);
     logger.debug(result.getUser());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertNotNull(result.getUser());
     Assert.assertEquals("glytoucan@gmail.com",result.getUser().getEmail());
     Assert.assertEquals("1",result.getUser().getExternalId());
     Assert.assertEquals("Toucan",result.getUser().getFamilyName());
   }
   @Test
   @Transactional
   public void testUserKeyCheckRequest() {
     UserKeyCheckRequest request = new UserKeyCheckRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("glytoucan@gmail.com");
     request.setApiKey(apiKey);
     marshaller.setPackagesToScan(ClassUtils.getPackageName(UserKeyCheckRequest.class));

//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
//         + port + "/ws", request);
     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:8031/ws", request);
     assertNotNull(wsResult);
     UserKeyCheckResponse result = (UserKeyCheckResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertTrue(result.isResult());
   }

   
   @Test
   @Transactional
   public void testUserKeyCheckRequestDirect() {
     UserKeyCheckRequest request = new UserKeyCheckRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("glytoucan@gmail.com");
     request.setApiKey(apiKey);
     marshaller.setPackagesToScan(ClassUtils.getPackageName(UserKeyCheckRequest.class));

     UserKeyCheckResponse result = userEndpoint.userKeyCheckRequest(request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
//         + port + "/ws", request);
//     assertNotNull(wsResult);
//     UserKeyCheckResponse result = (UserKeyCheckResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertTrue(result.isResult());
   }
   
//   @Test
   public void testGenerateHash() {
     UserGenerateKeyRequest request = new UserGenerateKeyRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("glytoucan@gmail.com");
     
//     UserGenerateKeyResponse result = userEndpoint.generateKey(request);
     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
     assertNotNull(wsResult);
     UserGenerateKeyResponse result = (UserGenerateKeyResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertNotNull(result.getKey());
   }
   
   @Test
   public void testMarshaller() throws IOException {
     UserGenerateKeyRequest request = new UserGenerateKeyRequest();
     Authentication auth = new Authentication();
     auth.setId("1");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
     marshaller.setPackagesToScan(ClassUtils.getPackageName(UserGenerateKeyRequest.class));
     try {
         marshaller.marshal(request, new StreamResult(os));
     } finally {
         if (os != null) {
             os.close();;
         }
     }
     logger.debug(os.toString());
     
   }
}
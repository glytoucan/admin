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
import org.glytoucan.admin.model.ErrorCode;
import org.glytoucan.admin.model.User;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserGenerateKeyRequest;
import org.glytoucan.admin.model.UserGenerateKeyResponse;
import org.glytoucan.admin.model.UserKeyCheckRequest;
import org.glytoucan.admin.model.UserKeyCheckResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.glytoucan.admin.model.UserRegisterRequest;
import org.glytoucan.admin.model.UserRegisterResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
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
////@RunWith(SpringRunner.class)
////@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebIntegrationTest(randomPort = true)
////@IntegrationTest("server.port:0")
//@EnableAutoConfiguration

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
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
	
	String userEmail="aokinobu@gmail.com";
  String token="ya29.GlytA_61fgAisbjaxC8RnpAZC36PMLJm69yC3adMTqDL_Tof6lwrpyudKiymhO0PlsBNXWvNJ-d9uyGToQ9Gn6srY-C4cdG-AEv3jWzRjcV92IXL9TSJqXQq8um6Zw";
//  String apiKey = "JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";
  String apiKey = "b83f8b8040a584579ab9bf784ef6275fe47b5694b1adeb82e076289bf17c2632";
//  String adminEmail = "glytoucan@gmail.com";
  String adminEmail = "815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d";

	@Test
	@Transactional
	public void testInvalidAuth() {
		UserKeyRequest request = new UserKeyRequest();
		Authentication auth = new Authentication();
		auth.setId(adminEmail);
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
		Assert.assertEquals(ErrorCode.AUTHENTICATION_FAILURE.toString(),response.getResponseMessage().getErrorCode());
		Assert.assertNull(response.getKey());
	}
	
//	 @Test maybe setting 
	  public void testTokenRequest() {
	    UserKeyRequest request = new UserKeyRequest();
	    Authentication auth = new Authentication();
	    auth.setId(adminEmail);
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
	     auth.setId(adminEmail);
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
     auth.setId(adminEmail);
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
     auth.setId(adminEmail);
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
     Assert.assertEquals("815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d",result.getUser().getExternalId());
     Assert.assertEquals("",result.getUser().getFamilyName());
   }

   @Test
   @Transactional
   public void testUserKeyCheckRequest() {
     UserKeyCheckRequest request = new UserKeyCheckRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setContributorId(adminEmail);
     request.setApiKey(apiKey);
     marshaller.setPackagesToScan(ClassUtils.getPackageName(UserKeyCheckRequest.class));

     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:8031/ws", request);
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
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setContributorId(adminEmail);
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
   @Transactional
   public void testUserKeyCheckRequestOAuth() {
     UserKeyCheckRequest request = new UserKeyCheckRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setContributorId(userEmail);
     request.setApiKey(token);
     marshaller.setPackagesToScan(ClassUtils.getPackageName(UserKeyCheckRequest.class));

     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:8031/ws", request);
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
   public void testUserKeyCheckRequestContributorDirect() {
     UserKeyCheckRequest request = new UserKeyCheckRequest();
     Authentication auth = new Authentication();
     auth.setId("815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d");
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setContributorId(adminEmail);
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
   
   @Test
   @Transactional
   public void testGenerateHashDirect() {
     UserGenerateKeyRequest request = new UserGenerateKeyRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     request.setPrimaryId("testglytoucan@gmail.com"); // assuming this exists - will be created with other indirect test case 
     
     UserGenerateKeyResponse result = userEndpoint.generateKey(request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
//         + port + "/ws", request);
//     assertNotNull(wsResult);
//     UserGenerateKeyResponse result = (UserGenerateKeyResponse)wsResult;
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
     auth.setId(adminEmail);
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
   
   
   @Test
   @Transactional
   public void testUserAddDirect() {
     UserRegisterRequest request = new UserRegisterRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     User user = new User();
     user.setEmail("testglytoucanfalse@gmail.com");
     user.setEmailVerified("false");
     user.setExternalId("12");
     user.setGivenName("testfalse");
     user.setFamilyName("familyName");
     request.setUser(user);

     UserRegisterResponse result = userEndpoint.userRegisterRequest(request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
//         + port + "/ws", request);
//     assertNotNull(wsResult);
//     UserKeyCheckResponse result = (UserKeyCheckResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertEquals(user.getEmail(), result.getUser().getEmail());
     Assert.assertEquals(user.getGivenName(), result.getUser().getGivenName());
     Assert.assertEquals(user.getExternalId(), result.getUser().getExternalId());
     Assert.assertEquals(user.getEmailVerified(), result.getUser().getEmailVerified());
   }
   
   @Test
   @Transactional
   public void testUserAddVerifiedDirect() {
     UserRegisterRequest request = new UserRegisterRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     User user = new User();
     user.setEmail("testglytoucan11@gmail.com");
     user.setEmailVerified("true");
     user.setExternalId("11");
     user.setGivenName("test11");
     user.setFamilyName("familyName");
     request.setUser(user);

     UserRegisterResponse result = userEndpoint.userRegisterRequest(request);
//     Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
//         + port + "/ws", request);
//     assertNotNull(wsResult);
//     UserKeyCheckResponse result = (UserKeyCheckResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertEquals(user.getEmail(), result.getUser().getEmail());
     Assert.assertEquals(user.getGivenName(), result.getUser().getGivenName());
     Assert.assertEquals(user.getExternalId(), result.getUser().getExternalId());
     Assert.assertEquals(user.getEmailVerified(), result.getUser().getEmailVerified());
   }
   
   @Test
   @Transactional
   public void testUserAdd() {
     UserRegisterRequest request = new UserRegisterRequest();
     Authentication auth = new Authentication();
     auth.setId(adminEmail);
     auth.setApiKey(apiKey);
     request.setAuthentication(auth);
     User user = new User();
     user.setEmail("testglytoucan@gmail.com");
     user.setEmailVerified("true");
     user.setExternalId("10");
     user.setGivenName("test");
     user.setFamilyName("familyName");
     request.setUser(user);

//     UserRegisterResponse result = userEndpoint.register(request);
    Object wsResult = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
         + port + "/ws", request);
     assertNotNull(wsResult);
     UserRegisterResponse result = (UserRegisterResponse)wsResult;
     assertNotNull(result);
     logger.debug(result);
     logger.debug(result.getResponseMessage());
     logger.debug(result.getResponseMessage().getTime());
     Assert.assertEquals("0",result.getResponseMessage().getErrorCode());
     Assert.assertEquals(user.getEmail(), result.getUser().getEmail());
     Assert.assertEquals(user.getGivenName(), result.getUser().getGivenName());
     Assert.assertEquals(user.getExternalId(), result.getUser().getExternalId());
     Assert.assertEquals(user.getEmailVerified(), result.getUser().getEmailVerified());
   }
}
package org.glytoucan.admin.service;

import org.glytoucan.admin.Application;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.ErrorCode;
import org.glytoucan.admin.model.ResponseMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
//@SpringBootTest
public class AuthServiceTest {
  @Autowired
  AuthService authService;
  
  @Test
  @Transactional
  public void testInvalidToken() throws UserException {
    String id="815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d";
    String token="asdf";
    
    Authentication auth = new Authentication();
    auth.setId(id);
    auth.setApiKey(token);
    
    ResponseMessage result = authService.authenticate(auth);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getErrorCode(), ErrorCode.AUTHENTICATION_FAILURE.toString());
    
  }
  
  @Test
  @Transactional
  public void testAdmin() throws UserException {
    String id="815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d";
    String token="JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";
    
    Authentication auth = new Authentication();
    auth.setId(id);
    auth.setApiKey(token);
    
    ResponseMessage result = authService.authenticate(auth);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getErrorCode(), ErrorCode.AUTHENTICATION_FAILURE.toString());
    
  }
  
//  @Test
  @Transactional
  public void testOAuth() throws UserException {
    String id="815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d";
    String token="ya29.CjBXA4l-rJxxG7g2PpaTzo3061sa6KIlLzF6y-SX39VRQjKVRGaWcqoZkvxVb48FX6U";
    
    Authentication auth = new Authentication();
    auth.setId(id);
    auth.setApiKey(token);
    
    ResponseMessage result = authService.authenticate(auth);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getErrorCode(), ErrorCode.AUTHENTICATION_SUCCESS.toString());
  }

//@Test
@Transactional
public void testUserOAuth() throws UserException {
  String id="aokinobu@gmail.com";
  String token="ya29.CjFgA6aE5uZo3zf98k1PKF0uTp4TU2y9n7QveyqR2f9Med8Ma74xhjlGNV44ubN7fskJ";
  
  Authentication auth = new Authentication();
  auth.setId(id);
  auth.setApiKey(token);
  
  ResponseMessage result = authService.authenticate(auth);
  Assert.assertNotNull(result);
  Assert.assertEquals(ErrorCode.AUTHENTICATION_SUCCESS.toString(), result.getErrorCode());
}

}

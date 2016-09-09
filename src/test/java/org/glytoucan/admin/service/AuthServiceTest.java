package org.glytoucan.admin.service;

import org.glytoucan.admin.Application;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.Authentication;
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
    String id="1";
    String token="asdf";
    
    Authentication auth = new Authentication();
    auth.setId(id);
    auth.setApiKey(token);
    
    boolean result = authService.authenticate(auth);
    Assert.assertFalse(result);
    
  }
  
//  @Test
  @Transactional
  public void testOAuth() throws UserException {
    String id="1";
    String token="ya29.CjBXA4l-rJxxG7g2PpaTzo3061sa6KIlLzF6y-SX39VRQjKVRGaWcqoZkvxVb48FX6U";
    
    Authentication auth = new Authentication();
    auth.setId(id);
    auth.setApiKey(token);
    
    boolean result = authService.authenticate(auth);
    Assert.assertTrue(result); 
  }
}

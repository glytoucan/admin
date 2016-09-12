package org.glytoucan.admin.service;

import static java.util.Arrays.asList;
import static org.springframework.security.oauth2.common.AuthenticationScheme.form;

import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.convert.error.ConvertException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.DerivatizedMass;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.ResourceEntry;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.exception.InvalidException;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.ErrorCode;
import org.glytoucan.admin.model.ResponseMessage;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.github.fromi.openidconnect.security.UserInfo;

/**
 * 
 * @author aoki
 * 
 *         This work is licensed under the Creative Commons Attribution 4.0
 *         International License. To view a copy of this license, visit
 *         http://creativecommons.org/licenses/by/4.0/.
 *
 */
@Service
public class AuthService {
  private static final Log logger = LogFactory.getLog(AuthService.class);

  private UserProcedure userProcedure;

  @Autowired
  public AuthService(UserProcedure userProcedure) {
    this.userProcedure = userProcedure;
  }

  /**
   * @param auth
   * @return
   * @throws UserException
   */
  @Transactional
  public ResponseMessage authenticate(Authentication auth) {
    System.out.println("user:>" + auth.getId());
    System.out.println("key:>" + auth.getApiKey());
    String id = auth.getId();

    ResponseMessage rm = new ResponseMessage();
    rm.setErrorCode(ErrorCode.AUTHENTICATION_SUCCESS.toString());
    try {
      if (StringUtils.contains(id, "@")) {
        id = userProcedure.getIdByEmail(id);
      }
      if (!userProcedure.checkApiKey(id, auth.getApiKey())) {
        DefaultOAuth2AccessToken defToken = new DefaultOAuth2AccessToken(auth.getApiKey());
        DefaultOAuth2ClientContext defaultContext = new DefaultOAuth2ClientContext();
        defaultContext.setAccessToken(defToken);
        OAuth2RestOperations rest = new OAuth2RestTemplate(googleOAuth2Details(), defaultContext);
        UserInfo user = null;
        try {
          final ResponseEntity<UserInfo> userInfoResponseEntity = rest
              .getForEntity("https://www.googleapis.com/oauth2/v2/userinfo", UserInfo.class);
          logger.debug("userInfo:>" + userInfoResponseEntity.toString());
          user = userInfoResponseEntity.getBody();
        } catch (HttpClientErrorException e) {
          logger.debug("oauth failed:>" + e.getMessage());
          rm.setErrorCode(ErrorCode.AUTHENTICATION_FAILURE.toString());
          rm.setMessage("oauth failed:>" + e.getMessage());
          return rm;
        }
        String idFromEmail = userProcedure.getIdByEmail(user.getEmail());
        if (!StringUtils.equals(idFromEmail, auth.getId())) {
          rm.setErrorCode(ErrorCode.AUTHENTICATION_FAILURE.toString());
          rm.setMessage("id do not equal:>" + idFromEmail + "<> " + auth.getId());
          return rm;
        }
      } else {
        return rm;
      }
    } catch (UserException e1) {
      rm.setErrorCode(ErrorCode.AUTHENTICATION_FAILURE.toString());
      rm.setMessage("rdf checks failed:>" + e1.getMessage());
      return rm;
    }

    return rm;
  }

  public OAuth2ProtectedResourceDetails googleOAuth2Details() {
    AuthorizationCodeResourceDetails googleOAuth2Details = new AuthorizationCodeResourceDetails();
    googleOAuth2Details.setAuthenticationScheme(form);
    googleOAuth2Details.setClientAuthenticationScheme(form);
    googleOAuth2Details.setClientId(clientId);
    googleOAuth2Details.setClientSecret(clientSecret);
    googleOAuth2Details.setUserAuthorizationUri("https://accounts.google.com/o/oauth2/auth");
    googleOAuth2Details.setAccessTokenUri("https://www.googleapis.com/oauth2/v3/token");
    googleOAuth2Details.setScope(asList("email"));
    return googleOAuth2Details;
  }

  @Value("${admin.email:glytoucan@gmail.com}")
  private String adminEmail;

  @Value("${google.oauth2.clientId}")
  private String clientId;

  @Value("${google.oauth2.clientSecret}")
  private String clientSecret;
}
package com.symphony.bdk.core.auth.impl;

import com.symphony.bdk.core.auth.AuthSessionExtensionApp;
import com.symphony.bdk.core.auth.ExtensionAppAuthenticator;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.gen.api.model.ExtensionAppTokens;

import javax.annotation.Nullable;

/**
 * {@link AuthSessionExtensionApp} impl for rsa extension app authentication mode.
 */
public class AuthSessionExtensionAppRsaImpl implements AuthSessionExtensionApp {

  private final ExtensionAppAuthenticatorRsaImpl authenticator;
  private String symphonySessionToken;
  private String appToken;
  private Long expireAt;

  public AuthSessionExtensionAppRsaImpl(ExtensionAppAuthenticatorRsaImpl authenticator, String appToken) {
    this.authenticator = authenticator;
    this.appToken = appToken;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public String getSymphonyToken() {
    return this.symphonySessionToken;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public String getAppToken() {
    return this.appToken;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public Long expireAt() {
    return this.expireAt;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void refresh() throws AuthUnauthorizedException {
    ExtensionAppTokens appTokens = this.authenticator.retrieveExtensionAppSession(this.appToken);
    this.symphonySessionToken = appTokens.getSymphonyToken();
    this.appToken = appTokens.getAppToken();
    this.expireAt = appTokens.getExpireAt();
  }

  /**
   * This method is only visible for testing.
   */
  protected ExtensionAppAuthenticator getAuthenticator() {
    return authenticator;
  }
}

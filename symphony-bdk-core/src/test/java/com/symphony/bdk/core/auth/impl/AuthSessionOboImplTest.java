package com.symphony.bdk.core.auth.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;

import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Test class for the {@link AuthSessionOboImpl}.
 */
class AuthSessionOboImplTest {

  @Test
  void testRefreshForUsername() throws AuthUnauthorizedException {

    final String sessionToken = UUID.randomUUID().toString();

    final OboAuthenticatorRSAImpl auth = mock(OboAuthenticatorRSAImpl.class);
    when(auth.retrieveOboSessionTokenByUsername(eq("username"))).thenReturn(sessionToken);

    final AuthSessionOboImpl session = new AuthSessionOboImpl(auth, "username");
    session.refresh();

    assertEquals(sessionToken, session.getSessionToken());
    assertNull(session.getKeyManagerToken());

    verify(auth, times(1)).retrieveOboSessionTokenByUsername(eq("username"));
    verify(auth, times(0)).retrieveOboSessionTokenByUserId(anyLong());
  }

  @Test
  void testRefreshForUserId() throws AuthUnauthorizedException {

    final String sessionToken = UUID.randomUUID().toString();

    final OboAuthenticatorRSAImpl auth = mock(OboAuthenticatorRSAImpl.class);
    when(auth.retrieveOboSessionTokenByUserId(eq(1234L))).thenReturn(sessionToken);

    final AuthSessionOboImpl session = new AuthSessionOboImpl(auth, 1234L);
    session.refresh();

    assertEquals(sessionToken, session.getSessionToken());
    assertNull(session.getKeyManagerToken());

    verify(auth, times(1)).retrieveOboSessionTokenByUserId(eq(1234L));
    verify(auth, times(0)).retrieveOboSessionTokenByUsername(anyString());
  }
}
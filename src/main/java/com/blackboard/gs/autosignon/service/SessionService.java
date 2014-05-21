/* ****************************************************************************
 * Copyright (c) 2010, Blackboard Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   -- Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *
 *   -- Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *
 *   -- Neither the name of Blackboard nor the names of its contributors may be
 *        used to endorse or promote products derived from this software
 *        without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ***************************************************************************/

package com.blackboard.gs.autosignon.service;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import blackboard.data.user.User;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.user.UserDbPersister;
import blackboard.platform.context.ContextManager;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.platform.security.authentication.SessionStub;
import blackboard.platform.session.BbSession;
import blackboard.platform.session.BbSessionManagerService;
import blackboard.platform.session.BbSessionManagerServiceFactory;
import blackboard.platform.tracking.TrackingEventManagerFactory;
import blackboard.platform.tracking.data.TrackingEvent;

/**
 * This class provides functionality for creating and associating Blackboard sessions
 * to use with the AutoSignon SSO.
 *
 */
public class SessionService {

	private static final Logger LOG = LogManager.getLogger(SessionService.class);

	// User DB Loader/Persister
	private UserDbLoader uLoader;
	private UserDbPersister uPersister;

	// Context Manager
	private ContextManager contextManager;

	// Session Manager
	private BbSessionManagerService sessionManager;

	private static SessionService instance = null;
	public static synchronized SessionService getInstance() {
		if(null == instance) {
			instance = new SessionService();
		}
		return instance;
	}
	private SessionService() {
		try {
			uLoader = UserDbLoader.Default.getInstance();
			uPersister = UserDbPersister.Default.getInstance();
			contextManager = ContextManagerFactory.getInstance();
			sessionManager = BbSessionManagerServiceFactory.getInstance();
		} catch(Exception e) {
			LOG.error("Error during init, failed to get all services", e);
		}
	}


	/**
	 * Creates a valid Blackboard session associated with the given user.
	 *
	 * @param batchUid
	 *         the <code>batch uid</code> of the user that the Blackboard session will be created for.
	 * @param request
	 * @param response
	 *
	 * @return <code>true</code> if the session was successfully created, <code>false</code> otherwise
	 */
	public boolean createSession(final String batchUid, final HttpServletRequest request, final HttpServletResponse response) {

    // ensure that this user exists and is available
    User user = null;
    try {
      user = uLoader.loadByBatchUid(batchUid);
    } catch(Exception e) {
      user = null;
    }
    if (user == null) {
      try {
        user = uLoader.loadByUserName(batchUid);
      } catch(Exception e) {
        LOG.warn("Authentication requested for invalid user: " + batchUid, e);
        return false;
      }
    }
		if(!user.getIsAvailable()) {
			LOG.debug("Authentication requested for disabled user: " + batchUid);
			return false;
		}

		// call safeGetSession to make sure that a session cookie & db record exist
		sessionManager.safeGetSession(request,response);

        // create a session stub
  		final SessionStub sessionStub;
  		try {
   			sessionStub = new SessionStub(request);
   			sessionStub.associateSessionWithUser(user.getUserName());
   		} catch(Exception e) {
   			LOG.error("Failed to associate session with user: " + batchUid, e);
   			return false;
   		}

   		// refresh session (and context) since we've updated the session
        contextManager.purgeContext();
        contextManager.setContext(request);

  	    // update last login time
   	    updateLastLogin(user);

	    return true;
	}


	/**
	 * Updates last login date of given user to the current system time.
	 *
	 * @param user
	 *         the {@link User} that will be updated
	 */
	private void updateLastLogin(final User user) {
		user.setLastLoginDate(Calendar.getInstance());
		try {
			uPersister.persist(user);
		} catch(Exception e) {
			LOG.error("Failed to update user last access time: " + user.getBatchUid(), e);
		}
		/*
		 * FIX by (@johnfontaine): Add event tracking call to post to activity accumulator
		 * 
		 */
		BbSession session = ContextManagerFactory.getInstance().getContext().getSession();
		TrackingEvent event = new TrackingEvent();
	    event.setType( TrackingEvent.Type.LOGIN_ATTEMPT );
	    event.setStatus( TrackingEvent.Status.SUCCESS );
	    event.setData( "Login succeeded." );
	    event.setSessionId( session.getBbSessionId() );
	    event.setUserId( user.getId() );
	    TrackingEventManagerFactory.getInstance().postTrackingEvent( event );
		
	}
}

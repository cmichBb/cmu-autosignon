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

package com.blackboard.gs.autosignon.struts.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseMembership.Role;
import blackboard.persist.Id;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.context.ContextManagerFactory;

import com.blackboard.gs.autosignon.AutoSignonConfig;
import com.blackboard.gs.autosignon.service.SecurityService;
import com.blackboard.gs.autosignon.service.SessionService;
import java.util.Calendar;

/**
 * This class provides point-to-point single sign-on (SSO) between a trusted system and Blackboard.
 * Incoming SSO requests are received through the following URL:
 *
 * New installations of 9.1:
 * http://<host>/webapps/bbgs-autosignon-BBLEARN/autoSignon.do
 *
 * Systems upgraded to 9.1:
 * http://<host>/webapps/bbgs-autosignon-bb_bb60/autoSignon.do
 *
 * with the proper SSO request parameters and message authentication code appended. Once
 * an SSO request has been validated, the user is either redirected to a given URL, Blackboard
 * course, or the default landing page (My Institution tab) based on the SSO request. See
 * AutoSignon Administration Manual for more information.
 */
public class AutoSignonAction extends MappingDispatchAction {

    private static final Logger LOG = LogManager.getLogger(AutoSignonAction.class);

    // internal services
    private final SecurityService       securityService;
    private final SessionService        sessionService;

    // blackboard loaders
    private CourseDbLoader courseLoader;
    private CourseCourseDbLoader courseCourseLoader;
    private CourseMembershipDbLoader courseMembershipLoader;

    public AutoSignonAction() {
        securityService = SecurityService.getInstance();
        sessionService  = SessionService.getInstance();
        try {
            courseLoader = CourseDbLoader.Default.getInstance();
        } catch(Exception e) {
            LOG.error("Failed during init of action class, services not available", e);
        }
        try {
            courseCourseLoader = CourseCourseDbLoader.Default.getInstance();
        } catch(Exception e) {
            LOG.error("Failed during init of action class, services not available", e);
        }
        try {
            courseMembershipLoader = CourseMembershipDbLoader.Default.getInstance();
        } catch(Exception e) {
            LOG.error("Failed during init of action class, services not available", e);
        }   
    }

    /**
     * Entry point for performing AutoSignon.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward autoSignon(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        // is SSO enabled?
        if(AutoSignonConfig.isSsoEnabled()) {

            if(securityService.validateMacInRequest(request)) {

                // create a session for the user
                final String userId = request.getParameter(AutoSignonConfig.getUserIdRequestParameter());
                if(sessionService.createSession(userId, request, response)) {

                    // determine the correct forwarding location and forward
                    response.sendRedirect(generateForwardUrl(request));
                    return null;

                }

            }

        }

        return mapping.findForward("accessDenied");

    }

    /**
     * This method checks the configured Course Forward setting and looks up a course_id for a batch_uid.
     * This is externalized so that it can be used separately from auto-signon.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward courseForward(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final String courseForward = generateCourseForwardUrl(request);

        if(null != courseForward) {
            response.sendRedirect(generateCourseForwardUrl(request));
            return null;
        }

        return mapping.findForward("accessDenied");

    }

    /**
     * Internal method to generate a URL for forwarding based on the configured options
     * and the parameters provided.
     *
     * @param request
     * @return
     */
    private String generateForwardUrl(final HttpServletRequest request) {

        // try to generate a course forward url
        final String courseForward = generateCourseForwardUrl(request);
        if(null != courseForward) {
            return courseForward;
        }

        // standard forward applies next
        final String forward = request.getParameter(AutoSignonConfig.getForwardRequestParameter());
        if(null != forward) {
            return forward;
        }

        // otherwise, forward to home
        return AutoSignonConfig.BB_FORWARD_HOME;

    }

    /**
     * Internal method to generate a URL for course forwarding based on the configured options
     * and the parameters provided. Will forward to home if course is not valid.
     *
     * @param request
     * @return the generated URL for course forwarding
     */
    private String generateCourseForwardUrl(final HttpServletRequest request) {

        // course forward takes precendence if it is present
        final String courseForward = request.getParameter(AutoSignonConfig.getCourseIdRequestParameter());
        if(null != courseForward) {

            // Begin modified code by John Madrak <madrak@lasalle.edu> to support cross listing, detection of unavailable courses, etc
            Course forwardedCourse;
            Course courseToCheck;
            Id courseID;
            Calendar today = Calendar.getInstance();
            Calendar startDate = null;
            Calendar endDate = null;
            Role courseRole;
            try {
                forwardedCourse = courseLoader.loadByBatchUid(courseForward);  
                courseID = forwardedCourse.getId();
                startDate = forwardedCourse.getStartDate();
                endDate = forwardedCourse.getEndDate();
            } catch(Exception e) {
                LOG.warn("Course invalid: " + courseForward, e);
                return null;
            }
            try {
                ContextManagerFactory.getInstance().setContext(request);
                Id userID = ContextManagerFactory.getInstance().getContext().getUserId();
                courseRole = courseMembershipLoader.loadByCourseAndUserId(courseID, userID).getRole();
            } catch(Exception e) {
                LOG.warn("Could not retrieve course role" + courseForward, e);
                return null;
            }
            if(forwardedCourse.isChild() == true && !courseRole.equals(CourseMembership.Role.INSTRUCTOR)){
                try {
                    courseID = courseCourseLoader.loadParent(courseID).getParentCourseId();
                    courseToCheck = courseLoader.loadByCourseId(courseID.toString());
                } catch(Exception e) {
                    LOG.warn("Could not load parent " + courseForward, e);
                    return null;
                }
            } else {
                courseToCheck = forwardedCourse;
            }
            if(courseToCheck.getDurationType() == Course.Duration.CONTINUOUS && courseToCheck.getIsAvailable() == false && (!courseRole.equals(CourseMembership.Role.INSTRUCTOR)||!courseRole.equals(CourseMembership.Role.TEACHING_ASSISTANT)||!courseRole.equals(CourseMembership.Role.COURSE_BUILDER)||!courseRole.equals(CourseMembership.Role.GRADER))){
                return "course_notstarted.jsp";
            } else if(startDate!=null && courseToCheck.getDurationType() == Course.Duration.DATE_RANGE && startDate.after(today) && (!courseRole.equals(CourseMembership.Role.INSTRUCTOR)||!courseRole.equals(CourseMembership.Role.TEACHING_ASSISTANT)||!courseRole.equals(CourseMembership.Role.COURSE_BUILDER)||!courseRole.equals(CourseMembership.Role.GRADER))){
                return "course_notstarted.jsp";
            } else if(endDate!=null && courseToCheck.getDurationType() == Course.Duration.DATE_RANGE && endDate.before(today) && (!courseRole.equals(CourseMembership.Role.INSTRUCTOR)||!courseRole.equals(CourseMembership.Role.TEACHING_ASSISTANT)||!courseRole.equals(CourseMembership.Role.COURSE_BUILDER)||!courseRole.equals(CourseMembership.Role.GRADER))){
                return "course_over.jsp";                
            } else {
                return AutoSignonConfig.BB_COURSE_HOME + courseID.toExternalString();
            }
            //End modified code

        }

        return null;

    }

}
<%--
/*
  file:		course_unavailable.jsp
  project:	AutoSignOn
  description:
  	A Building Block Developed to allow autosignon into courses
  author:	John Madrak (madrak@lasalle.edu)
  date:		2012-02-08
  version:	0.0.5
  version history:
  	date :: note
*/
--%>

<%@ page	language="java"
		import="java.util.*,
                        java.text.*,
			blackboard.base.*"
		pageEncoding="UTF-8"
%>
<%@ taglib uri="/bbNG" prefix="bbNG" %>

<%@ taglib uri="/bbData" prefix="bbData" %>
<bbNG:learningSystemPage>
    <div style="width: 550px; margin: 0 auto; border: 1px solid #ccc; border-radius: 5px; background: #eee; height: 130px; padding: 25px;">
        <div style="float: left;"><img src="images/error.png" style="height: 128px; width: 128px;" /></div>
        <div style="float: right; font-size: 24px; padding-top: 50px;">This course has not started yet</div>
    </div>
</bbNG:learningSystemPage>
<%!

%>
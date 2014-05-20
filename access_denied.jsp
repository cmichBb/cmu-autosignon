<%--
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
--%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbNG"    prefix="bbNG"%>

<bbNG:genericPage authentication="N" title="Single Sign-On Error">

<bbNG:receipt type="FAIL"
              title="Single Sign-On Error"
              recallUrl="/">
There was a problem attempting to single sign-on. <a href="/">You will be redirected to the Blackboard login page in five seconds, or you can click here to be redirected now</a>.

<script type="text/javascript">
// http://stackoverflow.com/a/16541769
var url = "/?audience=bblocal";
var delay = "5000";

window.onload = function ()
{
    DoTheRedirect()
}
function DoTheRedirect()
{
    setTimeout(GoToURL, delay)
}
function GoToURL()
{
    // IE8 and lower fix
    if (navigator.userAgent.match(/MSIE\s(?!9.0)/))
    {
        var referLink = document.createElement("a");
        referLink.href = url;
        document.body.appendChild(referLink);
        referLink.click();
    }

    // All other browsers
    else { window.location.replace(url); }
}
</script>

</bbNG:receipt>

</bbNG:genericPage>
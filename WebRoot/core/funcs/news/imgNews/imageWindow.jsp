<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String id = request.getParameter("id");
String contextPath = request.getContextPath();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>图片浏览</title>
<script src="js/AC_OETags.js" language="javascript"></script>
<script language="JavaScript" type="text/javascript">
<!--
// -----------------------------------------------------------------------------
// Globals
// Major version of Flash required
var requiredMajorVersion =9;
// Minor version of Flash required
var requiredMinorVersion = 0;
// Minor version of Flash required
var requiredRevision = 124;
// -----------------------------------------------------------------------------
// -->
function setTitle(title) {
  document.title = title;
}
</script>
</head>
<body style="margin:0px;padding:0px;voerflow:auto">
<script language="JavaScript" type="text/javascript">
<!--
// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
var hasProductInstall = DetectFlashVer(6, 0, 65);

// Version check based upon the values defined in globals
var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

if ( hasProductInstall && !hasRequestedVersion ) {
  // DO NOT MODIFY THE FOLLOWING FOUR LINES
  // Location visited after installation is complete if installation is required
  var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
  var MMredirectURL = window.location;
    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
    var MMdoctitle = document.title;

  AC_FL_RunContent(
    "src", "playerProductInstall",
    "FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"&path=<%=contextPath %>&newsId=<%=id %>",
    "width", "100%",
    "height", "900",
    "align", "middle",
    "id", "imageView",
    "quality", "high",
    "bgcolor", "#000000",
    "name", "imageView",
    "allowScriptAccess","sameDomain",
    "type", "application/x-shockwave-flash",
    "pluginspage", "http://www.adobe.com/go/getflashplayer"
  );
} else if (hasRequestedVersion) {
  // if we've detected an acceptable version
  // embed the Flash Content SWF when all tests are passed
  AC_FL_RunContent(
      "FlashVars","path=<%=contextPath %>&newsId=<%=id %>",
      "src", "imageView",
      "width", "100%",
      "height", "900",
      "align", "middle",
      "id", "imageView",
      "quality", "high",
      "bgcolor", "#000000",
      "name", "imageView",
      "allowScriptAccess","sameDomain",
      "allowFullScreen","true",
      "type", "application/x-shockwave-flash",
      "pluginspage", "http://www.adobe.com/go/getflashplayer"
  );
  } else {  // flash is too old or we can't detect the plugin
    var alternateContent = 'Alternate HTML content should be placed here. '
    + 'This content requires the Adobe Flash Player. '
    + '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
    document.write(alternateContent);  // insert non-flash content
  }
// -->
</script>
<noscript>
    <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
      id="imageView" width="100%" height="800"
      codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
      <param name="movie" value="imageView.swf?path=<%=contextPath %>&newsId=<%=id %>" />
      <param name="quality" value="high" />
      <param name="bgcolor" value="#000000" />
      <param name="allowScriptAccess" value="sameDomain" />
      <embed src="imageView.swf" quality="high" bgcolor="#000000"
        width="100%" height="800" name="imageView" align="middle"
        play="true"
        loop="false"
        quality="high"
        allowScriptAccess="sameDomain"
        allowFullScreen="true"
        type="application/x-shockwave-flash"
        pluginspage="http://www.adobe.com/go/getflashplayer">
      </embed>
  </object>
  </noscript>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="t9.core.global.T9SysProps" %>
<%@ page import="t9.core.funcs.person.data.T9Person" %>
<%
String contextPath = request.getContextPath();
if (contextPath.equals("")) {
  contextPath = "/t9";
}
T9Person loginPerson = (T9Person)session.getAttribute("LOGIN_USER");
%>
<!doctype html>
<html>
<head>
<title><%=  T9SysProps.getString("productName") %></title>
<meta name="viewport" content="width=device-width" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<link rel="stylesheet" type="text/css" href="<%=contextPath %>/pda/style/list.css" />
</head>
<body>
<div id="list_top">
  <div class="list_top_left"><a class="ButtonBack" href="<%=contextPath %>/pda/main.jsp?P=<%=loginPerson.getSeqId() %>"></a></div>
  <span class="list_top_center">区号邮编查询</span>
  <div class="list_top_right"><a class="ButtonB" href="javascript:document.form1.submit();">查询</a></div>
</div>
<div id="list_main" class="list_main">
<form action="<%=contextPath %>/t9/pda/telNo/act/T9PdaTelNoAct/search.act"  method="get" name="form1">
   市/区/县/街道的名称包含：<br>
   <input type="text" name="area" size="22"><br><br>
   区号：<br>
   <input type="text" name="telNo" size="22"><br><br>
   邮编：<br>
   <input type="text" name="postNo" size="22"><br><br>
   <input type="hidden" name="p" value="<%=loginPerson.getSeqId() %>">
</form>
</div>
<div id="list_bottom">
  <div class="list_bottom_right"><a class="ButtonHome" href="<%=contextPath %>/pda/main.jsp?P=<%=loginPerson.getSeqId() %>"></a></div>
</div>
</body>
<script type="text/javascript" src="<%=contextPath %>/pda/js/logic.js"></script>
</html>

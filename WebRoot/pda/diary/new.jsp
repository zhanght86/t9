<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="t9.core.global.T9SysProps" %>
<%@ page import="t9.core.funcs.person.data.T9Person" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="t9.core.util.T9Utility" %>
<%
String contextPath = request.getContextPath();
if (contextPath.equals("")) {
  contextPath = "/t9";
}
T9Person loginPerson = (T9Person)session.getAttribute("LOGIN_USER");
Date date = new Date();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
String dateStr = sdf.format(date);

String diaType = (String)request.getParameter("diaType");
String content = (String)request.getParameter("content");
String day = (String)request.getParameter("day");

if (!T9Utility.isNullorEmpty(day)) {
  dateStr = day;
}

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
  <div class="list_top_left"><a class="ButtonBack" href="<%=contextPath %>/t9/pda/diary/act/T9PdaDiaryAct/doint.act?P=<%= loginPerson.getSeqId()%>"></a></div>
  <span class="list_top_center">今日日志</span>
  <div class="list_top_right"><a class="ButtonA" href="javascript:document.form1.submit();">保存</a></div>
</div>
<div id="list_main" class="list_main">
<form action="<%=contextPath %>/t9/pda/diary/act/T9PdaDiaryAct/newDiary.act"  method="post" name="form1">
   日志类型:<br>
 <select name="diaType" class="BigSelect" style="width:100%;" >
   
   <option value="1" <% if (diaType == null || "1".equals(diaType) || "".equals(diaType)) {%> selected<%} %>>工作日志</option>
   <option value="2" <% if ("2".equals(diaType)) {%> selected<%} %>个人日志</option>
 </select><br>
 日期：<input type="text" style="width:100%;" name="day" value="<%=dateStr %>" /> <span style="color:red"><%if (!T9Utility.isNullorEmpty(day)) { %>日期格式不对！<% } %></span>  <br>
   日志内容:<br>
<textarea cols="18" name="content" rows="6" wrap="on" style="width:100%;"><%=(content == null ? "" : content) %></textarea>
<br>
<input type="hidden" name="P" value="<%=loginPerson.getSeqId() %>">
</form>
</div>
<div id="list_bottom">
  <div class="list_bottom_right"><a class="ButtonHome" href="<%=contextPath %>/pda/main.jsp?P=<%=loginPerson.getSeqId() %>"></a></div>
</div>
</body>
<script type="text/javascript" src="<%=contextPath %>/pda/js/logic.js"></script>
</html>
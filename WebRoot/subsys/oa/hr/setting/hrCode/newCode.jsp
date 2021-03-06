<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/core/inc/header.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>显示CODE_CLASS的信息</title>
<link rel="stylesheet" href = "<%=cssPath %>/style.css">
<script type="text/Javascript" src="<%=contextPath%>/core/js/datastructs.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/sys.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/prototype.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/smartclient.js" ></script>
<script type="text/javascript">
function doInit(){
  if(checkForm()){
    var pars = $('form1').serialize() ;
    var url = "<%=contextPath %>/t9/subsys/oa/hr/setting/act/T9HrCodeAct/addCode.act";
    var rtJson = getJsonRs(url, pars);
    if (rtJson.rtState != "0") {
      alert(rtJson.rtMsrg);
    }
    var prc = rtJson.rtData;
    if(prc.errer == '1'){
      alert("主分类代码编号重复！");
      $("codeNo").focus();
      $("codeNo").select();
    }else{
      alert("添加成功！");
      window.location.reload();
      parent.navigateFrame.location.reload();
    }
  }
}
function checkForm(){
  if($("codeNo").value == ''){
    alert("代码编号是必填项！");
    $("codeNo").focus();
    $("codeNo").select();
    return false;
  }
  if($("codeOrder").value == ''){
    $("codeOrder").focus();
    $("codeOrder").select();
    alert("代码排序号是必填项！");
    return false;
  }
  if($("codeName").value == ''){
    $("codeName").focus();
    $("codeName").select();
    alert("代码描述是必填项！");
    return false;
  }
  return true;
}
</script>
<body onload="">
<form name="form1" id="form1" method="post">
   
    <h2><img src="<%=contextPath %>/core/styles/imgs/green_plus.gif"></img>添加代码主分类</h2> 
 
  <input type="hidden" name="sqlId" id="sqlId" value=""></input>
  <input type="hidden" id="classNofirst" name="classNofirst" value=""/><br/>
<table class="TableBlock" cellscpacing="1" cellpadding="3" width="300" align="center">
  <tr>
    <td class="TableData">代码编号:</td>
    <td class="TableData"><font style="color:red">*</font><input type="text" id="codeNo" name="codeNo" id="codeNo" class="SmallInput" value=""/></td>
  </tr>
  <tr>
    <td class="TableData">排序号:</td>
    <td class="TableData"><font style="color:red">*</font><input type="text" id="codeOrder" name="codeOrder" class="SmallInput" value=""/></td>
  </tr>
  <tr>
    <td class="TableData">代码描述:</td>
    <td class="TableData"><font style="color:red">*</font><input type="text" id="codeName" name="codeName" class="SmallInput" value=""/></td>
  </tr>
  <tr>
    <td class="TableData">代码级别:</td>
    <td class="TableData">用户自定义:<input type="radio" name="codeFlag" id="codeFlag1" value="1" checked/>
  	          系统预定义:<input type="radio" name="codeFlag"  id="codeFlag0" value="0"/>
  	</td>
  </tr>
  <tr>
    <td nowrap  class="TableControl" colspan="2" align="center">
      <input type="button" value="提交" class="SmallButton" onclick="doInit()">
    </td>
  </tr>
</table>
</form>
</body>
</html>
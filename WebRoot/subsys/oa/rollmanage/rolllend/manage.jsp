<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ include file="/core/inc/header.jsp" %>
<%@ page import="java.text.*" %>
<%@ page import="t9.core.global.T9RegistProps" %>
<%@ page import="t9.core.data.T9AuthKeys" %>
<%
  boolean regist = T9Utility.isNullorEmpty(T9RegistProps.getProp(T9AuthKeys.REGIST_ORG + ".t9"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>案卷借阅</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="<%=cssPath %>/cmp/tab.css" type="text/css" />
<link rel="stylesheet" href="<%=cssPath%>/page.css">
<link rel="stylesheet" href = "<%=cssPath%>/style.css">
<script type="text/javascript" src="<%=contextPath %>/core/js/prototype.js"></script>
<script type="text/javascript" src="<%=contextPath %>/core/js/datastructs.js"></script>
<script type="text/javascript" src="<%=contextPath %>/core/js/sys.js"></script>
<script type="text/javascript" src="<%=contextPath %>/core/js/smartclient.js"></script>
<script type="text/javascript" src="<%=contextPath%>/core/js/cmp/page.js"></script>
<script type="text/javascript" src="<%=contextPath %>/subsys/oa/rollmanage/js/util.js"></script>
<script type="text/javascript" src="<%=contextPath %>/subsys/oa/rollmanage/js/rmsrolllend.js"></script>
<script type="text/javascript" src="<%=contextPath %>/subsys/oa/rollmanage/js/rmsrolllogic.js"></script>
<script type="text/javascript">
var regist = "<%=regist%>";
var pageMgr = null;
function doInit(){
  if(regist){
    $("registDiv").style.display = '';
  }else{
    $("registDiv").style.display = 'none';
  }
  checkSelectBox();
  var url = "<%=contextPath%>/t9/subsys/oa/rollmanage/act/T9RmsRollAct/getRmsRollJosn.act?seqId=${param.rollId}&flag=1";
  var cfgs = {
      dataAction: url,
      container: "listContainer",
      sortIndex: 1,
      sortDirect: "desc",
      colums: [
         {type:"hidden", name:"seqId", text:"顺序号", dataType:"int"},
         {type:"data", name:"rollCode",  width: '10%', text:"案卷号", render:rollCodeFunc, sortDef:{type: 0, direct:"desc"}},       
         {type:"data", name:"rollName",  width: '10%', text:"案卷名称", render:rollCenterFunc, sortDef:{type: 0, direct:"desc"}},
         {type:"data", name:"roomId",  width: '10%', text:"所属卷库", render:getRmsRollRoomNameFunc, sortDef:{type: 0, direct:"desc"}},
         {type:"data", name:"categoryNo",  width: '10%', text:"全宗号", render:rollCenterFunc, sortDef:{type: 0, direct:"desc"}},
         {type:"data", name:"certificateKind",  width: '10%', text:"凭证类别", render:getCodeNameKindFunc, sortDef:{type: 0, direct:"desc"}},
         {type:"data", name:"secret",  width: '10%', text:"案卷密级", render:getCodeNameSecretFunc, sortDef:{type: 0, direct:"desc"}},
         {type:"data", name:"status",  width: '15%', text:"案卷状态",render:rollStatusFunc}, 
         {type:"selfdef", text:"操作", width: '15%',render:optsLend}]
    };
    pageMgr = new T9JsPage(cfgs);
    pageMgr.show();
    var total = pageMgr.pageInfo.totalRecord;
    if(total){
      count = total;
      showCntrl('listContainer');
      var mrs = " 共 "+ total + " 条记录 ！";
      showCntrl('delOpt');
    }else{
      WarningMsrg('无已发布的案卷', 'msrg');
    }

}

function checkSelectBox(){
  var url = "<%=contextPath%>/t9/subsys/oa/rollmanage/act/T9RmsStatisticAct/getRmsStatisticSelect.act";
  var rtJson = getJsonRs(url);
  var selectObj =  $('rollId');
  var rollId='${param.rollId}';
  //selectObj.length = 2;
  if(rtJson.rtState == "0"){
   for(var i = 0; i < rtJson.rtData.length; i++){
     var option = document.createElement("option");
     option.value = rtJson.rtData[i].seqId;
     option.innerHTML = rtJson.rtData[i].rommName;
     selectObj.appendChild(option);
     if(rollId && (rollId == rtJson.rtData[i].seqId)){
       option.selected = true;
     }
   }
  }
}

function changeBox(dom){
  var rollId = dom.value;
  window.location = contextPath + "/subsys/oa/rollmanage/rolllend/manage.jsp?rollId="+rollId;
}
</script>
</head>
<body topmargin="5" onload="doInit()">
<table border="0" width="100%" cellspacing="0" cellpadding="3" class="small">
  <tr>
    <td class="Big"><img src="<%=imgPath%>/notify_open.gif" align="absmiddle"><span class="big3"> 案卷借阅</span>&nbsp;
    <select id="rollId" name="rollId" onChange="changeBox(this);">
        <option value="">所有卷库</option>
     </select>
    </td>
  </td>
    </tr>
</table>
<br>
<div id="listContainer" style="display:none;width:100;">
</div>

<div id="msrg">
</div>
<br><br><div id="registDiv" style="display:'none'" align=center style='font-size:9pt;color:gray'></div>
</body>
</html>
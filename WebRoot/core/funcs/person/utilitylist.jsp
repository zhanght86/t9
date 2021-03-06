<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/core/inc/header.jsp" %>
<%@ page import="t9.core.funcs.person.data.T9Person" %>
<%
  T9Person loginUser = (T9Person)request.getSession().getAttribute(T9Const.LOGIN_USER);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>显示各种功能</title>
<link rel="stylesheet" href = "<%=cssPath %>/style.css">
<link rel="stylesheet" href = "<%=cssPath %>/menu_left.css">
<script type="text/Javascript" src="<%=contextPath%>/core/js/datastructs.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/sys.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/prototype.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/smartclient.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/cmp/select.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/cmp/DTree1.0.js" ></script>
<script type="text/Javascript" src="<%=contextPath%>/core/js/cmp/MenuList.js" ></script>
<script type="text/Javascript">	
var loginUserId = <%=loginUser.getSeqId()%>;
var isAdmin = <%=loginUser.isAdminRole()%>;
var postPrivs = <%=loginUser.getPostPriv()%>;
var tree =  "";
var index = "";
function doInit(){ 
  var data = {
        fix:true,
    	panel:'menuList',
    	data:[{title:'在职人员', action:getTree},
          {title:'用户查询或导出', action:getQuery}
    	  ]
          //{title:'导出RTX格式'}
       };
   if(postPrivs == "1"){
    data.data.push({title:'离职人员/外部人员', action:getUserNew});
   }
   if(isAdmin){
     data.data.push({title:'用户导入', action:getImport});
     data.data.push({title:'批量用户个性设置', action:getSet});
   }

   //是否启用动态密码卡
   var url = "<%=contextPath%>/t9/core/funcs/system/security/act/T9SecurityAct/getSecritySecureKey.act";
   var rtJson = getJsonRs(url);
   if (rtJson.rtState == "0") {
     bindJson2Cntrl(rtJson.rtData);
     //loginSecureKey = document.getElementById("paraName").value;
     if(rtJson.rtData.paraValue == 1){
    	 data.data.push({title:'动态密码卡管理', action:getSecureCard});
     }
   }else {
     alert(rtJson.rtMsrg); 
   }

   
   var menu = new MenuList(data);
   index = menu.getContainerId(1);
   // Ino = menu.getContainerId(2);
   menu.showItem(this,{},1);
   getTree();

      
 // tree = new DTree({bindToContainerId:'deplist'
 //   , requestUrl:'<%=contextPath%>/t9/core/funcs/person/act/T9PersonAct/getTree.act?id='
 //   , isOnceLoad:false
 //   , treeStructure:{isNoTree:false}
 //   , linkPara:{clickFunc:getChildOrEdit}
 // });
 //tree.show(); 
// var node = tree.getFirstNode();
// tree.open(node.nodeId);
}

function getTree(){
  $(index).update("");
  tree = new DTree({bindToContainerId:index
    , requestUrl:'<%=contextPath%>/t9/core/module/org_select/act/T9OrgSelectModule/getPersonTree.act?id='
    , isOnceLoad:false
    , treeStructure:{isNoTree:false}
    , linkPara:{clickFunc:getChildOrEdit}
    , isUserModule:true
    , isHaveTitle:true
  });
	tree.show(); 
	tree.open("organizationNodeId");
}

function getUserNew(){
  var person = window.parent.contentFrame;
  var deptIdStr = 0;
  person.location = "<%=contextPath%>/core/funcs/person/usernew.jsp?deptId="+deptIdStr;
}

function getQuery(){
  var person = window.parent.contentFrame;
  person.location = "<%=contextPath%>/core/funcs/person/query.jsp";
}

function getImport(){
  var flag = "1";
  var person = window.parent.contentFrame;
  person.location = "<%=contextPath%>/core/funcs/person/importPerson.jsp?flag="+flag;
}

function getSet(){
  var person = window.parent.contentFrame;
  person.location = "<%=contextPath%>/core/funcs/person/set.jsp";
}

function getSecureCard(){
	  var person = window.parent.contentFrame;
	  person.location = "<%=contextPath%>/core/funcs/person/secureCard.jsp";
	}

function getChildOrEdit(id){
  var dept = tree.getNode(id);
  var userName = dept.name;
  var userId = dept.extData;
  if(id == "organizationNodeId"){
    return;
  }
  if(id.indexOf('r') == -1){
//	parent.contentFrame.location = "<%=contextPath%>/t9/core/funcs/person/act/T9PersonAct/listInDutyPerson.act?id="+ id;
   var urls = "<%=contextPath%>/t9/core/funcs/person/act/T9PersonAct/getPostPrivDept.act";
   var rtJsons = getJsonRs(urls, "deptId="+id);
   if(rtJsons.rtState == '0'){
     if(rtJsons.rtData[0].isPriv){
       parent.contentFrame.location = "<%=contextPath%>/core/funcs/person/usernew.jsp?deptId="+ id;
       }
 //    if(rtJsons.rtData[0].postPriv == "0"){
 //      if(rtJsons.rtData[0].loginDeptId == id){
 //        parent.contentFrame.location = "<%=contextPath%>/core/funcs/person/usernew.jsp?deptId="+ id;
 //      }
 //    }else if(rtJsons.rtData[0].postPriv == "2"){
 //      var postDeptStr = rtJsons.rtData[0].postDept.split(',');
  //     for(var i = 0; i < postDeptStr.length; i++){
  //       if(postDeptStr[i] == id){
 //          parent.contentFrame.location = "<%=contextPath%>/core/funcs/person/usernew.jsp?deptId="+ id;
  //       }
  //     }
  //   }else{
       //parent.contentFrame.location = "<%=contextPath%>/core/funcs/person/usernew.jsp?deptId="+ id;
 //    }
   }
  }else{   
	id = id.substring(1, id.length);
	parent.contentFrame.location = "<%=contextPath%>/core/funcs/person/indutypersoninput.jsp?seqId="+ id +"&userName="+encodeURI(userName)+"&userId="+userId;
  }
}
</script>
</head>
<body onload="doInit()" style="overflow-x:hidden;">
<div id="menuList"></div>
 <!--  <table cellscpacing="1" cellpadding="3" width="98%">
  <tr class="TableLine2">
    <td align="center">
      <a href="<%=contextPath%>/t9/core/funcs/person/act/T9PersonAct/listOffDutyPerson.act" target="contentFrame" title="离职人员/外部人员"><span>离职人员/外部人员</span></a>
    </td>
  </tr>
</table>-->
</body>
</html>
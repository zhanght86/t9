package t9.core.funcs.notify.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import t9.core.funcs.dept.data.T9Department;
import t9.core.funcs.news.data.T9News;
import t9.core.funcs.notify.data.T9Notify;
import t9.core.funcs.notify.data.T9NotifyCont;
import t9.core.funcs.person.data.T9Person;
import t9.core.funcs.person.logic.T9UserPrivLogic;
import t9.core.funcs.workflow.util.T9WorkFlowUtility;
import t9.core.global.T9Const;
import t9.core.global.T9SysPropKeys;
import t9.core.global.T9SysProps;
import t9.core.util.T9Out;
import t9.core.util.T9Utility;
import t9.core.util.db.T9DBUtility;
import t9.core.util.db.T9ORM;
/**
 * 我的办公桌里公告通知
 * @author qwx110
 *
 */
public class T9NotifyShowLogic {
  private static Logger log = Logger.getLogger("t9.core.act.action.T9TestAct");
  private T9NotifyManageUtilLogic newsManageUtil = new T9NotifyManageUtilLogic();

  private T9UserPrivLogic userPrivLogic = new T9UserPrivLogic();
  //未读公告
  public String getnotifyNoReadList(Connection conn,T9Person loginUser,int showLen,String type,String ascDesc,String field, 
      int pageIndex)throws Exception{
    Statement stmt = null;
    ResultSet rs = null;
    boolean temp = false;//判断是否阅读过
    int count = 0;//查询出来的记录数
    int pageCount = 0;//页码数
    int recordCount = 0;//总记录数
    int pgStartRecord = 0;//开始索引
    int pgEndRecord = 0;//结束索引
    int loginDeptId = loginUser.getDeptId();
    String userPriv = loginUser.getUserPriv();
    int seqUserId = loginUser.getSeqId();
    Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StringBuffer sb  = new StringBuffer();
    //T9Out.println("userPriv="+userPriv+"-------"+"seqUserId:"+seqUserId+"--------");
    sb.append("{");
    sb.append("listData:[");
    try{//是发布状态，而且不是待发布，且不是已终止状态，而且属于发布范围之内，而且未读过      String dbms = T9SysProps.getString(T9SysPropKeys.DBCONN_DBMS);
      String queryNotifySql = null;
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        queryNotifySql = "SELECT SEQ_ID,FROM_ID,TO_ID,SUBJECT,[TOP],PRIV_ID,USER_ID,READERS,"
          +"TYPE_ID,SEND_TIME,FORMAT from NOTIFY  where  BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and (END_DATE>="+T9DBUtility.currDateTime()
          +" or END_DATE is null) and PUBLISH='1' and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "TO_ID") 
          + " or " + T9DBUtility.findInSet(userPriv,"PRIV_ID") +" or " 
          + T9DBUtility.findInSet(Integer.toString(seqUserId),"USER_ID") + " or "+T9DBUtility.findInSet("0", "TO_ID")+") "
          + " and ("+ T9DBUtility.findNoInSet(Integer.toString(seqUserId),"READERS")+" or READERS is NULL) ";
      }else {
        queryNotifySql = "SELECT SEQ_ID,FROM_ID,TO_ID,SUBJECT,TOP,PRIV_ID,USER_ID,READERS,"
          +"TYPE_ID,SEND_TIME,FORMAT from NOTIFY  where  BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and (END_DATE>="+T9DBUtility.currDateTime()
          +" or END_DATE is null) and PUBLISH='1' and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "TO_ID") 
          + " or " + T9DBUtility.findInSet(userPriv,"PRIV_ID") +" or " 
          + T9DBUtility.findInSet(Integer.toString(seqUserId),"USER_ID") + " or "+T9DBUtility.findInSet("0", "TO_ID")+") "
          + " and ("+ T9DBUtility.findNoInSet(Integer.toString(seqUserId),"READERS")+" or READERS is NULL) ";
      }
      
      if("".equals(type)){//选择“无类型“
        queryNotifySql = queryNotifySql + " and (TYPE_ID='' or TYPE_ID=' ' or TYPE_ID is null)";
      }else if(!"0".equals(type)) {//选择的不是”所有类型“
        queryNotifySql = queryNotifySql + " and (TYPE_ID='"+ type + "')";
      }
      
      if("".equals(field)) {
        if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
          queryNotifySql = queryNotifySql + " order by [TOP] desc,SEND_TIME desc";
        }else {
          queryNotifySql = queryNotifySql + " order by TOP desc,SEND_TIME desc";
        }
        
      }else {
        if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
          queryNotifySql = queryNotifySql + " order by [TOP] desc," + field;
        }else {
          queryNotifySql = queryNotifySql + " order by TOP desc," + field;
        }
        
        if("1".equals(ascDesc)) {
          queryNotifySql = queryNotifySql + " desc";
        }else {
          queryNotifySql = queryNotifySql + " asc";
        }
      }
      stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY); 
      rs = stmt.executeQuery(queryNotifySql);
      rs.last(); 
      recordCount = rs.getRow(); //总记录数 
      //总页数 
      pageCount = recordCount / showLen; 
      if (recordCount % showLen != 0) { 
        pageCount++; 
      } 
      if (pageIndex < 1) { 
        pageIndex = 1; 
      } 
      if (pageIndex > pageCount) { 
        pageIndex = pageCount; 
      } 
      //开始索引
      pgStartRecord = (pageIndex - 1) * showLen + 1;
      rs.absolute( (pageIndex - 1) * showLen + 1); 
      for (int i = 0; i < showLen && !rs.isAfterLast()&&recordCount > 0; i++) { 
        count ++;
        Statement stmtt = null;
        ResultSet rss = null;
        String toNameTitle = "";
        String toNameStr = "";
        String subjectTitle = "";
        String publishDesc = "";
        String typeName = "";  
        int seqId =  rs.getInt("SEQ_ID");//自增ID
        String fromId = rs.getString("FROM_ID");
        String fromName = "";
        String deptName = "";
        String toId = T9WorkFlowUtility.clob2String(rs.getClob("TO_ID"));
        String subject = rs.getString("SUBJECT");
        String top = rs.getString("TOP");
        String privId = T9WorkFlowUtility.clob2String(rs.getClob("PRIV_ID"));
        String userId = T9WorkFlowUtility.clob2String(rs.getClob("USER_ID"));
        String typeId = rs.getString("TYPE_ID");
        String format = rs.getString("FORMAT");
        Date sendTime = (Date)rs.getObject("SEND_TIME");
        if(subject.length()>50) {
          subjectTitle = subject;
          subject = subject.substring(0, 50) + "...";
        }
        String deptId = "";
        try {
          String queryuserNameStr = "select USER_NAME,DEPT_ID from PERSON where SEQ_ID='"+fromId+"'";
          stmtt = conn.createStatement(); 
          rss = stmtt.executeQuery(queryuserNameStr);
          if(rss.next()) {
            fromName = rss.getString("USER_NAME");
            deptId = rss.getString("DEPT_ID");
          }
        } catch(Exception ex) {
          ex.printStackTrace();
        } finally {
          T9DBUtility.close(stmtt, rss, null);
        }
        
        
        String querydeptNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID='"+deptId+"'";
        stmtt = conn.createStatement(); 
        rss = stmtt.executeQuery(querydeptNameStr);
        if(rss.next()) {
          deptName = rss.getString("DEPT_NAME");
        }
        //根据新闻类型字段的值，获取新闻类型的代码描述        if(typeId!=null){
          if(!"".equals(typeId.trim())&&!"null".equals(typeId)) {
            try {
              String queryTypeNameStr = "SELECT CLASS_DESC from CODE_ITEM where SEQ_ID="+typeId;
              stmtt = conn.createStatement(); 
              rss = stmtt.executeQuery(queryTypeNameStr);
              if (rss.next()) {
                typeName = rss.getString("CLASS_DESC");
              }
            } catch(Exception ex) {
              ex.printStackTrace();
            } finally {
              T9DBUtility.close(stmtt, rss, null);
            }
          }
       }else{
         typeName = "";
       }
       //得到发布范围-部门的名称（串）
       String toName = "";
       String queryToNameStr = "";
       if(toId != null && !"".equals(toId)){
         String[] toIds = toId.split(",");
         toId = "";
         for(int j = 0 ;j < toIds.length ; j++){
           toId += toIds[j] + ",";
         }
         toId = toId.substring(0, toId.length() - 1);
       }
       if("0".equals(toId)) {
         toName = "全体部门";
       }else if(!"".equals(toId.trim())&&toId!=null){
         try {
           queryToNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID in (" + toId + ")";
           stmtt = conn.createStatement();
           rss = stmtt.executeQuery(queryToNameStr);
           while(rss.next()) {
             toName = toName + rss.getString("DEPT_NAME") + ",";
           }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
       }
       //得到发布范围-角色的名称（串）
       String privName = "";
       String queryPrivNameStr = "";
       if(privId != null && !"".equals(privId)){
         String[] privIds = privId.split(",");
         privId = "";
         for(int j = 0 ;j < privIds.length ; j++){
           privId += privIds[j] + ",";
         }
         privId = privId.substring(0, privId.length() - 1);
       }
       if(privId != null && !"".equals(privId.trim())){
         try {
           queryPrivNameStr = "select PRIV_NAME from USER_PRIV where SEQ_ID in (" + privId + ")";
           stmtt = conn.createStatement();
           rss = stmtt.executeQuery(queryPrivNameStr);
           while(rss.next()) {
             privName = privName + rss.getString("PRIV_NAME") + ",";
           }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
       }
       //得到发布范围-人员的姓名（串）
       String userName = "";
       String queryUserNameStr = "";
       if(userId != null && !"".equals(userId)){
         String[] userIds = userId.split(",");
         userId = "";
         for(int j = 0 ;j < userIds.length ; j++){
          userId += userIds[j] + ",";
         }
         userId = userId.substring(0, userId.length() - 1);
       }
       if(userId != null && !"".equals(userId.trim())){
         try {
           queryUserNameStr = "select USER_NAME from PERSON where SEQ_ID in (" + userId + ")";
           stmtt = conn.createStatement();
           rss = stmtt.executeQuery(queryUserNameStr);
           while(rss.next()) {
             userName = userName + rss.getString("USER_NAME") + ",";
           }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
       }
                
      //设置好部门的名称（串）的显示格式
      if(!"".equals(toName)) {
        toNameTitle = "部门:" + toName;  
        if(toName.length()>20) {
          toName = toName.substring(0, 15)+"...";
        }
        toNameStr = "<font color=#0000FF><b>部门：</b></font>";
        toNameStr += toName;
        toNameStr += "<br>";
        }
       
        //设置好角色的名称（串）的显示格式
      String privNameTitle = "";
      String privNameStr = "";
      if(!"".equals(privName)) {
        privNameTitle =   "角色：" + privName;
        if(privName.length()>20) {
          privName = privName.substring(0, 15)+"...";
        }
        privNameStr =  "<font color=#0000FF><b>角色：</b></font>";
        privNameStr += privName;
        privNameStr += "<br>";
      }
                
      //设置好人员的姓名（串）的显示格式
      String userNameTitle = "";
      String userNameStr = "";
      if(!"".equals(userName)) {
        userNameTitle =  "人员：" + userName;
        if(userName.length() > 20) {
          userName = userName.substring(0, 15)+"...";
        }
        userNameStr =  "<font color=#0000FF><b>人员：</b></font>";
        userNameStr += userName;
        userNameStr += "<br>";
      }
      
      if("1".equals(top)){
        subject = "<font color=red><b>置顶:" + subject + "</b></font>";
      }
      sb.append("{");
      sb.append("seqId:" + seqId);
      sb.append(",fromId:\"" + fromId + "\"");
      sb.append(",fromName:\"" + T9Utility.encodeSpecial(fromName) + "\"");
      sb.append(",typeName:\"" + T9Utility.encodeSpecial(typeName) + "\"");
      sb.append(",toNameTitle:\"" + T9Utility.encodeSpecial(toNameTitle) + "\"");
      sb.append(",toNameStr:\"" + T9Utility.encodeSpecial(toNameStr) + "\"");
      sb.append(",privNameTitle:\"" + T9Utility.encodeSpecial(privNameTitle) + "\"");
      sb.append(",privNameStr:\"" + T9Utility.encodeSpecial(privNameStr) + "\"");
      sb.append(",userNameTitle:\"" + T9Utility.encodeSpecial(userNameTitle) + "\"");
      sb.append(",userNameStr:\"" + T9Utility.encodeSpecial(userNameStr) + "\"");
      sb.append(",subjectTitle:\"" + T9Utility.encodeSpecial(subjectTitle) + "\"");
      sb.append(",toName:\"" + T9Utility.encodeSpecial(toName) + "\"");
      sb.append(",publishDesc:\"" + T9Utility.encodeSpecial(publishDesc) + "\"");
      sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
      sb.append(",format:\"" + format + "\"");
      sb.append(",subject:\"" + T9Utility.encodeSpecial(subject) + "\"");
      sb.append(",top:\"" + top + "\"");
      sb.append(",sendTime:\"" + sendTime + "\"");
      sb.append("},");               
        rs.next();
        T9DBUtility.close(stmtt, rss, null);
}
      //结束索引
      pgEndRecord =(pageIndex - 1) * showLen + count;
      if(count>0) {
      sb.deleteCharAt(sb.length() - 1); 
      }
      
      sb.append("]");
      sb.append(",pageData:");
      sb.append("{");
      sb.append("pageCount:" + pageCount);
      sb.append(",recordCount:" + recordCount);
      sb.append(",pgStartRecord:" + pgStartRecord);
      sb.append(",pgEndRecord:" + pgEndRecord);
      sb.append("}");
      sb.append("}");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
    T9DBUtility.close(stmt, rs, log);
  }
  return sb.toString();
}
  
    public boolean isHasRead(String reader,T9Person loginUser){
      boolean temp = false;
      if("".equals(reader)||reader==null){
        temp = true;
      }else{
          String[] readers = reader.split(",");
          reader = "";
          for(int j = 0 ;j < readers.length ; j++){
            reader = readers[j];
            if(reader.equals(Integer.toString(loginUser.getSeqId()))){
              temp = false; 
              break;
            }
         }
      }
     return temp;
    }
    
   //我的办公桌的全部公告
  public String getnotifyShowList(Connection conn,T9Person loginUser,int showLen,String type,String ascDesc,String field, 
      int pageIndex,String sendTimetemp)throws Exception{
    Statement stmt = null;
    ResultSet rs = null;
    boolean temp = false;
    int count = 0;
    int pageCount = 0;//页码数
    int recordCount = 0;//总记录数
    int pgStartRecord = 0;//开始索引
    int pgEndRecord = 0;//结束索引
    int loginDeptId = loginUser.getDeptId();
    String userPriv = loginUser.getUserPriv();
    int seqUserId = loginUser.getSeqId();
    Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StringBuffer sb  = new StringBuffer();
    sb.append("{");
    sb.append("listData:[");
    String queryPapaSql = "select PARA_VALUE from SYS_PARA where PARA_NAME='NOTIFY_TOP_DAYS'";
    String notifyTopDays = "";
    try {
      Statement sttt = conn.createStatement();
      ResultSet rsss = sttt.executeQuery(queryPapaSql);
      if(rsss.next()) {
        notifyTopDays =  rsss.getString("PARA_VALUE");
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      throw e1;
    }
    try{
      String queryNotifySql = null;
      String dbms = T9SysProps.getString(T9SysPropKeys.DBCONN_DBMS);
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        queryNotifySql = "SELECT SEQ_ID,FROM_ID,TO_ID,SUBJECT,[TOP],TOP_DAYS,PRIV_ID,USER_ID,READERS,"
          +"TYPE_ID,SEND_TIME,BEGIN_DATE,FORMAT from NOTIFY  where  BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and (END_DATE>=" + T9DBUtility.currDateTime()
          +" or END_DATE is null) and PUBLISH='1' and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "TO_ID")
          + " or " + T9DBUtility.findInSet(userPriv,"PRIV_ID") +" or " 
          + T9DBUtility.findInSet(Integer.toString(seqUserId),"USER_ID" ) + " or "+T9DBUtility.findInSet("0", "TO_ID")+") ";
      }else {
        queryNotifySql = "SELECT SEQ_ID,FROM_ID,TO_ID,SUBJECT,TOP,TOP_DAYS,PRIV_ID,USER_ID,READERS,"
          +"TYPE_ID,SEND_TIME,BEGIN_DATE,FORMAT from NOTIFY  where  BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and (END_DATE>=" + T9DBUtility.currDateTime()
          +" or END_DATE is null) and PUBLISH='1' and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "TO_ID")
          + " or " + T9DBUtility.findInSet(userPriv,"PRIV_ID") +" or " 
          + T9DBUtility.findInSet(Integer.toString(seqUserId),"USER_ID" ) + " or "+T9DBUtility.findInSet("0", "TO_ID")+") ";
      }
      if("".equals(type)){//选择“无类型“
        queryNotifySql = queryNotifySql + " and (TYPE_ID='' or TYPE_ID=' ' or TYPE_ID is null)";
      }else if(!"0".equals(type)) {//选择的不是”所有类型“
        queryNotifySql = queryNotifySql + " and (TYPE_ID='"+ type + "')";
      }
      if(!"".equals(sendTimetemp)&&sendTimetemp!=null&&!"null".equals(sendTimetemp)){
        queryNotifySql = queryNotifySql + " and "+ T9DBUtility.getDayFilter("SEND_TIME", T9Utility.parseDate(sendTimetemp));//to_char(SEND_TIME,'yyyy-MM-dd')='"+ sendTimetemp + "'";
      }
      if("".equals(field)) {
        if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
          queryNotifySql = queryNotifySql + " order by [TOP] desc,SEND_TIME desc";
        }else {
          queryNotifySql = queryNotifySql + " order by TOP desc,SEND_TIME desc";
        }
      }else {
        if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
          queryNotifySql = queryNotifySql + " order by [TOP] desc," + field;
        }else {
          queryNotifySql = queryNotifySql + " order by TOP desc," + field;
        }
        if("1".equals(ascDesc)) {
          queryNotifySql = queryNotifySql + " desc";
        }else {
          queryNotifySql = queryNotifySql + " asc";
        }
      }
      //T9Out.println(queryNotifySql+"*********************");
      stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
      rs = stmt.executeQuery(queryNotifySql);
      rs.last(); 
      recordCount = rs.getRow(); //总记录数 
      
    //总页数 
      pageCount = recordCount / showLen; 
     if (recordCount % showLen != 0) { 
       pageCount++; 
     } 
     if (pageIndex < 1) { 
       pageIndex = 1; 
     } 
     if (pageIndex > pageCount) { 
       pageIndex = pageCount; 
     } 
     //开始索引

     pgStartRecord = (pageIndex - 1) * showLen + 1;
   
     rs.absolute( (pageIndex - 1) * showLen + 1); 
     for (int i = 0; i < showLen && !rs.isAfterLast()&&recordCount > 0; i++) { 
        count ++;
        Statement stmtt = null;
        ResultSet rss = null;
        String toNameTitle = "";
        String toNameStr = "";
        String subjectTitle = "";
        String publishDesc = "";
        String typeName = "";  
        int seqId =  rs.getInt("SEQ_ID");//自增ID
        String fromId = rs.getString("FROM_ID");
        String fromName = "";
        String deptName = "";
        String toId = rs.getString("TO_ID");
        String subject = rs.getString("SUBJECT");
        String top = rs.getString("TOP");
        String privId = rs.getString("PRIV_ID");
        String userId = rs.getString("USER_ID");
        String typeId = rs.getString("TYPE_ID");
        String format = rs.getString("FORMAT");
        Date sendTime = rs.getTimestamp("SEND_TIME");
        if(subject.length()>50) {
          subjectTitle = subject;
          subject = subject.substring(0, 50) + "...";
        }
        
        String deptId = "";
       
         try {
           String queryuserNameStr = "select USER_NAME,DEPT_ID from PERSON where SEQ_ID='"+fromId+"'";
           stmtt = conn.createStatement(); 
           rss = stmtt.executeQuery(queryuserNameStr);
            if(rss.next()) {
              fromName = rss.getString("USER_NAME");
              deptId = rss.getString("DEPT_ID");
            }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
         
         try {
           String querydeptNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID='"+deptId+"'";
           stmtt = conn.createStatement(); 
           rss = stmtt.executeQuery(querydeptNameStr);
            if(rss.next()) {
              deptName = rss.getString("DEPT_NAME");
            }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
         
        //根据新闻类型字段的值，获取新闻类型的代码描述        
          if(typeId!=null){
             if(!"".equals(typeId.trim())&&!"null".equals(typeId)){
               
                try {
                  String queryTypeNameStr = "SELECT CLASS_DESC from CODE_ITEM where SEQ_ID="+typeId;
                  stmtt = conn.createStatement(); 
                  rss = stmtt.executeQuery(queryTypeNameStr);
                   if(rss.next()) {
                     typeName = rss.getString("CLASS_DESC");
                   }
                } catch(Exception ex) {
                  ex.printStackTrace();
                } finally {
                  T9DBUtility.close(stmtt, rss, null);
                }
             }
         }else{
           typeName = "";
         }
        //得到发布范围-部门的名称（串）
        String toName = "";
        String queryToNameStr = "";
        if(toId != null && !"".equals(toId)){
          String[] toIds = toId.split(",");
          toId = "";
          for(int j = 0 ;j < toIds.length ; j++){
            toId += toIds[j] + ",";
          }
          toId = toId.substring(0, toId.length() - 1);
        }
        if("0".equals(toId)) {
          toName = "全体部门";
        }else if(!"".equals(toId.trim())&&toId!=null){
          try {
            queryToNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID in (" + toId + ")";
            stmtt = conn.createStatement();
            rss = stmtt.executeQuery(queryToNameStr);
            while(rss.next()) {
              toName = toName + rss.getString("DEPT_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
           try {
             queryToNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID in (" + toId + ")";
             stmtt = conn.createStatement();
             rss = stmtt.executeQuery(queryToNameStr);
             while(rss.next()) {
               toName = toName + rss.getString("DEPT_NAME") + ",";
             }
           } catch(Exception ex) {
             ex.printStackTrace();
           } finally {
             T9DBUtility.close(stmtt, rss, null);
           }
        }
        //得到发布范围-角色的名称（串）
        String privName = "";
        String queryPrivNameStr = "";
        if(privId != null && !"".equals(privId)){
          String[] privIds = privId.split(",");
          privId = "";
          for(int j = 0 ;j < privIds.length ; j++){
            privId += privIds[j] + ",";
          }
          privId = privId.substring(0, privId.length() - 1);
        }
        if(privId != null && !"".equals(privId.trim())){
          try {
            queryPrivNameStr = "select PRIV_NAME from USER_PRIV where SEQ_ID in (" + privId + ")";
            stmtt = conn.createStatement();
            rss = stmtt.executeQuery(queryPrivNameStr);
            while(rss.next()) {
              privName = privName + rss.getString("PRIV_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
        }
        //得到发布范围-人员的姓名（串）
        String userName = "";
        String queryUserNameStr = "";
        if(userId != null && !"".equals(userId)){
          String[] userIds = userId.split(",");
          userId = "";
          for(int j = 0 ;j < userIds.length ; j++){
            userId += userIds[j] + ",";
          }
          userId = userId.substring(0, userId.length() - 1);
        }
        if(userId != null && !"".equals(userId.trim())){
          try {
            queryUserNameStr = "select USER_NAME from PERSON where SEQ_ID in (" + userId + ")";
            stmtt = conn.createStatement();
            rss = stmtt.executeQuery(queryUserNameStr);
            while(rss.next()) {
              userName = userName + rss.getString("USER_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
        }
        
        //设置好部门的名称（串）的显示格式
        if(!"".equals(toName)) {
          toNameTitle = "部门:" + toName;  
          if(toName.length()>20) {
            toName = toName.substring(0, 15)+"...";
          }
          toNameStr = "<font color=#0000FF><b>部门：</b></font>";
          toNameStr += toName;
          toNameStr += "<br>";
          }
         
          //设置好角色的名称（串）的显示格式
        String privNameTitle = "";
        String privNameStr = "";
        if(!"".equals(privName)) {
          privNameTitle =   "角色：" + privName;
          if(privName.length()>20) {
            privName = privName.substring(0, 15)+"...";
          }
          privNameStr =  "<font color=#0000FF><b>角色：</b></font>";
          privNameStr += privName;
          privNameStr += "<br>";
            }
            
         //设置好人员的姓名（串）的显示格式
        String userNameTitle = "";
        String userNameStr = "";
        if(!"".equals(userName)) {
          userNameTitle =  "人员：" + userName;
          if(userName.length()>20) {
            userName = userName.substring(0, 15)+"...";
          }
          userNameStr =  "<font color=#0000FF><b>人员：</b></font>";
          userNameStr += userName;
          userNameStr += "<br>";
        }
        
        if("1".equals(top)){
          subject = "<font color=red><b>置顶:" + subject + "</b></font>";
        }
        sb.append("{");
        sb.append("seqId:" + seqId);
        sb.append(",fromId:\"" + fromId + "\"");
        sb.append(",fromName:\"" + T9Utility.encodeSpecial(fromName) + "\"");
        sb.append(",typeName:\"" + T9Utility.encodeSpecial(typeName) + "\"");
        sb.append(",toNameTitle:\"" + T9Utility.encodeSpecial(toNameTitle) + "\"");
        sb.append(",toNameStr:\"" + T9Utility.encodeSpecial(toNameStr) + "\"");
        sb.append(",privNameTitle:\"" + T9Utility.encodeSpecial(privNameTitle) + "\"");
        sb.append(",privNameStr:\"" + T9Utility.encodeSpecial(privNameStr) + "\"");
        sb.append(",userNameTitle:\"" + T9Utility.encodeSpecial(userNameTitle) + "\"");
        sb.append(",userNameStr:\"" + T9Utility.encodeSpecial(userNameStr) + "\"");
        sb.append(",subjectTitle:\"" + T9Utility.encodeSpecial(subjectTitle) + "\"");
        sb.append(",toName:\"" + T9Utility.encodeSpecial(toName) + "\"");
        sb.append(",publishDesc:\"" + T9Utility.encodeSpecial(publishDesc) + "\"");
        sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
        sb.append(",format:\"" + format + "\"");
        sb.append(",subject:\"" + T9Utility.encodeSpecial(subject) + "\"");
        sb.append(",top:\"" + top + "\"");
        sb.append(",sendTime:\"" + sendTime + "\"");
        sb.append(",iread:\"" + haveRead(conn,loginUser.getSeqId(),seqId) + "\"");
        sb.append("},");
              rs.next();
      }
     
      //结束索引
      pgEndRecord =(pageIndex - 1) * showLen + count;
      if(count>0) {
        sb.deleteCharAt(sb.length() - 1); 
      }
     
      sb.append("]");
      sb.append(",pageData:");
      sb.append("{");
      sb.append("pageCount:" + pageCount);
      sb.append(",recordCount:" + recordCount);
      sb.append(",pgStartRecord:" + pgStartRecord);
      sb.append(",pgEndRecord:" + pgEndRecord);
      sb.append("}");
      sb.append("}");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      T9DBUtility.close(stmt, rs, log);
    }
    return sb.toString();
}
  
  //初看公告
  public String showObject(Connection dbConn,T9Person person,int seqId,String isManage,T9Notify notify) throws Exception{
    Statement st = null;
    ResultSet rs = null;
    Calendar cld = Calendar.getInstance();
    int year = cld.get(Calendar.YEAR) % 100;
    int month = cld.get(Calendar.MONTH) + 1;
    String mon = month >= 10 ? month + "" : "0" + month;
    String hard = year + mon;
    T9ORM orm = new T9ORM();
//    int userId = person.getSeqId();
    
    Date endDate = null;
    StringBuffer sb = new StringBuffer("{");
    String notifyAuditingSingle = "";
    String queryPapaSql = "select PARA_VALUE from SYS_PARA where PARA_NAME='NOTIFY_AUDITING_SINGLE'";
    try {
      st = dbConn.createStatement();
      rs = st.executeQuery(queryPapaSql);
      if(rs.next()) {
       String papaValue =  rs.getString("PARA_VALUE");
       notifyAuditingSingle = papaValue;
      }else {
        notifyAuditingSingle = "0";
      }
      
     String readers = notify.getReaders();
     if(readers==null){
       readers = "";
     }
     boolean contains = false;
     String updateSql = "";
     Statement stmtt = null;
     ResultSet rss = null;
     if(readers != null && !"".equals(readers)){
       String[] readerList = readers.split(",");
       for(int j = 0 ;j < readerList.length ; j++){
         String reader = readerList[j];
         String [] rList = reader.split("_");
         for (int i = 0; i < rList.length; i++) {
             String r = rList[i];
             if(r.equals(Integer.toString(person.getSeqId()))){
                 contains = true;//代表已经读过
                 break;
             }
         }
       }
     }
     if(!contains){//如果没有读过
       readers = readers + person.getSeqId()+ "_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString() + ",";
       updateSql = "update NOTIFY set READERS='"+ readers + "' where SEQ_ID='" + seqId + "'";
       stmtt = dbConn.createStatement();
       stmtt.execute(updateSql);
     }
    
     String fromName = "";
     String deptName = "";
     String auditerName = "";
     String auditerDeptName = "";
     int auditerDeptId= 0;
     int deptId = 0;
//     if("1".equals(notifyAuditingSingle)&&!"".equals(notify.getAuditer())) {
//       fromId =  "'" + fromId + "'" + "," + "'" + notify.getAuditer() + "'";
//     }
     String queryUserSql = "SELECT USER_NAME,DEPT_ID from PERSON where SEQ_ID ="+notify.getFromId();
     stmtt = dbConn.createStatement();
     rss = stmtt.executeQuery(queryUserSql);
     if(rss.next()) {
       fromName = rss.getString("USER_NAME");//发布人的姓名
       deptId = rss.getInt("DEPT_ID");
     }
     
     if(notify.getAuditer()!=null){
       if(!"".equals(notify.getAuditer().trim())&&!"null".equals(notify.getAuditer().trim())){
         String queryAuditerSql = "SELECT USER_NAME,DEPT_ID from PERSON where SEQ_ID ="+notify.getAuditer();
         stmtt = dbConn.createStatement();
         rss = stmtt.executeQuery(queryAuditerSql);
         if(rss.next()) {
           auditerName = rss.getString("USER_NAME");//审批人的姓名
           auditerDeptId = rss.getInt("DEPT_ID");
         }
       }
     }
     
     String queryDeptSql = "SELECT DEPT_NAME FROM DEPARTMENT Where SEQ_ID="+deptId;
     stmtt = dbConn.createStatement();
     rss = stmtt.executeQuery(queryDeptSql);
     if(rss.next()) {
       deptName = rss.getString("DEPT_NAME");//发布人的部门名称
     }
     
     if(!"".equals(auditerDeptId)&&auditerDeptId!=0){
       String queryAuditerDeptSql = "SELECT DEPT_NAME FROM DEPARTMENT Where SEQ_ID="+auditerDeptId;
       stmtt = dbConn.createStatement();
       rss = stmtt.executeQuery(queryAuditerDeptSql);
       if(rss.next()) {
         auditerDeptName = rss.getString("DEPT_NAME");//审批人的部门名称
       }
     }
//     int fromDept = notify.getFromDept();
     String isHasNotifyFunc = "0";//        1--可以转发      0--不能转发
     String userPriv = person.getUserPriv();
     String userPrivOther = person.getUserPrivOther();
     if(!"".equals(userPrivOther)&&userPrivOther!=null){
       userPriv = userPriv + "," + userPrivOther;
     }
     //T9Out.println(userPriv);
     if(userPriv != null && !"".equals(userPriv)){
       boolean isHas = false;
       String[] userPrivs = userPriv.split(",");
       for(int j = 0 ;j < userPrivs.length ; j++){
         userPriv = userPrivs[j];
         isHas =  isHasFuns(dbConn,userPriv);
         if(isHas==true) {
           isHasNotifyFunc = "1";
           break;
         }
       }
     }
     String attachmentId = notify.getAttachmentId();
     String attachmentName = notify.getAttachmentName();
     
     sb.append("seqId:" + seqId);
     sb.append(",fromId:\"" + notify.getFromId() + "\"");
     sb.append(",fromName:\"" + T9Utility.encodeSpecial(fromName) + "\"");
     sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
     sb.append(",notifyAuditingSingle:\"" + T9Utility.encodeSpecial(notifyAuditingSingle) + "\"");
     sb.append(",auditer:\"" + T9Utility.encodeSpecial(notify.getAuditer()) + "\"");
     sb.append(",auditerName:\"" + T9Utility.encodeSpecial(auditerName) + "\"");
     sb.append(",auditerDeptName:\"" + T9Utility.encodeSpecial(auditerDeptName) + "\"");
     sb.append(",download:\"" + T9Utility.encodeSpecial(notify.getDownload()) + "\"");
     sb.append(",print:\"" + T9Utility.encodeSpecial(notify.getPrint()) + "\"");
     sb.append(",subject:\"" + T9Utility.encodeSpecial(notify.getSubject()) + "\"");
     sb.append(",content:\"" + T9Utility.encodeSpecial(notify.getContent()) + "\"");
     sb.append(",format:\"" + notify.getFormat() + "\"");
     sb.append(",isHasNotifyFunc:\"" + T9Utility.encodeSpecial(isHasNotifyFunc) + "\"");
     sb.append(",beginDate:\"" + notify.getSendTime() + "\"");
     sb.append(",ym:\"" + hard + "\"");
     if(!"".equals(T9Utility.null2Empty(notify.getAttachmentId()).trim())&& T9Utility.null2Empty(notify.getAttachmentId()).trim()!=null&&!",".equals(T9Utility.null2Empty(notify.getAttachmentId()).trim())){
        sb.append(",attachFile:\"" + notify.getAttachmentId().replaceAll("[\\w]+_", "")+"_"+notify.getAttachmentName().substring(0, notify.getAttachmentName().length()-1) + "\"");
     }
     sb.append(",attachmentId:\"" + notify.getAttachmentId() + "\"");
     sb.append(",attachmentName:\"" + T9Utility.encodeSpecial(notify.getAttachmentName()) + "\"");
     sb.append(",subjectFont:\"" + T9Utility.encodeSpecial(notify.getSubjectFont()) + "\"");
     sb.append("}");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }finally {
      T9DBUtility.close(st, rs, log);
    }
    return sb.toString();
  }
  
  public List isPicture(String attachNanme){
    String[] pic = {"gif","jpg","jpeg","png"};
    String picTemp = null;
    List list = new ArrayList();
    if(attachNanme != null && !"".equals(attachNanme)){
      String[] attachNanmes = attachNanme.split(",");
      for(int j = 0 ;j < attachNanmes.length ; j++){
        attachNanme = attachNanmes[j];
        for(int i = 0;i < pic.length;i ++) {
          picTemp = pic[i];
          if(attachNanme.contains(picTemp)){
            list.add(attachNanme);
            continue;
          }
        }
      }
    }
    return list;
  }
  
  //判断是不是可以转发的权限
  public boolean isHasFuns(Connection dbConn,String userPriv) throws Exception{
    Statement stmt = null;
    ResultSet rs = null;
    String notifyFuncs = T9NotifyCont.FUNC;
    String funcIdStr = "";
    boolean isHas = false;
    if("".equals(userPriv.trim())||userPriv==null){
      return false;
    }
    String sql = "select FUNC_ID_STR from USER_PRIV where SEQ_ID="+ userPriv;
    //T9Out.println(sql+"**********");
    stmt = dbConn.createStatement();
    rs = stmt.executeQuery(sql);
    if(rs.next()) {
      funcIdStr = rs.getString("FUNC_ID_STR");
    }
    if(funcIdStr != null && !"".equals(funcIdStr)){
      String[] funcIdStrs = funcIdStr.split(",");
      for(int j = 0 ;j < funcIdStrs.length ; j++){
        funcIdStr =  funcIdStrs[j];
        if(notifyFuncs.equals(funcIdStr)){
          isHas = true;
          break;
        }
      }
    }
    return isHas;
  }
  
  //我的办公桌里面，查询公告
  public String queryNotify(Connection dbConn, T9Notify notify,T9Person loginUser,String beginDatetemp,
      String endDatetemp,String ascDesc,String field) throws Exception{
    Statement stmt = null;
    ResultSet rs = null;
    int temp = 0;
    int pageCount = 0;//页码数
    int recordCount = 0;//总记录数
    int pgStartRecord = 0;//开始索引
    int pgEndRecord = 0;//结束索引
    int loginDeptId = loginUser.getDeptId();
    String userPriv = loginUser.getUserPriv();
    int seqUserId = loginUser.getSeqId();
    Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StringBuffer sb  = new StringBuffer();
    sb.append("{");
    sb.append("listData:[");
    String querynewsParam = "";
    String formattemp = notify.getFormat();
    if(!"".equals(formattemp))
    querynewsParam = querynewsParam + " and n.FORMAT ='" + formattemp + "'";
    String typeIdtemp = notify.getTypeId();
    if(!"".equals(typeIdtemp))
    querynewsParam = querynewsParam + " and n.TYPE_ID ='" + typeIdtemp + "'";
    String fromIdtemp = notify.getFromId();
    if(!"".equals(fromIdtemp))
    querynewsParam = querynewsParam + " and n.FROM_ID in (" + fromIdtemp + ")";
    String subjecttemp = notify.getSubject();
    if(!"".equals(subjecttemp))
    querynewsParam = querynewsParam + " and n.SUBJECT like '%" + T9DBUtility.escapeLike(subjecttemp) + "%' "+T9DBUtility.escapeLike();
    if(!"".equals(beginDatetemp))
    querynewsParam = querynewsParam + " and " + T9DBUtility.getDateFilter("n.SEND_TIME", beginDatetemp, ">=");//to_char(n.SEND_TIME,'yyyy-mm-dd')>='" + beginDatetemp + "'";
    if(!"".equals(endDatetemp))
    querynewsParam = querynewsParam + " and " + T9DBUtility.getDateFilter("n.SEND_TIME", T9Utility.getDateTimeStr(T9Utility.getDayAfter(endDatetemp,1)), "<");//to_char(n.SEND_TIME,'yyyy-mm-dd')<='" + endDatetemp + "'";
    String content = notify.getContent(); 
    if(!"".equals(content))
    querynewsParam = querynewsParam + " and n.CONTENT like '%" + T9DBUtility.escapeLike(content) + "%' "+T9DBUtility.escapeLike();
    String queryNotifySql = "SELECT p.USER_NAME,d.DEPT_NAME,n.* from NOTIFY n,PERSON p,DEPARTMENT d where PUBLISH='1' and n.FROM_ID=p.SEQ_ID and p.DEPT_ID=d.SEQ_ID"
                            +" and BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and (END_DATE>=" + T9DBUtility.currDateTime()
                            +" or END_DATE is null) and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "n.TO_ID") + " or " + T9DBUtility.findInSet(userPriv,"n.PRIV_ID")
                            +" or " + T9DBUtility.findInSet(Integer.toString(seqUserId),"n.USER_ID") + " or " +T9DBUtility.findInSet("0", "TO_ID")+ ") ";
    String querySql = queryNotifySql+querynewsParam;
    String dbms = T9SysProps.getString(T9SysPropKeys.DBCONN_DBMS);
    if("".equals(field)) {
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        querySql = querySql + " order by [TOP] desc,SEND_TIME desc";
      }else {
        querySql = querySql + " order by TOP desc,SEND_TIME desc";
      }
      
    }else {
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        querySql = querySql + " order by [TOP] desc," + field;
      }else {
        querySql = querySql + " order by TOP desc," + field;
      }
      
      if("1".equals(ascDesc)) {
        querySql = querySql + " desc";
      }else {
        querySql = querySql + " asc";
      }
    }
    stmt = dbConn.createStatement();
    rs = stmt.executeQuery(querySql);
   while(rs.next()) { 
     
          temp ++;  
          
          Statement stmtt = null;
          ResultSet rss = null;
          String toNameTitle = "";
          String toNameStr = "";
          String publishDesc = "";
          String typeName = "";  
          String notifyStatus = "";
          T9News newss = new T9News(); 
          int seqId = rs.getInt("SEQ_ID");//自增ID
          String subject = rs.getString("SUBJECT");//新闻标题
          String fromId = rs.getString("FROM_ID");//新闻发布人
          String fromName = rs.getString("USER_NAME");
          String readers = rs.getString("READERS");//新闻发布时间
          String top = rs.getString("TOP");//评论类型（0-实名评论；1-匿名评论；2-禁止评论）
          String format = rs.getString("FORMAT");//新闻格式（0-普通格式；1-MHT格式；2-超链接）
          String providerName = rs.getString("USER_NAME");
          String deptName = rs.getString("DEPT_NAME");
          String privId = rs.getString("PRIV_ID");//新闻发布人
          String userId = rs.getString("USER_ID");//新闻发布人
          String toId = rs.getString("TO_ID");//新闻发布人
          String typeId = rs.getString("TYPE_ID");//新闻类型，对应小代码表
          String publish = rs.getString("PUBLISH");
          Date sendTime = rs.getDate("SEND_TIME");//发送时间
          Date beginDate = rs.getDate("BEGIN_DATE");//开始时间
          Date endDate = rs.getDate("END_DATE");//结束时间
          notify.setSeqId(rs.getInt("SEQ_ID"));
        
        //新闻标题超链接对应的title属性，显示完整的新闻标题
        String subjectTitle = "";
        if(subject.length()>50) {
          subjectTitle = subject;
          subject = subject.substring(0, 50) + "...";
        }
        
        if("0".equals(publish)){
          publishDesc = "<font color=red>未发布</font>";
        }
        if("2".equals(publish)){
          publishDesc = "<font color=blue>待审批</font>";
        }
        if("3".equals(publish)){
          publishDesc = "<font color=red>未通过</font><br><a href='#'>审批意见</a>";
        }
        if("1".equals(publish)){
          publishDesc = "";
        }
        
       String endDateStr = "";
        if(!"".equals(endDateStr)&&endDateStr!=null) {
          endDateStr = endDate.toString();
        }
        String deptId = "";
        String queryuserNameStr = "select USER_NAME,DEPT_ID from PERSON where SEQ_ID='"+fromId+"'";
        
         
         try {
           stmtt = dbConn.createStatement(); 
           rss = stmtt.executeQuery(queryuserNameStr);
            if(rss.next()) {
              fromName = rss.getString("USER_NAME");
              deptId = rss.getString("DEPT_ID");
            }
         } catch(Exception ex) {
           ex.printStackTrace();
         } finally {
           T9DBUtility.close(stmtt, rss, null);
         }
         
         
         
          
          try {
            String querydeptNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID='"+deptId+"'";
            stmtt = dbConn.createStatement(); 
            rss = stmtt.executeQuery(querydeptNameStr);
             if(rss.next()) {
               deptName = rss.getString("DEPT_NAME");
             }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
          
          
        //根据新闻类型字段的值，获取新闻类型的代码描述
          if(typeId!=null){
             if(!"".equals(typeId.trim())&&!"null".equals(typeId)){
                try {
                  String queryTypeNameStr = "SELECT CLASS_DESC from CODE_ITEM where SEQ_ID="+typeId;
                  stmtt = dbConn.createStatement(); 
                  rss = stmtt.executeQuery(queryTypeNameStr);
                   if(rss.next()) {
                     typeName = rss.getString("CLASS_DESC");
                   }
                } catch(Exception ex) {
                  ex.printStackTrace();
                } finally {
                  T9DBUtility.close(stmtt, rss, null);
                }
             }
         }else{
           typeName = "";
         }
        
        //得到发布范围-部门的名称（串）
        String toName = "";
        String queryToNameStr = "";
        if(toId != null && !"".equals(toId)){
          String[] toIds = toId.split(",");
          toId = "";
          for(int j = 0 ;j < toIds.length ; j++){
            toId += toIds[j] + ",";
          }
          toId = toId.substring(0, toId.length() - 1);
        }
        if("0".equals(toId)) {
          toName = "全体部门";
        }else if(!"".equals(toId.trim())&&toId!=null){
           
           try {
             queryToNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID in (" + toId + ")";
             stmtt = dbConn.createStatement();
             rss = stmtt.executeQuery(queryToNameStr);
             while(rss.next()) {
               toName = toName + rss.getString("DEPT_NAME") + ",";
             }
           } catch(Exception ex) {
             ex.printStackTrace();
           } finally {
             T9DBUtility.close(stmtt, rss, null);
           }
           
        }
        //得到发布范围-角色的名称（串）
        String privName = "";
        String queryPrivNameStr = "";
        if(privId != null && !"".equals(privId)){
          String[] privIds = privId.split(",");
          privId = "";
          for(int j = 0 ;j < privIds.length ; j++){
            privId += privIds[j] + ",";
          }
          privId = privId.substring(0, privId.length() - 1);
        }
        if(privId != null && !"".equals(privId.trim())){
          try {
            queryPrivNameStr = "select PRIV_NAME from USER_PRIV where SEQ_ID in (" + privId + ")";
            stmtt = dbConn.createStatement();
            rss = stmtt.executeQuery(queryPrivNameStr);
            while(rss.next()) {
              privName = privName + rss.getString("PRIV_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
          
        }
    //得到发布范围-人员的姓名（串）
        String userName = "";
        String queryUserNameStr = "";
        if(userId != null && !"".equals(userId)){
          String[] userIds = userId.split(",");
          userId = "";
          for(int j = 0 ;j < userIds.length ; j++){
            userId +=  userIds[j] + ",";
          }
          userId = userId.substring(0, userId.length() - 1);
        }
        if(userId != null && !"".equals(userId.trim())){
          
          try {
            queryUserNameStr = "select USER_NAME from PERSON where SEQ_ID in (" + userId + ")";
            stmtt = dbConn.createStatement();
            rss = stmtt.executeQuery(queryUserNameStr);
            while(rss.next()) {
              userName = userName + rss.getString("USER_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
        }
        
        //设置好部门的名称（串）的显示格式
        if(!"".equals(toName)) {
          toNameTitle = "部门:" + toName;  
          if(toName.length()>20) {
            toName = toName.substring(0, 15)+"...";
          }
          toNameStr = "<font color=#0000FF><b>部门：</b></font>";
          toNameStr += toName;
          toNameStr += "<br>";
        }
       
        //设置好角色的名称（串）的显示格式
        String privNameTitle = "";
        String privNameStr = "";
        if(!"".equals(privName)) {
          privNameTitle =   "角色：" + privName;
          if(privName.length() > 20) {
            privName = privName.substring(0, 15)+"...";
          }
          privNameStr =  "<font color=#0000FF><b>角色：</b></font>";
          privNameStr += privName;
          privNameStr += "<br>";
        }
        
     //设置好人员的姓名（串）的显示格式
        String userNameTitle = "";
        String userNameStr = "";
        if(!"".equals(userName)) {
          userNameTitle =  "人员：" + userName;
          if(userName.length() > 20) {
            userName = userName.substring(0, 15)+"...";
          }
          userNameStr =  "<font color=#0000FF><b>人员：</b></font>";
          userNameStr += userName;
          userNameStr += "<br>";
        }
        
        String notifyStatusStr = "";
        if(currentDate.compareTo(beginDate)<0) {
          notifyStatus = "1";
          notifyStatusStr = "待生效";
        }else {
          notifyStatus = "2";
          if(!"2".equals(publish)&&!"3".equals(publish)) {
            notifyStatusStr = "<font color='#00AA00'><b>生效</font>";
          }else {
            if("2".equals(publish)) {
              notifyStatusStr = "<font color='blue'><b>待审批</font>";
            }
            if("3".equals(publish)) {
              notifyStatusStr = "<font color='red'><b>未通过</font><br><a href='javascript:my_affair($NOTIFY_ID)'; title='点击查看审批意见'>审批意见</a>"; 
            }
          }
        }
        if((!"".equals(endDate)&&endDate!=null)||"0".equals(publish)) {
          if(currentDate.compareTo(endDate)>0||endDate==null) {
            notifyStatus = "3";
            notifyStatusStr = "<font color='#FF0000'><b>终止</font>";
          }
        }
        if("0".equals(publish)) {
          notifyStatusStr = "";
        }
        if("1".equals(top)){
          subject = "<font color=red><b>置顶:" + subject + "</b></font>";
        }
        sb.append("{");
        sb.append("seqId:" + seqId);
        sb.append(",fromId:\"" + fromId + "\"");
        sb.append(",fromName:\"" + T9Utility.encodeSpecial(fromName) + "\"");
        sb.append(",typeName:\"" + T9Utility.encodeSpecial(typeName) + "\"");
        sb.append(",toNameTitle:\"" + T9Utility.encodeSpecial(toNameTitle) + "\"");
        sb.append(",toNameStr:\"" + T9Utility.encodeSpecial(toNameStr) + "\"");
        sb.append(",privNameTitle:\"" + T9Utility.encodeSpecial(privNameTitle) + "\"");
        sb.append(",privNameStr:\"" + T9Utility.encodeSpecial(privNameStr) + "\"");
        sb.append(",userNameTitle:\"" + T9Utility.encodeSpecial(userNameTitle) + "\"");
        sb.append(",userNameStr:\"" + T9Utility.encodeSpecial(userNameStr) + "\"");
        sb.append(",subjectTitle:\"" + T9Utility.encodeSpecial(subjectTitle) + "\"");
        sb.append(",toName:\"" + T9Utility.encodeSpecial(toName) + "\"");
        sb.append(",publishDesc:\"" + publishDesc + "\"");
        sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
        sb.append(",subject:\"" + T9Utility.encodeSpecial(subject) + "\"");
        sb.append(",top:\"" + top + "\"");
        sb.append(",notifyStatusStr:\"" + T9Utility.encodeSpecial(notifyStatusStr) + "\"");
        sb.append(",sendTime:\"" + sendTime + "\"");
        sb.append("},");
     }

     if(temp>0) {
       sb.deleteCharAt(sb.length() - 1); 
     }
      sb.append("]");
      sb.append(",pageData:");
      sb.append("{");
      sb.append("pageCount:" + pageCount);
      sb.append(",recordCount:" + recordCount);
      sb.append(",pgStartRecord:" + pgStartRecord);
      sb.append(",pgEndRecord:" + pgEndRecord);
      sb.append("}");
      sb.append("}");
    return sb.toString();
   } 
//我的办公桌里面，查询公告
  public String queryNotify2(Connection dbConn, T9Notify notify,T9Person loginUser,String beginDatetemp,
      String endDatetemp,String ascDesc,String field) throws Exception{
    Statement stmt = null;
    ResultSet rs = null;
    
    int temp = 0;
    int pageCount = 0;//页码数

    int recordCount = 0;//总记录数
    int pgStartRecord = 0;//开始索引

    int pgEndRecord = 0;//结束索引
    int loginDeptId = loginUser.getDeptId();
    String userPriv = loginUser.getUserPriv();
    int seqUserId = loginUser.getSeqId();
    Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StringBuffer sb  = new StringBuffer();
    sb.append("{");
    sb.append("listData:[");
    String querynewsParam = "";
    String formattemp = notify.getFormat();
    if(!"".equals(formattemp))
    querynewsParam = querynewsParam + " and n.FORMAT ='" + formattemp + "'";
    String typeIdtemp = notify.getTypeId();
    if(!"".equals(typeIdtemp))
    querynewsParam = querynewsParam + " and n.TYPE_ID ='" + typeIdtemp + "'";
    String fromIdtemp = notify.getFromId();
    if(!"".equals(fromIdtemp))
    querynewsParam = querynewsParam + " and n.FROM_ID in (" + fromIdtemp + ")";
    String subjecttemp = notify.getSubject();
    if(!"".equals(subjecttemp))
    querynewsParam = querynewsParam + " and n.SUBJECT like '%" + T9DBUtility.escapeLike(subjecttemp) + "%' "+T9DBUtility.escapeLike();
    if(!"".equals(beginDatetemp))
    querynewsParam = querynewsParam + " and " + T9DBUtility.getDateFilter("n.SEND_TIME", beginDatetemp, ">=");//to_char(n.SEND_TIME,'yyyy-mm-dd')>='" + beginDatetemp + "'";
    if(!"".equals(endDatetemp))
    querynewsParam = querynewsParam + " and " + T9DBUtility.getDateFilter("n.SEND_TIME", T9Utility.getDateTimeStr(T9Utility.getDayAfter(endDatetemp,1)), "<");//to_char(n.SEND_TIME,'yyyy-mm-dd')<='" + endDatetemp + "'";
    String content = notify.getContent(); 
    if(!"".equals(content))
    querynewsParam = querynewsParam + " and n.CONTENT like '%" + T9DBUtility.escapeLike(content) + "%' "+T9DBUtility.escapeLike();
    
    String queryNotifySql = "SELECT p.USER_NAME,d.DEPT_NAME,n.* from NOTIFY n,PERSON p,DEPARTMENT d where PUBLISH='1' and n.FROM_ID=p.SEQ_ID and p.DEPT_ID=d.SEQ_ID"
                            +" and BEGIN_DATE<="+ T9DBUtility.currDateTime()+" and ("+ T9DBUtility.findInSet(Integer.toString(loginDeptId), "n.TO_ID") + " or " + T9DBUtility.findInSet(userPriv,"n.PRIV_ID")
                            +" or " + T9DBUtility.findInSet(Integer.toString(seqUserId),"n.USER_ID") + " or " +T9DBUtility.findInSet("0", "TO_ID")+ ") ";
    String querySql = queryNotifySql+querynewsParam;
    String dbms = T9SysProps.getString(T9SysPropKeys.DBCONN_DBMS);
    if("".equals(field)) {
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        querySql = querySql + " order by [TOP] desc, BEGIN_DATE desc,SEND_TIME desc";
      }else {
        querySql = querySql + " order by TOP desc, BEGIN_DATE desc,SEND_TIME desc";
      }
      
    }else {
      if (dbms.equals(T9Const.DBMS_SQLSERVER)) {
        querySql = querySql + " order by [TOP] desc," + field;
      }else {
        querySql = querySql + " order by TOP desc," + field;
      }
      
      if("1".equals(ascDesc)) {
        querySql = querySql + " desc";
        System.out.println("SQL1111111:"+querySql);
      }else {
        querySql = querySql + " asc";
      }
    }
    stmt = dbConn.createStatement();
    rs = stmt.executeQuery(querySql);
   while(rs.next()) { 
     
          temp ++;  
          
         
          String toNameTitle = "";
          String toNameStr = "";
          String publishDesc = "";
          String typeName = "";  
          String notifyStatus = "";
          T9News newss = new T9News(); 
          int seqId = rs.getInt("SEQ_ID");//自增ID
          String subject = rs.getString("SUBJECT");//新闻标题
          String fromId = rs.getString("FROM_ID");//新闻发布人

          String fromName = rs.getString("USER_NAME");
          String readers = rs.getString("READERS");//新闻发布时间
          String top = rs.getString("TOP");//评论类型（0-实名评论；1-匿名评论；2-禁止评论）

          String format = rs.getString("FORMAT");//新闻格式（0-普通格式；1-MHT格式；2-超链接）
          String providerName = rs.getString("USER_NAME");
          String deptName = rs.getString("DEPT_NAME");
          String privId = rs.getString("PRIV_ID");//新闻发布人

          String userId = rs.getString("USER_ID");//新闻发布人

          String toId = rs.getString("TO_ID");//新闻发布人

          String typeId = rs.getString("TYPE_ID");//新闻类型，对应小代码表

          String publish = rs.getString("PUBLISH");
          Date sendTime = rs.getDate("SEND_TIME");//发送时间

          Date beginDate = rs.getDate("BEGIN_DATE");//开始时间

          Date endDate = rs.getDate("END_DATE");//结束时间
          notify.setSeqId(rs.getInt("SEQ_ID"));
        
        //新闻标题超链接对应的title属性，显示完整的新闻标题

        String subjectTitle = "";
        if(subject.length()>50) {
          subjectTitle = subject;
          subject = subject.substring(0, 50) + "...";
        }
        
        if("0".equals(publish)){
          publishDesc = "<font color=red>未发布</font>";
        }
        if("2".equals(publish)){
          publishDesc = "<font color=blue>待审批</font>";
        }
        if("3".equals(publish)){
          publishDesc = "<font color=red>未通过</font><br><a href='#'>审批意见</a>";
        }
        if(endDate != null || "0".equals(publish)){
          if(endDate.compareTo(new Date()) < 0 ){
             publishDesc ="<font color='#FF0000'><b>终止</font>";
          }
        }
        
        
        if("1".equals(publish)){
          publishDesc = "";
        }
        
       String endDateStr = "";
        if(!"".equals(endDateStr)&&endDateStr!=null) {
          endDateStr = endDate.toString();
        }
        String deptId = "";
        String queryuserNameStr = "select USER_NAME,DEPT_ID from PERSON where SEQ_ID='"+fromId+"'";
        
        Statement stmtt = null;
        ResultSet rss = null;
        try {
          stmtt = dbConn.createStatement(); 
          rss = stmtt.executeQuery(queryuserNameStr);
           if(rss.next()) {
             fromName = rss.getString("USER_NAME");
             deptId = rss.getString("DEPT_ID");
           }
        } catch(Exception ex) {
          ex.printStackTrace();
        } finally {
          T9DBUtility.close(stmtt, rss, null);
        }
        
         
        try {
          String querydeptNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID='"+deptId+"'";
          stmtt = dbConn.createStatement(); 
          rss = stmtt.executeQuery(querydeptNameStr);
           if(rss.next()) {
             deptName = rss.getString("DEPT_NAME");
           }
        } catch(Exception ex) {
          ex.printStackTrace();
        } finally {
          T9DBUtility.close(stmtt, rss, null);
        }
         
        //根据新闻类型字段的值，获取新闻类型的代码描述

          if(typeId!=null){
             if(!"".equals(typeId.trim())&&!"null".equals(typeId)){
               
                try {
                  String queryTypeNameStr = "SELECT CLASS_DESC from CODE_ITEM where SEQ_ID="+typeId;
                  stmtt = dbConn.createStatement(); 
                  rss = stmtt.executeQuery(queryTypeNameStr);
                   if(rss.next()) {
                     typeName = rss.getString("CLASS_DESC");
                   }
                } catch(Exception ex) {
                  ex.printStackTrace();
                } finally {
                  T9DBUtility.close(stmtt, rss, null);
                }
                
                
             }
         }else{
           typeName = "";
         }
        
        //得到发布范围-部门的名称（串）
        String toName = "";
        String queryToNameStr = "";
        if(toId != null && !"".equals(toId)){
          String[] toIds = toId.split(",");
          toId = "";
          for(int j = 0 ;j < toIds.length ; j++){
            toId += toIds[j] + ",";
          }
          toId = toId.substring(0, toId.length() - 1);
        }
        if("0".equals(toId)) {
          toName = "全体部门";
        }else if(!"".equals(toId.trim())&&toId!=null){
           
           try {
             queryToNameStr = "select DEPT_NAME from DEPARTMENT where SEQ_ID in (" + toId + ")";
             stmtt = dbConn.createStatement();
             rss = stmtt.executeQuery(queryToNameStr);
             while(rss.next()) {
               toName = toName + rss.getString("DEPT_NAME") + ",";
             }
           } catch(Exception ex) {
             ex.printStackTrace();
           } finally {
             T9DBUtility.close(stmtt, rss, null);
           }
        }
        //得到发布范围-角色的名称（串）
        String privName = "";
        String queryPrivNameStr = "";
        if(privId != null && !"".equals(privId)){
          String[] privIds = privId.split(",");
          privId = "";
          for(int j = 0 ;j < privIds.length ; j++){
            privId += privIds[j] + ",";
          }
          privId = privId.substring(0, privId.length() - 1);
        }
        if(privId != null && !"".equals(privId.trim())){
          
          try {
            queryPrivNameStr = "select PRIV_NAME from USER_PRIV where SEQ_ID in (" + privId + ")";
            stmtt = dbConn.createStatement();
            rss = stmtt.executeQuery(queryPrivNameStr);
            while(rss.next()) {
              privName = privName + rss.getString("PRIV_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
        }
    //得到发布范围-人员的姓名（串）
        String userName = "";
        String queryUserNameStr = "";
        if(userId != null && !"".equals(userId)){
          String[] userIds = userId.split(",");
          userId = "";
          for(int j = 0 ;j < userIds.length ; j++){
            userId +=  userIds[j] + ",";
          }
          userId = userId.substring(0, userId.length() - 1);
        }
        if(userId != null && !"".equals(userId.trim())){
          
          try {
            queryUserNameStr = "select USER_NAME from PERSON where SEQ_ID in (" + userId + ")";
            stmtt = dbConn.createStatement();
            rss = stmtt.executeQuery(queryUserNameStr);
            while(rss.next()) {
              userName = userName + rss.getString("USER_NAME") + ",";
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          } finally {
            T9DBUtility.close(stmtt, rss, null);
          }
          
        }
        
        //设置好部门的名称（串）的显示格式
        if(!"".equals(toName)) {
          toNameTitle = "部门:" + toName;  
          if(toName.length()>20) {
            toName = toName.substring(0, 15)+"...";
          }
          toNameStr = "<font color=#0000FF><b>部门：</b></font>";
          toNameStr += toName;
          toNameStr += "<br>";
        }
       
        //设置好角色的名称（串）的显示格式
        String privNameTitle = "";
        String privNameStr = "";
        if(!"".equals(privName)) {
          privNameTitle =   "角色：" + privName;
          if(privName.length() > 20) {
            privName = privName.substring(0, 15)+"...";
          }
          privNameStr =  "<font color=#0000FF><b>角色：</b></font>";
          privNameStr += privName;
          privNameStr += "<br>";
        }
        
     //设置好人员的姓名（串）的显示格式
        String userNameTitle = "";
        String userNameStr = "";
        if(!"".equals(userName)) {
          userNameTitle =  "人员：" + userName;
          if(userName.length() > 20) {
            userName = userName.substring(0, 15)+"...";
          }
          userNameStr =  "<font color=#0000FF><b>人员：</b></font>";
          userNameStr += userName;
          userNameStr += "<br>";
        }
        
        String notifyStatusStr = "";
        if(currentDate.compareTo(beginDate)<0) {
          notifyStatus = "1";
          notifyStatusStr = "待生效";
        }else {
          notifyStatus = "2";
          if(!"2".equals(publish)&&!"3".equals(publish)) {
            notifyStatusStr = "<font color='#00AA00'><b>生效</font>";
          }else {
            if("2".equals(publish)) {
              notifyStatusStr = "<font color='blue'><b>待审批</font>";
            }
            if("3".equals(publish)) {
              notifyStatusStr = "<font color='red'><b>未通过</font><br><a href='javascript:my_affair($NOTIFY_ID)'; title='点击查看审批意见'>审批意见</a>"; 
            }
          }
        }
        if((!"".equals(endDate)&&endDate!=null)||"0".equals(publish)) {
          if(currentDate.compareTo(endDate)>0||endDate==null) {
            notifyStatus = "3";
            notifyStatusStr = "<font color='#FF0000'><b>终止</font>";
          }
        }
        if("0".equals(publish)) {
          notifyStatusStr = "";
        }
        if("1".equals(top)){
          subject = "<font color=red><b>置顶:" + subject + "</b></font>";
        }
        sb.append("{");
        sb.append("seqId:" + seqId);
        sb.append(",fromId:\"" + fromId + "\"");
        sb.append(",fromName:\"" + T9Utility.encodeSpecial(fromName) + "\"");
        sb.append(",typeName:\"" + T9Utility.encodeSpecial(typeName) + "\"");
        sb.append(",toNameTitle:\"" + T9Utility.encodeSpecial(toNameTitle) + "\"");
        sb.append(",toNameStr:\"" + T9Utility.encodeSpecial(toNameStr) + "\"");
        sb.append(",privNameTitle:\"" + T9Utility.encodeSpecial(privNameTitle) + "\"");
        sb.append(",privNameStr:\"" + T9Utility.encodeSpecial(privNameStr) + "\"");
        sb.append(",userNameTitle:\"" + T9Utility.encodeSpecial(userNameTitle) + "\"");
        sb.append(",userNameStr:\"" + T9Utility.encodeSpecial(userNameStr) + "\"");
        sb.append(",subjectTitle:\"" + T9Utility.encodeSpecial(subjectTitle) + "\"");
        sb.append(",toName:\"" + T9Utility.encodeSpecial(toName) + "\"");
        sb.append(",publishDesc:\"" + publishDesc + "\"");
        sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
        sb.append(",subject:\"" + T9Utility.encodeSpecial(subject) + "\"");
        sb.append(",top:\"" + top + "\"");
        sb.append(",notifyStatusStr:\"" + T9Utility.encodeSpecial(notifyStatusStr) + "\"");
        sb.append(",sendTime:\"" + sendTime + "\"");
        sb.append("},");
     }

     if(temp>0) {
       sb.deleteCharAt(sb.length() - 1); 
     }
      sb.append("]");
      sb.append(",pageData:");
      sb.append("{");
      sb.append("pageCount:" + pageCount);
      sb.append(",recordCount:" + recordCount);
      sb.append(",pgStartRecord:" + pgStartRecord);
      sb.append(",pgEndRecord:" + pgEndRecord);
      sb.append("}");
      sb.append("}");
    return sb.toString();
   } 
  
  //--
  public String  showReader(Connection conn,String seqId,String displayAll) throws Exception {
    T9ORM orm = new T9ORM();
    ResultSet rs = null;
    Statement st = null;
    String providerName = "";
    int deptId = 0;
    String deptName = "";
//    String deptPriv ="";
    String optionText = "";
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    int seqID = 0;
    if(!"".equals(seqId)&&seqId!=null) {
      seqID = Integer.parseInt(seqId);
    }
    int unReaderCount = 0;
    try {
      T9Notify notify = (T9Notify)orm.loadObjSingle(conn, T9Notify.class, seqID);
      String queryUserSql = "SELECT p.USER_NAME,p.DEPT_ID,d.DEPT_NAME from PERSON p,DEPARTMENT d where p.DEPT_ID = d.SEQ_ID and p.SEQ_ID='"+notify.getFromId()+"'";
      st = conn.createStatement();
      rs = st.executeQuery(queryUserSql);
      if(rs.next()) {
        providerName = rs.getString("USER_NAME");
        deptId = rs.getInt("DEPT_ID");
        deptName = rs.getString("DEPT_NAME");
//      deptPriv = "1";
      }
      sb.append("seqId:" + notify.getSeqId());
      sb.append(",subject:\"" + T9Utility.encodeSpecial(notify.getSubject()) + "\"");
      sb.append(",deptName:\"" + T9Utility.encodeSpecial(deptName) + "\"");
      sb.append(",providerName:\"" + T9Utility.encodeSpecial(providerName) + "\"");
      sb.append(",newsTime:\"" + notify.getSendTime() + "\"");
      String userNameStr = "";
      String unUser = "";
      
      String toId = notify.getToId();
      
      String privId = notify.getPrivId();
      if(!"0".equals(toId)||!"ALL_DEPT".equals(toId)){
        if(!T9Utility.isNullorEmpty(privId)){
          String[] privIds = privId.split(",");
          privId = "";
          for(int j = 0 ;j < privIds.length ; j++){
            privId += privIds[j] + ",";
          }
          privId = privId.substring(0, privId.length() - 1);
        }
        if(privId != null && !"".equals(privId.trim())){
          Statement privSt = null;
          ResultSet privRs = null;
          try {
            String queryPrivSql = "SELECT DEPT_ID from PERSON where USER_PRIV in (" + privId + ")";
            privSt = conn.createStatement();
            privRs = privSt.executeQuery(queryPrivSql);
            while(privRs.next()) {
              boolean temp = false;
              deptId = privRs.getInt("DEPT_ID");
              if(!T9Utility.isNullorEmpty(toId)){
                String[] toIds = toId.split(",");
                for(int j = 0 ;j < toIds.length ; j++){
                  String toIdTemp =  toIds[j];
                  if(toIdTemp.equals(Integer.toString(deptId))){
                    temp = true;
                    break;
                  }
                }
              }
              if(temp==false){
                toId = toId +","+ deptId + ",";
              }
            }
          } catch (Exception e) {
            throw e;
          } finally {
            T9DBUtility.close(privSt, privRs, log);
          }

        }
      }
      
     String userId = notify.getUserId();
     if(!"0".equals(toId)||!"ALL_DEPT".equals(toId)){
       if(!T9Utility.isNullorEmpty(userId)){
         String[] userIds = userId.split(",");
         userId = "";
         for(int j = 0 ;j < userIds.length ; j++){
           userId += userIds[j] + ",";
         }
         userId = userId.substring(0, userId.length() - 1);
       }
       if(!T9Utility.isNullorEmpty(userId)){
         Statement userSt = null;
         ResultSet userRs = null;
         try {
           queryUserSql = "SELECT DEPT_ID from PERSON where SEQ_ID in (" + userId + ")";
           userSt = conn.createStatement();
           userRs = userSt.executeQuery(queryUserSql);
           while(userRs.next()) {
             boolean temp = false;
             deptId = userRs.getInt("DEPT_ID");
             if(!T9Utility.isNullorEmpty(toId)){
               String[] toIds = toId.split(",");
               for(int j = 0 ;j < toIds.length ; j++){
                 String toIdTemp =  toIds[j];
                 if(toIdTemp.equals(Integer.toString(deptId))){
                   temp = true; 
                   break;
                 }
               }
             }
             if(temp==false) {
               toId = toId +","+deptId + ",";
             }
           }
         } catch (Exception e) {
           throw e;
         } finally {
           T9DBUtility.close(userSt, userRs, log);
         }
       }
     }
     
//     optionText = deptTreeList(conn,0,toId,news,displayAll);
     String temp = getDeptTreeJson(notify,0 , conn,toId);
     sb.append(",listData:").append(temp);
     sb.append("}");
     //T9Out.println(sb.toString());
    } catch (Exception e) {
      throw e;
    }finally {
      T9DBUtility.close(st, rs, log);
    }
    return sb.toString();
  }
  
  public String getDeptTreeJson(T9Notify notify,int deptId , Connection conn,String toId) throws Exception{
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    this.getDeptTree(notify,deptId, sb, 0 , conn,toId);
    if(sb.charAt(sb.length() - 1) == ','){
      sb.deleteCharAt(sb.length() - 1);
    }
    sb.append("]");
    
    return sb.toString();
  }
  
  public void getDeptTree(T9Notify notify,int deptId , StringBuffer sb , int level , Connection conn,String toId) throws Exception{
    //首选分级，然后记录级数，是否为最后一个。。。

    List<T9Department> list = this.getDeptByParentId(deptId , conn,toId);
    
    for(int i = 0 ;i < list.size() ;i ++){
      String flag = "├";
//      if(i == list.size() - 1 ){
//        flag = "└";
//      }
      String tmp = "";
      for(int j = 0 ;j < level ; j++){
        tmp += "　";
      }
      flag = tmp + flag;
      
      T9Department dp = list.get(i);
      sb.append("{");
      sb.append("deptName:\"" +flag + T9Utility.encodeSpecial(dp.getDeptName()) + "\"");
      sb = getUserById(notify, conn,dp.getSeqId(),sb);
    
      this.getDeptTree(notify,dp.getSeqId(), sb, level + 1 , conn,toId);
    }
   
  }
  
  public List<T9Department> getDeptByParentId(int deptId ,Connection conn,String toId) throws Exception {
    // TODO Auto-generated method stub
    T9ORM orm = new T9ORM();
    List<T9Department> list = new ArrayList();
    List<T9Department> listNew = new ArrayList();
    Map filters = new HashMap();
    filters.put("DEPT_PARENT", deptId);
    list  = orm.loadListSingle(conn ,T9Department.class , filters);
    if(!"0".equals(toId)&&!"ALL_DEPT".equals(toId)){//判断是不是全体部门
      for(int i=0;i<list.size();i++) {
        boolean temp = false;
        T9Department dept = list.get(i);
        if(toId != null && !"".equals(toId)){
          String[] toIds = toId.split(",");
          for(int j = 0 ;j < toIds.length ; j++){
            String toIdTemp =  toIds[j];
            if(toIdTemp.equals(Integer.toString(dept.getSeqId()))||childToId(conn,toId,Integer.toString(dept.getSeqId()))==true){
              temp = true;
              break;
            }
          }
        }
        if(temp == true) {
          listNew.add(dept);
        }
      }
    }else{
      listNew = list;
    }
    return listNew;
    
  }
  
 public boolean childToId(Connection conn,String toId,String deptId) throws Exception{
   T9ORM orm = new T9ORM();
   List<T9Department> list = new ArrayList();
   Map filters = new HashMap();
   filters.put("DEPT_PARENT", deptId);
   list  = orm.loadListSingle(conn ,T9Department.class , filters);
   boolean result = false;
   for(int i=0;i<list.size();i++) {
     boolean temp = false;
     T9Department dept = list.get(i);
     if(toId != null && !"".equals(toId)){
       String[] toIds = toId.split(",");
       for(int j = 0 ;j < toIds.length ; j++){
         String toIdTemp =  toIds[j];
         if(toIdTemp.equals(Integer.toString(dept.getSeqId()))){
           temp = true;
           break;
         }
       }
     }
     boolean temp2 = false;
     temp2 = childToId(conn,toId,Integer.toString(dept.getSeqId()));
     if(temp==true||temp2==true){
       result = true;
     }
   }
  
   
   return result;
 }
 
 public boolean findToId(String object,String object2){
   boolean temp = false;
   if(object != null && !"".equals(object)){
     String[] toIds = object.split(",");
     for(int j = 0 ;j < toIds.length ; j++){
       String toIdTemp =  toIds[j];
       if(toIdTemp.equals(object2)){
         temp = true;
         break;
       }
     }
   }
   return temp;
 }
 
//没有从toId中取
  public StringBuffer getUserById(T9Notify notify,Connection conn,int deptIdtemp,StringBuffer sb)throws Exception{
    String unUser = "";
    String userNameStr = ""; // 阅读人员
    String readerDate = ""; // 阅读时间
    Statement userSt = null;
    ResultSet userRs = null;
    try {
//    if("ALL_DEPT".equals(news.getToId())){
      String queryUserSql = "select SEQ_ID,USER_PRIV,USER_NAME from PERSON where DEPT_ID='"+deptIdtemp+"'";
      userSt = conn.createStatement();
      userRs = userSt.executeQuery(queryUserSql);
      while(userRs.next()){
        int userId = userRs.getInt("SEQ_ID");
        String userPriv = userRs.getString("USER_PRIV");
        String userName = userRs.getString("USER_NAME");
        if(notify.getReaders()!=null&& getReaderNames(conn,notify.getReaders(),userName)){
          userNameStr = userNameStr + userName + ",";
          String[] readers = notify.getReaders().split(",");
          for(int j = 0 ;j < readers.length ; j++){
            readers[j] = readers[j].substring(readers[j].indexOf("_")+1,readers[j].length() -1);
            readerDate +=  readers[j]  + ",";
          }
          readerDate = readerDate.substring(0, readerDate.length() - 1);
          //System.err.println(readerDate);
          
//          readCount ++;
        }else{
          if("0".equals(notify.getToId())||findToId(notify.getToId(),Integer.toString(deptIdtemp))==true || findToId(notify.getPrivId(),userPriv)==true || findToId(notify.getUserId(),Integer.toString(userId)) == true){
            unUser = unUser + userName + ",";
 //           unReadCount ++;
          }
        }//end if
      }//end while
 //    } //end if
      sb.append(",userNameStr:\"" +userNameStr + "\"");
      sb.append(",readerDate:\"" +readerDate + "\"");
      sb.append(",unUser:\"" +unUser + "\"");
      sb.append("},");
      return sb;
    } catch (Exception e) {
      throw e;
    } finally {
      T9DBUtility.close(userSt, userRs, null);
    }

  }
  
  public boolean getReaderNames(Connection conn,String reader,String userName) throws Exception{
    String readerNames = "";
    String queryUserNameStr = "";
    Statement st = null;
    ResultSet rs = null;
    
    try {
      boolean temp = false;
      if(reader != null && !"".equals(reader)){
        String[] readers = reader.split(",");
        reader = "";
        for(int j = 0 ;j < readers.length ; j++){
          readers[j] = readers[j].substring(0,readers[j].indexOf("_"));
          reader +=  readers[j]  + ",";
        }
        reader = reader.substring(0, reader.length() - 1);
      }
      if(reader != null && !"".equals(reader.trim())){
        queryUserNameStr = "select USER_NAME from PERSON where SEQ_ID in (" + reader + ")";
        st = conn.createStatement();
        rs = st.executeQuery(queryUserNameStr);
        while(rs.next()) {
          readerNames = readerNames + rs.getString("USER_NAME") + ",";
        }
      }
      
      if(readerNames != null && !"".equals(readerNames)){
        String[] readerNamess = readerNames.split(",");
        readerNames = "";
        for(int j = 0 ;j < readerNamess.length ; j++){
          readerNames =  readerNamess[j] ;
          if(readerNames.equals(userName)) {
            temp = true;
            break;
          }
        }
      }
      return temp;
    } catch (Exception e) {
      throw e;
    } finally {
      T9DBUtility.close(st, rs, null);
    }
  }
  //---查阅情况，
 /**
  * 返回阅读人员的id串
  */
  public String readStatus(Connection conn, int id)throws Exception{
    PreparedStatement ps = null;
    ResultSet rs = null; 
    String sql = "Select readers from notify where seq_id =" + id;
    try{
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();
      if(rs.next()){
        return  rs.getString(1);
      }
    } catch (Exception e){
     throw e;
    }finally{
      T9DBUtility.close(ps, rs, null);
    }
    return null;
  }
  
  /**
   * 判端这个读了这个消息了么
   * @param conn
   * @param userId
   * @param noId
   * @return
   * @throws Exception
   */
  public String haveRead(Connection conn, int userId, int noId)throws Exception{
    String ids = readStatus(conn, noId);    
    if(ids!=null && ids !=""){
      String[] id = ids.split(",");
      for(int i=0; i<id.length; i++){
        if((userId+"").equalsIgnoreCase(id[i])){        
          return "yes";   //这个人读了
        }
      }
    }
    return "no";
  }
  public static void main(String[] args){
    String file = "1012_ABCDEFGHI";
    String dd = file.substring(0);
  }
}

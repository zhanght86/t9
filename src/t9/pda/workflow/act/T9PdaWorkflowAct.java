package t9.pda.workflow.act;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import t9.core.data.T9RequestDbConn;
import t9.core.funcs.person.data.T9Person;
import t9.core.funcs.workflow.data.T9FlowRun;
import t9.core.funcs.workflow.data.T9FlowType;
import t9.core.funcs.workflow.logic.T9FlowRunLogic;
import t9.core.funcs.workflow.logic.T9FlowTypeLogic;
import t9.core.funcs.workflow.logic.T9FlowUserSelectLogic;
import t9.core.funcs.workflow.util.T9PrcsRoleUtility;
import t9.core.funcs.workflow.util.T9WorkFlowUtility;
import t9.core.global.T9BeanKeys;
import t9.core.global.T9Const;
import t9.core.util.T9Utility;
import t9.core.util.db.T9DBUtility;
import t9.pda.workflow.data.T9PdaFlowProcess;
import t9.pda.workflow.data.T9PdaSign;
import t9.pda.workflow.data.T9PdaWorkflow;
import t9.pda.workflow.logic.T9PdaWorkflowLogic;

public class T9PdaWorkflowAct {

  public void search(HttpServletRequest request, HttpServletResponse response) throws Exception{
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int pageSize = Integer.parseInt(request.getParameter("pageSize") == "" || request.getParameter("pageSize") == null ? "5" : request.getParameter("pageSize"));
      int thisPage = Integer.parseInt(request.getParameter("thisPage") == "" || request.getParameter("thisPage") == null ? "1" : request.getParameter("thisPage"));
      //int totalPage = Integer.parseInt(request.getParameter("totalPage") == "" || request.getParameter("totalPage") == null ? "1" : request.getParameter("totalPage"));
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      String sql = " SELECT frp.PRCS_ID, frp.RUN_ID, fr.RUN_NAME, fr.FLOW_ID, ft.FLOW_NAME, ft.FLOW_TYPE, "
      		       + " frp.PRCS_FLAG, frp.FLOW_PRCS, frp.OP_FLAG, fp.PRCS_NAME, fp.FEEDBACK "
                 + " from FLOW_RUN_PRCS frp "
                 + " join FLOW_RUN fr on frp.RUN_ID = fr.RUN_ID "
                 + " join FLOW_TYPE ft on fr.FLOW_ID = ft.SEQ_ID "
                 + " left join FLOW_PROCESS fp on fp.FLOW_SEQ_ID=fr.FLOW_ID and fp.PRCS_ID=frp.FLOW_PRCS "
                 + " WHERE USER_ID='"+person.getSeqId()+"' "
                 + " and fr.DEL_FLAG=0 "
                 + " and PRCS_FLAG<'3' "
                 + " and not (TOP_FLAG='1' and PRCS_FLAG=1) "
                 + " order by fr.RUN_ID desc ";
      
      List<T9PdaWorkflow> list = new ArrayList<T9PdaWorkflow>();
      ps = dbConn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs = ps.executeQuery();
      rs.last();
      int totalSize = rs.getRow();
      if (totalSize == 0) {
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("thisPage", 0);
        request.setAttribute("totalPage", 0);
        request.setAttribute("workflowes", list);
        request.getRequestDispatcher("/pda/workflow/index.jsp").forward(request, response);
        return;
      }
      rs.absolute((thisPage-1) * pageSize + 1);
      int count = 0;
      while(!rs.isAfterLast()) {
        if(count >= pageSize)
          break;
        T9PdaWorkflow workflow = new T9PdaWorkflow();
        workflow.setPrcsId(rs.getInt("PRCS_ID"));
        workflow.setRunId(rs.getInt("RUN_ID"));
        workflow.setRunName(rs.getString("RUN_NAME"));
        workflow.setFlowId(rs.getInt("FLOW_ID"));
        workflow.setFlowName(rs.getString("FLOW_NAME"));
        workflow.setFlowType(rs.getString("FLOW_TYPE"));
        workflow.setPrcsFlag(rs.getString("PRCS_FLAG"));
        workflow.setFlowPrcs(rs.getInt("FLOW_PRCS"));
        workflow.setOpFlag(rs.getString("OP_FLAG"));
        workflow.setPrcsName(rs.getString("PRCS_NAME"));
        workflow.setFeedback(rs.getString("FEEDBACK"));
        list.add(workflow);
        rs.next();
        count++;
      }
      request.setAttribute("pageSize", pageSize);
      request.setAttribute("thisPage", thisPage);
      request.setAttribute("totalPage", totalSize/pageSize + (totalSize%pageSize == 0 ? 0 : 1));
      request.setAttribute("workflowes", list);
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/index.jsp").forward(request, response);
    return;
  }
  
  public void turn(HttpServletRequest request, HttpServletResponse response) throws Exception{
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      String sql = " SELECT FLOW_NAME, FLOW_TYPE, fr.RUN_NAME,p.USER_NAME,PARENT_RUN,PRCS_NAME,PRCS_TO,REMIND_FLAG "
                 + " from FLOW_TYPE ft "
                 + " join FLOW_RUN fr on fr.FLOW_ID = ft.SEQ_ID "
                 + " join FLOW_PROCESS fp on fr.FLOW_ID = fp.FLOW_SEQ_ID " 
                 + " LEFT JOIN PERSON p ON fr.BEGIN_USER=p.SEQ_ID "
                 + " WHERE fr.RUN_ID="+runId
                 + " and fr.FLOW_ID="+flowId
                 + " and fp.PRCS_ID="+flowPrcs;
      ps = dbConn.prepareStatement(sql);
      rs = ps.executeQuery();
      String  prcsTo = "";
      if(rs.next()){
        String flowName = rs.getString("FLOW_NAME");
        String flowType = rs.getString("FLOW_TYPE");
        String runName = rs.getString("RUN_NAME");
        String beginUserName = rs.getString("USER_NAME");
        String parentRun = rs.getString("PARENT_RUN");
        prcsTo = rs.getString("PRCS_TO") == null ? "" : rs.getString("PRCS_TO");
        if(prcsTo.endsWith(",")){
          prcsTo = prcsTo.substring(0, prcsTo.length()-1);
        }
        
        request.setAttribute("flowName", flowName);
        request.setAttribute("flowType", flowType);
        request.setAttribute("runName", runName);
        request.setAttribute("beginUserName", beginUserName);
        request.setAttribute("parentRun", parentRun);
      }
      
      if(T9Utility.isNullorEmpty(prcsTo)){
        sql = " SELECT MAX(PRCS_ID) from FLOW_PROCESS WHERE FLOW_SEQ_ID="+flowId;
        ps = dbConn.prepareStatement(sql);
        rs = ps.executeQuery();
        if(rs.next()){
          int prcsMax = rs.getInt(1);
          if(flowPrcs != prcsMax){
            prcsTo = String.valueOf(flowPrcs+1);
          }
          else{
            prcsTo = "0";
          }
        }
      }
      
      //结束流程
      if("0".equals(prcsTo)){
        List<T9PdaFlowProcess> list = new ArrayList<T9PdaFlowProcess>();
        T9PdaFlowProcess flowProcess = new T9PdaFlowProcess();
        flowProcess.setPrcsName("结束");
        list.add(flowProcess);
        request.setAttribute("flowProcesses", list);
      }
      else{
        sql = " SELECT PRCS_ID,PRCS_NAME,PRCS_IN,PRCS_IN_SET,CONDITION_DESC,USER_LOCK,TOP_DEFAULT,CHILD_FLOW,AUTO_BASE_USER "
        		+ " from FLOW_PROCESS where FLOW_SEQ_ID="+flowId+" and PRCS_ID IN ("+prcsTo+") ";
        ps = dbConn.prepareStatement(sql);
        rs = ps.executeQuery();
        List<T9PdaFlowProcess> list = new ArrayList<T9PdaFlowProcess>();
        while(rs.next()){
          T9PdaFlowProcess flowProcess = new T9PdaFlowProcess();
          flowProcess.setPrcsId(rs.getInt("PRCS_ID"));
          flowProcess.setPrcsName(rs.getString("PRCS_NAME"));
          flowProcess.setPrcsIn(rs.getString("PRCS_IN"));
          flowProcess.setPrcsInSet(rs.getString("PRCS_IN_SET"));
          flowProcess.setConditionDesc(rs.getString("CONDITION_DESC"));
          flowProcess.setUserLock(rs.getString("USER_LOCK"));
          flowProcess.setTopDefault(rs.getString("TOP_DEFAULT"));
          flowProcess.setTopDefault(rs.getString("TOP_DEFAULT"));
          flowProcess.setChildFlow(rs.getInt("CHILD_FLOW"));
          flowProcess.setAutoBaseUser(rs.getInt("AUTO_BASE_USER"));
          list.add(flowProcess);
        }
        request.setAttribute("flowProcesses", list);
      }
      request.setAttribute("flowId", flowId);
      request.setAttribute("runId", runId);
      request.setAttribute("prcsId", prcsId);
      request.setAttribute("flowPrcs", flowPrcs);
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/turn.jsp").forward(request, response);
    return;
  }
  
  @SuppressWarnings("unchecked")
  public void turnNext(HttpServletRequest request, HttpServletResponse response) throws Exception{
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    PreparedStatement ps2 = null;
    ResultSet rs2 = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      String prcsIdNext = (String)request.getParameter("prcsIdNext");
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      if(prcsIdNext == null || "0".equals(prcsIdNext)){
        T9FlowRunLogic flowRunLogic = new T9FlowRunLogic();
        flowRunLogic.turnEnd(person, runId, flowId, prcsId, flowPrcs, "", "", ""  , request.getRemoteAddr() , dbConn , "" , "");
        request.setAttribute("flag", 1);
        request.getRequestDispatcher("/pda/workflow/stop.jsp").forward(request, response);
        return;
      }
      else{
        String sql = " SELECT FLOW_NAME, FLOW_TYPE, fr.RUN_NAME,p.USER_NAME,PARENT_RUN,PRCS_NAME,PRCS_TO,REMIND_FLAG "
                   + " from FLOW_TYPE ft "
                   + " join FLOW_RUN fr on fr.FLOW_ID = ft.SEQ_ID "
                   + " join FLOW_PROCESS fp on fr.FLOW_ID = fp.FLOW_SEQ_ID " 
                   + " LEFT JOIN PERSON p ON fr.BEGIN_USER=p.SEQ_ID "
                   + " WHERE fr.RUN_ID="+runId
                   + " and fr.FLOW_ID="+flowId
                   + " and fp.PRCS_ID="+flowPrcs;
        ps = dbConn.prepareStatement(sql);
        rs = ps.executeQuery();
        String  prcsTo = "";
        if(rs.next()){
          String flowName = rs.getString("FLOW_NAME");
          String flowType = rs.getString("FLOW_TYPE");
          String runName = rs.getString("RUN_NAME");
          String beginUserName = rs.getString("USER_NAME");
          String parentRun = rs.getString("PARENT_RUN");
          prcsTo = rs.getString("PRCS_TO") == null ? "" : rs.getString("PRCS_TO");
          if(prcsTo.endsWith(",")){
            prcsTo = prcsTo.substring(0, prcsTo.length()-1);
          }
          request.setAttribute("flowName", flowName);
          request.setAttribute("flowType", flowType);
          request.setAttribute("runName", runName);
          request.setAttribute("beginUserName", beginUserName);
          request.setAttribute("parentRun", parentRun);
        }
        
        sql = " SELECT PRCS_USER, PRCS_DEPT, PRCS_PRIV, AUTO_TYPE, AUTO_USER from FLOW_PROCESS where FLOW_SEQ_ID="+flowId+" and PRCS_ID ="+prcsIdNext;
        ps2 = dbConn.prepareStatement(sql);
        rs2 = ps2.executeQuery();
        String prcsUser = "";
        String prcsDept = "";
        String prcsPriv = "";
        Map<String,String> op = new HashMap<String,String>();
        Map<String,String> other = new HashMap<String,String>();
        request.setAttribute("op", op);
        request.setAttribute("other", other);
        if(rs2.next()){
          prcsUser = rs2.getString("PRCS_USER") == null ? "" : rs2.getString("PRCS_USER");
          prcsDept = rs2.getString("PRCS_DEPT") == null ? "" : rs2.getString("PRCS_DEPT");
          prcsPriv = rs2.getString("PRCS_PRIV") == null ? "" : rs2.getString("PRCS_PRIV");
          String autoType = rs2.getString("AUTO_TYPE");
          String autoUser = rs2.getString("AUTO_USER");
//          if("3".equals(autoType)){
//              if(!T9Utility.isNullorEmpty(autoUser)){
//                
//                //默认主办人//                //默认经办人//              }
//          } else{
            T9FlowUserSelectLogic logic = new T9FlowUserSelectLogic();
            List<Map> list = logic.getPersons(prcsUser, prcsDept, prcsPriv, dbConn);
            for(Map map : list){
              String seqId = map.get("seqId") + "";
              String userName = map.get("userName") + "";
              op.put(seqId , userName);
              other.put(seqId, userName);
            }
//          }
        } 
        request.setAttribute("flowId", flowId);
        request.setAttribute("runId", runId);
        request.setAttribute("prcsId", prcsId);
        request.setAttribute("flowPrcs", flowPrcs);
        request.setAttribute("prcsIdNext", prcsIdNext);
      }
    } catch(Exception ex){
      throw ex;
    }finally{
      T9DBUtility.close(ps, rs, null);
      T9DBUtility.close(ps2, rs2, null);
    }
    request.getRequestDispatcher("/pda/workflow/turnNext.jsp").forward(request, response);
    return;
  }
  
  @SuppressWarnings("unchecked")
  public void turnSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      String prcsIdNext = (String)request.getParameter("prcsIdNext");
      String prcsUser_ = (String)request.getParameter("prcsUser_"+prcsIdNext) ;
      String[] prcsOpUser_ = request.getParameterValues("prcsOpUser_"+prcsIdNext);
      List list = null;
      if(prcsOpUser_ != null){
        list = Arrays.asList(prcsOpUser_);
      } else {
        list = new ArrayList();
        list.add(prcsUser_);
      }
      
      String topFlag_ = "0";
      Map opUserMap = new HashMap();
      opUserMap.put("prcsUser_"+prcsIdNext, prcsUser_);
      opUserMap.put("prcsOpUser_"+prcsIdNext, list == null ? "" : list.toString().substring(1, list.toString().length() - 1));
      opUserMap.put("topFlag_"+prcsIdNext, topFlag_);
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      T9FlowRunLogic flowRunLogic = new T9FlowRunLogic();
      flowRunLogic.turnNext(person, runId, flowId, prcsId, flowPrcs, prcsIdNext, opUserMap, "手机", dbConn);
      request.setAttribute("flag", 1);
    }
    catch(Exception ex){
      ex.printStackTrace();
      request.setAttribute("flag", 0);
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/turnSubmit.jsp").forward(request, response);
    return;
  }
  
  public void sign(HttpServletRequest request, HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      String content = (String)request.getParameter("content");
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      String sql = " SELECT frf.SEQ_ID, frf.PRCS_ID, frf.USER_ID, CONTENT, ATTACHMENT_ID,"
      		       + " ATTACHMENT_NAME, EDIT_TIME, SIGN_DATA, p.USER_NAME, d.DEPT_NAME, frp.FLOW_PRCS, fp.PRCS_NAME  " 
      		       + " from FLOW_RUN_FEEDBACK frf " 
                 + " join PERSON p on frf.USER_ID = p.SEQ_ID "
                 + " join department d on p.DEPT_ID = d.SEQ_ID "
                 + " left join flow_run_prcs frp on frf.RUN_ID = frp.RUN_ID and frf.PRCS_ID = frp.PRCS_ID "
                 + " left join FLOW_PROCESS fp on frf.PRCS_ID = fp.PRCS_ID and fp.FLOW_SEQ_ID ="+flowId
      		       + " where frf.RUN_ID="+runId+" " 
      		       + " order by frf.PRCS_ID,EDIT_TIME ";
      
      List<T9PdaSign> list = new ArrayList<T9PdaSign>();
      ps = dbConn.prepareStatement(sql);
      rs = ps.executeQuery();
      while(rs.next()){
        T9PdaSign sign = new T9PdaSign();
        sign.setSeqId(rs.getInt("SEQ_ID"));
        sign.setPrcsId(rs.getInt("PRCS_ID"));
        sign.setUserId(rs.getInt("USER_ID"));
        sign.setUserName(rs.getString("USER_NAME"));
        sign.setDeptName(rs.getString("DEPT_NAME"));
        sign.setContent(rs.getString("CONTENT"));
        sign.setAttachmentId(rs.getString("ATTACHMENT_ID"));
        sign.setAttachmentName(rs.getString("ATTACHMENT_NAME"));
        sign.setEditTime(rs.getTimestamp("EDIT_TIME"));
        sign.setSignData(rs.getString("SIGN_DATA"));
        sign.setFlowPrcs(rs.getInt("FLOW_PRCS"));
        sign.setPrcsName(rs.getString("PRCS_NAME"));
        list.add(sign);
      }
      request.setAttribute("signs", list);
      request.setAttribute("flowId", flowId);
      request.setAttribute("runId", runId);
      request.setAttribute("prcsId", prcsId);
      request.setAttribute("flowPrcs", flowPrcs);
    }
    catch(Exception ex){
    
      ex.printStackTrace();
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/sign.jsp").forward(request, response);
    return;
  }
  
  public void signSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      String content = (String)request.getParameter("content");
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      String sql = " INSERT INTO FLOW_RUN_FEEDBACK (RUN_ID,PRCS_ID,USER_ID,CONTENT,ATTACHMENT_ID,ATTACHMENT_NAME,EDIT_TIME) "
      		       + " VALUES ("+runId+","+prcsId+","+person.getSeqId()+",'"+content+"','','',?) ";
      ps = dbConn.prepareStatement(sql);
      ps.setTimestamp(1, T9Utility.parseTimeStamp(T9Utility.parseDate(T9Utility.getDateTimeStr(null)).getTime()));
      int flag = ps.executeUpdate();
      
      request.setAttribute("flag", flag);
    }
    catch(Exception ex){
      ex.printStackTrace();
      request.setAttribute("flag", 0);
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/signSubmit.jsp").forward(request, response);
    return;
  }
  
  public void stop(HttpServletRequest request, HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      int flowId = Integer.parseInt(request.getParameter("flowId"));
      int runId = Integer.parseInt(request.getParameter("runId"));
      int prcsId = Integer.parseInt(request.getParameter("prcsId"));
      int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
      
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      String sql = " update FLOW_RUN_PRCS set PRCS_FLAG='4',DELIVER_TIME=? WHERE RUN_ID="+runId+" and PRCS_ID="+prcsId+" and USER_ID="+person.getSeqId();
      ps = dbConn.prepareStatement(sql);
      ps.setTimestamp(1, T9Utility.parseTimeStamp(T9Utility.parseDate(T9Utility.getDateTimeStr(null)).getTime()));
      int flag = ps.executeUpdate();
      
      request.setAttribute("flag", flag);
    }
    catch(Exception ex){
      ex.printStackTrace();
      request.setAttribute("flag", 0);
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/stop.jsp").forward(request, response);
    return;
  }
  
  
  public void getFormPrintMsg(HttpServletRequest request, HttpServletResponse response) throws Exception{
    int flowId = Integer.parseInt(request.getParameter("flowId"));
    int runId = Integer.parseInt(request.getParameter("runId"));
    int prcsId = Integer.parseInt(request.getParameter("prcsId"));
    int flowPrcs = Integer.parseInt(request.getParameter("flowPrcs"));
    String sRunId = request.getParameter("runId");
    String sFlowId = request.getParameter("flowId");
    String opFlag = request.getParameter("opFlag");
    String feedback = request.getParameter("feedback");
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try{
      T9Person loginUser = (T9Person)request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      
      T9FlowRunLogic frl = new T9FlowRunLogic();
      T9FlowRun flowRun = frl.getFlowRunByRunId(Integer.parseInt(sRunId) , dbConn);
      T9FlowTypeLogic ftl = new T9FlowTypeLogic();
      T9FlowType ft = ftl.getFlowTypeById(Integer.parseInt(sFlowId),dbConn);
      T9Person user = (T9Person) request.getSession().getAttribute("LOGIN_USER");
      String imgPath = T9WorkFlowUtility.getImgPath(request);
      Map form =  frl.getPrintForm(user, flowRun , ft , false, dbConn , imgPath ,"") ;
      request.setAttribute("js", (String)form.get("script"));
      request.setAttribute("css", (String)form.get("css"));
      request.setAttribute("form", (String)form.get("form"));
      
//      if(flowView.indexOf("2") != -1){
//        if(sb.length() != 1){
//          sb.append(",");
//        }
//        T9AttachmentLogic attachLogic = new T9AttachmentLogic();
//        sb.append("attachment:["+ attachLogic.getAttachments(loginUser, flowRun.getRunId() , Integer.parseInt(sFlowId) , dbConn) +"]");
//      }
//      if(flowView.indexOf("3") != -1){
//        if(sb.length() != 1){
//          sb.append(",");
//        }
//        T9FeedbackLogic feedbackLogic = new T9FeedbackLogic();
//        String feedbacks = feedbackLogic.getFeedbacks(loginUser, flowRun.getFlowId() , flowRun.getRunId() ,dbConn);
//        sb.append("feedbacks:" + feedbacks);
//      }
//      if(flowView.indexOf("4") != -1){
//        if(sb.length() != 1){
//          sb.append(",");
//        }
//        T9MyWorkLogic workLogic = new T9MyWorkLogic();
//        workLogic.getPrcsList(flowRun.getRunId(), ft , dbConn , sb);
//      }
      request.setAttribute("flowId", flowId);
      request.setAttribute("runId", runId);
      request.setAttribute("prcsId", prcsId);
      request.setAttribute("flowPrcs", flowPrcs);
      request.setAttribute("opFlag", opFlag);
      request.setAttribute("feedback", feedback); 
      
      String sql = " select RUN_NAME,ATTACHMENT_ID,ATTACHMENT_NAME,BEGIN_TIME from FLOW_RUN where RUN_ID="+runId;
      ps = dbConn.prepareStatement(sql);
      rs = ps.executeQuery();
      if(rs.next()){
        request.setAttribute("runName", rs.getString("RUN_NAME"));
        request.setAttribute("attachmentId", rs.getString("ATTACHMENT_ID"));
        request.setAttribute("attachmentName", rs.getString("ATTACHMENT_NAME"));
        request.setAttribute("beginTime", rs.getTimestamp("BEGIN_TIME")); 
      }
    }catch(Exception ex){
      throw ex;
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/form.jsp").forward(request, response);
    return ;
  }
  
  
  
  
  
  public void getHandlerData(HttpServletRequest request,  HttpServletResponse response) throws Exception {
    String flowIdStr = request.getParameter("flowId");
    String runIdStr = request.getParameter("runId");
    String prcsIdStr = request.getParameter("prcsId");
    String flowPrcsStr = request.getParameter("flowPrcs");
    String isWriteLog = "1";
    String opFlag = request.getParameter("opFlag");
    String feedback = request.getParameter("feedback");
    
    Connection dbConn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
          .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      T9Person loginUser = (T9Person)request.getSession().getAttribute(T9Const.LOGIN_USER);
      
      int runId = Integer.parseInt(runIdStr);
      int prcsId = Integer.parseInt(prcsIdStr);
      int flowId = Integer.parseInt(flowIdStr);
      
      int flowPrcs = 0;
      if (flowPrcsStr != null && !"".equals(flowPrcsStr) && !"null".equals(flowPrcsStr)){
        flowPrcs = Integer.parseInt(flowPrcsStr);
      }
      T9FlowRunLogic flowRunLogic = new T9FlowRunLogic();
      if (!flowRunLogic.canHandlerWrok(runId, prcsId, flowPrcs, loginUser.getSeqId(), dbConn)) {
        String tmp = "此工作已经收回或转交至下一步或结束，您不能办理！";
        request.setAttribute("flag", tmp);
        return ;
      }
      if (flowRunLogic.hasDelete(runId, dbConn)) {
        String tmp = "此工作已经删除，您不能办理！";
        request.setAttribute("flag", tmp);
      } else {
      //取表单相关信息

        String imgPath = T9WorkFlowUtility.getImgPath(request);
        T9PdaWorkflowLogic logic = new T9PdaWorkflowLogic();
        Map map = logic.getHandlerMsg(loginUser , runId , prcsId , flowPrcsStr   , request.getRemoteAddr() , dbConn , imgPath ,isWriteLog);
        String form =  (String)map.get("formMsg");
        request.setAttribute("formMsg",form);
        request.setAttribute("js", (String)map.get("js"));
        request.setAttribute("css", (String)map.get("css"));
      }
      
      request.setAttribute("flowId", flowId);
      request.setAttribute("runId", runId);
      request.setAttribute("prcsId", prcsId);
      request.setAttribute("flowPrcs", flowPrcs);
      request.setAttribute("opFlag", opFlag);
      request.setAttribute("feedback", feedback); 
      
      String sql = " select RUN_NAME,ATTACHMENT_ID,ATTACHMENT_NAME,BEGIN_TIME from FLOW_RUN where RUN_ID="+runId;
      ps = dbConn.prepareStatement(sql);
      rs = ps.executeQuery();
      if(rs.next()){
        request.setAttribute("runName", rs.getString("RUN_NAME"));
        request.setAttribute("attachmentId", rs.getString("ATTACHMENT_ID"));
        request.setAttribute("attachmentName", rs.getString("ATTACHMENT_NAME"));
        request.setAttribute("beginTime", rs.getTimestamp("BEGIN_TIME")); 
      }
    } catch (Exception ex) {
      throw ex;
    }
    finally{
      T9DBUtility.close(ps, rs, null);
    }
    request.getRequestDispatcher("/pda/workflow/edit.jsp").forward(request, response);
    return ;
  }
  
  public void saveFormData(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String flowIdStr = request.getParameter("flowId");
    String runIdStr = request.getParameter("runId");
    String prcsIdStr = request.getParameter("prcsId");
    String flowPrcsStr = request.getParameter("flowPrcs");
    String hiddenStr = request.getParameter("hiddenStr");
    String readOnlyStr = request.getParameter("readOnlyStr");
    Connection dbConn = null;
    T9Person loginUser = (T9Person)request.getSession().getAttribute(T9Const.LOGIN_USER);
    try {
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      int runId = Integer.parseInt(runIdStr);
      int prcsId = Integer.parseInt(prcsIdStr);
      int flowId = Integer.parseInt(flowIdStr);
      int flowPrcs = Integer.parseInt(flowPrcsStr);
      if (hiddenStr == null ) {
        hiddenStr = "";
      }
      if (readOnlyStr == null) {
        readOnlyStr = "";
      }
      T9PrcsRoleUtility roleUtility = new T9PrcsRoleUtility();
      //验证是否有权限
      String roleStr = roleUtility.runRole(runId, flowId, prcsId, loginUser , dbConn);
      if("".equals(roleStr)){//没有权限
        request.setAttribute("flag", 0);
      }else{
        T9FlowRunLogic flowRunLogic = new T9FlowRunLogic();
        //取表单相关信息
        String msg = flowRunLogic.saveFormData(loginUser, flowId, runId, prcsId, flowPrcs, request , hiddenStr , readOnlyStr,dbConn);
        request.setAttribute("flag", 1);
      }
    } catch (Exception ex) {
      throw ex;
    }
    String isTurn = request.getParameter("isTurn");
    if ("1".equals(isTurn)) {
      request.getRequestDispatcher("/t9/pda/workflow/act/T9PdaWorkflowAct/turn.act?P="+loginUser.getSeqId()+"&flowId="+flowIdStr+"&runId="+runIdStr+"&prcsId="+prcsIdStr+"&flowPrcs="+flowPrcsStr).forward(request, response);
    } else {
      request.getRequestDispatcher("/pda/workflow/editSubmit.jsp").forward(request, response);
    }
    
    return ;
  }
}

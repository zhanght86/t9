package t9.core.funcs.workflow.act;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import t9.core.data.T9DbRecord;
import t9.core.data.T9RequestDbConn;
import t9.core.funcs.jexcel.util.T9JExcelUtil;
import t9.core.funcs.person.data.T9Person;
import t9.core.funcs.workflow.logic.T9ConfigLogic;
import t9.core.funcs.workflow.logic.T9FlowReportLogic;
import t9.core.global.T9ActionKeys;
import t9.core.global.T9BeanKeys;
import t9.core.global.T9Const;
import t9.core.util.T9Out;
import t9.core.util.T9Utility;
import t9.core.util.file.T9FileUploadForm;

public class T9FlowReportAct {
  T9FlowReportLogic logic=new T9FlowReportLogic();
  
  public String getReportAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);      
      dbConn = requestDbConn.getSysDbConn();
      String fId = request.getParameter("fId");
      String data="";
      data=this.logic.getReportLogic(dbConn,fId);
      data="{data:["+data+"]}";
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }

  public String getListItemAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String fId = request.getParameter("fId");
      String data="";
      data=this.logic.getListItemLogic(dbConn,fId);
      data="{data:["+data+"]}";
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String editReportAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String rId = request.getParameter("rId");
      String data="";
      data=this.logic.editReportLogic(dbConn,rId);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String getFidByRidAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String rId = request.getParameter("rId");
      String data="";
      data=this.logic.getFidByRidLogic(dbConn,rId);
      data="{fId:'"+data+"'}";
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String getReportPrivByRidAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String rId = request.getParameter("rId");
      String data="";
      data=this.logic.getReportPrivByRidLogic(dbConn,rId);
      data="{data:["+data+"]}";
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String getReportPrivByPidAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String pId = request.getParameter("pId");
      String data="";
      data=this.logic.getReportPrivByPidLogic(dbConn,pId);

      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String getReportByRidAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);      
      String rId = request.getParameter("rId");
      String data="";
      data=this.logic.getReportByRidLogic(dbConn,rId,person);

      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String addReportAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String fId = request.getParameter("fId"); 
      Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9FileUploadForm fileForm = new T9FileUploadForm();
      fileForm.parseUploadRequest(request);
      dbConn = requestDbConn.getSysDbConn();
      this.logic.addReportLogic(dbConn,fileForm,person);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);     
   
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/funcs/workflow/flowreport/newRemind.jsp?fId=" + fId;
  }
  public String addReportPrivAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
      String rid="";
      String rId=request.getParameter("rid");
      String fId = request.getParameter("fId");
      Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9FileUploadForm fileForm = new T9FileUploadForm();
      fileForm.parseUploadRequest(request);
      dbConn = requestDbConn.getSysDbConn();
       rid=request.getParameter("rid");
      this.logic.addReportPrivLogic(dbConn,fileForm,person);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);     
   
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }     
    return "/core/funcs/workflow/flowreport/set_priv/newRemind.jsp";
  }
  public String delReportByIdAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String rId = request.getParameter("rId");    
     this.logic.delReportByIdLogic(dbConn,rId);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);

    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String delReportPrivByIdAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String pId = request.getParameter("pId");    
     this.logic.delReportPrivByIdLogic(dbConn,pId);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);

    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
  
  public String updateReportAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String rId = request.getParameter("rId");
    String fId = request.getParameter("fId");
      Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9FileUploadForm fileForm = new T9FileUploadForm();
      fileForm.parseUploadRequest(request);
      dbConn = requestDbConn.getSysDbConn();
      this.logic.updateReportLogic(dbConn,fileForm,person);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);     
   
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/funcs/workflow/flowreport/editRemind.jsp?rId="+rId+"&fId="+ fId;
  }
  public String updateReportPrivAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String pId=request.getParameter("pId");
    String rId=request.getParameter("rid");
    String fId = request.getParameter("fId");
      Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      T9FileUploadForm fileForm = new T9FileUploadForm();
      fileForm.parseUploadRequest(request);
      dbConn = requestDbConn.getSysDbConn();
      this.logic.updateReportPrivLogic(dbConn,fileForm,person);
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);     
   
    } catch (Exception ex){
       request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }
    return "/core/funcs/workflow/flowreport/set_priv/editRemind.jsp?rid="+rId+"&fId="+fId+"&pId=" + pId;
  }
  
  public String getReportListAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      dbConn = requestDbConn.getSysDbConn();
      String data=this.logic.getReportListLogic(dbConn,person);
      data="{data:["+data+"]}";
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
     request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
     throw ex;
   }
   return "/core/inc/rtjson.jsp";
  }
  
  public String getTableListAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception{
    
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      dbConn = requestDbConn.getSysDbConn();
      String s = request.getParameter("versionNo");
      int versionNo = 1;
      if (!T9Utility.isNullorEmpty(s)) {
        versionNo = Integer.parseInt(s);
      }
      
      String data=this.logic.getTableListLogic(dbConn,person,request.getParameterMap() , versionNo);

    
      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
     request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
     throw ex;
   }
   return "/core/inc/rtjson.jsp";
  }
  public String getChartAct(HttpServletRequest request,
      HttpServletResponse response) throws Exception{
    Connection dbConn = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();

      String data=this.logic.getChartLogic(dbConn,request.getParameterMap());
      data="{data:'"+T9Utility.encodeSpecial(data)+"'}";

      request.setAttribute(T9ActionKeys.RET_STATE,T9Const.RETURN_OK);
      request.setAttribute(T9ActionKeys.RET_DATA,data);
    } catch (Exception ex){
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
     request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
     throw ex;
   }
   return "/core/inc/rtjson.jsp";
  }
  
  public String toExcel(HttpServletRequest request,
      HttpServletResponse response) throws Exception{
    Connection dbConn = null;
    OutputStream ops = null;
    try{
      T9RequestDbConn requestDbConn = (T9RequestDbConn) request
      .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      String s = request.getParameter("versionNo");
      int versionNo = 1;
      if (!T9Utility.isNullorEmpty(s)) {
        versionNo = Integer.parseInt(s);
      }
      T9Person person = (T9Person) request.getSession().getAttribute(T9Const.LOGIN_USER);
      List<LinkedList<String>> data=this.logic.toExcel(dbConn,person,request.getParameterMap() , versionNo);
    
      String fileName = URLEncoder.encode("数据报表.xls","UTF-8");
      fileName = fileName.replaceAll("\\+", "%20");
      response.setHeader("Cache-control","private");
      response.setHeader("Cache-Control","maxage=3600");
      response.setHeader("Pragma","public");
      response.setContentType("application/vnd.ms-excel");
      response.setHeader("Accept-Ranges","bytes");
      response.setHeader("Content-disposition","attachment; filename=\"" + fileName + "\"");
      ops = response.getOutputStream(); 
      ArrayList<T9DbRecord > dbL = this.logic.convertList(data);
      T9JExcelUtil.writeExc(ops, dbL);
    } catch (Exception e){
      e.printStackTrace();
      throw e;
    }finally{
      ops.close();
    }
    return null;
  }
  
  
}

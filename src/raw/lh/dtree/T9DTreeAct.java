package raw.lh.dtree;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import t9.core.data.T9RequestDbConn;
import t9.core.global.T9ActionKeys;
import t9.core.global.T9BeanKeys;
import t9.core.global.T9Const;
import t9.core.util.db.T9DBUtility;

public class T9DTreeAct {
  /**
   * log                                               
   */
  private static Logger log = Logger.getLogger("lh.raw.lh.dtree.T9DTreeAct");
  public String getTree(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String idStr = request.getParameter("id");
    int id = 0;
    if(idStr!=null && !"".equals(idStr)){
      id = Integer.parseInt(idStr);
    }
    Connection dbConn = null;
    Statement stmt = null;
    ResultSet rs = null;
    String queryStr = "select * from TREE where PARENT_ID= " + id;
    try {
       T9RequestDbConn requestDbConn = (T9RequestDbConn)request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
       dbConn = requestDbConn.getSysDbConn();
       stmt = dbConn.createStatement();
       rs = stmt.executeQuery(queryStr);
       StringBuffer sb = new StringBuffer("[");
        while(rs.next()){
          int nodeId = rs.getInt("NODE_Id");
          String name = rs.getString("NAME");
          int isHaveChild = rs.getInt("IS_HAVE_CHILD");
          String extData = rs.getString("EXT_DATA");
          if(extData == null){
            extData = "";
          }
          String imgAddress = rs.getString("IMG_ADDRESS");
          if(imgAddress == null){
            imgAddress = "";
          }
          sb.append("{");
          sb.append("nodeId:\"" + nodeId + "\"");
          sb.append(",name:\"" + name + "\"");
          sb.append(",isHaveChild:" + isHaveChild + "");
          sb.append(",extData:\"" + extData + "\"");
          sb.append(",imgAddress:\"" + imgAddress + "\"");
          sb.append("},");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_OK);
        request.setAttribute(T9ActionKeys.RET_MSRG, "成功返回结果！");
        request.setAttribute(T9ActionKeys.RET_DATA, sb.toString());
        
    }catch(Exception ex) {
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      ex.printStackTrace();
      throw ex;
    }finally {
        T9DBUtility.close(stmt, rs, log);
    }
    return "/core/inc/rtjson.jsp";
  }
 
  public String getTreeOnce(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    
    Connection dbConn = null;
    Statement stmt = null;
    ResultSet rs = null;
    String queryStr = "select * from TREE";
    try {
      T9RequestDbConn requestDbConn = (T9RequestDbConn)request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      stmt = dbConn.createStatement();
       rs = stmt.executeQuery(queryStr);
       StringBuffer sb = new StringBuffer("[");
        while(rs.next()){
          int nodeId = rs.getInt("NODE_Id");
          String name = rs.getString("NAME");
          int isHaveChild = rs.getInt("IS_HAVE_CHILD");
          int parentId = rs.getInt("PARENT_ID");
          String extData = rs.getString("EXT_DATA");
          if(extData == null){
            extData = "";
          }
          String imgAddress = rs.getString("IMG_ADDRESS");
          if(imgAddress == null){
            imgAddress = "";
          }
          sb.append("{");
          sb.append("nodeId:\"" + nodeId + "\"");
          sb.append(",name:\"" + name + "\"");
          sb.append(",parentId:\"" + parentId + "\"");
          sb.append(",isHaveChild:" + isHaveChild + "");
          sb.append(",extData:\"" + extData + "\"");
          sb.append(",imgAddress:\"" + imgAddress + "\"");
          sb.append("},");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_OK);
        request.setAttribute(T9ActionKeys.RET_MSRG, "成功返回结果！");
        request.setAttribute(T9ActionKeys.RET_DATA, sb.toString());
        
    }catch(Exception ex) {
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }finally {
        T9DBUtility.close(stmt, rs, log);
    }
    return "/core/inc/rtjson.jsp";
  }
  public String getNoTree(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    
    String num = request.getParameter("num");
    String length = request.getParameter("length");
    if(num==null||num.equals("0")){
      num = "";
    }
    Connection dbConn = null;
    Statement stmt = null;
    ResultSet rs = null;
    String queryStr = "SELECT * from NOTREE WHERE NODE_ID LIKE '" + num + "%' and LENGTH(NODE_ID) =" + length;
    try {
       T9RequestDbConn requestDbConn = (T9RequestDbConn)request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
       dbConn = requestDbConn.getSysDbConn();
       stmt = dbConn.createStatement();
        rs = stmt.executeQuery(queryStr);
        
        StringBuffer sb = new StringBuffer("[");
        while(rs.next()){
          String nodeId = rs.getString("NODE_Id");
          String name = rs.getString("NAME");
          int isHaveChild = rs.getInt("IS_HAVE_CHILD");
          
          String extData = rs.getString("EXT_DATA");
          if(extData == null){
            extData = "";
          }
          String imgAddress = rs.getString("IMG_ADDRESS");
          if(imgAddress == null){
            imgAddress = "";
          }
          sb.append("{");
          sb.append("nodeId:\"" + nodeId + "\"");
          sb.append(",name:\"" + name + "\"");
          sb.append(",isHaveChild:" + isHaveChild + "");
          sb.append(",extData:\"" + extData + "\"");
          sb.append(",imgAddress:\"" + imgAddress + "\"");
          sb.append("},");
          
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_OK);
        request.setAttribute(T9ActionKeys.RET_MSRG, "成功返回结果！");
        request.setAttribute(T9ActionKeys.RET_DATA, sb.toString());
        
    }catch(Exception ex) {
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }finally {
        T9DBUtility.close(stmt, rs, log);
    }
    return "/core/inc/rtjson.jsp";
  }
  public String getNoTreeOnce(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Connection dbConn = null;
    Statement stmt = null;
    ResultSet rs = null;
    //String url = "jdbc:mysql://localhost:3336/tree?user=root&password=myoa888&useUnicode=true&amp;characterEncoding=utf8";
    String queryStr = "select * from NOTREE";
    try {
      T9RequestDbConn requestDbConn = (T9RequestDbConn)request.getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
      dbConn = requestDbConn.getSysDbConn();
      stmt = dbConn.createStatement();
  
        rs = stmt.executeQuery(queryStr);
        System.out.println(queryStr);
        StringBuffer sb = new StringBuffer("[");
        //{nodeId:'5',name:'1.1.3',isHaveChild:1,extData:'1.1.3',imgAddrss:''}
        //,{nodeId:'4',name:'1.1.2',isHaveChild:0,extData:'1.1.3',imgAddrss:''}
        //,{nodeId:'9',name:'1.1.1',isHaveChild:0,extData:'1.1.3',imgAddrss:''}
        while(rs.next()){
          String nodeId = rs.getString("NODE_Id");
          String name = rs.getString("NAME");
          int isHaveChild = rs.getInt("IS_HAVE_CHILD");
          
          String extData = rs.getString("EXT_DATA");
          if(extData == null){
            extData = "";
          }
          String imgAddress = rs.getString("IMG_ADDRESS");
          if(imgAddress == null){
            imgAddress = "";
          }
          sb.append("{");
          sb.append("nodeId:\"" + nodeId + "\"");
          sb.append(",name:\"" + name + "\"");
          sb.append(",isHaveChild:" + isHaveChild + "");
          sb.append(",extData:\"" + extData + "\"");
          sb.append(",imgAddress:\"" + imgAddress + "\"");
          sb.append("},");
          
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_OK);
        request.setAttribute(T9ActionKeys.RET_MSRG, "成功返回结果！");
        request.setAttribute(T9ActionKeys.RET_DATA, sb.toString());
        
    }catch(Exception ex) {
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG, ex.getMessage());
      throw ex;
    }finally {
        T9DBUtility.close(stmt, rs, log);
    }
    return "/core/inc/rtjson.jsp";
  }
}

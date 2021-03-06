package t9.core.esb.frontend.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import t9.core.esb.frontend.data.T9EsbUploadTask;
import t9.core.util.T9Utility;
import t9.core.util.db.T9DBUtility;
import t9.user.api.core.db.T9DbconnWrap;

public class T9EsbFrontendLogic {
  public List<T9EsbUploadTask> getUploadFieldTaskByStatus( String status) throws Exception{
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    String query = "select * from ESB_UPLOAD_TASK WHERE STATUS in (" + status + ")";
    Statement stm2 = null; 
    ResultSet rs2 = null; 
    List<T9EsbUploadTask> list = new ArrayList<T9EsbUploadTask>();
    try { 
      conn2 = dbUtil.getSysDbConn();
      stm2 = conn2.createStatement(); 
      rs2 = stm2.executeQuery(query); 
      while (rs2.next()){ 
        int seqId = rs2.getInt("SEQ_ID");
        String fileName = rs2.getString("FILE_NAME");
        String guid = rs2.getString("GUID");
        int st = rs2.getInt("STATUS");
        String toId = rs2.getString("TO_ID");
        String optGuid = rs2.getString("OPT_GUID");
        String message = rs2.getString("MESSAGE");
        
        
        T9EsbUploadTask task = new T9EsbUploadTask();
        task.setSeqId(seqId);
        task.setFileName(fileName);
        task.setGuid(guid);
        task.setStatus(st);
        task.setToId(toId);
        task.setOptGuid(optGuid);
        task.setMessage(message);
        list.add(task);
      } 
    } catch(Exception ex) { 
      throw ex; 
    } finally { 
      T9DBUtility.close(stm2, rs2, null); 
      T9DBUtility.closeDbConn(conn2, null);
    }
    return list;
  }
  public void updateStatus( String guid , String status)  {
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    Statement stm2 = null; 
    try {
      conn2 = dbUtil.getSysDbConn();
      String query = "update ESB_UPLOAD_TASK set STATUS = " + status + " where guid='" + guid + "'";
      stm2 = conn2.createStatement(); 
      stm2.executeUpdate(query); 
      conn2.commit();
    } catch(Exception ex) { 
      //throw ex; 
      ex.printStackTrace();
    } finally { 
      T9DBUtility.close(stm2, null, null); 
      T9DBUtility.closeDbConn(conn2, null);
    }
  }
  public void addEsbUploadTask(String fileName , String guid , int status , String toId, String optGuid , String message) throws Exception {
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    PreparedStatement stm2 = null; 
    try {
      conn2 = dbUtil.getSysDbConn();
      optGuid = T9Utility.null2Empty(optGuid);
      String query = "insert into ESB_UPLOAD_TASK ( FILE_NAME, GUID, STATUS , TO_ID, OPT_GUID , MESSAGE) values(?,?,? ,?,? , ? ) ";
      stm2 = conn2.prepareStatement(query);
      stm2.setString(1, fileName);
      stm2.setString(2, guid);
      stm2.setInt(3, status);
      stm2.setString(4, toId);
      stm2.setString(5, optGuid);
      stm2.setString(6, message);
      stm2.executeUpdate(); 
      conn2.commit();
    } catch(Exception ex) { 
      //throw ex; 
      ex.printStackTrace();
    } finally { 
      T9DBUtility.close(stm2, null, null); 
      T9DBUtility.closeDbConn(conn2, null);
    }
  }
  
  public boolean hasEsbUploadTask( String guid) {
    // TODO Auto-generated method stub
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn2 = dbUtil.getSysDbConn();
      String sql = "select 1 " +
          " from ESB_UPLOAD_TASK" +
          " where GUID =?";
      ps = conn2.prepareStatement(sql);
      ps.setString(1, guid);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      T9DBUtility.close(ps, null, null);
      T9DBUtility.closeDbConn(conn2, null);
    }
    return false;
  }
  public boolean hasEsbUploadFinish( String guid) {
    // TODO Auto-generated method stub
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn2 = dbUtil.getSysDbConn();
      String sql = "select 1 " +
          " from ESB_UPLOAD_TASK" +
          " where GUID =? and STATUS = '0'";
      ps = conn2.prepareStatement(sql);
      ps.setString(1, guid);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      T9DBUtility.close(ps, null, null);
      T9DBUtility.closeDbConn(conn2, null);
    }
    return false;
  }
  public boolean hasSendField(String guid) {
    // TODO Auto-generated method stub
    T9DbconnWrap dbUtil = new T9DbconnWrap();
    
    Connection conn2 = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn2 = dbUtil.getSysDbConn();
      String sql = "select 1 " +
          " from ESB_UPLOAD_TASK" +
          " where GUID =? and STATUS = '-3'";
      ps = conn2.prepareStatement(sql);
      ps.setString(1, guid);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      T9DBUtility.close(ps, null, null);
      T9DBUtility.closeDbConn(conn2, null);
    }
    return false;
  }
}

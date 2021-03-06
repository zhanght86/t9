package t9.core.funcs.org.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import raw.cy.db.generics.T9ORM;
import t9.core.funcs.org.data.T9Organization;
import t9.core.module.oa.logic.T9OaSyncLogic;
import t9.core.module.report.logic.T9NnitSyncLogic;
import t9.core.module.report.logic.T9ReportSyncLogic;
import t9.core.util.db.T9DBUtility;

public class T9OrgLogic {
  private static Logger log = Logger.getLogger("t9.core.act.action.T9TestAct");
  
  public T9Organization get(Connection conn)throws Exception {
    Statement stmt = null;
    ResultSet rs = null; 
    T9Organization org = null;
    try {
      String queryStr = "select SEQ_ID, UNIT_NAME, TELEPHONE, MAX, POSTCODE," +
          " ADDRESS, WEBSITE, EMAIL, SIGN_IN_USER, ACCOUNT from ORGANIZATION";
      stmt = conn.createStatement();
      rs = stmt.executeQuery(queryStr);
      //System.out.println(queryStr);
      
      if (rs.next()) {
        org =  new T9Organization();
        org.setSeqId(rs.getInt("SEQ_ID"));
        org.setUnitName(rs.getString("UNIT_NAME"));
        org.setTelephone(rs.getString("TELEPHONE"));
        org.setMax(rs.getString("MAX"));
        org.setPostcode(rs.getString("POSTCODE"));
        org.setAddress(rs.getString("ADDRESS"));
        org.setWebsite(rs.getString("WEBSITE"));
        org.setEmail(rs.getString("EMAIL"));
        org.setSignInUser(rs.getString("SIGN_IN_USER"));
        org.setAccount(rs.getString("ACCOUNT"));
      }     
    }catch(Exception ex) {
      throw ex;
    }finally {
      T9DBUtility.close(stmt, rs, log);
    }
    return org;
  }
  
  public void update(Connection conn, T9Organization org)throws Exception {
    PreparedStatement pstmt = null;
    try{   
      String queryStr = "update ORGANIZATION set UNIT_NAME = ?, TELEPHONE = ?, MAX = ?, POSTCODE = ?," +
          " ADDRESS = ?, WEBSITE = ?, EMAIL = ?, SIGN_IN_USER = ?, ACCOUNT = ? where SEQ_ID = ?";
      pstmt = conn.prepareStatement(queryStr);
      pstmt.setString(1, org.getUnitName());
      pstmt.setString(2, org.getTelephone());
      pstmt.setString(3, org.getMax());
      pstmt.setString(4, org.getPostcode());
      pstmt.setString(5, org.getAddress());
      pstmt.setString(6, org.getWebsite());
      pstmt.setString(7, org.getEmail());
      pstmt.setString(8, org.getSignInUser());
      pstmt.setString(9, org.getAccount());
      pstmt.setInt(10, org.getSeqId());
      pstmt.executeUpdate();
    }catch(Exception ex) {
      throw ex;
    }finally {
      T9DBUtility.close(pstmt, null, log);
    }
    updateOa( org); 
  }
  public void updateOa(T9Organization org) throws Exception {
    if (T9ReportSyncLogic.hasSync) {
      Connection reportConn = T9ReportSyncLogic.getReportConn();
      T9NnitSyncLogic logic = new T9NnitSyncLogic();
      logic.delNuit(reportConn);
      logic.addNuit(org, reportConn);
      if (reportConn != null) {
        reportConn.close();
      }
    }
    if (T9OaSyncLogic.hasSync) {
      Connection oaConn = T9OaSyncLogic.getOAConn();
      t9.core.module.oa.logic.T9NnitSyncLogic logic = new t9.core.module.oa.logic.T9NnitSyncLogic();
      logic.delNuit(oaConn);
      logic.addNuit(org, oaConn);
      if (oaConn != null) {
        oaConn.close();
      }
    }
  }
  public void add(Connection conn, T9Organization org)throws Exception {
    PreparedStatement pstmt = null;
    try{
      String queryStr = "insert into ORGANIZATION (UNIT_NAME, TELEPHONE, MAX, POSTCODE" +
          ",ADDRESS, WEBSITE, EMAIL, SIGN_IN_USER, ACCOUNT) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
      pstmt = conn.prepareStatement(queryStr);
      pstmt.setString(1, org.getUnitName());
      pstmt.setString(2, org.getTelephone());
      pstmt.setString(3, org.getMax());
      pstmt.setString(4, org.getPostcode());
      pstmt.setString(5, org.getAddress());
      pstmt.setString(6, org.getWebsite());
      pstmt.setString(7, org.getEmail());
      pstmt.setString(8, org.getSignInUser());
      pstmt.setString(9, org.getAccount());
      pstmt.executeUpdate();
    }catch(Exception ex) {
      throw ex;
    }finally {
      T9DBUtility.close(pstmt, null, log);
    }
    updateOa( org); 
  }
  
}

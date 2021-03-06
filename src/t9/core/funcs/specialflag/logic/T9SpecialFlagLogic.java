package t9.core.funcs.specialflag.logic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import t9.core.util.db.T9DBUtility;

public class T9SpecialFlagLogic {
  private static Logger log = Logger.getLogger("t9.core.act.action.T9SpecialFlagLogic");
  
  public int selectFlag(Connection conn, String flagCode, String flagSort)throws Exception {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String queryStr = "select count(*) from SPECIAL_FLAG where FLAG_CODE='" + flagCode + "'and FLAG_SORT = '" + flagSort +"'";
      stmt = conn.createStatement();
      rs = stmt.executeQuery(queryStr);
      int num = 0;
      if(rs.next()){      
        num = rs.getInt(1);
      }
      return num;
    }catch(Exception ex) {
      throw ex;
    }finally {
      T9DBUtility.close(stmt, null, log);
    }
  }
  
  public int getMaxFlagCode(Connection conn, String sort)throws Exception {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String queryStr = "select max(FLAG_CODE) from SPECIAL_FLAG where FLAG_SORT='" + sort + "'"; 
      stmt = conn.createStatement();
      rs = stmt.executeQuery(queryStr);
      int max = 0;
      if (rs.next()){ 
        if(rs.getString(1) != null ){
          max = Integer.parseInt(rs.getString(1));
        }      
      }
      return max;
    }catch(Exception ex) {
      throw ex;
    }finally {
      T9DBUtility.close(stmt, null, log);
    }
  }
}

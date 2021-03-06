package t9.rad.dsdef.act;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import t9.core.data.T9DsTable;
import t9.core.data.T9RequestDbConn;
import t9.core.global.T9BeanKeys;
import t9.core.util.T9Utility;
import t9.rad.dsdef.logic.T9DsDefLogic;

public class T9DsDefFormAct {
  private static Logger log = Logger.getLogger("t9.rad.dsdef.logic.T9DsDefAct");

  public Object build(HttpServletRequest request, String classTable, String tableNo) throws Exception {

    //String classTable = (String) request.getParameter("T9DsTable");

    Class classTypeTable = Class.forName(classTable);
    //System.out.println(classTypeTable);

    T9DsTable obj = (T9DsTable) classTypeTable.newInstance();
    Field[] fields = classTypeTable.getDeclaredFields();
    Object valueSet = null;

    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      Object objo;
      String strg = "get";
      if (field.getType().equals(Boolean.TYPE)) {
        strg = "is";
      }
      String fieldN = field.getName();
      //System.out.println(fieldN+"88");
      String valueStr = request.getParameter(fieldN);
      
      //System.out.println(valueStr+"2222222");
      if (field.getType().equals(Integer.TYPE)) {
        if (T9Utility.isNullorEmpty(valueStr) || valueStr.equals("")) {
          objo = new Integer(0);
        } else {
          objo = Integer.valueOf(valueStr);
        }
      } else if (field.getType().equals(Float.TYPE)) {
        objo = Float.valueOf(valueStr);
      } else if (field.getType().equals(Double.TYPE)) {
        objo = Double.valueOf(valueStr);
      } else {
        objo = valueStr;
      }
      
      String stringLetter = fieldN.substring(0, 1).toUpperCase();

      String getName = strg + stringLetter + fieldN.substring(1);
      String setName = "set" + stringLetter + fieldN.substring(1);

      Method getMethod = classTypeTable.getMethod(getName);
      
      //System.out.println(getMethod);
      Method setMethod = classTypeTable.getMethod(setName, new Class[] { field
          .getType() });

      valueSet = setMethod.invoke(obj, new Object[] { objo });
      Object value = getMethod.invoke(obj);

      //System.out.println(fieldN + " : " + value);

    }

    T9DsDefLogic ddl = new T9DsDefLogic();

    Connection dbConn = null;
    T9RequestDbConn requestDbConn = (T9RequestDbConn) request
        .getAttribute(T9BeanKeys.REQUEST_DB_CONN_MGR);
    dbConn = requestDbConn.getSysDbConn();

    ddl.DsDefInsert(dbConn, obj, tableNo);
    //ddl.DsDefInsertZ(dbConn, tableNo, obj);

    return obj;
  }

}

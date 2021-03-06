package t9.setup.fis.act;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import t9.core.global.T9ActionKeys;
import t9.core.global.T9Const;
import t9.core.global.T9SysPropKeys;
import t9.core.global.T9SysProps;
import t9.setup.fis.logic.T9FISSetupLogic;
import t9.setup.util.T9ERPSetupUitl;

public class T9FisSetupAct {
  /**
   * T9Fis安装包
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  public String setup(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    try {
      String erpcontextPath = "t9erp";
      String setupContentPath = "setup";

      T9FISSetupLogic fsl = new T9FISSetupLogic(request.getParameterMap(), "sqlserver", T9SysProps.getProp(T9SysPropKeys.JSP_ROOT_DIR));
      fsl.createFisDb(erpcontextPath, "TDSYS"); //创建账套库
      fsl.insertErpMeun2T9(erpcontextPath); //注册T9菜单
      String installPath = T9SysProps.getRootPath();
      T9ERPSetupUitl.updateInstallInfo(installPath, setupContentPath, "fis", "1");
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_OK);
      request.setAttribute(T9ActionKeys.RET_MSRG, "");
    }catch(Exception ex) {
      request.setAttribute(T9ActionKeys.RET_STATE, T9Const.RETURN_ERROR);
      request.setAttribute(T9ActionKeys.RET_MSRG,  ex.getMessage());
      throw ex;
    }
    return "/core/inc/rtjson.jsp";
  }
}

package t9.core.webinfo.dto;

public class T9WebInfoAttachment {
  private String name ;
  private String filePath;
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getFilePath() {
    return filePath;
  }
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
  public StringBuffer toJSON(){
    StringBuffer sb = new StringBuffer("{");
    sb.append("\"fileName\":\"" + this.name);
    sb.append("\",\"filePath\":\"" + this.filePath);
    sb.append("\"}");
    return sb; 
  }
}

package t9.custom.attendance.data;

import java.util.Date;

public class T9AnnualLeave {
  private int seqId;
  private String userId;
  private Date applyTime;
  private String leaderId;
  private int leaveDays;
  private String leaveType;
  private Date leaveDate1;
  private Date leaveDate2;
  private String status;
  private String reason;
  private String allow;
  private Date destroyTime;
  public Date getDestroyTime() {
    return destroyTime;
  }
  public void setDestroyTime(Date destroyTime) {
    this.destroyTime = destroyTime;
  }
  public String getAllow() {
    return allow;
  }
  public void setAllow(String allow) {
    this.allow = allow;
  }
  public int getSeqId() {
    return seqId;
  }
  public void setSeqId(int seqId) {
    this.seqId = seqId;
  }
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public Date getApplyTime() {
    return applyTime;
  }
  public void setApplyTime(Date applyTime) {
    this.applyTime = applyTime;
  }
  public String getLeaderId() {
    return leaderId;
  }
  public void setLeaderId(String leaderId) {
    this.leaderId = leaderId;
  }
  public int getLeaveDays() {
    return leaveDays;
  }
  public void setLeaveDays(int leaveDays) {
    this.leaveDays = leaveDays;
  }
  public String getLeaveType() {
    return leaveType;
  }
  public void setLeaveType(String leaveType) {
    this.leaveType = leaveType;
  }
  public Date getLeaveDate1() {
    return leaveDate1;
  }
  public void setLeaveDate1(Date leaveDate1) {
    this.leaveDate1 = leaveDate1;
  }
  public Date getLeaveDate2() {
    return leaveDate2;
  }
  public void setLeaveDate2(Date leaveDate2) {
    this.leaveDate2 = leaveDate2;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getReason() {
    return reason;
  }
  public void setReason(String reason) {
    this.reason = reason;
  }
}

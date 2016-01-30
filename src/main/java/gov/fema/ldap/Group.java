package gov.fema.ldap;

public class Group {
	
	private String reportType;
	private String disasterNumber;
	private String name;
	private String member;
	
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getDisasterNumber() {
		return disasterNumber;
	}
	public void setDisasterNumber(String disasterNumber) {
		this.disasterNumber = disasterNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}

}

// structure for gson
public class ProjectParameters {
    String projectName;
    String adminName;
    String billingCode;

    public void print() {
        System.out.println( "projectName: " + this.projectName);
        System.out.println( "adminName: " + this.adminName);
        System.out.println( "billingCode: " + this.billingCode);
    };
}

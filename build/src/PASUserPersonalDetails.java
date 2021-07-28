class PASUserPersonalDetails {
  String firstName;
  String middleName;
  String lastName;

  public void print() {
    System.out.println("  personalDetails:");
    System.out.println("    firstName: "+ this.firstName);
    System.out.println("    middleName: "+ this.middleName);
    System.out.println("    lastName: "+ this.lastName);
  }
}

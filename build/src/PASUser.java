class PASUser {
  Integer id;
  String username;
  String source;
  String userType;
  Boolean componentUser;
  String[] vaultAuthorization;
  String location;
  PASUserPersonalDetails personalDetails;

  public void print() {
	System.out.println( "  id: " + this.id);
	System.out.println( "  username: " + this.username);
	System.out.println( "  source: " + this.source);
	System.out.println( "  userType: " + this.userType);
	System.out.println( "  componentUser: " + this.componentUser);
	System.out.println( "  vaultAuthorization: ");
        for(Integer i=0; i<this.vaultAuthorization.length; i++) {
           System.out.println("    "+this.vaultAuthorization[i]);
        }
	System.out.println( "  location: " + this.id);
	this.personalDetails.print();
  }

} // PASUser

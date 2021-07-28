public class PASSafe {

    String safeUrlId;
    String safeName;
    Integer safeNumber;
    String description;
    String location;
    PASSafeCreator creator;
    Boolean olacEnabled;
    String managingCPM;
    Integer numberOfVersionsRetention;
    Integer numberOfDaysRetention;
    Boolean autoPurgeEnabled;
    Long creationTime;
    Long lastModificationTime;
    Boolean isExpired;

    public void print() {
	System.out.println( "safeUrlId: " + this.safeUrlId);
	System.out.println( "safeName: " + this.safeName);
	System.out.println( "safeNumber: " + Integer.toString(this.safeNumber));
	System.out.println( "description: " + this.description);
	System.out.println( "location: " + this.location);
        this.creator.print();
	System.out.println( "olacEnabled: " + this.olacEnabled);
	System.out.println( "managingCPM: " + this.managingCPM);
	System.out.println( "numberOfVersionsRetention: " + this.numberOfVersionsRetention);
	System.out.println( "numberOfDaysRetention: " + this.numberOfDaysRetention);
	System.out.println( "autoPurgeEnabled: " + this.autoPurgeEnabled);
	System.out.println( "creationTime: " + Long.toString(this.creationTime));
	System.out.println( "lastModificationTime: " + Long.toString(this.lastModificationTime));
	System.out.println( "isExpired: " + this.isExpired);
    };
}

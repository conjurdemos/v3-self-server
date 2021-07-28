class PASUserList {
  PASUser[] Users;
  Integer Total;

  public void print() {
    System.out.println("Users:");
    for(Integer i=0; i<this.Users.length; i++) {
      this.Users[i].print();
      System.out.println("");
    }
    System.out.println("Total: "+Integer.toString(this.Total)); 
  }
}

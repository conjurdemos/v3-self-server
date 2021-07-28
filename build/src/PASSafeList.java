public class PASSafeList {
    PASSafe[] value;
    Integer count;

    public void print() {
	for(Integer i=0; i<value.length; i++) {
		this.value[i].print();
		System.out.println("");
	}
    }
}

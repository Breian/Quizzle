import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {
    //COMPARATAORE DELLA TREEMAP PER ORDINARE LA CLASSIFICA
    Map<String, Long> base;

    public ValueComparator(Map<String, Long> values) {
        this.base = values;
    }

    @Override
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 merge keys
    }
}
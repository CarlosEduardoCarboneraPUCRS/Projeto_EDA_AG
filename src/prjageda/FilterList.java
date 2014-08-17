package prjageda;

import java.util.ArrayList;
import java.util.List;

public class FilterList<E> {

    public <T> List filterList(List<T> original, Filter filter, E text) {
        List<T> filterList = new ArrayList<>();

        for (T object : original) {
            if (filter.isMatched(object, text)) {
                filterList.add(object);

            }

        }

        return filterList;

    }

}

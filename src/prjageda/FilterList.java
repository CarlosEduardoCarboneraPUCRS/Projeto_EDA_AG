/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package prjageda;

import java.util.ArrayList;
import java.util.List;

public class FilterList<E> {
    public  <T> List filterList(List<T> originalList, Filter filter, E text) {
        List<T> filterList = new ArrayList<>();
        for (T object : originalList) {
            if (filter.isMatched(object, text)) {
                filterList.add(object);
            } 
        }
        return filterList;
    }
}
package com.andb.apps.todo;

import android.graphics.Color;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FilteredListsTest {

    private ArrayList<Tasks> initialTaskList = new ArrayList<>();
    private ArrayList<Tags> initialTagList = new ArrayList<>();
    private ArrayList<Integer> initialFilters = new ArrayList<>();

    private ArrayList<Tasks> expectedInboxList = new ArrayList<>();
    private ArrayList<Tasks> expectedBrowseList = new ArrayList<>();
    private ArrayList<Integer> expectedFilters = new ArrayList<>();


    @Test
    public void filtersAreCorrect() {

        setupTagLists();
        setupFilters();
        setupTaskList();

        Tags parent = initialTagList.get(initialFilters.get(initialFilters.size() - 1));
        ArrayList<Integer> filteredFilters = FilteredLists.filterChildren(
                initialTagList,
                parent,
                initialFilters
        );

        ArrayList<Tasks> filteredInboxList = FilteredLists.filterInbox(initialTaskList, initialFilters);
        ArrayList<Tasks> filteredBrowseList = FilteredLists.filterBrowse(filteredInboxList, initialFilters, parent.getChildren(), initialTagList, true);

        assert expectedInboxList.equals(filteredInboxList);
        assert expectedBrowseList.equals(filteredBrowseList);
        assert expectedFilters.equals(filteredFilters);
    }


    private void setupFilters() {
        initialFilters.addAll(Arrays.asList(5, 0));
        expectedFilters.addAll(Arrays.asList(1, 2));
    }

    private void setupTagLists() {
        Tags groceries = new Tags("groceries", randomColor(), true);
        groceries.setChildren(new ArrayList<>(Arrays.asList(1, 2)));

        Tags produce = new Tags("produce", randomColor(), false);
        produce.setChildren(new ArrayList<>(Arrays.asList(7, 8)));

        Tags meats = new Tags("meats", randomColor(), true);

        Tags reminders = new Tags("reminders", randomColor(), false);

        Tags computer = new Tags("computer", randomColor(), false);
        computer.setChildren(new ArrayList<>(Arrays.asList(7)));

        Tags lists = new Tags("lists", randomColor(), false);
        lists.setChildren(new ArrayList<>(Arrays.asList(0)));

        Tags arduino = new Tags("arduino", randomColor(), true);

        Tags fruits = new Tags("fruits", randomColor(), true);

        Tags vegetables = new Tags("vegetables", randomColor(), true);

        initialTagList.addAll(Arrays.asList(groceries, produce, meats, reminders, computer, lists, arduino, fruits, vegetables));

    }

    private void setupTaskList() {
        Tasks serial = new Tasks("serial", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(4, 7)), DateTime.now(), false, 1);
        Tasks salmon = new Tasks("salmon", new ArrayList<String>(Arrays.asList("1 lb", "boneless")), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 0, 2)), DateTime.now(), false, 2);
        Tasks cauliflower = new Tasks("cauliflower", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 0, 1, 8)), DateTime.now(), false, 3);
        Tasks parts = new Tasks("computer parts", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 4)), DateTime.now(), false, 4);

        initialTaskList.addAll(Arrays.asList(serial, salmon, cauliflower, parts));
        expectedBrowseList.addAll(Arrays.asList(salmon));
        expectedInboxList.addAll(Arrays.asList(salmon, cauliflower));
    }


    private int randomColor() {
        Random random = new Random();
        return getIntFromColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

}

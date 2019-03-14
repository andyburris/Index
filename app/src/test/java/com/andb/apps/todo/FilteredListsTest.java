package com.andb.apps.todo;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.objects.reminders.LocationFence;
import com.andb.apps.todo.objects.reminders.SimpleReminder;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        ArrayList<Integer> filteredFilters = FilteredLists.INSTANCE.filterChildren(
                initialTagList,
                parent,
                initialFilters
        );

        ArrayList<Tasks> filteredInboxList = FilteredLists.INSTANCE.filterInbox(initialTaskList, initialFilters);
        ArrayList<Tasks> filteredBrowseList = FilteredLists.INSTANCE.filterBrowse(filteredInboxList, initialFilters, parent.getChildren(), initialTagList, true);

        assert expectedInboxList.equals(filteredInboxList);
        assert expectedBrowseList.equals(filteredBrowseList);
        assert expectedFilters.equals(filteredFilters);
    }


    private void setupFilters() {
        initialFilters.addAll(Arrays.asList(5, 0));
        expectedFilters.addAll(Arrays.asList(1, 2));
    }

    private void setupTagLists() {
        Tags groceries = new Tags(0, "groceries", randomColor(), true, new ArrayList<>(), 0, 0);
        groceries.setChildren(new ArrayList<>(Arrays.asList(1, 2)));

        Tags produce = new Tags(1, "produce", randomColor(), false, new ArrayList<>(), 0, 1);
        produce.setChildren(new ArrayList<>(Arrays.asList(7, 8)));

        Tags meats = new Tags(2, "meats", randomColor(), true, new ArrayList<>(), 0, 2);

        Tags reminders = new Tags(3, "reminders", randomColor(), false, new ArrayList<>(), 0, 3);

        Tags computer = new Tags(4, "computer", randomColor(), false, new ArrayList<>(), 0, 4);
        computer.setChildren(new ArrayList<>(Collections.singletonList(7)));

        Tags lists = new Tags(5, "lists", randomColor(), false, new ArrayList<>(), 0, 5);
        lists.setChildren(new ArrayList<>(Collections.singletonList(0)));

        Tags arduino = new Tags(6, "arduino", randomColor(), true, new ArrayList<>(), 0, 6);

        Tags fruits = new Tags(7, "fruits", randomColor(), true, new ArrayList<>(), 0, 7);

        Tags vegetables = new Tags(8, "vegetables", randomColor(), true, new ArrayList<>(), 0, 8);

        initialTagList.addAll(Arrays.asList(groceries, produce, meats, reminders, computer, lists, arduino, fruits, vegetables));

    }

    private void setupTaskList() {
        Tasks serial = new Tasks("serial", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(4, 7)), new ArrayList<SimpleReminder>(Arrays.asList(new SimpleReminder(DateTime.now()))), new ArrayList<LocationFence>(), 1, -1, false);
        Tasks salmon = new Tasks("salmon", new ArrayList<String>(Arrays.asList("1 lb", "boneless")), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 0, 2)), new ArrayList<SimpleReminder>(Arrays.asList(new SimpleReminder(DateTime.now()))), new ArrayList<LocationFence>(), 2, -1, false);
        Tasks cauliflower = new Tasks("cauliflower", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 0, 1, 8)), new ArrayList<SimpleReminder>(Arrays.asList(new SimpleReminder(DateTime.now()))), new ArrayList<LocationFence>(), 3, -1, false);
        Tasks parts = new Tasks("computer parts", new ArrayList<String>(), new ArrayList<Boolean>(), new ArrayList<Integer>(Arrays.asList(5, 4)), new ArrayList<SimpleReminder>(Arrays.asList(new SimpleReminder(DateTime.now()))), new ArrayList<LocationFence>(), 4, -1, false);

        initialTaskList.addAll(Arrays.asList(serial, salmon, cauliflower, parts));
        expectedBrowseList.addAll(Collections.singletonList(salmon));
        expectedInboxList.addAll(Arrays.asList(salmon, cauliflower));
    }


    private int randomColor() {
        Random random = new Random();
        return getIntFromColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private int getIntFromColor(int Red, int Green, int Blue) {
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

}

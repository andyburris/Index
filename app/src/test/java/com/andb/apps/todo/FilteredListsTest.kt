package com.andb.apps.todo

import com.andb.apps.todo.filtering.filterInbox
import com.andb.apps.todo.filtering.filterTags
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.data.model.Tag
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.data.model.reminders.LocationFence
import com.andb.apps.todo.data.model.reminders.SimpleReminder
import com.andb.apps.todo.ui.inbox.SORT_TIME
import com.andb.apps.todo.ui.inbox.isDivider
import com.andb.apps.todo.utilities.Current
import org.joda.time.DateTime
import org.junit.Test
import java.util.*

class FilteredListsTest {

    private val initialTaskList = ArrayList<Task>()
    private val initialTagList = ArrayList<Tag>()
    private val initialFilters = ArrayList<Tag>()

    private val expectedInboxList = ArrayList<Task>()
    private val expectedFilters = ArrayList<Tag>()


    @Test
    fun filtersAreCorrect() {

        setupProject()
        setupTagLists()
        setupFilters()
        setupTaskList()

        val filteredFilters = initialTagList.filterTags(initialFilters)

        val filteredInboxList = initialTaskList.filterInbox(SORT_TIME, initialFilters)
            .filter { !isDivider(it) }

        println("expectedFilters: $expectedFilters")
        println("filteredFilters: $filteredFilters")
        println("expectedInbox: $expectedInboxList")
        println("filteredInbox: $filteredInboxList")

        assert(expectedFilters == filteredFilters)
        assert(expectedInboxList.map { it.listName } == filteredInboxList.map { it.listName })
    }

    private fun setupProject() {
        Current.bufferProjects.add(Project(0, "", 0x000000, 0))
    }

    private fun setupFilters() {
        initialFilters.addAll(Arrays.asList(initialTagList[5], initialTagList[0]))
        expectedFilters.addAll(Arrays.asList(initialTagList[1], initialTagList[2]))
    }

    private fun setupTagLists() {
        val groceries = Tag(0, "groceries", randomColor(), true, ArrayList(), 0, 0)
        groceries.children = ArrayList(Arrays.asList(1, 2))

        val produce = Tag(1, "produce", randomColor(), false, ArrayList(), 0, 1)
        produce.children = ArrayList(Arrays.asList(7, 8))

        val meats = Tag(2, "meats", randomColor(), true, ArrayList(), 0, 2)

        val reminders = Tag(3, "reminders", randomColor(), false, ArrayList(), 0, 3)

        val computer = Tag(4, "computer", randomColor(), false, ArrayList(), 0, 4)
        computer.children = ArrayList(listOf(7))

        val lists = Tag(5, "lists", randomColor(), false, ArrayList(), 0, 5)
        lists.children = ArrayList(listOf(0))

        val arduino = Tag(6, "arduino", randomColor(), true, ArrayList(), 0, 6)

        val fruits = Tag(7, "fruits", randomColor(), true, ArrayList(), 0, 7)

        val vegetables = Tag(8, "vegetables", randomColor(), true, ArrayList(), 0, 8)

        initialTagList.addAll(Arrays.asList(groceries, produce, meats, reminders, computer, lists, arduino, fruits, vegetables))

    }

    private fun setupTaskList() {
        val sub = ArrayList<String>()
        val chck = ArrayList<Boolean>()
        val rems = ArrayList(listOf(SimpleReminder(DateTime.now().minusDays(2))))
        val locs = ArrayList<LocationFence>()

        val serial = Task("serial", sub, chck, ArrayList(Arrays.asList(4, 7)), rems, locs, 1, 0, false)
        val salmon = Task("salmon", ArrayList(Arrays.asList("1 lb", "boneless")), chck, ArrayList(Arrays.asList(5, 0, 2)), rems, locs, 2, 0, false)
        val cauliflower = Task("cauliflower", sub, chck, ArrayList(Arrays.asList(5, 0, 1, 8)), rems, locs, 3, 0, false)
        val parts = Task("computer parts", sub, chck, ArrayList(Arrays.asList(5, 4)), rems, locs, 4, 0, false)

        initialTaskList.addAll(Arrays.asList(serial, salmon, cauliflower, parts))
        expectedInboxList.addAll(Arrays.asList(cauliflower, salmon))
    }


    private fun randomColor(): Int {
        val random = Random()
        return getIntFromColor(random.nextInt(255), random.nextInt(255), random.nextInt(255))
    }

    private fun getIntFromColor(red: Int, green: Int, blue: Int): Int {
        var red = red
        var green = green
        var blue = blue
        red = red shl 16 and 0x00FF0000 //Shift red 16-bits and mask out other stuff
        green = green shl 8 and 0x0000FF00 //Shift green 8-bits and mask out other stuff
        blue = blue and 0x000000FF //Mask out anything not blue.

        return -0x1000000 or red or green or blue //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

}

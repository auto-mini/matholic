package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.ClassRosterSelectionState
import com.local.matholickiosk.kiosk.domain.SingleFlightGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminUiAsyncStateTest {
    @Test
    fun selectingAnotherClassClearsThePreviousRosterWhileLoading() {
        val state = ClassRosterSelectionState()
        state.resolve("class-a", setOf("student-a"))

        assertEquals("class-b", state.select("class-b")?.classId)
        assertEquals("class-b", state.selectedClassId)
        assertTrue(state.membershipStudentIds.isEmpty())
        assertTrue(state.isLoading)
    }

    @Test
    fun lateRosterResultCannotOverwriteTheCurrentClass() {
        val state = ClassRosterSelectionState()
        val classARequest = requireNotNull(state.select("class-a"))
        state.select("class-b")

        assertFalse(state.apply(classARequest, setOf("student-a")))
        assertEquals("class-b", state.selectedClassId)
        assertTrue(state.membershipStudentIds.isEmpty())
        assertTrue(state.isLoading)
    }

    @Test
    fun currentRosterResultFinishesLoading() {
        val state = ClassRosterSelectionState()
        val request = requireNotNull(state.select("class-b"))

        assertTrue(state.apply(request, setOf("student-b")))
        assertEquals(setOf("student-b"), state.membershipStudentIds)
        assertFalse(state.isLoading)
    }

    @Test
    fun oldResultForARevisitedClassCannotReplaceTheLatestRequest() {
        val state = ClassRosterSelectionState()
        val oldRequest = requireNotNull(state.select("class-a"))
        state.select("class-b")
        val currentRequest = requireNotNull(state.select("class-a"))

        assertFalse(state.apply(oldRequest, setOf("stale-student")))
        assertTrue(state.isLoading)
        assertTrue(state.apply(currentRequest, setOf("current-student")))
        assertEquals(setOf("current-student"), state.membershipStudentIds)
    }

    @Test
    fun savedRosterInvalidatesAnOlderLoadForTheSameClass() {
        val state = ClassRosterSelectionState()
        val request = requireNotNull(state.select("class-a"))

        assertTrue(state.replaceIfSelected("class-a", setOf("saved-student")))
        assertFalse(state.apply(request, setOf("stale-student")))
        assertEquals(setOf("saved-student"), state.membershipStudentIds)
    }

    @Test
    fun refreshCannotReplaceANewerAvailableClassSelection() {
        val state = ClassRosterSelectionState()
        state.resolve("class-a", setOf("student-a"))
        val refreshSelection = state.snapshotSelection()
        val classBRequest = requireNotNull(state.select("class-b"))

        assertFalse(
            state.resolveRefresh(
                snapshot = refreshSelection,
                loadedClassId = "class-c",
                loadedStudentIds = setOf("student-c"),
                availableClassIds = setOf("class-a", "class-b", "class-c"),
                forceLoadedSelection = false,
            ),
        )
        assertEquals("class-b", state.selectedClassId)
        assertTrue(state.membershipStudentIds.isEmpty())
        assertTrue(state.isLoading)
        assertTrue(state.apply(classBRequest, setOf("student-b")))
        assertEquals(setOf("student-b"), state.membershipStudentIds)
    }

    @Test
    fun activeSessionClassStillReplacesANewerSelection() {
        val state = ClassRosterSelectionState()
        state.resolve("class-a", setOf("student-a"))
        val refreshSelection = state.snapshotSelection()
        val staleClassBRequest = requireNotNull(state.select("class-b"))

        assertTrue(
            state.resolveRefresh(
                snapshot = refreshSelection,
                loadedClassId = "class-a",
                loadedStudentIds = setOf("student-a-current"),
                availableClassIds = setOf("class-a", "class-b"),
                forceLoadedSelection = true,
            ),
        )
        assertEquals("class-a", state.selectedClassId)
        assertEquals(setOf("student-a-current"), state.membershipStudentIds)
        assertFalse(state.isLoading)
        assertFalse(state.apply(staleClassBRequest, setOf("student-b")))
    }

    @Test
    fun refreshFallsBackWhenTheNewerSelectionWasDeleted() {
        val state = ClassRosterSelectionState()
        state.resolve("class-a", setOf("student-a"))
        val refreshSelection = state.snapshotSelection()
        state.select("class-b")

        assertTrue(
            state.resolveRefresh(
                snapshot = refreshSelection,
                loadedClassId = "class-a",
                loadedStudentIds = setOf("student-a-current"),
                availableClassIds = setOf("class-a"),
                forceLoadedSelection = false,
            ),
        )
        assertEquals("class-a", state.selectedClassId)
        assertEquals(setOf("student-a-current"), state.membershipStudentIds)
        assertFalse(state.isLoading)
    }

    @Test
    fun singleFlightGateRejectsDuplicatesUntilTheOperationFinishes() {
        val gate = SingleFlightGate()

        assertTrue(gate.tryStart())
        assertFalse(gate.tryStart())
        assertTrue(gate.isActive)

        gate.finish()

        assertFalse(gate.isActive)
        assertTrue(gate.tryStart())
    }
}

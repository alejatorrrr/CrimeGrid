package com.crimegrid.domain

import com.crimegrid.data.*
import org.junit.Assert.*
import org.junit.Test

class CastleArtTest {
    @Test fun existingCase003RemainsUniqueAndUsesItsOriginalLogicalSuspects(){
        val case=CaseRepository{javaClass.getResource("/cases/case003.json")!!.readText()}.load()
        assertEquals(listOf("ada","gael","lara","víctor"),case.suspects.map{it.id})
        assertEquals(listOf(case.solution),Engine.solve(case))
        assertEquals("Lara",Engine.culprit(case,case.solution.positions)?.name)
        assertEquals(Position(0,1),case.office)
    }
}

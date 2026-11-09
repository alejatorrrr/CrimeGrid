package com.crimegrid.domain

import com.crimegrid.data.*
import org.junit.Assert.*
import org.junit.Test

class ResolutionTest {
    private fun load(id:String)=CaseRepository{javaClass.getResource("/cases/case$id.json")!!.readText()}.load()
    @Test fun stepsUseUnchangedCluesAndConvergeToOneRealSolution(){
        for(id in listOf("001","002","003","004")){
            val case=load(id)
            val steps=Resolution.steps(case,case.solution.positions)
            assertEquals(case.clues.map{it.text},steps.map{it.clue})
            assertEquals(1,steps.last().remaining)
            assertTrue(steps.zipWithNext().all{(a,b)->a.remaining>=b.remaining})
            assertEquals(8,steps.sumOf{it.facts.size})
        }
    }
    @Test fun incorrectBoardCannotProduceAnOfficialExplanation(){
        val case=load("004")
        assertThrows(IllegalArgumentException::class.java){Resolution.steps(case,emptyMap())}
    }
}

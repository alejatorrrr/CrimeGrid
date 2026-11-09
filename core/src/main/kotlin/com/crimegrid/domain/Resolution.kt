package com.crimegrid.domain

import com.crimegrid.data.*

/** Post-solution explanation built only from the real constraints; never used as in-game feedback. */
object Resolution {
    data class Step(val clue:String,val remaining:Int,val facts:List<String>)
    fun steps(case:Case,positions:Map<String,Position>):List<Step> {
        require(Engine.complete(case,positions))
        require(case.size==4) { "Detailed enumeration is bounded to the current 4x4 cases" }
        val known=mutableSetOf<String>()
        return case.clues.mapIndexed{index,clue->
            val candidates=Engine.solve(case.copy(clues=case.clues.take(index+1)),1000)
            check(candidates.isNotEmpty())
            val facts=buildList {
                case.suspects.forEach { suspect ->
                    val rows=candidates.map { it.positions.getValue(suspect.id).row }.toSet()
                    val columns=candidates.map { it.positions.getValue(suspect.id).column }.toSet()
                    if(rows.size==1 && known.add("${suspect.id}:row")) add("${suspect.name}: fila ${rows.single()+1}.")
                    if(columns.size==1 && known.add("${suspect.id}:column")) add("${suspect.name}: columna ${('A'.code+columns.single()).toChar()}.")
                }
            }
            Step(clue.text,candidates.size,facts)
        }
    }
}

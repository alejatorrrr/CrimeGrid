package com.crimegrid.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Imported artwork is not a case: no suspects, clues, crime location or solution are inferred. */
@Serializable data class ArtRoom(val id:String,val name:String,val file:String,
    @SerialName("source_crop") val sourceCrop:List<Int>)
@Serializable data class ArtMap(
    @SerialName("case_id") val sourceCaseId:String,
    val theme:String,
    @SerialName("source_size") val sourceSize:List<Int>,
    @SerialName("tile_size") val tileSize:List<Int>,
    val orientation:String,
    val rooms:List<ArtRoom>
) {
    fun validate():ArtMap {
        require(sourceSize==listOf(1536,1536) && tileSize==listOf(384,384))
        val expected=(1..4).flatMap { row->('A'..'D').map { column->"$column$row" } }.toSet()
        require(rooms.map { it.id }.toSet()==expected && rooms.size==16)
        require(rooms.map { it.file }.toSet().size==16)
        rooms.forEach { room->
            val x=(room.id[0]-'A')*384;val y=(room.id[1]-'1')*384
            require(room.sourceCrop==listOf(x,y,x+384,y+384))
            require(room.file=="${room.id}.png")
        }
        return this
    }
}
class ArtMapRepository(private val readJson:()->String) {
    fun load()=Json.decodeFromString<ArtMap>(readJson()).validate()
}

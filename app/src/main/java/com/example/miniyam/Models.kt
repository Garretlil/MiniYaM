package com.example.miniyam

data class Track(
    val id: Long,
    var title: String,
    var artist:String,
    var duration:Int,
    var imageUri: String,
    var audioUri: String,
)
class TrackList {
    private val tracks = mutableListOf<Track>()
    private val title:String =""


    fun addTrack(track: Track) {
        tracks.add(track)
    }
    fun getTracks(): List<Track> {
        return tracks
    }
    fun removeTrack(track: Track) {
        tracks.remove(track)
    }
    fun playTrack(track: Track) {

    }

}
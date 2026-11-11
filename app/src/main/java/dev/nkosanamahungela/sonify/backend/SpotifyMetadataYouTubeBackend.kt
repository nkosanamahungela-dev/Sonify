package dev.nkosanamahungela.sonify.backend

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.SongItem
import dev.nkosanamahungela.sonify.SonifyApplication
import dev.nkosanamahungela.sonify.backend.youtube.YouTubeBackend
import dev.nkosanamahungela.sonify.data.CatalogPlaylist
import dev.nkosanamahungela.sonify.data.CatalogTrack
import dev.nkosanamahungela.sonify.data.SearchItem
import dev.nkosanamahungela.sonify.data.SearchResults
import dev.nkosanamahungela.sonify.data.SpotifyHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * A hybrid source: Spotify describes the music; YouTube plays and downloads it.
 *
 * Spotify URIs identify catalogue pages, while the player and download store
 * need a YouTube video id because those are the streams they can resolve.
 * Matching is cached for the life of the process. No YouTube Music catalogue
 * endpoint is used here.
 */
@UnstableApi
class SpotifyMetadataYouTubeBackend(
    private val container: SonifyApplication,
    private val metadata: MusicBackend,
    private val playback: MusicBackend,
) : MusicBackend {

    override val id = BackendId.YOUTUBE_MUSIC
    override val authState get() = playback.authState
    override val isReady get() = playback.isReady
    override val searchNeedsSetup get() = metadata.searchNeedsSetup

    private val youtubeIds = ConcurrentHashMap<String, String>()
    private val spotifyUris = ConcurrentHashMap<String, String>()
    private val resolverSlots = Semaphore(4)

    override suspend fun refreshAuth() {
        playback.refreshAuth()
        metadata.refreshAuth()
    }

    override suspend fun logOut() = playback.logOut()

    override suspend fun search(query: String, labels: SearchLabels, offset: Int): SearchResults {
        val result = metadata.search(query, labels, offset)
        return result.copy(
            tracks = playable(result.tracks),
            episodes = emptyList(),
            videos = emptyList(),
        )
    }

    override suspend fun playlists(): List<CatalogPlaylist> = metadata.playlists()

    /** Spotify's saved albums, through the user's registered Web API session. */
    override suspend fun savedAlbums(): List<CatalogPlaylist> {
        if (!container.webApi.isReady) return emptyList()
        val loaded = mutableListOf<CatalogPlaylist>()
        var offset = 0
        while (true) {
            val page = container.api.savedAlbums(limit = 50, offset = offset)
            loaded += page.items.mapNotNull { saved ->
                val album = saved.album
                val uri = album.uri ?: return@mapNotNull null
                CatalogPlaylist(
                    uri = uri,
                    name = album.name,
                    artworkUrl = album.images.firstOrNull()?.url,
                )
            }
            offset += page.items.size
            if (page.items.isEmpty() || offset >= page.total) return loaded
        }
    }

    /** Spotify's followed artists, cursor-paged until the account's list ends. */
    override suspend fun followedArtists(): List<SearchItem> {
        if (!container.webApi.isReady) return emptyList()
        val loaded = mutableListOf<SearchItem>()
        var after: String? = null
        do {
            val page = container.api.followedArtists(after = after).artists
            loaded += page.items.mapNotNull { artist ->
                val uri = artist.uri ?: return@mapNotNull null
                SearchItem(
                    uri = uri,
                    title = artist.name,
                    subtitle = "Artist",
                    artworkUrl = artist.images.firstOrNull()?.url,
                )
            }
            after = page.cursors?.after
        } while (after != null)
        return loaded
    }

    override suspend fun savedShows(): List<CatalogPlaylist> = metadata.savedShows()

    /** Spotify's new releases, not YouTube Music's release feed. */
    override suspend fun newRows(): List<HomeRow> {
        if (!container.webApi.isReady) return emptyList()
        val albums = container.api.newReleases(limit = 20).albums?.items.orEmpty()
            .mapNotNull { album ->
                val uri = album.uri ?: return@mapNotNull null
                CatalogPlaylist(
                    uri = uri,
                    name = album.name,
                    artworkUrl = album.images.firstOrNull()?.url,
                )
            }
        return if (albums.isEmpty()) emptyList() else listOf(HomeRow("New releases", items = albums))
    }
    override suspend fun newEpisodes(): List<CatalogTrack> = playable(metadata.newEpisodes())

    /** Spotify's personalised home feed, parsed from the same gateway as Spotify search. */
    override suspend fun homeFeed(cursor: String?, params: String?): HomeFeed {
        if (cursor != null) return HomeFeed()
        val facet = params.orEmpty()
        val raw = container.gateway.homeFacet(facet) ?: return HomeFeed()
        return HomeFeed(
            rows = SpotifyHome.parse(raw, podcasts = facet == PODCAST_FACET).map { shelf ->
                HomeRow(title = shelf.title, items = shelf.items, strapline = shelf.artist)
            },
        )
    }

    override suspend fun tracksOf(uri: String): List<CatalogTrack> =
        if (uri.startsWith("spotify:")) playable(metadata.tracksOf(uri))
        else playback.tracksOf(uri)

    /** Lyrics and their offline cache remain associated with the YouTube media id. */
    override suspend fun lyrics(uri: String, title: String, artist: String, durationMs: Long) =
        playback.lyrics(uri, title, artist, durationMs)

    override val canEditPlaylists get() = metadata.canEditPlaylists
    override val describesPlaylists get() = metadata.describesPlaylists
    override suspend fun createPlaylist(name: String, description: String, public: Boolean) =
        metadata.createPlaylist(name, description, public)
    override suspend fun setPlaylistCover(uri: String, jpeg: ByteArray) =
        metadata.setPlaylistCover(uri, jpeg)
    override suspend fun playlistDetails(uri: String) = metadata.playlistDetails(uri)
    override suspend fun updatePlaylist(uri: String, name: String, description: String, public: Boolean) =
        metadata.updatePlaylist(uri, name, description, public)
    override suspend fun renamePlaylist(uri: String, name: String) = metadata.renamePlaylist(uri, name)
    override suspend fun deletePlaylist(uri: String) = metadata.deletePlaylist(uri)
    override suspend fun writablePlaylists() = metadata.writablePlaylists()
    override fun canWriteTo(playlistUri: String) = metadata.canWriteTo(playlistUri)
    override suspend fun addToPlaylist(playlistUri: String, trackUri: String) =
        metadata.addToPlaylist(playlistUri, spotifyUris[trackUri] ?: trackUri)
    override suspend fun removeFromPlaylist(playlistUri: String, trackUri: String) =
        metadata.removeFromPlaylist(playlistUri, spotifyUris[trackUri] ?: trackUri)
    override suspend fun isFollowing(artistUri: String) = metadata.isFollowing(artistUri)
    override suspend fun setFollowing(artistUri: String, follow: Boolean) =
        metadata.setFollowing(artistUri, follow)

    override fun owns(uri: String): Boolean =
        uri.startsWith(YouTubeBackend.URI_SCHEME) || uri.startsWith("spotify:")

    override fun createPlayer(host: PlaybackHost): Player = playback.createPlayer(host)

    /** Converts Spotify artist-page tracks to ids the YouTube player can stream. */
    suspend fun toPlayable(tracks: List<CatalogTrack>): List<CatalogTrack> = playable(tracks)

    private suspend fun playable(tracks: List<CatalogTrack>): List<CatalogTrack> =
        coroutineScope {
            tracks.map { track -> async { playable(track) } }.awaitAll().filterNotNull()
        }

    private suspend fun playable(track: CatalogTrack): CatalogTrack? {
        if (track.uri.startsWith(YouTubeBackend.TRACK_PREFIX)) return track
        if (!track.uri.startsWith("spotify:track:")) return null

        val videoId = youtubeIds[track.uri] ?: resolverSlots.withPermit {
            youtubeIds[track.uri] ?: withContext(Dispatchers.IO) {
                val songs = YouTube.search(
                    "${track.name} ${track.artist}",
                    YouTube.SearchFilter.FILTER_SONG,
                ).getOrDefault(emptyList()).filterIsInstance<SongItem>()

                val wantedTitle = normalize(track.name)
                val wantedArtist = normalize(track.artist.substringBefore(','))
                songs.firstOrNull { song ->
                    val title = normalize(song.title)
                    val artist = normalize(song.artists.firstOrNull()?.name.orEmpty())
                    val titleMatches = title == wantedTitle ||
                        title.startsWith(wantedTitle) || wantedTitle.startsWith(title)
                    val artistMatches = wantedArtist.isEmpty() ||
                        artist == wantedArtist || artist.contains(wantedArtist) ||
                        wantedArtist.contains(artist)
                    val durationMs = song.duration?.times(1_000L) ?: 0L
                    val durationMatches = track.durationMs <= 0 || durationMs <= 0 ||
                        abs(durationMs - track.durationMs) <= 30_000L
                    titleMatches && artistMatches && durationMatches
                }?.id
            }?.also { youtubeIds[track.uri] = it }
        } ?: return null

        val mediaUri = "${YouTubeBackend.TRACK_PREFIX}$videoId"
        spotifyUris[mediaUri] = track.uri
        return track.copy(uri = mediaUri)
    }

    private fun normalize(value: String): String =
        value.lowercase().filter(Char::isLetterOrDigit)

    private companion object {
        const val PODCAST_FACET = "podcasts-chip"
    }
}

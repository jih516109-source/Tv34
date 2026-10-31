package com.example.smarttvplayer

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.json.JSONArray
import org.json.JSONObject

data class Channel(var name: String, var url: String, var favorite: Boolean = false)

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private lateinit var prefs: android.content.SharedPreferences
    private var player: ExoPlayer? = null
    private val channels = mutableListOf<Channel>()
    private var showingFavorites = false

    private val bg = 0xFF080F1C.toInt()
    private val panel = 0xFF142238.toInt()
    private val accent = 0xFF55D6BE.toInt()
    private val white = 0xFFF4F7FB.toInt()
    private val muted = 0xFF9FB0C5.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        prefs = getSharedPreferences("smart_tv_player", MODE_PRIVATE)
        loadChannels()
        buildHome()
        if (channels.isEmpty()) {
            channels.add(Channel("ONE+ONE", "https://dash2.antik.sk/live/test_one_plus_one_int_tizen/playlist.m3u8"))
            saveChannels()
            renderChannels()
        }
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(12))
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        val title = TextView(this).apply {
            text = "SMART TV  •  PLAYER"
            textSize = 22f
            setTextColor(white)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val add = makeButton("+ Add channel") { showAddDialog() }
        header.addView(add)
        root.addView(header)

        status = TextView(this).apply {
            text = "লাইভ চ্যানেল চালাতে তালিকা থেকে নির্বাচন করুন"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(8), 0, dp(10))
        }
        root.addView(status)

        player = createPlayer()
        playerView = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            setBackgroundColor(0xFF000000.toInt())
            this.player = this@MainActivity.player
        }
        root.addView(playerView, LinearLayout.LayoutParams(-1, 0, 1.05f))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(6))
        }
        actions.addView(makeButton("All channels") { showingFavorites = false; renderChannels() })
        actions.addView(makeButton("Favorites") { showingFavorites = true; renderChannels() })
        actions.addView(makeButton("＋ URL") { showAddDialog() })
        root.addView(actions)

        val scroll = ScrollView(this).apply {
            isFocusable = false
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                this@MainActivity.list = this
            })
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 0.85f))
        setContentView(root)
        renderChannels()
    }

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(this).build().also { exo ->
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    status.text = "স্ট্রিম চালানো যায়নি: ${error.errorCodeName}"
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) status.text = "লোড হচ্ছে…"
                    if (playbackState == Player.STATE_READY) status.text = "● LIVE"
                }
            })
        }
    }

    private fun renderChannels() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val visible = channels.filter { !showingFavorites || it.favorite }
        if (visible.isEmpty()) {
            list.addView(TextView(this).apply {
                text = if (showingFavorites) "এখনও কোনো Favorite নেই" else "কোনো চ্যানেল নেই — + Add channel চাপুন"
                setTextColor(muted); textSize = 14f; setPadding(dp(8), dp(16), dp(8), dp(16))
            })
        }
        visible.forEach { channel ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(7), dp(6), dp(7))
                setBackgroundColor(panel)
                isFocusable = true
                isFocusableInTouchMode = true
                setOnClickListener { play(channel) }
            }
            val name = TextView(this).apply {
                text = "▶   ${channel.name}"
                textSize = 15f; setTextColor(white)
                setPadding(dp(5), dp(8), dp(5), dp(8))
            }
            row.addView(name, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(makeSmallButton(if (channel.favorite) "★" else "☆") {
                channel.favorite = !channel.favorite; saveChannels(); renderChannels()
            })
            row.addView(makeSmallButton("Edit") { showEditDialog(channel) })
            row.addView(makeSmallButton("×") {
                AlertDialog.Builder(this).setTitle("চ্যানেল মুছবেন?")
                    .setMessage(channel.name)
                    .setNegativeButton("না", null)
                    .setPositiveButton("মুছুন") { _, _ ->
                        channels.remove(channel); saveChannels(); renderChannels()
                    }.show()
            })
            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.bottomMargin = dp(6)
            list.addView(row, lp)
        }
    }

    private fun play(channel: Channel) {
        try {
            status.text = "${channel.name} লোড হচ্ছে…"
            player?.setMediaItem(MediaItem.fromUri(channel.url.trim()))
            player?.prepare()
            player?.playWhenReady = true
        } catch (e: Exception) {
            status.text = "লিংকটি সঠিক নয়"
        }
    }

    private fun showAddDialog() = channelDialog(null)
    private fun showEditDialog(channel: Channel) = channelDialog(channel)

    private fun channelDialog(existing: Channel?) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val name = EditText(this).apply {
            hint = "Channel name"; setSingleLine(true)
            setText(existing?.name ?: "")
        }
        val url = EditText(this).apply {
            hint = "M3U8 / HLS stream URL"
            setText(existing?.url ?: "")
            setSingleLine(false)
            minLines = 2
        }
        box.addView(name); box.addView(url)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add channel" else "Edit channel")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = name.text.toString().trim()
                val u = url.text.toString().trim()
                if (n.isBlank() || !(u.startsWith("https://") || u.startsWith("http://"))) {
                    Toast.makeText(this, "নাম এবং সঠিক http(s) URL দিন", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (existing == null) channels.add(Channel(n, u))
                else { existing.name = n; existing.url = u }
                saveChannels(); renderChannels(); dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 12f
        setTextColor(white); setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel))
        setOnClickListener { action() }
        isFocusable = true
    }

    private fun makeSmallButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 11f
        minWidth = dp(38); setPadding(dp(3), 0, dp(3), 0)
        setTextColor(accent); setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF20334B.toInt()))
        setOnClickListener { action() }; isFocusable = true
    }

    private fun saveChannels() {
        val arr = JSONArray()
        channels.forEach { c ->
            arr.put(JSONObject().put("name", c.name).put("url", c.url).put("favorite", c.favorite))
        }
        prefs.edit().putString("channels", arr.toString()).apply()
    }

    private fun loadChannels() {
        try {
            val raw = prefs.getString("channels", null) ?: return
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                channels.add(Channel(o.getString("name"), o.getString("url"), o.optBoolean("favorite")))
            }
        } catch (_: Exception) { channels.clear() }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}

package pw.aru.libs.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.configurator.AndePlayerConfigurator;
import pw.aru.libs.andeclient.entities.player.PlayerControls;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndePlayerEvent;
import pw.aru.libs.andeclient.events.player.internal.*;
import pw.aru.libs.andeclient.util.AndesiteUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class AndePlayerImpl implements AndePlayer {

    private final AndeClientImpl client;
    final AndesiteNodeImpl node;
    private final long guildId;

    AudioTrack playingTrack;
    private PlayerControlsImpl playerControls;
    private long lastTime;
    private long lastPosition;
    private int lastVolume;
    private boolean isPaused;
    private Collection<? extends PlayerFilter> lastFilters;

    public AndePlayerImpl(AndePlayerConfigurator configurator) {
        this.client = (AndeClientImpl) configurator.client();
        this.node = (AndesiteNodeImpl) configurator.andesiteNode();
        this.guildId = configurator.guildId();

        if (this.client.players.containsKey(guildId)) {
            throw new IllegalStateException("there's already a player for that guild!");
        }

        this.node.children.put(guildId, this);
        this.client.players.put(guildId, this);
        this.client.events.publish(PostedNewPlayerEvent.of(this));
    }

    @Nonnull
    @Override
    public AndesiteNode connectedNode() {
        return node;
    }

    @Nonnull
    @Override
    public AndeClient client() {
        return client;
    }

    @Nonnull
    @Override
    public PlayerControls controls() {
        if (playerControls == null) playerControls = new PlayerControlsImpl(this);
        return playerControls;
    }

    @Override
    public long guildId() {
        return guildId;
    }

    @Override
    public long serverTime() {
        return lastTime;
    }

    @Nullable
    @Override
    public AudioTrack playingTrack() {
        return playingTrack;
    }

    @Override
    public long position() {
        return lastPosition;
    }

    @Override
    public int volume() {
        return lastVolume;
    }

    @Override
    public boolean paused() {
        return isPaused;
    }

    @Override
    public Collection<? extends PlayerFilter> filters() {
        return lastFilters;
    }

    @Override
    public void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint) {
        node.handleOutcoming(
            new JSONObject()
                .put("op", "voice-server-update")
                .put("guildId", Long.toString(guildId))
                .put("sessionId", sessionId)
                .put("event", new JSONObject()
                    .put("endpoint", endpoint)
                    .put("token", voiceToken)
                )
        );
    }

    @Override
    public void destroy() {
        node.handleOutcoming(
            new JSONObject()
                .put("op", "destroy")
                .put("guildId", Long.toString(guildId))
        );

        node.children.remove(guildId);
        client.players.remove(guildId);
        client.events.publish(PostedPlayerRemovedEvent.of(this));
    }

    void update(JSONObject state) {
        final var wasPaused = this.isPaused;

        this.lastTime = Long.parseLong(state.getString("time"));
        this.lastPosition = state.optInt("position", -1);
        this.isPaused = state.getBoolean("paused");
        this.lastVolume = state.getInt("volume");
        this.lastFilters = AndesiteUtil.playerFilters(state.getJSONObject("filters"));

        this.client.events.publish(
            PostedPlayerUpdateEvent.builder()
                .player(this)
                .timestamp(lastTime)
                .position(lastPosition)
                .volume(lastVolume)
                .filters(lastFilters)
                .build()
        );

        if (wasPaused && !isPaused) {
            this.client.events.publish(PostedPlayerResumeEvent.of(this));
        } else if (!wasPaused && isPaused) {
            this.client.events.publish(PostedPlayerPauseEvent.of(this));
        }
    }

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndePlayerEvent> consumer) {
        return client.on(event -> {
            if (event instanceof AndePlayerEvent && ((AndePlayerEvent) event).player() == this) {
                consumer.onEvent((AndePlayerEvent) event);
            }
        });
    }
}
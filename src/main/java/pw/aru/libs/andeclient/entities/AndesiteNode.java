package pw.aru.libs.andeclient.entities;

import com.grack.nanojson.JsonObject;
import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.SimpleData;
import pw.aru.libs.andeclient.entities.configurator.AndesiteNodeConfigurator;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndesiteNodeConfigurator;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.EventType;
import pw.aru.libs.eventpipes.api.EventConsumer;
import pw.aru.libs.eventpipes.api.EventSubscription;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("unused")
public interface AndesiteNode {
    /**
     * Returns the parent AndeClient of this player.
     *
     * @return an AndeClient instance.
     */
    @Nonnull
    @CheckReturnValue
    AndeClient client();

    /**
     * Returns the state of the AndesiteNode.
     *
     * @return one of possible states.
     */
    @CheckReturnValue
    @Nonnull
    EntityState state();

    /**
     * Returns the node connect info.
     *
     * @return this node's connect info.
     */
    ConnectInfo connectInfo();

    /**
     * Returns the node info, gathered at the start of the node.
     *
     * @return this node's info.
     */
    @Nonnull
    @CheckReturnValue
    Info nodeInfo();

    /**
     * Asks for the node's current stats.
     *
     * @return a completion stage which completes with this node's stats.
     */
    @Nonnull
    @CheckReturnValue
    CompletionStage<Stats> stats();

    /**
     * Loads tracks based on a given identifier.
     *
     * @param identifier the identifier to try to load tracks.
     * @return a completion stage which completes with a load result.
     */
    @Nonnull
    @CheckReturnValue
    CompletionStage<AudioLoadResult> loadTracksAsync(String identifier);


    /**
     * Add an event consumer for all events of this player with the given handler callback.
     *
     * @param consumer The consumer for events.
     * @return The Event subscription, so you can close it later.
     */
    EventSubscription<AndeClientEvent> on(EventConsumer<AndesiteNodeEvent> consumer);

    /**
     * Add a consumer for the specified event type of this player with the given handler callback.
     *
     * @param type     The type of event to listen on.
     * @param consumer The consumer for events.
     * @param <T>      The object type of event being listened on.
     * @return The Event subscription, so you can close it later.
     */
    @SuppressWarnings("unchecked")
    default <T extends AndesiteNodeEvent> EventSubscription<AndeClientEvent> on(EventType<T> type, EventConsumer<T> consumer) {
        return on(event -> {
            if (event.type() == type) {
                consumer.onEvent((T) event);
            }
        });
    }

    /**
     * Destroys the node and closes all players connected to this node.
     */
    void destroy();

    /**
     * Info used to connect to the node.
     */
    @Value.Immutable
    @SimpleData
    interface ConnectInfo {
        /**
         * Returns the host of this node.
         *
         * @return the host string.
         */
        @Nonnull
        @CheckReturnValue
        String host();

        /**
         * Returns the port of this node.
         *
         * @return the port.
         */
        @CheckReturnValue
        int port();

        /**
         * Returns the password used to connect to the node, if any.
         *
         * @return the password string, or null.
         */
        @Nullable
        @CheckReturnValue
        String password();

        /**
         * Returns the relative path of this node, if any.
         *
         * @return the path string, or null.
         */
        @Nullable
        @CheckReturnValue
        String relativePath();

        /**
         * Returns the wait timeout value, in milliseconds.
         *
         * @return the value in milliseconds.
         */
        @Nonnegative
        @CheckReturnValue
        int timeout();

        /**
         * Creates a {@link AndesiteNodeConfigurator} based on this info.
         *
         * @return a new configurator.
         */
        @Nonnull
        @CheckReturnValue
        default ActualAndesiteNodeConfigurator asConfigurator() {
            return new ActualAndesiteNodeConfigurator().from(this);
        }
    }

    /**
     * Info sent from Andesite, received on the `metadata` op, which is sent at the start of the websocket connection.
     */
    @Value.Immutable
    @SimpleData
    interface Info {
        String version();

        String versionMajor();

        String versionMinor();

        String versionRevision();

        long versionBuild();

        String versionCommit();

        String nodeRegion();

        String nodeId();

        List<String> enabledSources();

        List<String> loadedPlugins();
    }

    /**
     * Stats retrieved from Andesite.
     */
    @Value.Immutable
    @SimpleData
    interface Stats {
        AndesiteNode node();

        JsonObject raw();

        int players();

        int playingPlayers();

        long uptime();

        double systemLoad();

        double andesiteLoad();

        long sentFrames();

        long nulledFrames();

        long deficitFrames();
    }
}

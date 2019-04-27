package pw.aru.lib.andeclient.events.player;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.player.PlayerFilter;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.EventType;

import javax.annotation.Nonnull;
import java.util.Collection;

@Value.Immutable
@Event
public abstract class PlayerUpdateEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    public abstract long timestamp();

    public abstract long position();

    public abstract int volume();

    public abstract Collection<? extends PlayerFilter> filters();

    @Override
    @Nonnull
    public EventType<PlayerUpdateEvent> type() {
        return EventType.PLAYER_UPDATE_EVENT;
    }
}

package org.l2j.gameserver.mobius.gameserver.model.olympiad;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.mobius.gameserver.instancemanager.InstanceManager;
import org.l2j.gameserver.mobius.gameserver.model.L2Spawn;
import org.l2j.gameserver.mobius.gameserver.model.Location;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2DoorInstance;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.instancezone.Instance;
import org.l2j.gameserver.mobius.gameserver.model.zone.ZoneId;
import org.l2j.gameserver.mobius.gameserver.model.zone.type.L2OlympiadStadiumZone;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExOlympiadUserInfo;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SystemMessage;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author JIV
 */
public class OlympiadStadium {
    private static final Logger LOGGER = Logger.getLogger(OlympiadStadium.class.getName());
    private final L2OlympiadStadiumZone _zone;
    private final Instance _instance;
    private final List<L2Spawn> _buffers;
    private OlympiadGameTask _task = null;

    protected OlympiadStadium(L2OlympiadStadiumZone olyzone, int stadium) {
        _zone = olyzone;
        _instance = InstanceManager.getInstance().createInstance(olyzone.getInstanceTemplateId(), null);
        _buffers = _instance.getNpcs().stream().map(L2Npc::getSpawn).collect(Collectors.toList());
        _buffers.stream().map(L2Spawn::getLastSpawn).forEach(L2Npc::decayMe);
    }

    public L2OlympiadStadiumZone getZone() {
        return _zone;
    }

    public final void registerTask(OlympiadGameTask task) {
        _task = task;
    }

    public OlympiadGameTask getTask() {
        return _task;
    }

    public Instance getInstance() {
        return _instance;
    }

    public final void openDoors() {
        _instance.getDoors().forEach(L2DoorInstance::openMe);
    }

    public final void closeDoors() {
        _instance.getDoors().forEach(L2DoorInstance::closeMe);
    }

    public final void spawnBuffers() {
        _buffers.forEach(L2Spawn::startRespawn);
        _buffers.forEach(L2Spawn::doSpawn);
    }

    public final void deleteBuffers() {
        _buffers.forEach(L2Spawn::stopRespawn);
        _buffers.stream().map(L2Spawn::getLastSpawn).filter(Objects::nonNull).forEach(L2Npc::deleteMe);
    }

    public final void broadcastStatusUpdate(L2PcInstance player) {
        final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
        for (L2PcInstance target : _instance.getPlayers()) {
            if (target.inObserverMode() || (target.getOlympiadSide() != player.getOlympiadSide())) {
                target.sendPacket(packet);
            }
        }
    }

    public final void broadcastPacket(IClientOutgoingPacket packet) {
        _instance.broadcastPacket(packet);
    }

    public final void broadcastPacketToObservers(IClientOutgoingPacket packet) {
        for (L2PcInstance target : _instance.getPlayers()) {
            if (target.inObserverMode()) {
                target.sendPacket(packet);
            }
        }
    }

    public final void updateZoneStatusForCharactersInside() {
        if (_task == null) {
            return;
        }

        final boolean battleStarted = _task.isBattleStarted();
        final SystemMessage sm;
        if (battleStarted) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
        } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
        }

        for (L2PcInstance player : _instance.getPlayers()) {
            if (player.inObserverMode()) {
                return;
            }

            if (battleStarted) {
                player.setInsideZone(ZoneId.PVP, true);
                player.sendPacket(sm);
            } else {
                player.setInsideZone(ZoneId.PVP, false);
                player.sendPacket(sm);
                player.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
            }
        }
    }

    public final void updateZoneInfoForObservers() {
        if (_task == null) {
            return;
        }

        for (L2PcInstance player : _instance.getPlayers()) {
            if (!player.inObserverMode()) {
                return;
            }

            final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
            final List<Location> spectatorSpawns = nextArena.getStadium().getZone().getSpectatorSpawns();
            if (spectatorSpawns.isEmpty()) {
                LOGGER.warning(getClass().getSimpleName() + ": Zone: " + nextArena.getStadium().getZone() + " doesn't have specatator spawns defined!");
                return;
            }
            final Location loc = spectatorSpawns.get(Rnd.get(spectatorSpawns.size()));
            player.enterOlympiadObserverMode(loc, player.getOlympiadGameId());
        }
    }
}
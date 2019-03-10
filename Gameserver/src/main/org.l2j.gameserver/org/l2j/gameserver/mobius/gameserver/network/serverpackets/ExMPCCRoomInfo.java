package org.l2j.gameserver.mobius.gameserver.network.serverpackets;

import org.l2j.gameserver.mobius.gameserver.model.matching.CommandChannelMatchingRoom;
import org.l2j.gameserver.mobius.gameserver.network.L2GameClient;
import org.l2j.gameserver.mobius.gameserver.network.OutgoingPackets;

import java.nio.ByteBuffer;

/**
 * @author Sdw
 */
public class ExMPCCRoomInfo extends IClientOutgoingPacket {
    private final CommandChannelMatchingRoom _room;

    public ExMPCCRoomInfo(CommandChannelMatchingRoom room) {
        _room = room;
    }

    @Override
    public void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.EX_MPCC_ROOM_INFO.writeId(packet);

        packet.putInt(_room.getId());
        packet.putInt(_room.getMaxMembers());
        packet.putInt(_room.getMinLvl());
        packet.putInt(_room.getMaxLvl());
        packet.putInt(_room.getLootType());
        packet.putInt(_room.getLocation());
        writeString(_room.getTitle(), packet);
    }
}

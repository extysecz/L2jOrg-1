package org.l2j.gameserver.network.serverpackets;

import io.github.joealisson.mmocore.StaticPacket;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;

/**
 * @author Gnacik
 **/
@StaticPacket
public class ExBirthdayPopup extends ServerPacket {
    public static final ExBirthdayPopup STATIC_PACKET = new ExBirthdayPopup();

    private ExBirthdayPopup() {
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerExPacketId.EX_NOTIFY_BIRTHDAY);
    }

}

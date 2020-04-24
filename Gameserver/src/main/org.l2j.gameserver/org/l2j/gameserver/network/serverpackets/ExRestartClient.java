package org.l2j.gameserver.network.serverpackets;

import io.github.joealisson.mmocore.StaticPacket;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;

/**
 * @author -Wooden-
 */
@StaticPacket
public class ExRestartClient extends ServerPacket {
    public static final ExRestartClient STATIC_PACKET = new ExRestartClient();

    private ExRestartClient() {
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerExPacketId.EX_RESTART_CLIENT);
    }

}

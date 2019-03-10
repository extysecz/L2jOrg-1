package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ActionFailed;

import java.nio.ByteBuffer;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreManageSell extends IClientIncomingPacket {
    @Override
    public void readImpl(ByteBuffer packet) {
        // TODO: implement me properly
        // packet.getInt();
        // packet.getLong();
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (player.isAlikeDead() || player.isInOlympiadMode()) {
            client.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
    }
}

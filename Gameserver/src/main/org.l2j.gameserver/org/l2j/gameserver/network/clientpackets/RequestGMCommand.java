package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.data.sql.impl.ClanTable;
import org.l2j.gameserver.model.Clan;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.serverpackets.*;
import org.l2j.gameserver.world.World;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestGMCommand extends ClientPacket {
    private String _targetName;
    private int _command;

    @Override
    public void readImpl() {
        _targetName = readString();
        _command = readInt();
    }

    @Override
    public void runImpl() {
        // prevent non gm or low level GMs from vieweing player stuff
        if (!client.getPlayer().isGM() || !client.getPlayer().getAccessLevel().allowAltG()) {
            return;
        }

        final Player player = World.getInstance().findPlayer(_targetName);

        final Clan clan = ClanTable.getInstance().getClanByName(_targetName);

        // player name was incorrect?
        if ((player == null) && ((clan == null) || (_command != 6))) {
            return;
        }

        switch (_command) {
            case 1: // player status
            {
                client.sendPacket(new GMViewCharacterInfo(player));
                client.sendPacket(new GMHennaInfo(player));
                break;
            }
            case 2: // player clan
            {
                if ((player != null) && (player.getClan() != null)) {
                    client.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
                }
                break;
            }
            case 3: // player skills
            {
                client.sendPacket(new GMViewSkillInfo(player));
                break;
            }
            case 4: // player quests
            {
                client.sendPacket(new GmViewQuestInfo(player));
                break;
            }
            case 5: // player inventory
            {
                client.sendPacket(new GMViewItemList(1, player));
                client.sendPacket(new GMViewItemList(2, player));
                client.sendPacket(new GMHennaInfo(player));
                break;
            }
            case 6: // player warehouse
            {
                // gm warehouse view to be implemented
                if (player != null) {
                    client.sendPacket(new GMViewWarehouseWithdrawList(1, player));
                    client.sendPacket(new GMViewWarehouseWithdrawList(2, player));
                } else {
                    client.sendPacket(new GMViewWarehouseWithdrawList(1, clan));
                    client.sendPacket(new GMViewWarehouseWithdrawList(2, clan));
                }
                break;
            }
        }
    }
}

/*
 * Copyright © 2019-2021 L2JOrg
 *
 * This file is part of the L2JOrg project.
 *
 * L2JOrg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2JOrg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.network.serverpackets;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.engine.item.Item;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;

import java.util.Collection;

/**
 * @author JIV
 */
public class ExQuestItemList extends AbstractItemPacket {
    private final int _sendType;
    private final Player _activeChar;
    private final Collection<Item> _items;

    public ExQuestItemList(int sendType, Player activeChar) {
        _sendType = sendType;
        _activeChar = activeChar;
        _items = activeChar.getInventory().getQuestItems();
    }

    @Override
    public void writeImpl(GameClient client, WritableBuffer buffer) {
        writeId(ServerExPacketId.EX_QUEST_ITEMLIST, buffer );
        buffer.writeByte(_sendType);
        if (_sendType == 2) {
            buffer.writeInt(_items.size());
        } else {
            buffer.writeShort(0);
        }
        buffer.writeInt(_items.size());
        for (Item item : _items) {
            writeItem(item, buffer);
        }
        writeInventoryBlock(_activeChar.getInventory(), buffer);
    }
}

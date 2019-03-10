package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.data.xml.impl.BeautyShopData;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.beautyshop.BeautyData;
import org.l2j.gameserver.mobius.gameserver.model.beautyshop.BeautyItem;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExResponseBeautyList;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExResponseBeautyRegistReset;

import java.nio.ByteBuffer;

/**
 * @author Sdw
 */
public class RequestRegistBeauty extends IClientIncomingPacket {
    private int _hairId;
    private int _faceId;
    private int _colorId;

    @Override
    public void readImpl(ByteBuffer packet) {
        _hairId = packet.getInt();
        _faceId = packet.getInt();
        _colorId = packet.getInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        final BeautyData beautyData = BeautyShopData.getInstance().getBeautyData(player.getRace(), player.getAppearance().getSexType());
        int requiredAdena = 0;
        int requiredBeautyShopTicket = 0;

        if (_hairId > 0) {
            final BeautyItem hair = beautyData.getHairList().get(_hairId);
            if (hair == null) {
                player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
                player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
                return;
            }

            if (hair.getId() != player.getVisualHair()) {
                requiredAdena += hair.getAdena();
                requiredBeautyShopTicket += hair.getBeautyShopTicket();
            }

            if (_colorId > 0) {
                final BeautyItem color = hair.getColors().get(_colorId);
                if (color == null) {
                    player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
                    player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
                    return;
                }

                requiredAdena += color.getAdena();
                requiredBeautyShopTicket += color.getBeautyShopTicket();
            }
        }

        if ((_faceId > 0) && (_faceId != player.getVisualFace())) {
            final BeautyItem face = beautyData.getFaceList().get(_faceId);
            if (face == null) {
                player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
                player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
                return;
            }

            requiredAdena += face.getAdena();
            requiredBeautyShopTicket += face.getBeautyShopTicket();
        }

        if ((player.getAdena() < requiredAdena) || ((player.getBeautyTickets() < requiredBeautyShopTicket))) {
            player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
            player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
            return;
        }

        if (requiredAdena > 0) {
            if (!player.reduceAdena(getClass().getSimpleName(), requiredAdena, null, true)) {
                player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
                player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
                return;
            }
        }

        if (requiredBeautyShopTicket > 0) {
            if (!player.reduceBeautyTickets(getClass().getSimpleName(), requiredBeautyShopTicket, null, true)) {
                player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
                player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
                return;
            }
        }

        if (_hairId > 0) {
            player.setVisualHair(_hairId);
        }

        if (_colorId > 0) {
            player.setVisualHairColor(_colorId);
        }

        if (_faceId > 0) {
            player.setVisualFace(_faceId);
        }

        player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.SUCCESS));
    }

}
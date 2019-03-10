/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.mobius.gameserver.model.conditions;

import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2MonsterInstance;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.model.items.L2Item;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;

/**
 * The Class ConditionTargetAggro.
 *
 * @author mkizub
 */
public class ConditionTargetAggro extends Condition {
    private final boolean _isAggro;

    /**
     * Instantiates a new condition target aggro.
     *
     * @param isAggro the is aggro
     */
    public ConditionTargetAggro(boolean isAggro) {
        _isAggro = isAggro;
    }

    @Override
    public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
        if (effected != null) {
            if (effected.isMonster()) {
                return ((L2MonsterInstance) effected).isAggressive() == _isAggro;
            }
            if (effected.isPlayer()) {
                return ((L2PcInstance) effected).getReputation() < 0;
            }
        }
        return false;
    }
}

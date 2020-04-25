package handlers.effecthandlers;

import org.l2j.gameserver.data.xml.impl.LevelData;
import org.l2j.gameserver.data.xml.impl.NpcData;
import org.l2j.gameserver.engine.skill.api.Skill;
import org.l2j.gameserver.engine.skill.api.SkillEffectFactory;
import org.l2j.gameserver.enums.Race;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.actor.instance.Servitor;
import org.l2j.gameserver.model.actor.templates.NpcTemplate;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.effects.EffectType;
import org.l2j.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.model.item.instance.Item;
import org.l2j.gameserver.model.skills.BuffInfo;

import static org.l2j.gameserver.util.GameUtils.isPlayer;

/**
 * Summon effect implementation.
 * @author UnAfraid
 */
public final class Summon extends AbstractEffect {
    private final int npcId;
    private final float expMultiplier;
    private final ItemHolder consumeItem;
    private final int lifeTime;

    private Summon(StatsSet params) {
        if (params.isEmpty()) {
            throw new IllegalArgumentException("Summon effect without parameters!");
        }

        npcId = params.getInt("npc");
        expMultiplier = params.getFloat("experience-multiplier", 1);
        consumeItem = new ItemHolder(params.getInt("consume-item", 0), params.getInt("consume-count", 1));
        lifeTime = params.getInt("life-time", 0) > 0 ? params.getInt("life-time") * 1000 : -1; // Classic change.
    }

    @Override
    public EffectType getEffectType()
    {
        return EffectType.SUMMON;
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void instant(Creature effector, Creature effected, Skill skill, Item item) {
        if (!isPlayer(effected)) {
            return;
        }

        final Player player = effected.getActingPlayer();
        if (player.hasServitors()) {
            player.getServitors().values().forEach(s -> s.unSummon(player));
        }
        final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
        final Servitor summon = new Servitor(template, player);
        final int consumeItemInterval =(template.getRace() != Race.SIEGE_WEAPON ? 240 : 60) * 1000;

        summon.setName(template.getName());
        summon.setTitle(effected.getName());
        summon.setReferenceSkill(skill.getId());
        summon.setExpMultiplier(expMultiplier);
        summon.setLifeTime(lifeTime <= 0 ? Integer.MAX_VALUE : lifeTime); // Classic hack. Resummon upon entering game.
        summon.setItemConsume(consumeItem);
        summon.setItemConsumeInterval(consumeItemInterval);

        var maxLevel = LevelData.getInstance().getMaxLevel();

        if (summon.getLevel() >= maxLevel) {
            summon.getStats().setExp(LevelData.getInstance().getExpForLevel(maxLevel - 1));
            LOGGER.warn("({}) NpcID: {} has a level above {}. Please rectify.", summon.getName(), summon.getId(), maxLevel);
        } else {
            summon.getStats().setExp(LevelData.getInstance().getExpForLevel(summon.getLevel() % LevelData.getInstance().getMaxLevel()));
        }

        // Summons must have their master buffs upon spawn.
        for (BuffInfo effect : player.getEffectList().getEffects()) {
            final Skill sk = effect.getSkill();
            if (!sk.isBad()) {
                sk.applyEffects(player, summon, false, effect.getTime());
            }
        }

        summon.setCurrentHp(summon.getMaxHp());
        summon.setCurrentMp(summon.getMaxMp());
        summon.setHeading(player.getHeading());

        player.addServitor(summon);

        summon.setShowSummonAnimation(true);
        summon.spawnMe();
        summon.setRunning();
    }

    public static class Factory implements SkillEffectFactory {

        @Override
        public AbstractEffect create(StatsSet data) {
            return new Summon(data);
        }

        @Override
        public String effectName() {
            return "summon";
        }
    }
}
